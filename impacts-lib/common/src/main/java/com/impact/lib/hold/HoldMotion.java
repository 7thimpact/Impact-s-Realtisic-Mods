package com.impact.lib.hold;

/**
 * How the arms move during a hold-to-use action, or during work a mod shows through {@link WorkMotions} (crafting at a
 * table): in third person through playerAnimator's layer when it's installed (with elbow bends, a torso lean and fades),
 * otherwise through Lib's own arm poses; in first person through Lib's own hand offsets. See {@link HoldMotionPoses}.
 */
public enum HoldMotion {
    /** Circling the main hand over the off hand, like grinding in a bowl. */
    STIR(WorkingHands.MAIN, 20),
    /** Raising and bringing down the off hand's hammer onto what the main hand holds, one blow per stroke. */
    HAMMER(WorkingHands.OFF, 10),
    /** Turning something in front of the body with both hands, like a spit or a crank. */
    TURN(WorkingHands.BOTH, 20),
    /** Putting something together with both hands in front of the body, like a craft at a table: the hands take turns. */
    CRAFT(WorkingHands.BOTH, 16);

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
