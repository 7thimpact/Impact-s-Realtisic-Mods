package com.impact.realisticfood.forge.datagen;

import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.lang.FoodTranslations;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;

/** Food's generated data: translations, item models, block states, item tags and recipes. Run with {@code runData}. */
public final class FoodDataGenerators {
    private static final String ENGLISH_LOCALE = "en_us";

    private FoodDataGenerators() {
    }

    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        generator.addProvider(event.includeClient(), new LanguageProvider(output, FoodMod.MOD_ID, ENGLISH_LOCALE) {
            @Override
            protected void addTranslations() {
                FoodTranslations.ENGLISH.forEach(this::add);
            }
        });
        generator.addProvider(event.includeClient(), new FoodItemModels(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new FoodBlockStates(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new FoodItemTags(output, event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new FoodRecipes(output));
    }
}
