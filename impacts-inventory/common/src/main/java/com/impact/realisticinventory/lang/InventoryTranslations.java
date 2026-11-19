package com.impact.realisticinventory.lang;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Inventory's translation keys and their English text. Data generation writes {@link #ENGLISH} to en_us.json. */
public final class InventoryTranslations {
    public static final String POCKETS_FULL = "message.impactsinventory.pockets_full";
    public static final String OPEN_PACK_KEY = "key.impactsinventory.open_pack";
    public static final String OPEN_PACK_BUTTON = "button.impactsinventory.open_pack";
    public static final String SATCHEL = "item.impactsinventory.satchel";
    public static final String BACKPACK = "item.impactsinventory.backpack";
    public static final String FRAME_PACK = "item.impactsinventory.frame_pack";
    public static final String FIRST_JOIN = "message.impactsinventory.first_join";
    public static final String SLOTS_LOCKED = "message.impactsinventory.slots_locked";
    public static final String MOVED_TO_POCKETS = "message.impactsinventory.moved_to_pockets";
    public static final String MOVED_TO_PACK = "message.impactsinventory.moved_to_pack";
    public static final String MOVED_TO_CHEST = "message.impactsinventory.moved_to_chest";
    public static final String MOVED_TO_GROUND = "message.impactsinventory.moved_to_ground";

    public static final Map<String, String> ENGLISH = englishText();

    private InventoryTranslations() {
    }

    private static Map<String, String> englishText() {
        Map<String, String> english = new LinkedHashMap<>();
        english.put(POCKETS_FULL, "Pockets full");
        english.put(OPEN_PACK_KEY, "Open Pack");
        english.put(OPEN_PACK_BUTTON, "Open pack (%s)");
        english.put(SATCHEL, "Satchel");
        english.put(BACKPACK, "Backpack");
        english.put(FRAME_PACK, "Frame Pack");
        english.put(FIRST_JOIN, "Impact's Realistic Inventory: you now carry two hands and nine pockets. The other inventory slots are locked.");
        english.put(SLOTS_LOCKED, "Survival mode: you carry two hands and nine pockets again.");
        english.put(MOVED_TO_POCKETS, "Items from the locked slots went into your pockets.");
        english.put(MOVED_TO_PACK, "Items from the locked slots went into your pockets and your worn pack.");
        english.put(MOVED_TO_CHEST, "Items that didn't fit in your pockets or pack went into a chest at %s.");
        english.put(MOVED_TO_GROUND, "Items that didn't fit in your pockets or pack were dropped at your feet.");
        return Collections.unmodifiableMap(english);
    }
}
