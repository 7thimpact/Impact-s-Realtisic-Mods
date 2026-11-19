package com.impact.realisticfood.cutting;

import com.impact.lib.ResourceIds;
import com.impact.lib.config.KeyValueEntries;
import com.impact.lib.crafting.Byproduct;
import com.impact.lib.crafting.ToolCraftingOverrides;
import com.impact.lib.crafting.ToolCraftingRecipe;
import com.impact.realisticfood.config.FoodServerConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Food's config for its tool-crafting recipes: {@code cutting.byproducts} switches byproducts off, and
 * {@code cutting.yieldOverrides} and {@code cutting.byproductChanceOverrides} change them per input item.
 */
public final class CuttingOverrides implements ToolCraftingOverrides {
    private static final int MINIMUM_YIELD = 1;
    private static final float NEVER = 0.0F;

    @Override
    public int resultCount(ToolCraftingRecipe recipe, int recipeCount) {
        List<String> overrides = FoodServerConfig.get().cutting.yieldOverrides.get();
        Optional<OptionalInt> configured = inputIdOf(recipe).map(inputId -> KeyValueEntries.findInt(overrides, inputId));
        return configured.filter(OptionalInt::isPresent).map(count -> Math.max(MINIMUM_YIELD, count.getAsInt())).orElse(recipeCount);
    }

    @Override
    public float byproductChance(ToolCraftingRecipe recipe, Byproduct byproduct) {
        FoodServerConfig.CuttingSettings settings = FoodServerConfig.get().cutting;
        if (!settings.areByproductsOn.get()) {
            return NEVER;
        }
        List<String> overrides = settings.byproductChanceOverrides.get();
        Optional<OptionalDouble> configured = inputIdOf(recipe).map(inputId -> KeyValueEntries.findDouble(overrides, inputId));
        return configured.filter(OptionalDouble::isPresent).map(chance -> (float) Mth.clamp(chance.getAsDouble(), 0.0, 1.0)).orElse(byproduct.chance());
    }

    /** The ID of the item being cut: the first item the recipe's first ingredient accepts. */
    static Optional<String> inputIdOf(ToolCraftingRecipe recipe) {
        return recipe.getIngredients().stream()
                .filter(ingredient -> !ingredient.isEmpty())
                .findFirst()
                .map(Ingredient::getItems)
                .flatMap(stacks -> Arrays.stream(stacks).findFirst())
                .map(ItemStack::getItem)
                .map(CuttingOverrides::idOf);
    }

    private static String idOf(Item item) {
        return ResourceIds.itemId(item).toString();
    }
}
