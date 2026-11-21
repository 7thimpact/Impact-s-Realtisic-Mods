package com.impact.realisticcrafting.recipe;

import com.impact.lib.ResourceIds;
import com.impact.lib.wood.WoodData;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.item.CraftingTool;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;

import java.util.HashSet;
import java.util.Set;

/** The Iron Saw doubles what trimming wood gives: planks from logs (4), sticks (8) and Trimmed Planks (2). */
public final class SawYields {
    private static final Set<ResourceLocation> DOUBLED_RECIPES = buildDoubledRecipes();

    private SawYields() {
    }

    public static boolean isDoubled(ResourceLocation recipeId, CraftingContainer grid) {
        return DOUBLED_RECIPES.contains(recipeId) && holdsSaw(grid);
    }

    private static boolean holdsSaw(CraftingContainer grid) {
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            if (grid.getItem(slot).is(CraftingItems.tool(CraftingTool.IRON_SAW))) {
                return true;
            }
        }
        return false;
    }

    private static Set<ResourceLocation> buildDoubledRecipes() {
        Set<ResourceLocation> doubled = new HashSet<>();
        doubled.add(ResourceIds.of(CraftingMod.MOD_ID, "sticks"));
        WoodData.VANILLA_WOODS.forEach(wood -> {
            doubled.add(ResourceIds.of(CraftingMod.MOD_ID, wood.getPath() + "_planks"));
            doubled.add(ResourceIds.of(CraftingMod.MOD_ID, "trimmed_" + wood.getPath() + "_planks"));
        });
        return Set.copyOf(doubled);
    }
}
