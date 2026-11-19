package com.impact.realisticfood.cutting;

import com.impact.lib.ResourceIds;
import com.impact.realisticfood.FoodMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * One cutting recipe: the input and a knife in any grid give {@code resultCount} results, and sometimes a byproduct.
 * Data generation writes each one as an {@code impactslib:tool_crafting} recipe with the ID {@link #recipeId()}.
 */
public record Cut(Item input, Supplier<Item> result, int resultCount, Optional<CutByproduct> byproduct) {
    private static final String RECIPE_FOLDER = "cutting/";

    public ResourceLocation recipeId() {
        return ResourceIds.of(FoodMod.MOD_ID, RECIPE_FOLDER + ResourceIds.itemId(input).getPath());
    }

    public record CutByproduct(Item item, float chance) {
    }
}
