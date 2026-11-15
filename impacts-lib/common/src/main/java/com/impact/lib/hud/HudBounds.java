package com.impact.lib.hud;

/** A rectangle on screen, in GUI-scaled pixels. */
public record HudBounds(int left, int top, int width, int height) {
    public int right() {
        return left + width;
    }

    public int bottom() {
        return top + height;
    }

    public int centerX() {
        return left + width / 2;
    }
}
