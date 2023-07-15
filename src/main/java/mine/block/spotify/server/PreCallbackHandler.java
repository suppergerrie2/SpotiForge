package mine.block.spotify.server;

import com.mojang.logging.LogUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.suppergerrie2.spotiforge.SpotiForge;
import mine.block.spotify.SpotifyHandler;
import mine.block.spotify.SpotifyUtils;
import org.slf4j.Logger;
import se.michaelthelin.spotify.SpotifyApi;

import java.io.IOException;
import java.net.URI;

public class PreCallbackHandler implements HttpHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        LOGGER.info(requestMethod + " " + exchange.getRequestURI());
        if (requestMethod.equalsIgnoreCase("GET")) {
            var queries = SpotifyUtils.queryToMap(exchange.getRequestURI().getQuery());
            var clientID = queries.get("clientid");
            var clientSecret = queries.get("clientsecret");

            SpotifyHandler.SPOTIFY_API = SpotifyApi.builder().setClientId(clientID).setClientSecret(clientSecret).setRedirectUri(URI.create("http://localhost:23435/callback")).build();

            SpotiForge.SPOTIFY_CONFIG.put("client-id", clientID);
            SpotiForge.SPOTIFY_CONFIG.put("client-secret", clientSecret);

            try (exchange) {
                exchange.sendResponseHeaders(200, 0);
            }
        }
    }
}
