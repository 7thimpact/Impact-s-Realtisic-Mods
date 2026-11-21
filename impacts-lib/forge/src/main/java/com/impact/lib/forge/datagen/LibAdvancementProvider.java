package com.impact.lib.forge.datagen;

import com.impact.lib.advancement.LibAdvancements;
import com.impact.lib.forge.condition.NoImpactModsCondition;
import net.minecraft.data.PackOutput;

/** Writes "Why am I here?" with Lib's load condition, so it only exists when Lib is the only Impact mod. */
final class LibAdvancementProvider extends ImpactsAdvancementProvider {
    LibAdvancementProvider(PackOutput output) {
        super(output, "Impact's Lib advancements");
    }

    @Override
    protected void addAdvancements(AdvancementOutput output) {
        output.accept(LibAdvancements.WHY_AM_I_HERE_ID, LibAdvancements.whyAmIHere(), NoImpactModsCondition.INSTANCE);
    }
}
