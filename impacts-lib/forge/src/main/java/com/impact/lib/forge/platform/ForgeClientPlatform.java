package com.impact.lib.forge.platform;

import com.impact.lib.hud.StatusRowHeights;
import com.impact.lib.platform.ClientPlatform;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.gui.overlay.ForgeGui;

/** Forge's answers to {@link ClientPlatform}. Found through {@code META-INF/services}; loaded only on the client. */
public final class ForgeClientPlatform implements ClientPlatform {
    @Override
    public StatusRowHeights statusRowHeights() {
        return new ForgeGuiRowHeights((ForgeGui) Minecraft.getInstance().gui);
    }

    /** The left and right row heights Forge's own HUD overlays use to stack. */
    private record ForgeGuiRowHeights(ForgeGui gui) implements StatusRowHeights {
        @Override
        public int leftHeight() {
            return gui.leftHeight;
        }

        @Override
        public int rightHeight() {
            return gui.rightHeight;
        }

        @Override
        public void raiseLeft(int pixels) {
            gui.leftHeight += pixels;
        }

        @Override
        public void raiseRight(int pixels) {
            gui.rightHeight += pixels;
        }
    }
}
