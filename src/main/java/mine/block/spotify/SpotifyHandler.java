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
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;

public class SpotifyHandler {
    public static SpotifyApi SPOTIFY_API;
    public static HashSet<SongChangeEvent> songChangeEvent = new HashSet<>();
    private static final Logger LOGGER = LogUtils.getLogger();

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
                LOGGER.error("Failed to setup spotify. ", e);
                System.exit(1);
            }
        }

        try {
            SpotiForge.SPOTIFY_CONFIG.markDirty();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface SongChangeEvent {
        void run(CurrentlyPlaying cp);
    }


    public static class PollingThread implements Runnable {
        public static CurrentlyPlaying CURRENTLY_PLAYING = null;
        @Override
        public void run() {
            while (true) {
                try {
                    CURRENTLY_PLAYING = SPOTIFY_API.getUsersCurrentlyPlayingTrack().build().execute();
                    if (CURRENTLY_PLAYING == null) return;
                    Minecraft.getInstance().execute(() -> songChangeEvent.forEach(event -> event.run(CURRENTLY_PLAYING)));
                } catch (IOException | SpotifyWebApiException | ParseException e) {
                    LOGGER.warn("Failed to poll: " + e);
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
