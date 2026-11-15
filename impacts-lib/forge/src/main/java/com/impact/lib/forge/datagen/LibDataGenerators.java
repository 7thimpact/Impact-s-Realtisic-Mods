package com.impact.lib.forge.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;

/** Lib's generated data: the "Why am I here?" advancement and the English translations. Run with {@code runData}. */
public final class LibDataGenerators {
    private LibDataGenerators() {
    }

    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        generator.addProvider(event.includeServer(), new LibAdvancementProvider(output));
        generator.addProvider(event.includeClient(), new LibLanguageProvider(output));
    }
}
