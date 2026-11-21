package com.impact.realisticfood.boiling;

import java.util.Optional;

/** What a pot holds in the way of soup, as clients see it. */
public enum SoupState {
    NONE,
    /** Soup ingredients, or a soup still cooking. */
    COOKING,
    READY;

    public static SoupState of(BoilingPot pot) {
        if (pot.isSoupReady()) {
            return READY;
        }
        return pot.holdsSoup() ? COOKING : NONE;
    }

    public static SoupState of(Optional<BoilingPot> pot) {
        return pot.map(SoupState::of).orElse(NONE);
    }
}
