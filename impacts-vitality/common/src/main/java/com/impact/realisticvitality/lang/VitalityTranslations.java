package com.impact.realisticvitality.lang;

import com.impact.realisticvitality.VitalityMod;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Vitality's translation keys and their English text. Data generation writes {@link #ENGLISH} to en_us.json. */
public final class VitalityTranslations {
    /** Minecraft names a tab after its root; this key is for tools that list tabs by mod. */
    public static final String ADVANCEMENT_TAB = "advancements." + VitalityMod.MOD_ID + ".tab";

    public static final Map<String, String> ENGLISH = buildEnglish();

    private VitalityTranslations() {
    }

    private static Map<String, String> buildEnglish() {
        Map<String, String> english = new LinkedHashMap<>();
        english.put(ADVANCEMENT_TAB, "Realistic Vitality");
        return Collections.unmodifiableMap(english);
    }
}
