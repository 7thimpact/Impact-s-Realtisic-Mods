package com.impact.lib.crafting;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * One finished tool craft, passed to {@link ToolCrafting#onCrafted} listeners on the server.
 *
 * @param toolsBefore the tools as they went in
 * @param toolsAfter  the same tools as they came back (empty stacks for tools that broke), in the same order
 * @param byproducts  the byproducts the player was given (or that dropped at their feet)
 */
public record CompletedToolCraft(ServerPlayer player, ToolCraftingRecipe recipe, ItemStack result, List<ItemStack> toolsBefore,
                                 List<ItemStack> toolsAfter, List<ItemStack> byproducts) {
}
