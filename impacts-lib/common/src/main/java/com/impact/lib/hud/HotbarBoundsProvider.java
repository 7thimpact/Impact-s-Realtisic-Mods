package com.impact.lib.hud;

/** Works out where the hotbar is drawn for a screen size. Impact's Realistic Inventory publishes one. */
@FunctionalInterface
public interface HotbarBoundsProvider {
    HudBounds boundsFor(int screenWidth, int screenHeight);
}
