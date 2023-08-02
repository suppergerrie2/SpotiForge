package com.suppergerrie2.spotiforge.ui;

import com.suppergerrie2.spotiforge.Config;
import mine.block.spotify.Lyrics;
import mine.block.spotify.SpotifyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class LyricsGuiOverlay implements IGuiOverlay {

    int currentLyricsPos;
    Lyrics lastLyrics = null;

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        final int fadeStartOutTime = 5*1000;
        final int fadeOutDuration = 2*1000;
        final boolean isPaused = !SpotifyUtils.NOW_PLAYING.getIs_playing();
        final long pauseDuration = isPaused ? System.currentTimeMillis() - SpotifyUtils.NOW_PLAYING.getTimestamp() : -1;

        if(SpotifyUtils.NOW_LYRICS == null || !Config.displayLyricsInHud || pauseDuration > fadeStartOutTime + fadeOutDuration) return;
        int midX = screenWidth / 2;
        int hotbarWidth = 182;
        int height = gui.leftHeight;

        int width = midX - hotbarWidth / 2;

        if (lastLyrics != SpotifyUtils.NOW_LYRICS) {
            lastLyrics = SpotifyUtils.NOW_LYRICS;
            currentLyricsPos = 0;
        }

        var textRenderer = Minecraft.getInstance().font;

        long timestamp = SpotifyUtils.NOW_PLAYING.getProgress_ms();

        if (SpotifyUtils.NOW_PLAYING.getIs_playing()) {
            timestamp += System.currentTimeMillis() - SpotifyUtils.LAST_NOW_UPDATE;
        }

        while (currentLyricsPos < SpotifyUtils.NOW_LYRICS.lines.length && SpotifyUtils.NOW_LYRICS.lines[currentLyricsPos].startTimeMs < timestamp) {
            currentLyricsPos++;
        }
        currentLyricsPos = Math.max(currentLyricsPos - 1, 0);

        float scale = 0.5f;
        float scaleInv = 1/scale;

        int surroundingLinesCount = Math.max((int) ((height * scaleInv) / textRenderer.lineHeight / 2 - 1), 1);
        int start = Math.max(0, currentLyricsPos - surroundingLinesCount);
        int end = Math.min(start + surroundingLinesCount * 2 + 1, SpotifyUtils.NOW_LYRICS.lines.length);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, scale);
        int x = 0;
        int y = screenHeight - height;
        int lineWidth = (int) (width * scaleInv);
        guiGraphics.setColor(1, 1, 1, isPaused && pauseDuration >= fadeStartOutTime ? 1 - (pauseDuration - fadeStartOutTime) / (float) fadeOutDuration : 1);
        for (int i = start; i < end; i++) {
            int distance = Math.abs(currentLyricsPos - i);
            Component line = Component.literal(SpotifyUtils.NOW_LYRICS.lines[i].words);
            var split = textRenderer.split(line, lineWidth);
            if (y > scaleInv * (screenHeight - split.size() * textRenderer.lineHeight - textRenderer.lineHeight)) break;
            for (FormattedCharSequence splitLine : split) {
                guiGraphics.fill(0, (int) (y*scaleInv), lineWidth, (int) ((y + textRenderer.lineHeight * scale) * scaleInv) - 1, gui.getMinecraft().options.getBackgroundColor(0.8F));
                guiGraphics.drawString(textRenderer, splitLine, x*scaleInv, y*scaleInv, Math.max((int) (0xFF / (0.5 * distance + 1)), 0x10) * 0x010101, false);
                y += textRenderer.lineHeight * scale;
            }
        }
        guiGraphics.setColor(1, 1, 1, 1);
        guiGraphics.pose().popPose();
    }
}
