package com.impact.realisticcrafting.item;

/**
 * The bucket is cast, not bent from plates: a clay mold is shaped and fired in a furnace, filled with 3 iron ingots,
 * melted in a furnace (or faster in a blast furnace), and cooled in water, which gives the bucket and the mold back.
 */
public enum BucketMold {
    UNFIRED("unfired_bucket_mold", 16),
    FIRED("bucket_mold", 16),
    FILLED("filled_bucket_mold", 1),
    MOLTEN("molten_bucket_mold", 1);

    public final String path;
    public final int stackSize;

    BucketMold(String path, int stackSize) {
        this.path = path;
        this.stackSize = stackSize;
    }
}
