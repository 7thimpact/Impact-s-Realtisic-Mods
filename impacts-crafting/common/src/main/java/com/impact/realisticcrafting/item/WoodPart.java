package com.impact.realisticcrafting.item;

/** The parts that keep their wood type ({@code wood} NBT): each wooden item made from them comes out in that wood. */
public enum WoodPart {
    TRIMMED_PLANKS("trimmed_planks", 300, false),
    WOODEN_PANEL("wooden_panel", 300, false),
    WOODEN_FRAME("wooden_frame", 400, false),
    FENCE_POST("fence_post", 150, false),
    BOAT_HULL("boat_hull", 900, true);

    public final String path;
    public final int burnTicks;
    /** Only woods with boats get this part (crimson and warped have none). */
    public final boolean needsBoatWood;

    WoodPart(String path, int burnTicks, boolean needsBoatWood) {
        this.path = path;
        this.burnTicks = burnTicks;
        this.needsBoatWood = needsBoatWood;
    }
}
