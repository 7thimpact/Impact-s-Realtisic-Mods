package com.impact.realisticinventory.pack;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** The player's back slot seen as a one-slot container, so it can sit in a menu like any other slot. */
public final class BackSlotContainer implements Container {
    private static final int ONLY_INDEX = 0;
    private static final int SIZE = 1;
    private static final int MAXIMUM_STACK_SIZE = 1;

    private final Player wearer;

    public BackSlotContainer(Player wearer) {
        this.wearer = wearer;
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        return BackSlotStorage.get(wearer).isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        return index == ONLY_INDEX ? BackSlotStorage.get(wearer) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack worn = getItem(index);
        if (worn.isEmpty() || count <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = worn.split(count);
        if (worn.isEmpty()) {
            BackSlotStorage.set(wearer, ItemStack.EMPTY);
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack worn = getItem(index);
        BackSlotStorage.set(wearer, ItemStack.EMPTY);
        return worn;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index == ONLY_INDEX) {
            BackSlotStorage.set(wearer, stack);
        }
    }

    @Override
    public int getMaxStackSize() {
        return MAXIMUM_STACK_SIZE;
    }

    // Changes reach clients through the menu's own sync and the per-tick back-slot sync.
    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        BackSlotStorage.set(wearer, ItemStack.EMPTY);
    }
}
