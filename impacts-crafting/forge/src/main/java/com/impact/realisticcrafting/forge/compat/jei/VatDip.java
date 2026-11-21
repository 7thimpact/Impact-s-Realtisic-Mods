package com.impact.realisticcrafting.forge.compat.jei;

import com.impact.realisticcrafting.recipe.VatDyeingRecipe;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * One way through the Dye Vat as JEI shows it: a vat dyeing recipe's input in water of one color (or plain water,
 * {@code color} empty) and what comes out.
 */
record VatDip(Ingredient input, Optional<DyeColor> color, ItemStack result, int charges, int dipTicks) {
    /** One entry per color a recipe has a result for. */
    static List<VatDip> of(VatDyeingRecipe recipe) {
        return recipe.results().entrySet().stream().map(colorResult -> dip(recipe, colorResult)).toList();
    }

    private static VatDip dip(VatDyeingRecipe recipe, Map.Entry<String, ItemStack> colorResult) {
        Optional<DyeColor> color = colorResult.getKey().equals(VatDyeingRecipe.PLAIN_WATER_KEY) ? Optional.empty()
                : Optional.ofNullable(DyeColor.byName(colorResult.getKey(), null));
        int charges = color.isPresent() ? recipe.charges() : 0;
        return new VatDip(recipe.input(), color, colorResult.getValue(), charges, recipe.dipTicks());
    }
}
