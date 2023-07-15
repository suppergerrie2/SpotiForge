package com.suppergerrie2.spotiforge.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.suppergerrie2.spotiforge.SpotiForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.specification.Episode;
import se.michaelthelin.spotify.model_objects.specification.Track;

import static mine.block.spotify.SpotifyUtils.NOW_ART;
import static mine.block.spotify.SpotifyUtils.NOW_ID;

public class SpotifyToast implements Toast {
    private long startTime;
    private boolean justUpdated;
    public final CurrentlyPlaying currentlyPlaying;
    public SpotifyToast(CurrentlyPlaying currentlyPlaying) {
        this.currentlyPlaying = currentlyPlaying;
    }

    @Override
    public @NotNull Visibility render(@NotNull GuiGraphics context, @NotNull ToastComponent manager, long startTime) {
        if (this.justUpdated) {
            this.startTime = startTime;
            this.justUpdated = false;
        }

        context.fill(0, 0, this.width(), this.height(), 0xFF191414);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();

        context.blit(NOW_ID, 2, 2, this.height() - 4, this.height() - 4, 0, 0, NOW_ART.getWidth(), NOW_ART.getHeight(), NOW_ART.getWidth(), NOW_ART.getHeight());
        context.blit(new ResourceLocation(SpotiForge.MODID, "textures/spotify_logo.png"), this.width() - (16+8), (this.height() / 2)-8, 16, 16, 0, 0, 96, 96, 96, 96);

        RenderSystem.disableBlend();

        context.drawString(manager.getMinecraft().font, Component.literal(currentlyPlaying.getItem().getName()), 43, 10, -256, false);

        if(currentlyPlaying.getItem() instanceof Track track) {
            context.drawString(manager.getMinecraft().font,Component.literal(track.getArtists()[0].getName()), 43, 21, -1, false);
        } else {
            context.drawString(manager.getMinecraft().font, Component.literal(((Episode) currentlyPlaying.getItem()).getShow().getName()), 43, 21, -1, false);
        }

        return startTime - this.startTime < 5000L ? Visibility.SHOW : Visibility.HIDE;
    }

    @Override
    public @NotNull Object getToken() {
        return Toast.super.getToken();
    }

    @Override
    public int width() {
        int widthName = (int) (Minecraft.getInstance().font.width(currentlyPlaying.getItem().getName()) * (0.75));
        int widthArtist;

        if(currentlyPlaying.getItem() instanceof Track track) {
            widthArtist = (int) (Minecraft.getInstance().font.width(Component.literal(track.getArtists()[0].getName())) * (0.75));
        } else {
            widthArtist = (int) (Minecraft.getInstance().font.width(Component.literal(((Episode) currentlyPlaying.getItem()).getShow().getName())) * (0.75));
        }

        return 160 + Math.max(widthArtist, widthName);
    }

    @Override
    public int height() {
        return 38;
    }
}
