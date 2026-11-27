package com.impact.lib.hold;

import java.util.Locale;

/**
 * How the arms move during a hold-to-use action, or during work or a gesture a mod shows through {@link WorkMotions}
 * (crafting at a table, shivering): in third person through playerAnimator's layer when it's installed (with elbow
 * bends, a torso lean and fades), otherwise through Lib's own arm poses; in first person through Lib's own hand offsets.
 * See {@link HoldMotionPoses}.
 */
public enum HoldMotion {
    /** Circling the main hand over the off hand, like grinding in a bowl. */
    STIR(WorkingHands.MAIN, 20, Priority.HOLDING),
    /** Raising and bringing down the off hand's hammer onto what the main hand holds, one blow per stroke. */
    HAMMER(WorkingHands.OFF, 10, Priority.HOLDING),
    /** Turning something in front of the body with both hands, like a spit or a crank. */
    TURN(WorkingHands.BOTH, 20, Priority.HOLDING),
    /** Putting something together with both hands in front of the body, like a craft at a table: the hands take turns. */
    CRAFT(WorkingHands.BOTH, 16, Priority.WORKING),
    /** Both hands cupped and raised to the mouth, drinking. */
    SIP(WorkingHands.BOTH, 20, Priority.DRINKING),
    /** Wiping the brow with the main hand every few seconds, too hot. */
    WIPE_BROW(WorkingHands.MAIN, 60, Priority.FEELING),
    /** Arms hugged around the chest, trembling, too cold. */
    SHIVER(WorkingHands.BOTH, 4, Priority.FEELING);

    /** Which hand does the work; the other one holds steady. */
    public final WorkingHands workingHands;
    /** Ticks per stroke or turn. A hammer stroke lands at the end of its ticks, on the action's blow. */
    public final int strokeTicks;
    /** When mods show several motions on one player at once, the one shown: crafting over drinking over how they feel. */
    public final Priority priority;

    HoldMotion(WorkingHands workingHands, int strokeTicks, Priority priority) {
        this.workingHands = workingHands;
        this.strokeTicks = strokeTicks;
        this.priority = priority;
    }

    public boolean worksWith(boolean isMainHand) {
        return workingHands == WorkingHands.BOTH || (workingHands == WorkingHands.MAIN) == isMainHand;
    }

    /** The name in logs and in Lib's client config ({@code craft}, {@code wipe_brow}...). */
    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public enum WorkingHands {
        MAIN, OFF, BOTH
    }

    /** Lowest first: a player's feelings give way to drinking, drinking to work, and everything to an item held in use. */
    public enum Priority {
        FEELING, DRINKING, WORKING, HOLDING
    }
}
