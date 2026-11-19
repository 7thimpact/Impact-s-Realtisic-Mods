package com.impact.realisticfood.lang;

import com.impact.lib.registry.CreativeTabs;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.food.FoodPiece;
import com.impact.realisticfood.item.KnifeMaterial;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Food's translation keys and their English text. Data generation writes {@link #ENGLISH} to en_us.json. */
public final class FoodTranslations {
    public static final String CREATIVE_TAB = CreativeTabs.titleKey(FoodMod.MOD_ID);
    private static final String ITEM_KEY_PREFIX = "item." + FoodMod.MOD_ID + ".";

    private static final Map<KnifeMaterial, String> KNIFE_NAMES = new EnumMap<>(Map.of(
            KnifeMaterial.FLINT, "Flint Knife",
            KnifeMaterial.IRON, "Iron Knife"));
    private static final Map<FoodPiece, String> PIECE_NAMES = new EnumMap<>(Map.ofEntries(
            Map.entry(FoodPiece.RAW_BEEF_CUT, "Raw Beef Cut"),
            Map.entry(FoodPiece.RAW_PORK_CUT, "Raw Pork Cut"),
            Map.entry(FoodPiece.RAW_MUTTON_CUT, "Raw Mutton Cut"),
            Map.entry(FoodPiece.RAW_CHICKEN_PIECE, "Raw Chicken Piece"),
            Map.entry(FoodPiece.RAW_RABBIT_PIECE, "Raw Rabbit Piece"),
            Map.entry(FoodPiece.RAW_COD_FILLET, "Raw Cod Fillet"),
            Map.entry(FoodPiece.RAW_SALMON_FILLET, "Raw Salmon Fillet"),
            Map.entry(FoodPiece.COOKED_BEEF_CUT, "Cooked Beef Cut"),
            Map.entry(FoodPiece.COOKED_PORK_CUT, "Cooked Pork Cut"),
            Map.entry(FoodPiece.COOKED_MUTTON_CUT, "Cooked Mutton Cut"),
            Map.entry(FoodPiece.COOKED_CHICKEN_PIECE, "Cooked Chicken Piece"),
            Map.entry(FoodPiece.COOKED_RABBIT_PIECE, "Cooked Rabbit Piece"),
            Map.entry(FoodPiece.COOKED_COD_FILLET, "Cooked Cod Fillet"),
            Map.entry(FoodPiece.COOKED_SALMON_FILLET, "Cooked Salmon Fillet"),
            Map.entry(FoodPiece.POTATO_SLICES, "Potato Slices"),
            Map.entry(FoodPiece.FRIED_POTATO_SLICES, "Fried Potato Slices"),
            Map.entry(FoodPiece.CARROT_SLICES, "Carrot Slices")));

    public static final Map<String, String> ENGLISH = Collections.unmodifiableMap(english());

    private FoodTranslations() {
    }

    public static String itemKey(String itemPath) {
        return ITEM_KEY_PREFIX + itemPath;
    }

    private static Map<String, String> english() {
        Map<String, String> translations = new LinkedHashMap<>();
        translations.put(CREATIVE_TAB, "Impact's Realistic Food");
        KNIFE_NAMES.forEach((material, name) -> translations.put(itemKey(material.itemPath), name));
        PIECE_NAMES.forEach((piece, name) -> translations.put(itemKey(piece.path), name));
        return translations;
    }
}
