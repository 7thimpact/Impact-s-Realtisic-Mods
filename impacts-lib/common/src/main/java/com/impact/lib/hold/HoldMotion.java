package com.impact.lib.hold;

/**
 * How the arms move during a hold-to-use action: through playerAnimator's layer when it's installed, otherwise
 * through Lib's own arm poses (third person) and hand offsets (first person). See {@link HoldMotionPoses}.
 */
public enum HoldMotion {
    /** Circling the main hand over the off hand, like grinding in a bowl. */
    STIR(WorkingHands.MAIN, 20),
    /** Raising and bringing down the off hand's hammer onto what the main hand holds, one blow per stroke. */
    HAMMER(WorkingHands.OFF, 10),
    /** Turning something in front of the body with both hands, like a spit or a crank. */
    TURN(WorkingHands.BOTH, 20);

    /** Which hand does the work; the other one holds steady. */
    public final WorkingHands workingHands;
    /** Ticks per stroke or turn. A hammer stroke lands at the end of its ticks, on the action's blow. */
    public final int strokeTicks;

    HoldMotion(WorkingHands workingHands, int strokeTicks) {
        this.workingHands = workingHands;
        this.strokeTicks = strokeTicks;
    }

    public boolean worksWith(boolean isMainHand) {
        return workingHands == WorkingHands.BOTH || (workingHands == WorkingHands.MAIN) == isMainHand;
    }

    public enum WorkingHands {
        MAIN, OFF, BOTH
    }
}
