package com.impact.realisticinventory.advancement;

import com.impact.lib.ResourceIds;
import com.impact.lib.advancement.EventTrigger;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticinventory.InventoryMod;

import java.util.List;

/**
 * Inventory's advancement triggers. The spec's four ({@code slot_switched}, {@code pickup_refused}, {@code pack_worn},
 * {@code pack_placed}) plus three for achievements no vanilla trigger can express: {@code quick_draw},
 * {@code pockets_filled} and {@code travel_light}.
 */
public final class InventoryTriggers {
    public static final SlotSwitchedTrigger SLOT_SWITCHED = new SlotSwitchedTrigger();
    public static final PackWornTrigger PACK_WORN = new PackWornTrigger();
    public static final EventTrigger PICKUP_REFUSED = new EventTrigger(ResourceIds.of(InventoryMod.MOD_ID, "pickup_refused"));
    public static final EventTrigger PACK_PLACED = new EventTrigger(ResourceIds.of(InventoryMod.MOD_ID, "pack_placed"));
    /** Hit a mob within a second of switching slots. */
    public static final EventTrigger QUICK_DRAW = new EventTrigger(ResourceIds.of(InventoryMod.MOD_ID, "quick_draw"));
    /** All 9 pockets hold something. */
    public static final EventTrigger POCKETS_FILLED = new EventTrigger(ResourceIds.of(InventoryMod.MOD_ID, "pockets_filled"));
    /** Entered the Nether with empty pockets and no pack. */
    public static final EventTrigger TRAVEL_LIGHT = new EventTrigger(ResourceIds.of(InventoryMod.MOD_ID, "travel_light"));
    /** Lib counter behind "Second Nature". */
    public static final String SLOT_SWITCHES_COUNTER = InventoryMod.MOD_ID + ":slot_switches";

    private InventoryTriggers() {
    }

    /** Registers every trigger with vanilla. Runs once, on the main thread, before any world loads. */
    public static void registerAll() {
        List.of(SLOT_SWITCHED, PACK_WORN, PICKUP_REFUSED, PACK_PLACED, QUICK_DRAW, POCKETS_FILLED, TRAVEL_LIGHT)
                .forEach(trigger -> LoaderPlatform.current().registerCriterionTrigger(trigger));
    }
}
