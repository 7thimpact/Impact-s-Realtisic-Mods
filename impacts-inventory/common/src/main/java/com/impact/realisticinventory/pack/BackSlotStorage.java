package com.impact.realisticinventory.pack;

import com.impact.lib.backslot.BackSlot;
import com.impact.realisticinventory.config.InventoryCommonConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * The back slot: one item worn on the player's back. It is Curios's {@code back} slot (through the loader module's
 * {@link BackSlotBackend}); Inventory is Lib's back-slot provider, so other Impact mods reach it through Lib's
 * {@code BackSlot}. It holds Inventory's packs and anything in {@code #impactslib:back_slot_items}.
 */
public final class BackSlotStorage {
    private static volatile BackSlotBackend backend;

    private BackSlotStorage() {
    }

    /** Called once by the loader module, during mod construction. */
    public static void useBackend(BackSlotBackend newBackend) {
        backend = newBackend;
    }

    public static boolean hasBackSlot(Player player) {
        BackSlotBackend currentBackend = backend;
        return currentBackend != null && currentBackend.hasBackSlot(player);
    }

    public static ItemStack get(Player player) {
        BackSlotBackend currentBackend = backend;
        return currentBackend == null ? ItemStack.EMPTY : currentBackend.get(player);
    }

    /** Puts the stack on the player's back. Returns false, changing nothing, when the player has no back slot. */
    public static boolean set(Player player, ItemStack stack) {
        BackSlotBackend currentBackend = backend;
        return currentBackend != null && currentBackend.set(player, stack);
    }

    /** The back slot can be switched off in the common config ({@code packs.backSlot}); packs then can't be worn. */
    public static boolean isEnabled() {
        return InventoryCommonConfig.get().features.isBackSlotEnabled.get();
    }

    /** Whether the item fits the back slot: one of Inventory's packs, or another mod's back-slot item. */
    public static boolean accepts(ItemStack stack) {
        return isEnabled() && (stack.is(InventoryTags.PACKS) || stack.is(BackSlot.BACK_SLOT_ITEMS));
    }
}
