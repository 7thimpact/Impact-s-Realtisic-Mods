package com.impact.realisticcrafting.lang;

import com.impact.lib.registry.CreativeTabs;
import com.impact.realisticcrafting.CraftingMod;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Crafting's translation keys and their English text. Data generation writes {@link #ENGLISH} to en_us.json. */
public final class CraftingTranslations {
    public static final String CREATIVE_TAB = CreativeTabs.titleKey(CraftingMod.MOD_ID);
    /** Minecraft names a tab after its root; this key is for tools that list tabs by mod. */
    public static final String ADVANCEMENT_TAB = "advancements." + CraftingMod.MOD_ID + ".tab";
    private static final String ITEM_KEY_PREFIX = "item." + CraftingMod.MOD_ID + ".";
    private static final String BLOCK_KEY_PREFIX = "block." + CraftingMod.MOD_ID + ".";

    public static final Map<String, String> ENGLISH = buildEnglish();

    private CraftingTranslations() {
    }

    private static Map<String, String> buildEnglish() {
        Map<String, String> english = new LinkedHashMap<>();
        english.put(CREATIVE_TAB, "Impact's Realistic Crafting");
        english.put(ADVANCEMENT_TAB, "Realistic Crafting");
        english.put(ITEM_KEY_PREFIX + "stone_pebble", "Stone Pebble");
        english.put(BLOCK_KEY_PREFIX + "loose_pebble", "Loose Pebbles");
        return Collections.unmodifiableMap(english);
    }
}
