package com.impact.realisticcrafting.timing;

import com.impact.lib.crafting.ToolCraftingRecipe;
import com.impact.realisticcrafting.config.CraftingFeature;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.config.KeyedNumbers;
import com.impact.realisticcrafting.menu.CraftingStation;
import net.minecraft.world.item.crafting.Recipe;

import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * How long a craft takes: 10 ticks per level of depth, capped at 100, or the fallback for recipes the depth table can't
 * place. A recipe's {@code recipeOverrides} entry or its own {@code craft_ticks} wins; a mod multiplier scales the rest.
 */
public final class CraftTimes {
    private CraftTimes() {
    }

    /** Whether crafts at this station are timed right now. */
    public static boolean isTimed(CraftingStation station) {
        CraftingServerConfig.TimedCraftingSettings settings = CraftingServerConfig.get().timedCrafting;
        if (!CraftingFeature.TIMED_CRAFTING.isOn() || !settings.isOn.get()) {
            return false;
        }
        return switch (station) {
            case INVENTORY_GRID -> settings.timesInventoryGrid.get();
            case MAKESHIFT_TABLE, MAKESHIFT_STONE_TABLE -> settings.timesMakeshiftTables.get();
            case CRAFTING_TABLE -> settings.timesCraftingTable.get();
        };
    }

    public static int ticksFor(Recipe<?> recipe) {
        CraftingServerConfig.TimedCraftingSettings settings = CraftingServerConfig.get().timedCrafting;
        OptionalDouble override = KeyedNumbers.find(settings.recipeOverrides.get(), recipe.getId().toString());
        if (override.isPresent()) {
            return (int) Math.round(override.getAsDouble());
        }
        if (recipe instanceof ToolCraftingRecipe toolRecipe && toolRecipe.craftTicks() != ToolCraftingRecipe.NO_CRAFT_TICKS) {
            return toolRecipe.craftTicks();
        }
        OptionalInt depth = CraftDepthTable.recipeDepth(recipe);
        int ticks = depth.isPresent() ? Math.min(settings.maxTicks.get(), settings.ticksPerDepth.get() * depth.getAsInt()) : settings.fallbackTicks.get();
        double multiplier = KeyedNumbers.find(settings.modMultipliers.get(), recipe.getId().getNamespace()).orElse(1.0);
        return (int) Math.round(ticks * multiplier);
    }

    /** An item's own craft time from its depth, for {@code /impacts dump held}. */
    public static OptionalInt ticksForDepth(OptionalInt depth) {
        CraftingServerConfig.TimedCraftingSettings settings = CraftingServerConfig.get().timedCrafting;
        return depth.isPresent() ? OptionalInt.of(Math.min(settings.maxTicks.get(), settings.ticksPerDepth.get() * depth.getAsInt())) : OptionalInt.empty();
    }
}
