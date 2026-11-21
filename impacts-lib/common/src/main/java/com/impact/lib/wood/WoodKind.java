package com.impact.lib.wood;

/** The vanilla items each wood type has, named by the suffix vanilla gives them ({@code oak_door}, {@code oak_boat}, ...). */
public enum WoodKind {
    PLANKS("planks"),
    SLAB("slab"),
    STAIRS("stairs"),
    FENCE("fence"),
    FENCE_GATE("fence_gate"),
    DOOR("door"),
    TRAPDOOR("trapdoor"),
    BUTTON("button"),
    PRESSURE_PLATE("pressure_plate"),
    SIGN("sign"),
    HANGING_SIGN("hanging_sign"),
    BOAT("boat"),
    CHEST_BOAT("chest_boat");

    public final String suffix;

    WoodKind(String suffix) {
        this.suffix = suffix;
    }
}
