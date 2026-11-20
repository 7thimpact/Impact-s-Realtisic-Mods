package com.impact.realisticcrafting.lang;

import com.impact.lib.registry.CreativeTabs;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.CraftingTool;
import com.impact.realisticcrafting.item.LooseItem;
import com.impact.realisticcrafting.item.MakeshiftTableItem;
import com.impact.realisticcrafting.item.ToolKind;
import com.impact.realisticcrafting.item.ToolMaterial;
import com.impact.realisticcrafting.menu.CraftingStation;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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

    // wooden_pickaxe_head -> Wooden Pickaxe Head
    private static String titleCase(String path) {
        return Arrays.stream(path.split("_")).map(word -> word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    private static Map<String, String> buildEnglish() {
        Map<String, String> english = new LinkedHashMap<>();
        english.put(CREATIVE_TAB, "Impact's Realistic Crafting");
        english.put(ADVANCEMENT_TAB, "Realistic Crafting");
        english.put(ITEM_KEY_PREFIX + "stone_pebble", "Stone Pebble");
        english.put(BLOCK_KEY_PREFIX + "loose_pebble", "Loose Pebbles");
        english.put(BLOCK_KEY_PREFIX + MakeshiftTable.WOODEN.path, "Makeshift Table");
        english.put(BLOCK_KEY_PREFIX + MakeshiftTable.STONE.path, "Makeshift Stone Table");
        english.put(MakeshiftTable.WOODEN.containerTitleKey(), "Makeshift Table");
        english.put(MakeshiftTable.STONE.containerTitleKey(), "Makeshift Stone Table");
        english.put(MakeshiftTableItem.USES_LEFT_KEY, "Uses left: %s / %s");
        english.put(ITEM_KEY_PREFIX + "trimmed_planks", "Trimmed Planks");
        english.put(ITEM_KEY_PREFIX + "trimmed_planks.wood", "%s Trimmed Planks");
        english.put(ITEM_KEY_PREFIX + CraftingPart.TRIMMED_STICK.path, "Trimmed Sticks");
        english.put(ITEM_KEY_PREFIX + CraftingPart.SHAPED_STONE.path, "Shaped Stone");
        english.put(ITEM_KEY_PREFIX + CraftingPart.STONE_HINGE.path, "Stone Hinge");
        english.put(ITEM_KEY_PREFIX + CraftingPart.TWINE.path, "Twine");
        english.put(ITEM_KEY_PREFIX + CraftingPart.STRING_CLUMP.path, "String Clump");
        english.put(ITEM_KEY_PREFIX + CraftingPart.WOOD_SCRAPS.path, "Wood Scraps");
        english.put(ITEM_KEY_PREFIX + CraftingTool.BONE_NEEDLE.path, "Bone Needle");
        english.put(ITEM_KEY_PREFIX + CraftingTool.FLINT_CHISEL.path, "Flint Chisel");
        english.put(LooseItem.HAMMER_HINT_KEY, "Hammer with a pebble in your off hand.");
        for (ToolMaterial material : ToolMaterial.values()) {
            for (ToolKind kind : ToolKind.values()) {
                english.put(ITEM_KEY_PREFIX + kind.partPath(material), titleCase(kind.partPath(material)));
                english.put(ITEM_KEY_PREFIX + kind.loosePath(material), titleCase(kind.loosePath(material)));
            }
        }
        english.put(CraftingStation.INVENTORY_GRID.needsTranslationKey(), "Needs your inventory grid");
        english.put(CraftingStation.MAKESHIFT_TABLE.needsTranslationKey(), "Needs a Makeshift Table");
        english.put(CraftingStation.MAKESHIFT_STONE_TABLE.needsTranslationKey(), "Needs a Makeshift Stone Table");
        english.put(CraftingStation.CRAFTING_TABLE.needsTranslationKey(), "Needs a Crafting Table");
        return Collections.unmodifiableMap(english);
    }
}
