package com.impact.realisticfood.boiling;

/** The two sizes of pot: a cauldron, or a bucket (on a campfire, or a stone-boiling pot). */
public enum PotKind {
    CAULDRON("cauldron"),
    BUCKET("bucket");

    public final String logName;

    PotKind(String logName) {
        this.logName = logName;
    }
}
