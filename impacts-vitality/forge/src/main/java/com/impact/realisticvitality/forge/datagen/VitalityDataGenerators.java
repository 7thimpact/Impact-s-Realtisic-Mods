package com.impact.realisticvitality.forge.datagen;

import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.lang.VitalityTranslations;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;

/** Vitality's generated data. Run with {@code runData}. */
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
    }
}
