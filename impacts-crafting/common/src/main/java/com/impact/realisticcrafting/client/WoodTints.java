package com.impact.realisticcrafting.client;

import com.impact.lib.wood.WoodData;
import com.impact.lib.wood.WoodKind;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The tint of a per-wood part's sprite: its wood's planks color on the map, brightened a little because the sprite is
 * shaded gray. Modded woods get theirs the same way. A part without a wood shows untinted.
 */
public final class WoodTints {
    private static final int UNTINTED = 0xFFFFFF;
    private static final float BRIGHTEN = 1.3F;
    private static final int CHANNEL_MAXIMUM = 255;
    private static final Map<ResourceLocation, Integer> TINTS = new ConcurrentHashMap<>();

    private WoodTints() {
    }

    public static int tintOf(ItemStack stack) {
        return WoodData.get(stack).map(wood -> TINTS.computeIfAbsent(wood, WoodTints::planksTint)).orElse(UNTINTED);
    }

    private static int planksTint(ResourceLocation wood) {
        return WoodData.vanillaItem(wood, WoodKind.PLANKS)
                .map(planks -> brighten(Block.byItem(planks).defaultMapColor().col))
                .orElse(UNTINTED);
    }

    private static int brighten(int color) {
        int red = Math.min(CHANNEL_MAXIMUM, Math.round(((color >> 16) & 0xFF) * BRIGHTEN));
        int green = Math.min(CHANNEL_MAXIMUM, Math.round(((color >> 8) & 0xFF) * BRIGHTEN));
        int blue = Math.min(CHANNEL_MAXIMUM, Math.round((color & 0xFF) * BRIGHTEN));
        return (red << 16) | (green << 8) | blue;
    }
}
