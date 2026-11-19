package com.impact.realisticinventory.pack;

import com.impact.lib.attachment.PlayerAttachmentData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** What the player wears in the back slot: a pack, or another mod's back-slot item such as an Air Tank. */
public final class BackSlotContents implements PlayerAttachmentData {
    private static final String ITEM_KEY = "item";

    private ItemStack stack = ItemStack.EMPTY;
    // Not saved: what the server last sent to clients, so changes (even in-place ones) are noticed each tick.
    private ItemStack lastSyncedStack = ItemStack.EMPTY;

    public ItemStack stack() {
        return stack;
    }

    public void setStack(ItemStack newStack) {
        stack = newStack;
    }

    /** What clients were last told the player wears. */
    public ItemStack lastSyncedStack() {
        return lastSyncedStack;
    }

    /** Whether the worn item changed since clients were last told. */
    public boolean hasChangedSinceSync() {
        return !ItemStack.matches(stack, lastSyncedStack);
    }

    public void markSynced() {
        lastSyncedStack = stack.copy();
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (!stack.isEmpty()) {
            tag.put(ITEM_KEY, stack.save(new CompoundTag()));
        }
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        stack = tag.contains(ITEM_KEY) ? ItemStack.of(tag.getCompound(ITEM_KEY)) : ItemStack.EMPTY;
    }
}
