package com.impact.realisticfood.forge.datagen;

import com.impact.lib.forge.datagen.ConditionalRecipes;
import com.impact.lib.forge.datagen.ImpactsAdvancementProvider;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.advancement.FoodAdvancements;
import net.minecraft.data.PackOutput;

/** Writes the "Realistic Food" advancement tab. Stone Soup loads only with Crafting installed. */
final class FoodAdvancementProvider extends ImpactsAdvancementProvider {
    FoodAdvancementProvider(PackOutput output) {
        super(output, "Impact's Realistic Food advancements");
    }

    @Override
    protected void addAdvancements(AdvancementOutput output) {
        FoodAdvancements.all().forEach(advancement -> {
            boolean needsCrafting = FoodAdvancements.NEEDS_CRAFTING.contains(advancement.id());
            if (needsCrafting) {
                output.accept(advancement.id(), advancement.build(), ConditionalRecipes.modLoaded(FoodMod.CRAFTING_MOD_ID));
            } else {
                output.accept(advancement.id(), advancement.build());
            }
        });
    }
}
