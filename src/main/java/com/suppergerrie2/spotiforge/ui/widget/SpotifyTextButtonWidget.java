package com.suppergerrie2.spotiforge.ui.widget;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class SpotifyTextButtonWidget extends AbstractButton {
    public interface PressAction {
        void onPress(AbstractWidget button);
    }

    private final PressAction pressAction;

    public SpotifyTextButtonWidget(int x, int y, int width, int height, Component message, PressAction action) {
        super(x, y, width, height, message);
        pressAction = action;
    }

    @Override
    public void onPress() {
        pressAction.onPress(this);
    }


    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
