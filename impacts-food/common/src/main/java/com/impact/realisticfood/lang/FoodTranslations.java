package com.impact.realisticfood.lang;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Food's translation keys and their English text. Data generation writes {@link #ENGLISH} to en_us.json. */
public final class FoodTranslations {
    public static final String CREATIVE_TAB = "itemGroup.impactsfood";

    public static final Map<String, String> ENGLISH = Collections.unmodifiableMap(english());

    private FoodTranslations() {
    }

    private static Map<String, String> english() {
        Map<String, String> translations = new LinkedHashMap<>();
        translations.put(CREATIVE_TAB, "Impact's Realistic Food");
        return translations;
    }
}
