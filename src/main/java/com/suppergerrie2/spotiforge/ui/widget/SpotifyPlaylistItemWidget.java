package com.suppergerrie2.spotiforge.ui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import mine.block.spotify.Lyrics;
import mine.block.spotify.SpotifyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import se.michaelthelin.spotify.model_objects.specification.Episode;
import se.michaelthelin.spotify.model_objects.specification.Track;

public class SpotifyPlaylistItemWidget extends AbstractWidget {
    public SpotifyPlaylistItemWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (SpotifyUtils.NOW_PLAYING == null || SpotifyUtils.NOW_ART == null) return;

        int x;
        int y = getY() + height / 3;

        int textOffsetX;
        int textOffsetY;

        if(SpotifyUtils.NOW_LYRICS != null) {
            x = getX() + width / 2 + 64;
            textOffsetX = 0;
            textOffsetY = 64 + 4;
            renderLyrics(graphics, partialTick);
        } else {
            x = getX() + width / 2 - 64;
            textOffsetX = 64 + 16;
            textOffsetY = 21 + 4;
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();

        int size = SpotifyUtils.NOW_ART.getWidth();
        graphics.blit(SpotifyUtils.NOW_ID, x, y, 64, 64, 0, 0, size, size, size, size);

        RenderSystem.disableBlend();

        var textRenderer = Minecraft.getInstance().font;

        graphics.drawString(textRenderer, Component.literal(SpotifyUtils.NOW_PLAYING.getItem().getName()), x + textOffsetX, y + textOffsetY, 0xffffff00, false);

        if (SpotifyUtils.NOW_PLAYING.getItem() instanceof Track track) {
            graphics.drawString(textRenderer, Component.literal(track.getArtists()[0].getName()), x + textOffsetX, y + textOffsetY + 11, 0xffffffff, false);
        } else {
            graphics.drawString(textRenderer, Component.literal(((Episode) SpotifyUtils.NOW_PLAYING.getItem()).getShow().getName()), x + textOffsetX, y + textOffsetY + 11, 0xffffffff, false);
        }
    }

    int currentLyricsPos;
    Lyrics lastLyrics = null;

    private void renderLyrics(@NotNull GuiGraphics graphics, float partialTick) {
        if(lastLyrics != SpotifyUtils.NOW_LYRICS) {
            lastLyrics = SpotifyUtils.NOW_LYRICS;
            currentLyricsPos = 0;
        }

        var textRenderer = Minecraft.getInstance().font;

        long timestamp = SpotifyUtils.NOW_PLAYING.getProgress_ms();

        if(SpotifyUtils.NOW_PLAYING.getIs_playing()) {
            timestamp += System.currentTimeMillis() - SpotifyUtils.LAST_NOW_UPDATE;
        }

        while(currentLyricsPos < SpotifyUtils.NOW_LYRICS.lines.length && SpotifyUtils.NOW_LYRICS.lines[currentLyricsPos].startTimeMs < timestamp) {
            currentLyricsPos++;
        }
        currentLyricsPos = Math.max(currentLyricsPos-1, 0);
        int surroundingLinesCount = Math.max(height / textRenderer.lineHeight / 2 - 1, 1);
        int start = Math.max(0, currentLyricsPos - surroundingLinesCount);
        int end = Math.min(start + surroundingLinesCount * 2 + 1, SpotifyUtils.NOW_LYRICS.lines.length);

        int x = 16;
        int y = 16;
        int lineWidth = width - 200;
        for (int i = start; i < end; i++) {
            int distance = Math.abs(currentLyricsPos - i);
            Component line = Component.literal(SpotifyUtils.NOW_LYRICS.lines[i].words);
            var split = textRenderer.split(line, lineWidth);
            if(y > x + height - split.size() * textRenderer.lineHeight - textRenderer.lineHeight) break;
            for (FormattedCharSequence splitLine : split) {
                graphics.drawString(textRenderer, splitLine, x, y, Math.max((int)(0xFF / (0.5*distance + 1)), 0x10) * 0x010101, false);
                y += textRenderer.lineHeight;
            }
        }
    }
}
