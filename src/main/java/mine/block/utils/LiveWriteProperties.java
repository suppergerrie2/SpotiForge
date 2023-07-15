package mine.block.utils;

import com.mojang.logging.LogUtils;
import com.suppergerrie2.spotiforge.SpotiForge;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public class LiveWriteProperties extends Properties {

    private final Path pathToConfig = FMLPaths.CONFIGDIR.get().resolve(SpotiForge.MODID).resolve("spotify.cred");
    public boolean empty = true;
    private static final Logger LOGGER = LogUtils.getLogger();

    public LiveWriteProperties() {
        if(Files.exists(pathToConfig)) {
            try (var stream = Files.newInputStream(pathToConfig)) {
                this.load(stream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if(this.getProperty("client-secret") == null || !Objects.equals(this.getProperty("version"), SpotiForge.VERSION)) {
                LOGGER.warn("Old configuration file! Removing.");
                try {
                    Files.delete(pathToConfig);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                this.clear();
                this.setProperty("version", SpotiForge.VERSION);
            } else {
                empty = false;
            }
        }
    }

    public void markDirty() throws IOException {
        if(!Files.isDirectory(pathToConfig.getParent())) Files.createDirectories(pathToConfig.getParent());
        try(var os = Files.newOutputStream(pathToConfig)) {
            this.store(os, null);
        }
    }
}
