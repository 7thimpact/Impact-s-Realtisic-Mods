package com.impact.realisticinventory.forge.datagen;

import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.lang.InventoryTranslations;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;

/** Inventory's generated data: translations, item models, item tags and recipes. Run with {@code runData}. */
public final class InventoryDataGenerators {
    private static final String ENGLISH_LOCALE = "en_us";

    private InventoryDataGenerators() {
    }

    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        generator.addProvider(event.includeClient(), new LanguageProvider(output, InventoryMod.MOD_ID, ENGLISH_LOCALE) {
            @Override
            protected void addTranslations() {
                InventoryTranslations.ENGLISH.forEach(this::add);
            }
        });
        generator.addProvider(event.includeClient(), new InventoryItemModels(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new InventoryItemTags(output, event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new InventoryRecipes(output));
    }
}
