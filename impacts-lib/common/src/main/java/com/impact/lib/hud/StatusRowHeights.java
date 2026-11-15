package com.impact.lib.hud;

/**
 * How high the status rows above the hotbar already reach, on the left (health, armor) and the right (hunger,
 * air), in pixels from the bottom of the screen. Each loader supplies its own; on Forge they are {@code ForgeGui}'s.
 */
public interface StatusRowHeights {
    int leftHeight();

    int rightHeight();

    void raiseLeft(int pixels);

    void raiseRight(int pixels);
}
