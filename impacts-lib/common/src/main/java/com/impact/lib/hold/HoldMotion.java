package com.impact.lib.hold;

/** How the arms move during a hold-to-use action, when playerAnimator is installed (otherwise the arm swings each cycle). */
public enum HoldMotion {
    /** Circling the main hand over the off hand, like grinding in a bowl. */
    STIR,
    /** Raising and bringing down the main hand, like hammering. */
    HAMMER,
    /** Turning something in front of the body, like a spit or a crank. */
    TURN
}
