package com.impact.realisticcrafting.item;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

/** A Bed Sheet: the last layer of a bed, and the bed's color. Sixteen items, one per color, like wool. */
public final class SheetItem extends Item {
    private static final int STACK_SIZE = 16;

    private final DyeColor color;

    public SheetItem(DyeColor color) {
        super(new Item.Properties().stacksTo(STACK_SIZE));
        this.color = color;
    }

    public DyeColor color() {
        return color;
    }
}
