package com.impact.realisticcrafting.advancement;

import com.impact.lib.ResourceIds;
import com.impact.lib.advancement.EventTrigger;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticcrafting.CraftingMod;

import java.util.List;

/**
 * Crafting's advancement triggers: the spec's five ({@code table_broken}, {@code long_craft}, {@code dyes_mixed},
 * {@code vat_dyed}, {@code bed_assembled}) plus ones for what no other trigger can tell, and the Lib counters behind
 * the counting achievements.
 */
public final class CraftingTriggers {
    /** A makeshift table fell apart from wear while the player crafted on it ("Falling Apart"). */
    public static final EventTrigger TABLE_BROKEN = event("table_broken");
    /** A craft of {@code achievements.longCraftTicks} or more finished ("Patience Is a Virtue"). */
    public static final EventTrigger LONG_CRAFT = event("long_craft");
    /** Two dyes ground together into a third ("Color Theory"). */
    public static final EventTrigger DYES_MIXED = event("dyes_mixed");
    /** An item dipped in a dyed (not plain-water) Dye Vat ("Dip It"). */
    public static final EventTrigger VAT_DYED = event("vat_dyed");
    public static final BedAssembledTrigger BED_ASSEMBLED = new BedAssembledTrigger();
    /** A grinder made a dye ("Grind Set"). */
    public static final EventTrigger DYE_GROUND = event("dye_ground");
    /** {@code achievements.scrapCollectorScraps} Wood Scraps collected ("Scrap Collector"). */
    public static final EventTrigger SCRAPS_COLLECTED = event("scraps_collected");
    /** {@code achievements.masterCraftsmanCrafts} items crafted with Crafting's recipes ("Master Craftsman"). */
    public static final EventTrigger MASTER_CRAFTSMAN = event("master_craftsman");
    /** Lib counter of Wood Scraps given as byproducts. */
    public static final String WOOD_SCRAPS_COUNTER = CraftingMod.MOD_ID + ":wood_scraps_collected";
    /** Lib counter of items crafted with Crafting's recipes, per result item. */
    public static final String ITEMS_CRAFTED_COUNTER = CraftingMod.MOD_ID + ":items_crafted";

    private CraftingTriggers() {
    }

    /** Registers every trigger with vanilla. Runs once, on the main thread, before any world loads. */
    public static void registerAll() {
        List.of(TABLE_BROKEN, LONG_CRAFT, DYES_MIXED, VAT_DYED, BED_ASSEMBLED, DYE_GROUND, SCRAPS_COLLECTED, MASTER_CRAFTSMAN)
                .forEach(trigger -> LoaderPlatform.current().registerCriterionTrigger(trigger));
    }

    private static EventTrigger event(String path) {
        return new EventTrigger(ResourceIds.of(CraftingMod.MOD_ID, path));
    }
}
