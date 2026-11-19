package com.impact.realisticinventory.forge.datagen;

import com.impact.lib.forge.datagen.ImpactsAdvancementProvider;
import com.impact.realisticinventory.advancement.InventoryAdvancements;
import net.minecraft.data.PackOutput;

/** Writes the "Realistic Inventory" advancement tab. */
final class InventoryAdvancementProvider extends ImpactsAdvancementProvider {
    InventoryAdvancementProvider(PackOutput output) {
        super(output, "Impact's Realistic Inventory advancements");
    }

    @Override
    protected void addAdvancements(AdvancementOutput output) {
        InventoryAdvancements.all().forEach(advancement -> output.accept(advancement.id(), advancement.build()));
    }
}
