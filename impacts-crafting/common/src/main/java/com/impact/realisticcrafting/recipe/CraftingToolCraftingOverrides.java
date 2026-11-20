package com.impact.realisticcrafting.recipe;

import com.impact.lib.crafting.Byproduct;
import com.impact.lib.crafting.ToolCraftingOverrides;
import com.impact.lib.crafting.ToolCraftingRecipe;
import com.impact.lib.wood.WoodData;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

/** Crafting's config numbers for its tool-crafting recipes: Wood Scraps per craft, and none from nether wood. */
public final class CraftingToolCraftingOverrides implements ToolCraftingOverrides {
    @Override
    public int byproductCount(ToolCraftingRecipe recipe, Byproduct byproduct, CraftingContainer grid) {
        if (!byproduct.stack().is(CraftingItems.part(CraftingPart.WOOD_SCRAPS))) {
            return byproduct.stack().getCount();
        }
        return holdsNetherWood(grid) ? 0 : CraftingServerConfig.get().woodScraps.perCraft.get();
    }

    private static boolean holdsNetherWood(CraftingContainer grid) {
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            if (isNetherWood(grid.getItem(slot))) {
                return true;
            }
        }
        return false;
    }

    // Crimson and warped items are in vanilla's non-flammable wood tag; Crafting's own parts carry their wood in NBT.
    private static boolean isNetherWood(ItemStack stack) {
        return stack.is(ItemTags.NON_FLAMMABLE_WOOD) || WoodData.get(stack).map(wood -> !WoodData.isFlammable(wood)).orElse(false);
    }
}
