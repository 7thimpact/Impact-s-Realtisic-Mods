package com.impact.realisticinventory.pack;

import com.impact.lib.backslot.BackSlotProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Inventory's back slot, offered to every Impact mod through Lib's {@code BackSlot}. */
public final class InventoryBackSlotProvider implements BackSlotProvider {
    @Override
    public ItemStack get(Player player) {
        return BackSlotStorage.get(player);
    }

    @Override
    public void set(Player player, ItemStack stack) {
        BackSlotStorage.set(player, stack);
    }

    @Override
    public boolean acceptsOwnItem(ItemStack stack) {
        return stack.is(InventoryTags.PACKS);
    }

    @Override
    public boolean isEnabled() {
        return BackSlotStorage.isEnabled();
    }
}
