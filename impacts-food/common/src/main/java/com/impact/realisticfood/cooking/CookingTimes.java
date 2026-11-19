package com.impact.realisticfood.cooking;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntSupplier;

/**
 * Cooking times from Food's server config for Food's own smelting, smoking and campfire recipes, looked up by recipe
 * ID. A mixin asks this whenever a furnace, smoker or campfire (or JEI) reads a cooking recipe's time.
 */
public final class CookingTimes {
    private static final Map<ResourceLocation, IntSupplier> TICKS_BY_RECIPE_ID = new ConcurrentHashMap<>();

    private CookingTimes() {
    }

    public static void register(ResourceLocation recipeId, IntSupplier configuredTicks) {
        TICKS_BY_RECIPE_ID.put(recipeId, configuredTicks);
    }

    /** The configured time for the recipe, or nothing to keep the time its JSON names. */
    public static OptionalInt configuredTicks(ResourceLocation recipeId) {
        IntSupplier configured = TICKS_BY_RECIPE_ID.get(recipeId);
        return configured == null ? OptionalInt.empty() : OptionalInt.of(configured.getAsInt());
    }
}
