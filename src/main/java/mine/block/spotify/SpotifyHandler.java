package mine.block.spotify;

import com.github.winterreisender.webviewko.WebviewKo;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.logging.LogUtils;
import com.sun.net.httpserver.HttpServer;
import com.suppergerrie2.spotiforge.SpotiForge;
import mine.block.spotify.server.*;
import net.minecraft.client.Minecraft;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.UnauthorizedException;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;

public class SpotifyHandler {
    public static SpotifyApi SPOTIFY_API;
    public static HashSet<SongChangeEvent> songChangeEvent = new HashSet<>();
    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean refreshAccessToken() {
        try {
            var creds = SPOTIFY_API.authorizationCodeRefresh().build().execute();

            SPOTIFY_API.setAccessToken(creds.getAccessToken());

            if(creds.getRefreshToken() != null) {
                SPOTIFY_API.setRefreshToken(creds.getRefreshToken());
                SpotiForge.SPOTIFY_CONFIG.put("refresh-token", creds.getRefreshToken());
            }

            SpotiForge.SPOTIFY_CONFIG.put("token", creds.getAccessToken());
            LOGGER.info("Refreshed Credentials.");
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            LOGGER.error("Failed to refresh access token. ", e);
            return false;
        }

        return true;
    }

    public static void setup(boolean reset) {
        if(reset || SpotiForge.SPOTIFY_CONFIG.empty) {
            LOGGER.info("Starting oauth creation screen.");

            WebviewKo webview = new WebviewKo(1, null);
            webview.title("SpotiForge - Setup");

            webview.size(GlStateManager.Viewport.width(), GlStateManager.Viewport.height(), WebviewKo.WindowHint.None);

            try {
                final HttpServer server = HttpServer.create(new InetSocketAddress(23435), 0);
                server.createContext("/no-internet", new NoNetHandler());
                server.createContext("/setup", new SetupHandler());
                server.createContext("/setup-2", new SetupTwoHandler());
                server.createContext("/pre-callback", new PreCallbackHandler());
                server.createContext("/callback", new CallbackHandler(server));
                server.start();
            } catch (IOException e) {
                LOGGER.error("Failed to setup spotify.");
                throw new RuntimeException(e);
            }

            if(!SpotifyUtils.netIsAvailable()) {
                webview.url("http://localhost:23435/no-internet");
                webview.show();
                return;
            } else {
                webview.url("http://localhost:23435/setup");
                webview.show();
            }
        } else {
            SPOTIFY_API = new SpotifyApi.Builder()
                    .setClientId(SpotiForge.SPOTIFY_CONFIG.getProperty("client-id"))
                    .setClientSecret(SpotiForge.SPOTIFY_CONFIG.getProperty("client-secret"))
                    .setAccessToken(SpotiForge.SPOTIFY_CONFIG.getProperty("token"))
                    .setRefreshToken(SpotiForge.SPOTIFY_CONFIG.getProperty("refresh-token"))
                    .build();
            if(!refreshAccessToken()) System.exit(1);
        }

        try {
            SpotiForge.SPOTIFY_CONFIG.markDirty();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface SongChangeEvent {
        void run(CurrentlyPlaying cp, Lyrics lyrics);
    }


    public static class PollingThread implements Runnable {
        public static CurrentlyPlaying CURRENTLY_PLAYING = null;
        private static Lyrics lyrics;
        int refreshAttempts = 0;
        @Override
        public void run() {
            LOGGER.info("Starting poll thread.");
            while (refreshAttempts < 10) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                try {
                    CURRENTLY_PLAYING = SPOTIFY_API.getUsersCurrentlyPlayingTrack().build().execute();
                    LOGGER.trace("Refreshed song: {}", CURRENTLY_PLAYING == null ? "null" : CURRENTLY_PLAYING.getItem().getName());
                    if (CURRENTLY_PLAYING == null) continue;
                    if(SpotifyUtils.NOW_PLAYING == null || !SpotifyUtils.NOW_PLAYING.getItem().getId().equals(CURRENTLY_PLAYING.getItem().getId())) lyrics = SpotifyUtils.gatherLyrics(CURRENTLY_PLAYING.getItem());
                    LOGGER.trace("Submitting song to client: {}", CURRENTLY_PLAYING == null ? "null" : CURRENTLY_PLAYING.getItem().getName());
                    Minecraft.getInstance().execute(() -> songChangeEvent.forEach(event -> event.run(CURRENTLY_PLAYING, lyrics)));
                } catch (UnauthorizedException e) {
                    if(refreshAccessToken()) refreshAttempts = 0;
                    else refreshAttempts++;
                } catch (IOException | SpotifyWebApiException | ParseException e) {
                    LOGGER.warn("Failed to poll: " + e);
                }
            }

            if(refreshAttempts >= 10) {
                LOGGER.warn("Failed to refresh access token, shutting down spotify connection!");
            }
        }
    }

}
