package com.suppergerrie2.spotiforge.ui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import mine.block.spotify.SpotifyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import se.michaelthelin.spotify.model_objects.specification.Episode;
import se.michaelthelin.spotify.model_objects.specification.Track;

public class SpotifyPlaylistItemWidget extends AbstractWidget {
    public SpotifyPlaylistItemWidget(int x, int y) {
        super(x, y, 0, 0, Component.empty());
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (SpotifyUtils.NOW_PLAYING != null && SpotifyUtils.NOW_ART != null) {
            int x = getX() - 64;
            int y = getY();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();

            int size = SpotifyUtils.NOW_ART.getWidth();
            graphics.blit(SpotifyUtils.NOW_ID, x, y, 64, 64, 0, 0, size, size, size, size);

            RenderSystem.disableBlend();

            var textRenderer = Minecraft.getInstance().font;
            graphics.drawString(textRenderer, Component.literal(SpotifyUtils.NOW_PLAYING.getItem().getName()), x + 64 + 16, y + 32 + 4, -256, false);

            if (SpotifyUtils.NOW_PLAYING.getItem() instanceof Track track) {
                graphics.drawString(textRenderer, Component.literal(track.getArtists()[0].getName()), x + 64 + 16, y + 21 + 4, -1, false);
            } else {
                graphics.drawString(textRenderer, Component.literal(((Episode) SpotifyUtils.NOW_PLAYING.getItem()).getShow().getName()), x + 64 + 16, y + 21 + 4, -1, false);
            }
        }
    }
}
