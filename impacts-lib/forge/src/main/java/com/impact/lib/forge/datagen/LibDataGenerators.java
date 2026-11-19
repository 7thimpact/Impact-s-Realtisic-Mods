package com.impact.lib.forge.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;

/** Lib's generated data: the "Why am I here?" advancement, the English translations, and Lib's item and block tags. Run with {@code runData}. */
public final class LibDataGenerators {
    private LibDataGenerators() {
    }

    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        generator.addProvider(event.includeServer(), new LibAdvancementProvider(output));
        generator.addProvider(event.includeClient(), new LibLanguageProvider(output));
        generator.addProvider(event.includeServer(), new LibItemTagsProvider(output, event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new LibBlockTagsProvider(output, event.getLookupProvider(), event.getExistingFileHelper()));
    }
}
