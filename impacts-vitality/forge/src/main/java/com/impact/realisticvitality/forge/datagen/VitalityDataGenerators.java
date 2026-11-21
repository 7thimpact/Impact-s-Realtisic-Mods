package com.impact.realisticvitality.forge.datagen;

import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.lang.VitalityTranslations;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

/** Vitality's generated data: translations, item and block models, sounds, tags, loot tables, recipes, advancements and damage types. Run with {@code runData}. */
public final class VitalityDataGenerators {
    private static final String ENGLISH_LOCALE = "en_us";

    private VitalityDataGenerators() {
    }

    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        generator.addProvider(event.includeClient(), new LanguageProvider(output, VitalityMod.MOD_ID, ENGLISH_LOCALE) {
            @Override
            protected void addTranslations() {
                VitalityTranslations.ENGLISH.forEach(this::add);
            }
        });
        generator.addProvider(event.includeClient(), new VitalityItemModels(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new VitalityBlockStates(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new VitalitySoundDefinitions(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new VitalityBlockTags(output, event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), VitalityLootTables.create(output));
        generator.addProvider(event.includeServer(), new VitalityItemTags(output, event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new VitalityRecipes(output));
        generator.addProvider(event.includeServer(), new VitalityAdvancementProvider(output));
        CompletableFuture<HolderLookup.Provider> lookupWithDamageTypes = VitalityDamageTypeData.withDamageTypes(event.getLookupProvider());
        generator.addProvider(event.includeServer(), VitalityDamageTypeData.damageTypes(output, lookupWithDamageTypes));
        generator.addProvider(event.includeServer(), new VitalityDamageTypeData.Tags(output, lookupWithDamageTypes, event.getExistingFileHelper()));
    }
}
