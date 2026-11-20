package com.impact.realisticvitality.forge.datagen;

import com.impact.lib.forge.datagen.ImpactsAdvancementProvider;
import com.impact.realisticvitality.advancement.VitalityAdvancements;
import net.minecraft.data.PackOutput;

/** Writes the "Realistic Vitality" advancement tab. */
final class VitalityAdvancementProvider extends ImpactsAdvancementProvider {
    VitalityAdvancementProvider(PackOutput output) {
        super(output, "Impact's Realistic Vitality advancements");
    }

    @Override
    protected void addAdvancements(AdvancementOutput output) {
        VitalityAdvancements.all().forEach(advancement -> output.accept(advancement.id(), advancement.build()));
    }
}
