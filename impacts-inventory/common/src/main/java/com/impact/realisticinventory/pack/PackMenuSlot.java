package com.impact.realisticinventory.pack;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** A slot of the open pack: packs and shulker boxes (and anything else on the nesting blacklist) can't go in. */
final class PackMenuSlot extends Slot {
    PackMenuSlot(Container packContainer, int packSlot, int x, int y) {
        super(packContainer, packSlot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return PackRules.canGoInPack(stack);
    }
}
