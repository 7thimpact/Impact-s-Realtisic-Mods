package com.impact.realisticinventory.lang;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Inventory's translation keys and their English text. Data generation writes {@link #ENGLISH} to en_us.json. */
public final class InventoryTranslations {
    public static final String POCKETS_FULL = "message.impactsinventory.pockets_full";

    public static final Map<String, String> ENGLISH = englishText();

    private InventoryTranslations() {
    }

    private static Map<String, String> englishText() {
        Map<String, String> english = new LinkedHashMap<>();
        english.put(POCKETS_FULL, "Pockets full");
        return Collections.unmodifiableMap(english);
    }
}
