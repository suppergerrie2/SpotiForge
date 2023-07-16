package com.suppergerrie2.spotiforge.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.suppergerrie2.spotiforge.SpotiForge;
import com.suppergerrie2.spotiforge.ui.widget.SpotifyPlaylistItemWidget;
import com.suppergerrie2.spotiforge.ui.widget.SpotifyTextButtonWidget;
import mine.block.spotify.SpotifyHandler;
import mine.block.spotify.SpotifyUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;
import se.michaelthelin.spotify.enums.ProductType;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.Objects;

public class SpotifyScreen extends Screen {
    private final boolean connected;
    private final Screen parent;
    public float percentageDone;
    public float progress;

    public SpotifyScreen(Screen parent) {
        super(Component.empty());
        this.parent = parent;
        this.connected = SpotifyHandler.SPOTIFY_API != null;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics context) {
        context.fill(0, 0, this.width, this.height, 0xFF121212);
    }

    public void triggerManualPoll() {
        new SpotifyHandler.PollingThread().run();
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(new SpotifyTextButtonWidget(2, height - 22, 46, 20, Component.keybind("⟵ Back"), (btn) -> Objects.requireNonNull(minecraft).setScreen(parent)));
        this.addRenderableWidget(new SpotifyPlaylistItemWidget(0, 0, width, height - 25));

        var currentlyPlaying = SpotifyUtils.NOW_PLAYING;

        if (currentlyPlaying != null) {
            progress = ((float) 0.5 * currentlyPlaying.getProgress_ms()) / ((float) 0.5 * currentlyPlaying.getItem().getDurationMs());
        }

        boolean isPremium = false;

        try {
            var profile = SpotifyHandler.SPOTIFY_API.getCurrentUsersProfile().build().execute();
            if (profile.getProduct() == ProductType.PREMIUM) {
                isPremium = true;
            }
        } catch (IOException | SpotifyWebApiException | ParseException ignored) {
        }

        SpotifyTextButtonWidget infoWidget = new SpotifyTextButtonWidget(2 + 46 + 2, height - 22, 20, 20, Component.literal("ℹ"), (btn) -> Util.getPlatform().openUri("https://github.com/suppergerrie2/SpotiForge"));
        this.addRenderableWidget(infoWidget);

        SpotifyTextButtonWidget previousWidget = new SpotifyTextButtonWidget(2 + 46 + 2 + 20 + 2, height - 22, 20, 20, Component.literal("←"), (btn) -> {
            btn.active = false;
            new Thread(() -> {
                try {
                    SpotifyHandler.SPOTIFY_API.skipUsersPlaybackToPreviousTrack().build().execute();
                    btn.active = true;
                    triggerManualPoll();
                } catch (IOException | ParseException | SpotifyWebApiException ignored) {
                }
            }).start();
        });

        previousWidget.active = isPremium;

        SpotifyTextButtonWidget pausePlayWidget = new SpotifyTextButtonWidget(2 + 46 + 2 + 20 + 2 + 20 + 2, height - 22, 20, 20, Component.literal("⏯"), (btn) -> {
            btn.active = false;
            new Thread(() -> {
                try {
                    var status = SpotifyHandler.SPOTIFY_API.getInformationAboutUsersCurrentPlayback().build().execute();
                    if (status.getIs_playing()) SpotifyHandler.SPOTIFY_API.pauseUsersPlayback().build().execute();
                    else SpotifyHandler.SPOTIFY_API.startResumeUsersPlayback().build().execute();
                    btn.active = true;
                    triggerManualPoll();
                } catch (IOException | ParseException | SpotifyWebApiException ignored) {
                }
            }).start();
        });

        pausePlayWidget.active = isPremium;

        SpotifyTextButtonWidget nextWidget = new SpotifyTextButtonWidget(2 + 46 + 2 + 20 + 2 + 20 + 2 + 20 + 2, height - 22, 20, 20, Component.literal("→"), (btn) -> {
            btn.active = false;
            new Thread(() -> {
                try {
                    SpotifyHandler.SPOTIFY_API.skipUsersPlaybackToNextTrack().build().execute();
                    btn.active = true;
                    triggerManualPoll();
                } catch (IOException | ParseException | SpotifyWebApiException ignored) {
                }
            }).start();
        });

        nextWidget.active = isPremium;

        SpotifyTextButtonWidget shuffleWidget = new SpotifyTextButtonWidget(width - 22, height - 22, 20, 20, Component.literal("S"), (btn) -> {
            btn.active = false;
            new Thread(() -> {
                try {
                    var playbackStatus = SpotifyHandler.SPOTIFY_API.getInformationAboutUsersCurrentPlayback().build().execute();
                    boolean shuffleState = playbackStatus.getShuffle_state();
                    SpotifyHandler.SPOTIFY_API.toggleShuffleForUsersPlayback(!shuffleState).build().execute();
                    btn.setMessage(shuffleState ? Component.literal("S") : Component.literal("S").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
                    btn.active = true;
                    triggerManualPoll();
                } catch (IOException | ParseException | SpotifyWebApiException ignored) {
                }
            }).start();
        });

        shuffleWidget.active = isPremium;

        SpotifyTextButtonWidget loopWidget = new SpotifyTextButtonWidget(width - 44, height - 22, 20, 20, Component.literal("R"), (btn) -> {
            btn.active = false;
            new Thread(() -> {
                try {
                    var playbackStatus = SpotifyHandler.SPOTIFY_API.getInformationAboutUsersCurrentPlayback().build().execute();
                    var repeat_state = playbackStatus.getRepeat_state();
                    switch (repeat_state) {
                        case "context" -> {
                            SpotifyHandler.SPOTIFY_API.setRepeatModeOnUsersPlayback("off").build().execute();
                            btn.setMessage(Component.literal("R"));
                        }
                        case "track" -> {
                            SpotifyHandler.SPOTIFY_API.setRepeatModeOnUsersPlayback("context").build().execute();
                            btn.setMessage(Component.literal("R").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));
                        }
                        case "off" -> {
                            SpotifyHandler.SPOTIFY_API.setRepeatModeOnUsersPlayback("track").build().execute();
                            btn.setMessage(Component.literal("R").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                        }
                    }
                    btn.active = true;
                    triggerManualPoll();
                } catch (IOException | ParseException | SpotifyWebApiException ignored) {
                }
            }).start();
        });

        loopWidget.active = isPremium;

        this.addRenderableWidget(shuffleWidget);
        this.addRenderableWidget(loopWidget);
        this.addRenderableWidget(previousWidget);
        this.addRenderableWidget(nextWidget);
        this.addRenderableWidget(pausePlayWidget);
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        for (var element : this.children()) {
            if (element instanceof Renderable drawable)
                drawable.render(context, mouseX, mouseY, delta);
        }

        var textRenderer = Minecraft.getInstance().font;

        if (!connected) {
            Component text = Component.literal("No Spotify Connection").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
            int widthe = textRenderer.width(text);
            context.drawString(textRenderer, text, (width / 2) - (widthe / 2), (height / 2) - (widthe / 2), 0xFFFF0000, false);
            return;
        }

        renderProgressBar(context);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        context.blit(new ResourceLocation(SpotiForge.MODID, "textures/spotify_logo_big.png"), this.width - 2 - 115, 4, 107, 32, 0, 0, 426, 128, 426, 128);

        RenderSystem.disableBlend();
    }

    private void renderProgressBar(GuiGraphics context) {
        float playbackBarWidth = this.width - 46;
        context.fill(2 + 46 + 2 + 20 + 2 + 20 + 2 + 20 + 2 + 20 + 2, height - 19, (int) playbackBarWidth, height - 4, 0xFF5E5E5E);
        this.percentageDone = Mth.clamp(this.percentageDone * 0.95F + this.progress * 0.050000012F, 0.0F, 1.0F);

        if (percentageDone <= 0) {
            return;
        }

        float filledWidth = 2 + 46 + 2 + 20 + 2 + 20 + 2 + 20 + 2 + 20 + 2 + Mth.ceil((float) ((this.width - 46) - (2 + 46 + 2 + 20 + 2 + 20 + 2 + 20 + 2 + 20 + 2) - 2) * this.percentageDone);
        context.fill(2 + 46 + 2 + 20 + 2 + 20 + 2 + 20 + 2 + 20 + 2, height - 19, (int) filledWidth, height - 4, 0xFFFFFFFF);
    }
}
