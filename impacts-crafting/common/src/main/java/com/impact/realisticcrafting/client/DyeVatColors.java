package com.impact.realisticcrafting.client;

import com.impact.realisticcrafting.block.DyeVatBlockEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;

/** The tint of a Dye Vat's water (its dye's color, or plain water blue) and of the Bed Sheets. */
public final class DyeVatColors {
    public static final int PLAIN_WATER = 0x3F76E4;
    private static final float CHANNEL_MAXIMUM = 255.0F;

    private DyeVatColors() {
    }

    public static int colorAt(BlockEntity blockEntity) {
        if (!(blockEntity instanceof DyeVatBlockEntity vat)) {
            return PLAIN_WATER;
        }
        return vat.color().map(DyeVatColors::rgb).orElse(PLAIN_WATER);
    }

    /** The dye's color as RGB, as vanilla tints sheep and leather. */
    public static int rgb(DyeColor color) {
        float[] channels = color.getTextureDiffuseColors();
        int red = Math.round(channels[0] * CHANNEL_MAXIMUM);
        int green = Math.round(channels[1] * CHANNEL_MAXIMUM);
        int blue = Math.round(channels[2] * CHANNEL_MAXIMUM);
        return (red << 16) | (green << 8) | blue;
    }
}
