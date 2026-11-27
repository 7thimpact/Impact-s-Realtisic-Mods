package com.impact.realisticinventory.pack;

import com.impact.lib.backslot.BackSlotProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Inventory's back slot, offered to every Impact mod through Lib's {@code BackSlot}. */
public final class InventoryBackSlotProvider implements BackSlotProvider {
    @Override
    public ItemStack get(Player player) {
        return WornSlots.get(player, WornOn.BACK);
    }

    @Override
    public boolean set(Player player, ItemStack stack) {
        return WornSlots.set(player, WornOn.BACK, stack);
    }

    @Override
    public boolean acceptsOwnItem(ItemStack stack) {
        return stack.is(InventoryTags.PACKS);
    }

    @Override
    public boolean isEnabled() {
        return WornSlots.isEnabled(WornOn.BACK);
    }
}
