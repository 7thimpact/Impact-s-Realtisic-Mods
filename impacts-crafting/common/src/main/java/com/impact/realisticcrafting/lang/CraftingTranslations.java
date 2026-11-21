package com.impact.realisticcrafting.lang;

import com.impact.lib.advancement.ImpactsAdvancementBuilder;
import com.impact.lib.registry.CreativeTabs;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.advancement.CraftingAdvancements;
import com.impact.realisticcrafting.bed.BedLayering;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.CraftingTool;
import com.impact.realisticcrafting.item.LooseIron;
import com.impact.realisticcrafting.item.LooseItem;
import com.impact.realisticcrafting.item.MakeshiftTableItem;
import com.impact.realisticcrafting.item.PebbleKind;
import com.impact.realisticcrafting.item.ToolKind;
import com.impact.realisticcrafting.item.ToolMaterial;
import com.impact.realisticcrafting.item.WoodPart;
import com.impact.realisticcrafting.menu.CraftingStation;
import com.impact.realisticcrafting.registry.CraftingSounds;

import net.minecraft.resources.ResourceLocation;
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
    public static final String JEI_HAMMERING = jeiKey("hammering");
    public static final String JEI_DYE_VAT = jeiKey("dye_vat");
    public static final String JEI_HOLD_TIME = jeiKey("hold_time");
    public static final String JEI_HOLD_TIME_RANGE = jeiKey("hold_time_range");
    public static final String JEI_VAT_COLOR = jeiKey("vat_color");
    public static final String JEI_VAT_COLOR_CHARGES = jeiKey("vat_color_charges");
    public static final String JEI_PLAIN_WATER = jeiKey("plain_water");
    public static final String JEI_SECONDS = jeiKey("seconds");
    public static final String JEI_CRAFT_INFO = jeiKey("craft_info");
    public static final String JEI_PEBBLES_INFO = jeiKey("info.pebbles");
    public static final String JEI_BEDS_INFO = jeiKey("info.beds");
    public static final String JEI_DYE_VAT_INFO = jeiKey("info.dye_vat");
    public static final String JEI_LOOSE_ITEMS_INFO = jeiKey("info.loose_items");
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
        for (PebbleKind kind : PebbleKind.values()) {
            english.put(ITEM_KEY_PREFIX + kind.itemPath, titleCase(kind.itemPath));
            english.put(BLOCK_KEY_PREFIX + kind.blockPath, kind == PebbleKind.STONE ? "Loose Pebbles" : "Loose " + titleCase(kind.itemPath) + "s");
        }
        english.put(BLOCK_KEY_PREFIX + "dye_vat", "Dye Vat");
        english.put(BLOCK_KEY_PREFIX + "bed_frame", "Bed Frame");
        english.put(ITEM_KEY_PREFIX + "comforter", "Comforter");
        for (DyeColor color : DyeColor.values()) {
            english.put(ITEM_KEY_PREFIX + color.getName() + "_sheet", titleCase(color.getName() + "_sheet"));
            english.put(ITEM_KEY_PREFIX + color.getName() + "_banner_cloth", titleCase(color.getName() + "_banner_cloth"));
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
        english.put(ITEM_KEY_PREFIX + CraftingPart.REDSTONE_MECHANISM.path, "Redstone Mechanism");
        english.put(ITEM_KEY_PREFIX + CraftingPart.PISTON_HEAD.path, "Piston Head");
        english.put(ITEM_KEY_PREFIX + CraftingPart.BOW_STAVE.path, "Bow Stave");
        english.put(ITEM_KEY_PREFIX + CraftingPart.LEATHER_PANEL.path, "Leather Panel");
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
        addAdvancements(english);
        addJei(english);
        addSubtitles(english);
        addHints(english);
        return Collections.unmodifiableMap(english);
    }

    private static void addHints(Map<String, String> english) {
        english.put(CraftingHints.MADE_FROM_KEY, "Made from %s");
        english.put(CraftingHints.NEXT_KEY, "Next: %s");
        CraftingHints.ENGLISH.forEach((hintName, hint) -> {
            english.put(CraftingHints.madeFromKey(hintName), hint.madeFrom());
            english.put(CraftingHints.nextKey(hintName), hint.next());
        });
    }

    private static void addJei(Map<String, String> english) {
        english.put(JEI_HAMMERING, "Hammering");
        english.put(JEI_DYE_VAT, "Dye Vat");
        english.put(JEI_HOLD_TIME, "Hold for %s");
        english.put(JEI_HOLD_TIME_RANGE, "Hold for %s to %s");
        english.put(JEI_VAT_COLOR, "%s water");
        english.put(JEI_VAT_COLOR_CHARGES, "%s water, %s charges");
        english.put(JEI_PLAIN_WATER, "Plain water");
        english.put(JEI_SECONDS, "%ss");
        english.put(JEI_CRAFT_INFO, "%s, %s");
        english.put(CraftingStation.INVENTORY_GRID.nameTranslationKey(), "2×2 grid or any table");
        english.put(CraftingStation.MAKESHIFT_TABLE.nameTranslationKey(), "Makeshift Table");
        english.put(CraftingStation.MAKESHIFT_STONE_TABLE.nameTranslationKey(), "Makeshift Stone Table");
        english.put(CraftingStation.CRAFTING_TABLE.nameTranslationKey(), "Crafting Table");
        english.put(JEI_PEBBLES_INFO, "Pebbles are the first tool. Pick up the loose pebbles lying on grass, sand, stone and cave floors: "
                + "their kind follows the ground (sandstone on sand, granite on granite). Breaking gravel drops one sometimes, and breaking stone "
                + "by hand always does. Every kind works the same. A fresh pebble stacks to 64; once used, it stacks to 1.");
        english.put(JEI_BEDS_INFO, "Beds are built where they stand. Place a Bed Frame, right-click it with a Comforter, then with a Sheet: "
                + "the sheet's color is the bed's. A sheet on a finished bed recolors it, and a broken bed drops all three parts.");
        english.put(JEI_DYE_VAT_INFO, "Fill the vat with water from a bucket, or let rain fill it. One dye colors 8 dips, and a second dye mixes "
                + "with the first when the colors mix (red and yellow make orange). Hold right-click with an item to dip it. Leather armor and packs "
                + "take the vat's color; plain water washes dye off and soaks sugar cane into Paper Pulp.");
        english.put(JEI_LOOSE_ITEMS_INFO, "Loose items must be hammered before use: hold the loose item in your main hand and a hammer (a Stone "
                + "Pebble or the Iron Hammer) in your off hand, then hold right-click. Letting go early starts over and costs nothing.");
    }

    private static void addSubtitles(Map<String, String> english) {
        english.put(subtitle(CraftingSounds.HAMMER_FINISH), "Loose item hammered");
        english.put(subtitle(CraftingSounds.CRAFT_FINISH), "Craft finished");
        english.put(subtitle(CraftingSounds.TABLE_CREAK), "Makeshift table creaks");
        english.put(subtitle(CraftingSounds.VAT_FILL), "Dye Vat fills");
        english.put(subtitle(CraftingSounds.VAT_DYE), "Dye stirs in");
        english.put(subtitle(CraftingSounds.VAT_DIP), "Item dipped");
    }

    /** The subtitle key of one of Crafting's sounds. */
    public static String subtitle(String soundPath) {
        return "subtitles." + CraftingMod.MOD_ID + "." + soundPath;
    }

    private static String jeiKey(String path) {
        return "jei." + CraftingMod.MOD_ID + "." + path;
    }

    private static void addAdvancements(Map<String, String> english) {
        addAdvancement(english, CraftingAdvancements.HUMBLE_BEGINNINGS, "Humble Beginnings", "Pick up a Stone Pebble");
        addAdvancement(english, CraftingAdvancements.ROCK_SOLID, "Rock Solid", "Craft 9 pebbles into cobblestone");
        addAdvancement(english, CraftingAdvancements.CUT_TO_SIZE, "Cut to Size", "Trim planks into Trimmed Planks");
        addAdvancement(english, CraftingAdvancements.SCRAP_COLLECTOR, "Scrap Collector", "Collect 64 Wood Scraps");
        addAdvancement(english, CraftingAdvancements.MAKESHIFT_MINDSET, "Makeshift Mindset", "Place a Makeshift Table");
        addAdvancement(english, CraftingAdvancements.PATIENCE_IS_A_VIRTUE, "Patience Is a Virtue", "Wait out a full 5-second craft");
        addAdvancement(english, CraftingAdvancements.FALLING_APART, "Falling Apart", "Use a makeshift table until it breaks");
        addAdvancement(english, CraftingAdvancements.LOOSE_ENDS, "Loose Ends", "Craft a loose tool");
        addAdvancement(english, CraftingAdvancements.HAMMER_TIME, "Hammer Time", "Hammer a loose tool into a finished tool");
        addAdvancement(english, CraftingAdvancements.STONE_AGE, "Stone Age", "Finish a stone tool");
        addAdvancement(english, CraftingAdvancements.HINGED, "Hinged", "Craft a Stone Hinge");
        addAdvancement(english, CraftingAdvancements.BUCKET_LIST, "Bucket List", "Craft a Wooden Bucket");
        addAdvancement(english, CraftingAdvancements.STORAGE_SOLUTION, "Storage Solution", "Craft a chest from a Wooden Bucket, a Chest Top and Stone Hinges");
        addAdvancement(english, CraftingAdvancements.SET_IN_STONE, "Set in Stone", "Place a Makeshift Stone Table");
        addAdvancement(english, CraftingAdvancements.TOOLS_OF_THE_TRADE, "Tools of the Trade", "Own an Iron Saw, Iron Hammer and Iron Tweezers at once");
        addAdvancement(english, CraftingAdvancements.THE_REAL_DEAL, "The Real Deal", "Craft a Crafting Table");
        addAdvancement(english, CraftingAdvancements.GRIND_SET, "Grind Set", "Grind your first dye");
        addAdvancement(english, CraftingAdvancements.COLOR_THEORY, "Color Theory", "Mix two dyes in a grinder");
        addAdvancement(english, CraftingAdvancements.DIP_IT, "Dip It", "Dye an item in a Dye Vat");
        addAdvancement(english, CraftingAdvancements.SEW_WHAT, "Sew What?", "Craft something with a Bone Needle");
        addAdvancement(english, CraftingAdvancements.MAKING_THE_BED, "Making the Bed", "Build a bed from frame, comforter and sheet");
        addAdvancement(english, CraftingAdvancements.FULL_SPECTRUM, "Full Spectrum", "Make a bed in all 16 sheet colors");
        addAdvancement(english, CraftingAdvancements.MECHANICALLY_INCLINED, "Mechanically Inclined", "Craft a Redstone Mechanism");
        addAdvancement(english, CraftingAdvancements.PUSHING_IT, "Pushing It", "Hammer a loose piston into a piston");
        addAdvancement(english, CraftingAdvancements.EVERY_BRANCH, "Every Branch", "Craft every shared part in the parts library");
        addAdvancement(english, CraftingAdvancements.MASTER_CRAFTSMAN, "Master Craftsman", "Craft 1,000 items with Realistic Crafting recipes");
    }

    private static void addAdvancement(Map<String, String> english, ResourceLocation advancementId, String title, String description) {
        english.put(ImpactsAdvancementBuilder.titleKey(advancementId), title);
        english.put(ImpactsAdvancementBuilder.descriptionKey(advancementId), description);
    }
}
