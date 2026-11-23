package com.impact.realisticinventory.lang;

import com.impact.lib.advancement.ImpactsAdvancementBuilder;
import com.impact.lib.registry.CreativeTabs;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.advancement.InventoryAdvancements;
import com.impact.realisticinventory.pack.PackKind;
import net.minecraft.resources.ResourceLocation;

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
    public static final String PLACED_PACK = "block.impactsinventory.placed_pack";
    public static final String SLOTS_USED_TOOLTIP = "tooltip.impactsinventory.slots_used";
    public static final String STACK_TOOLTIP = "tooltip.impactsinventory.stack";
    public static final String MORE_STACKS_TOOLTIP = "tooltip.impactsinventory.more_stacks";
    public static final String FRAME_PACK_SLOWNESS_TOOLTIP = "tooltip.impactsinventory.frame_pack_slowness";
    public static final String FIREPROOF_TOOLTIP = "tooltip.impactsinventory.fireproof";
    public static final String ENDER_PACK_TOOLTIP = "tooltip.impactsinventory.ender_pack";
    public static final String PACK_HINT_TOOLTIP = "tooltip.impactsinventory.pack_hint";
    /** Minecraft names a tab after its root; this key is for tools that list tabs by mod. */
    public static final String ADVANCEMENT_TAB = "advancements.impactsinventory.tab";
    public static final String CREATIVE_TAB = CreativeTabs.titleKey(InventoryMod.MOD_ID);
    public static final String FIRST_JOIN = "message.impactsinventory.first_join";
    public static final String SLOTS_LOCKED = "message.impactsinventory.slots_locked";
    public static final String MOVED_TO_POCKETS = "message.impactsinventory.moved_to_pockets";
    public static final String MOVED_TO_PACK = "message.impactsinventory.moved_to_pack";
    public static final String MOVED_TO_CHEST = "message.impactsinventory.moved_to_chest";
    public static final String MOVED_TO_GROUND = "message.impactsinventory.moved_to_ground";

    public static final Map<String, String> ENGLISH = englishText();

    private InventoryTranslations() {
    }

    public static String packName(PackKind kind) {
        return "item." + InventoryMod.MOD_ID + "." + kind.itemPath();
    }

    private static Map<String, String> englishText() {
        Map<String, String> english = new LinkedHashMap<>();
        english.put(POCKETS_FULL, "Pockets full");
        english.put(OPEN_PACK_KEY, "Open Pack");
        english.put(OPEN_PACK_BUTTON, "Open pack (%s)");
        english.put(CREATIVE_TAB, "Impact's Realistic Inventory");
        english.put(SATCHEL, "Satchel");
        english.put(BACKPACK, "Backpack");
        english.put(FRAME_PACK, "Frame Pack");
        english.put(packName(PackKind.EXPLORERS_PACK), "Explorer's Pack");
        english.put(packName(PackKind.EXPEDITION_PACK), "Expedition Pack");
        english.put(packName(PackKind.NETHERITE_PACK), "Netherite Pack");
        english.put(packName(PackKind.ENDER_PACK), "Ender Pack");
        english.put(PLACED_PACK, "Placed Pack");
        english.put(SLOTS_USED_TOOLTIP, "%s/%s slots used");
        english.put(STACK_TOOLTIP, "%s x%s");
        english.put(MORE_STACKS_TOOLTIP, "and %s more...");
        english.put(FRAME_PACK_SLOWNESS_TOOLTIP, "Walking %s%% slower while worn");
        english.put(FIREPROOF_TOOLTIP, "Survives fire and lava");
        english.put(ENDER_PACK_TOOLTIP, "Opens your Ender Chest");
        english.put(PACK_HINT_TOOLTIP, "Right-click to wear, sneak-right-click a block to set down");
        english.put(FIRST_JOIN, "Impact's Realistic Inventory: you now carry two hands and nine pockets. The other inventory slots are locked.");
        english.put(SLOTS_LOCKED, "Survival mode: you carry two hands and nine pockets again.");
        english.put(MOVED_TO_POCKETS, "Items from the locked slots went into your pockets.");
        english.put(MOVED_TO_PACK, "Items from the locked slots went into your pockets and your worn pack.");
        english.put(MOVED_TO_CHEST, "Items that didn't fit in your pockets or pack went into a chest at %s.");
        english.put(MOVED_TO_GROUND, "Items that didn't fit in your pockets or pack were dropped at your feet.");
        english.put(ADVANCEMENT_TAB, "Realistic Inventory");
        addAdvancement(english, InventoryAdvancements.HANDS_FULL, "Hands Full", "Switch between your two hotbar slots");
        addAdvancement(english, InventoryAdvancements.QUICK_DRAW, "Quick Draw", "Hit a mob within a second of switching slots");
        addAdvancement(english, InventoryAdvancements.SWORD_AND_BOARD, "Sword and Board", "Block an attack with a shield in your off hand while holding a sword");
        addAdvancement(english, InventoryAdvancements.JUGGLER, "Juggler", "Switch slots 10 times in 5 seconds");
        addAdvancement(english, InventoryAdvancements.SECOND_NATURE, "Second Nature", "Switch slots 1,000 times");
        addAdvancement(english, InventoryAdvancements.POCKET_SPACE, "Pocket Space", "Fill all 9 pockets");
        addAdvancement(english, InventoryAdvancements.NO_ROOM_AT_ALL, "No Room at All", "Walk over an item with every slot full");
        addAdvancement(english, InventoryAdvancements.PACKING_LIGHT, "Packing Light", "Wear a Satchel");
        addAdvancement(english, InventoryAdvancements.PACK_MULE, "Pack Mule", "Wear a Backpack");
        addAdvancement(english, InventoryAdvancements.HEAVY_LIFTING, "Heavy Lifting", "Wear a Frame Pack");
        addAdvancement(english, InventoryAdvancements.LIGHT_ON_YOUR_FEET, "Light on Your Feet", "Wear an Explorer's Pack");
        addAdvancement(english, InventoryAdvancements.INTO_THE_WILD, "Into the Wild", "Wear an Expedition Pack");
        addAdvancement(english, InventoryAdvancements.FIREPROOF_LUGGAGE, "Fireproof Luggage", "Wear a Netherite Pack");
        addAdvancement(english, InventoryAdvancements.POCKET_DIMENSION, "Pocket Dimension", "Wear an Ender Pack");
        addAdvancement(english, InventoryAdvancements.HOME_AWAY_FROM_HOME, "Home Away From Home", "Set a pack down as a block");
        addAdvancement(english, InventoryAdvancements.TRAVEL_LIGHT, "Travel Light", "Enter the Nether with empty pockets and no pack");
        return Collections.unmodifiableMap(english);
    }

    private static void addAdvancement(Map<String, String> english, ResourceLocation advancementId, String title, String description) {
        english.put(ImpactsAdvancementBuilder.titleKey(advancementId), title);
        english.put(ImpactsAdvancementBuilder.descriptionKey(advancementId), description);
    }
}
