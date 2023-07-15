package mine.block.spotify;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.suppergerrie2.spotiforge.Config;
import com.suppergerrie2.spotiforge.ui.SpotifyScreen;
import com.suppergerrie2.spotiforge.ui.SpotifyToast;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import org.slf4j.Logger;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.specification.Episode;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SpotifyUtils {

    public static boolean MC_LOADED = false;
    public static CurrentlyPlaying NOW_PLAYING = null;
    public static NativeImage NOW_ART = null;
    public static ResourceLocation NOW_ID = null;
    public static HashMap<ResourceLocation, NativeImage> TEXTURE = new HashMap<>();

    private static final Logger LOGGER = LogUtils.getLogger();

    public static InputStream loadHTMLFile(String id) throws IOException {
        var path = "/web/" + id + ".html";
        var result = SpotifyUtils.class.getResourceAsStream(path);
        if (result == null) {
            LOGGER.warn("Could not find resource: '{}'", path);
            throw new IOException("Could not find resource: " + path);
        }
        return result;
    }

    public static boolean netIsAvailable() {
        return isHostAvailable("google.com") || isHostAvailable("amazon.com")
                || isHostAvailable("facebook.com")|| isHostAvailable("apple.com");
    }

    private static boolean isHostAvailable(String hostName)
    {
        try(Socket socket = new Socket())
        {
            int port = 80;
            InetSocketAddress socketAddress = new InetSocketAddress(hostName, port);
            socket.connect(socketAddress, 3000);

            return true;
        }
        catch(Exception unknownHost)
        {
            return false;
        }
    }

    public static Map<String, String> queryToMap(String query) {
        if (query == null) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    public static void run(CurrentlyPlaying currentlyPlaying) {
        if(!MC_LOADED) return;
        if (NOW_PLAYING == null || !NOW_PLAYING.getItem().getId().equals(currentlyPlaying.getItem().getId())) {
            NOW_PLAYING = currentlyPlaying;

            Minecraft.getInstance().submit(() -> {
                if(Config.autoMuteIngameMusic && SpotifyUtils.NOW_PLAYING != null && SpotifyUtils.NOW_PLAYING.getIs_playing()) {
                    Minecraft.getInstance().getSoundManager().stop(null, SoundSource.MUSIC);
                }
            });

            var item = currentlyPlaying.getItem();

            ResourceLocation texture = new ResourceLocation("spotify_logo", currentlyPlaying.getItem().getId().toLowerCase());

            if (!TEXTURE.containsKey(texture)) {
                if (item instanceof Track track) {
                    try {
                        NOW_ART = NativeImage.read(new URL(track.getAlbum().getImages()[0].getUrl()).openStream());
                        TEXTURE.put(texture, NOW_ART);
                        NOW_ID = texture;

                        Minecraft.getInstance().getTextureManager().register(NOW_ID, new DynamicTexture(NOW_ART));
                    } catch (IOException e) {
                        return;
                    }
                } else {
                    try {
                        NOW_ART = NativeImage.read(new URL(((Episode) currentlyPlaying.getItem()).getImages()[0].getUrl()).openStream());
                        TEXTURE.put(texture, NOW_ART);
                        NOW_ID = texture;
                        Minecraft.getInstance().getTextureManager().register(NOW_ID, new DynamicTexture(NOW_ART));
                    } catch (IOException e) {
                        return;
                    }
                }
            } else {
                NOW_ART = TEXTURE.get(texture);
                NOW_ID = texture;
            }


            if (Minecraft.getInstance().gui != null && !(Minecraft.getInstance().screen instanceof SpotifyScreen) && NOW_ART != null) {
                Minecraft.getInstance().getToasts().addToast(new SpotifyToast(currentlyPlaying));
            }
        } else if (NOW_PLAYING.getIs_playing() != currentlyPlaying.getIs_playing()) {
            NOW_PLAYING = currentlyPlaying;

            Minecraft.getInstance().submit(() -> {
                if(Config.autoMuteIngameMusic && SpotifyUtils.NOW_PLAYING != null && SpotifyUtils.NOW_PLAYING.getIs_playing()) {
                    Minecraft.getInstance().getSoundManager().stop(null, SoundSource.MUSIC);
                }
            });
        }


        if (Minecraft.getInstance().screen instanceof SpotifyScreen spotifyScreen) {
            spotifyScreen.progress = (float) currentlyPlaying.getProgress_ms() / (float) currentlyPlaying.getItem().getDurationMs();
        }
    }
}
