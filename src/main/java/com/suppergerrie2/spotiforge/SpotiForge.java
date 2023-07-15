package com.suppergerrie2.spotiforge;

import mine.block.utils.LiveWriteProperties;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SpotiForge.MODID)
public class SpotiForge {

    public static final String MODID = "spotiforge";
    public static final String VERSION = "1.0.0";

    public static final LiveWriteProperties SPOTIFY_CONFIG = new LiveWriteProperties();

    public SpotiForge() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
