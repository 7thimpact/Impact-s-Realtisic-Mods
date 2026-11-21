package com.impact.lib.forge.datagen;

import com.impact.lib.wood.WoodData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.PartialNBTIngredient;

/** Recipe ingredients for per-wood parts: the item with that wood only ({@code forge:partial_nbt}), so woods never mix. */
public final class WoodIngredients {
    private WoodIngredients() {
    }

    public static Ingredient of(ItemLike item, ResourceLocation wood) {
        return PartialNBTIngredient.of(item, WoodData.tagFor(wood));
    }
}
