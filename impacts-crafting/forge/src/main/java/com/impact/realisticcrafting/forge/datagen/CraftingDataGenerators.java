package com.impact.realisticcrafting.forge.datagen;

import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.lang.CraftingTranslations;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;

/** Crafting's generated data: translations, models, tags, loot tables, loot modifiers and worldgen. Run with {@code runData}. */
public final class CraftingDataGenerators {
    private static final String ENGLISH_LOCALE = "en_us";

    private CraftingDataGenerators() {
    }

    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        generator.addProvider(event.includeClient(), new LanguageProvider(output, CraftingMod.MOD_ID, ENGLISH_LOCALE) {
            @Override
            protected void addTranslations() {
                CraftingTranslations.ENGLISH.forEach(this::add);
            }
        });
        generator.addProvider(event.includeClient(), new CraftingItemModels(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new CraftingBlockStates(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new CraftingBlockTags(output, event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), CraftingLootTables.create(output));
        generator.addProvider(event.includeServer(), new CraftingLootModifiers(output));
        generator.addProvider(event.includeServer(), CraftingWorldgenData.create(output, event.getLookupProvider()));
    }
}
