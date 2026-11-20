package com.impact.realisticcrafting.recipe;

import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.config.ItemEntries;
import com.impact.realisticcrafting.menu.CraftingTier;
import com.impact.realisticcrafting.registry.CraftingTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The tier of items and recipes. An item's tier comes from the config's extra lists first, then the tier tags. An
 * ingredient takes the lowest tier among the items it accepts (the cheapest way to fill it), and a recipe the highest
 * tier among its ingredients, tools included. Recipes without an ingredient list are non-metal.
 */
public final class TierResolver {
    // Keyed by the recipe object, so a data pack reload (new recipes, new tags) starts over on its own.
    private static final Map<Recipe<?>, CraftingTier> RECIPE_TIERS = Collections.synchronizedMap(new WeakHashMap<>());

    private TierResolver() {
    }

    public static CraftingTier tierOf(Recipe<?> recipe) {
        return RECIPE_TIERS.computeIfAbsent(recipe, TierResolver::computeTier);
    }

    public static CraftingTier tierOf(ItemStack stack) {
        CraftingServerConfig.TierSettings settings = CraftingServerConfig.get().tiers;
        if (ItemEntries.contains(settings.extraAdvanced.get(), stack)) {
            return CraftingTier.ADVANCED;
        }
        if (ItemEntries.contains(settings.extraStone.get(), stack) || stack.is(CraftingTags.TIER_STONE)) {
            return CraftingTier.STONE;
        }
        return stack.is(CraftingTags.TIER_ADVANCED) ? CraftingTier.ADVANCED : CraftingTier.NON_METAL;
    }

    public static CraftingTier tierOf(Ingredient ingredient) {
        return Arrays.stream(ingredient.getItems())
                .map(TierResolver::tierOf)
                .reduce(CraftingTier::lowest)
                .orElse(CraftingTier.NON_METAL);
    }

    /** Forgets every recipe's tier, for when the tier lists in the config change. */
    public static void clearCache() {
        RECIPE_TIERS.clear();
    }

    private static CraftingTier computeTier(Recipe<?> recipe) {
        return recipe.getIngredients().stream()
                .filter(ingredient -> !ingredient.isEmpty())
                .map(TierResolver::tierOf)
                .reduce(CraftingTier::highest)
                .orElse(CraftingTier.NON_METAL);
    }
}
