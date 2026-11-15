package com.impact.realisticinventory.forge.datagen;

import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.lang.InventoryTranslations;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;

/** Inventory's generated data. Run with {@code runData}. */
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
    }
}
