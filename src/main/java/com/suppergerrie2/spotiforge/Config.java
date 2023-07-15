package com.suppergerrie2.spotiforge;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = SpotiForge.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue AUTO_MUTE_INGAME_MUSIC = BUILDER
            .comment("Whether to auto mute minecraft's music when spotify is playing.")
            .define("autoMuteIngameMusic", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean autoMuteIngameMusic;


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        autoMuteIngameMusic = AUTO_MUTE_INGAME_MUSIC.get();
    }
}
