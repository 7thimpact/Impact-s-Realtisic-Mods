package com.impact.lib.crafting;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;

/** An extra item a craft gives through Give or drop, with a chance (1 = always): Food's bones, Crafting's Wood Scraps. */
public record Byproduct(ItemStack stack, float chance) {
    private static final String CHANCE_FIELD = "chance";
    private static final float ALWAYS = 1.0F;

    public boolean rolls(RandomSource random) {
        return chance >= ALWAYS || random.nextFloat() < chance;
    }

    public static Byproduct fromJson(JsonObject json) {
        return new Byproduct(ShapedRecipe.itemStackFromJson(json), GsonHelper.getAsFloat(json, CHANCE_FIELD, ALWAYS));
    }

    public static Byproduct fromNetwork(FriendlyByteBuf buffer) {
        return new Byproduct(buffer.readItem(), buffer.readFloat());
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeItem(stack);
        buffer.writeFloat(chance);
    }
}
