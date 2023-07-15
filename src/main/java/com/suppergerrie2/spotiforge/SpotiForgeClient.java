package com.suppergerrie2.spotiforge;

import com.mojang.blaze3d.platform.InputConstants;
import com.suppergerrie2.spotiforge.ui.SpotifyScreen;
import mine.block.spotify.SpotifyHandler;
import mine.block.spotify.SpotifyUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SpotiForgeClient {

    public static final Lazy<KeyMapping> OPEN_SPOTIFY_MENU = Lazy.of(() -> new KeyMapping(
            "key.F%s.open_spotify_menu".formatted(SpotiForge.MODID),
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.categories.misc"
    ));

    @Mod.EventBusSubscriber(modid = SpotiForge.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            SpotifyHandler.setup(false);

            SpotifyHandler.PollingThread thread = new SpotifyHandler.PollingThread();
            ExecutorService checkTasksExecutorService = new ThreadPoolExecutor(1, 10,
                    100000, TimeUnit.MILLISECONDS,
                    new SynchronousQueue<>());
            checkTasksExecutorService.execute(thread);
            SpotifyHandler.songChangeEvent.add(SpotifyUtils::run);
        }

        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event) {
            event.register(OPEN_SPOTIFY_MENU.get());
        }
    }

    @Mod.EventBusSubscriber(modid = SpotiForge.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)

    public static class ClientForgeEvents {

        @SubscribeEvent
        public static void onScreenInit(ScreenEvent.Init.Post event) {
            if (event.getScreen() instanceof TitleScreen) {
                SpotifyUtils.MC_LOADED = true;
            }
        }

        @SubscribeEvent
        public static void onSoundPlayed(PlaySoundEvent event) {
            if (event.getSound() == null || event.getSound().getSource() != SoundSource.MUSIC) return;

            if (Config.autoMuteIngameMusic && SpotifyUtils.NOW_PLAYING != null && SpotifyUtils.NOW_PLAYING.getIs_playing()) {
                event.setSound(null);
            }
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            while (OPEN_SPOTIFY_MENU.get().consumeClick()) {
                Minecraft.getInstance().setScreen(new SpotifyScreen(Minecraft.getInstance().screen));
            }
        }
    }

}
