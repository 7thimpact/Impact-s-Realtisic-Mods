package com.impact.realisticinventory.client;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.DyeableLeatherItem;

/** Packs tint their first texture layer with their dye color, like leather armor; the second layer (buckles, frame) stays as drawn. */
public final class PackItemColors {
    private static final int NO_TINT = -1;
    private static final int DYED_LAYER = 0;
    public static final ItemColor DYED_FIRST_LAYER = (stack, tintIndex) -> tintIndex == DYED_LAYER && stack.getItem() instanceof DyeableLeatherItem dyeable
            ? dyeable.getColor(stack)
            : NO_TINT;

    private PackItemColors() {
    }
}
