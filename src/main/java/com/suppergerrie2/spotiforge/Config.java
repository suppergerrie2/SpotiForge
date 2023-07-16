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

    private static final ForgeConfigSpec.BooleanValue DISPLAY_LYRICS_IN_HUD = BUILDER
            .comment("Whether to auto display the song's lyrics in the HUD under the chat.")
            .define("displayLyricsInHud", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean autoMuteIngameMusic;
    public static boolean displayLyricsInHud;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        autoMuteIngameMusic = AUTO_MUTE_INGAME_MUSIC.get();
        displayLyricsInHud = DISPLAY_LYRICS_IN_HUD.get();
    }
}
