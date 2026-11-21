package com.impact.realisticinventory.hotbar;

import java.util.Locale;

/** What made the player switch hotbar slots, for the log. */
public enum SwitchSource {
    NUMBER_KEY, SCROLL_WHEEL, SWAP_HANDS_KEY, PICK_BLOCK, OTHER;

    public String logName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
