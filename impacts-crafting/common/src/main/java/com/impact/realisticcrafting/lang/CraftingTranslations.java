package com.impact.realisticcrafting.lang;

import com.impact.lib.registry.CreativeTabs;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.bed.BedLayering;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.CraftingTool;
import com.impact.realisticcrafting.item.LooseIron;
import com.impact.realisticcrafting.item.LooseItem;
import com.impact.realisticcrafting.item.MakeshiftTableItem;
import com.impact.realisticcrafting.item.ToolKind;
import com.impact.realisticcrafting.item.ToolMaterial;
import com.impact.realisticcrafting.item.WoodPart;
import com.impact.realisticcrafting.menu.CraftingStation;

import net.minecraft.world.item.DyeColor;

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
        english.put(BLOCK_KEY_PREFIX + "dye_vat", "Dye Vat");
        english.put(BLOCK_KEY_PREFIX + "bed_frame", "Bed Frame");
        english.put(ITEM_KEY_PREFIX + "comforter", "Comforter");
        for (DyeColor color : DyeColor.values()) {
            english.put(ITEM_KEY_PREFIX + color.getName() + "_sheet", titleCase(color.getName() + "_sheet"));
        }
        english.put(BedLayering.COMFORTER_FIRST_KEY, "Lay the comforter first.");
        english.put(BLOCK_KEY_PREFIX + MakeshiftTable.WOODEN.path, "Makeshift Table");
        english.put(BLOCK_KEY_PREFIX + MakeshiftTable.STONE.path, "Makeshift Stone Table");
        english.put(MakeshiftTable.WOODEN.containerTitleKey(), "Makeshift Table");
        english.put(MakeshiftTable.STONE.containerTitleKey(), "Makeshift Stone Table");
        english.put(MakeshiftTableItem.USES_LEFT_KEY, "Uses left: %s / %s");
        for (WoodPart part : WoodPart.values()) {
            english.put(ITEM_KEY_PREFIX + part.path, titleCase(part.path));
            english.put(ITEM_KEY_PREFIX + part.path + ".wood", "%s " + titleCase(part.path));
        }
        english.put(ITEM_KEY_PREFIX + "loose_boat", "Loose Boat");
        english.put(ITEM_KEY_PREFIX + "loose_boat.wood", "%s Loose Boat");
        english.put(ITEM_KEY_PREFIX + "wooden_bucket", "Wooden Bucket");
        english.put(ITEM_KEY_PREFIX + "wooden_water_bucket", "Water-filled Wooden Bucket");
        english.put(ITEM_KEY_PREFIX + CraftingPart.TRIMMED_STICK.path, "Trimmed Sticks");
        english.put(ITEM_KEY_PREFIX + CraftingPart.SHAPED_STONE.path, "Shaped Stone");
        english.put(ITEM_KEY_PREFIX + CraftingPart.STONE_HINGE.path, "Stone Hinge");
        english.put(ITEM_KEY_PREFIX + CraftingPart.TWINE.path, "Twine");
        english.put(ITEM_KEY_PREFIX + CraftingPart.STRING_CLUMP.path, "String Clump");
        english.put(ITEM_KEY_PREFIX + CraftingPart.WOOD_SCRAPS.path, "Wood Scraps");
        english.put(ITEM_KEY_PREFIX + CraftingPart.CHEST_TOP.path, "Chest Top");
        english.put(ITEM_KEY_PREFIX + CraftingPart.OAR.path, "Oar");
        english.put(ITEM_KEY_PREFIX + CraftingPart.STONE_CASING.path, "Stone Casing");
        english.put(ITEM_KEY_PREFIX + CraftingPart.STONE_BASE.path, "Stone Base");
        english.put(ITEM_KEY_PREFIX + CraftingPart.MORTAR.path, "Mortar");
        english.put(ITEM_KEY_PREFIX + CraftingPart.LEATHER_STRIP.path, "Leather Strip");
        english.put(ITEM_KEY_PREFIX + CraftingPart.FLINT_ARROWHEAD.path, "Flint Arrowhead");
        english.put(ITEM_KEY_PREFIX + CraftingTool.BONE_NEEDLE.path, "Bone Needle");
        english.put(ITEM_KEY_PREFIX + CraftingTool.FLINT_CHISEL.path, "Flint Chisel");
        english.put(ITEM_KEY_PREFIX + CraftingTool.IRON_CHISEL.path, "Iron Chisel");
        english.put(ITEM_KEY_PREFIX + CraftingTool.IRON_SAW.path, "Iron Saw");
        english.put(ITEM_KEY_PREFIX + CraftingTool.IRON_HAMMER.path, "Iron Hammer");
        english.put(ITEM_KEY_PREFIX + CraftingTool.IRON_TWEEZERS.path, "Iron Tweezers");
        english.put(ITEM_KEY_PREFIX + CraftingPart.IRON_PLATE.path, "Iron Plate");
        english.put(ITEM_KEY_PREFIX + CraftingPart.IRON_ROD.path, "Iron Rod");
        english.put(ITEM_KEY_PREFIX + CraftingPart.IRON_RIVETS.path, "Iron Rivets");
        english.put(ITEM_KEY_PREFIX + CraftingPart.IRON_HINGE.path, "Iron Hinge");
        english.put(ITEM_KEY_PREFIX + CraftingPart.IRON_WHEEL.path, "Iron Wheel");
        english.put(ITEM_KEY_PREFIX + CraftingPart.PAPER_PULP.path, "Paper Pulp");
        for (LooseIron looseIron : LooseIron.values()) {
            english.put(ITEM_KEY_PREFIX + looseIron.path, titleCase(looseIron.path));
        }
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
