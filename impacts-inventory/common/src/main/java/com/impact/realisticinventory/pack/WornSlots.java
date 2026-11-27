package com.impact.realisticinventory.pack;

import com.impact.lib.backslot.BackSlot;
import com.impact.realisticinventory.config.InventoryCommonConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * The worn slots: one item on the player's back and one pack on the chest. They are Curios's {@code back} and
 * {@code body} slots (through the loader module's {@link WornSlotBackend}). The back slot is also Lib's shared back
 * slot, so other Impact mods reach it through Lib's {@code BackSlot}: it holds Inventory's packs and anything in
 * {@code #impactslib:back_slot_items}. The chest holds Inventory's packs only.
 */
public final class WornSlots {
    private static volatile WornSlotBackend backend;

    private WornSlots() {
    }

    /** Called once by the loader module, during mod construction. */
    public static void useBackend(WornSlotBackend newBackend) {
        backend = newBackend;
    }

    public static boolean hasSlot(Player player, WornOn place) {
        WornSlotBackend currentBackend = backend;
        return currentBackend != null && currentBackend.hasSlot(player, place);
    }

    public static ItemStack get(Player player, WornOn place) {
        WornSlotBackend currentBackend = backend;
        return currentBackend == null ? ItemStack.EMPTY : currentBackend.get(player, place);
    }

    /** Puts the stack in the worn slot. Returns false, changing nothing, when the player has no such slot. */
    public static boolean set(Player player, WornOn place, ItemStack stack) {
        WornSlotBackend currentBackend = backend;
        return currentBackend != null && currentBackend.set(player, place, stack);
    }

    /**
     * Each slot can be switched off in the common config ({@code packs.backSlot}, {@code packs.chestSlot}); nothing can
     * be worn there then.
     */
    public static boolean isEnabled(WornOn place) {
        InventoryCommonConfig.FeatureSettings features = InventoryCommonConfig.get().features;
        return place == WornOn.BACK ? features.isBackSlotEnabled.get() : features.isChestSlotEnabled.get();
    }

    /** Whether the slot is on and this player has it, so something can be worn there right now. */
    public static boolean isUsable(Player player, WornOn place) {
        return isEnabled(place) && hasSlot(player, place);
    }

    /** Whether the item fits the slot: Inventory's packs, and on the back another mod's back-slot item. */
    public static boolean accepts(WornOn place, ItemStack stack) {
        boolean isOtherModsBackItem = place == WornOn.BACK && stack.is(BackSlot.BACK_SLOT_ITEMS);
        return isEnabled(place) && (stack.is(InventoryTags.PACKS) || isOtherModsBackItem);
    }
}
