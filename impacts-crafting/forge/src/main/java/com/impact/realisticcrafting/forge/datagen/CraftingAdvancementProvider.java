package com.impact.realisticcrafting.forge.datagen;

import com.impact.lib.forge.datagen.ImpactsAdvancementProvider;
import com.impact.realisticcrafting.advancement.CraftingAdvancements;
import net.minecraft.data.PackOutput;

/** Writes the "Realistic Crafting" advancement tab. */
final class CraftingAdvancementProvider extends ImpactsAdvancementProvider {
    CraftingAdvancementProvider(PackOutput output) {
        super(output, "Impact's Realistic Crafting advancements");
    }

    @Override
    protected void addAdvancements(AdvancementOutput output) {
        CraftingAdvancements.all().forEach(advancement -> output.accept(advancement.id(), advancement.build()));
    }
}
