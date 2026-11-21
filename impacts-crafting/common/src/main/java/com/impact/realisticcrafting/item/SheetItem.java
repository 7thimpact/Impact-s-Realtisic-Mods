package com.impact.realisticcrafting.item;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

/** A colored cloth: a Bed Sheet (a bed's last layer and its color) or Banner Cloth. Sixteen items each, one per color, like wool. */
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
