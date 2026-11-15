package com.impact.lib.forge.datagen;

import com.impact.lib.ImpactsLib;
import com.impact.lib.lang.LibTranslations;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

/** Writes {@code assets/impactslib/lang/en_us.json} from {@link LibTranslations#ENGLISH}. */
final class LibLanguageProvider extends LanguageProvider {
    private static final String ENGLISH_LOCALE = "en_us";

    LibLanguageProvider(PackOutput output) {
        super(output, ImpactsLib.MOD_ID, ENGLISH_LOCALE);
    }

    @Override
    protected void addTranslations() {
        LibTranslations.ENGLISH.forEach(this::add);
    }
}
