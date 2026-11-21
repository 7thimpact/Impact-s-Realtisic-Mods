package com.impact.lib.audit;

import net.minecraft.resources.ResourceLocation;

/** Two recipes that could match the same crafting grid, or a vanilla recipe two Impact mods both override. */
public record RecipeConflict(ResourceLocation first, ResourceLocation second, String reason) {
}
