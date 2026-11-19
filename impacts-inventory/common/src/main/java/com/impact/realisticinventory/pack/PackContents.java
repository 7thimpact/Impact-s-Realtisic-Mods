package com.impact.realisticinventory.pack;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes a pack's contents: the {@code Items} list in the pack item's NBT, each entry with its {@code Slot},
 * like a shulker box. A pack whose configured size shrank keeps the items beyond its size aside, never deleting them.
 */
public final class PackContents {
    public static final String ITEMS_KEY = "Items";
    private static final String SLOT_KEY = "Slot";
    private static final int SLOT_NUMBER_MASK = 0xFF;

    private PackContents() {
    }

    /** The pack's slots, filled from its NBT. Items in slots beyond the pack's size go into {@code itemsBeyondSize}. */
    public static NonNullList<ItemStack> read(ItemStack pack, int slotCount, List<ItemStack> itemsBeyondSize) {
        NonNullList<ItemStack> slots = NonNullList.withSize(slotCount, ItemStack.EMPTY);
        CompoundTag packTag = pack.getTag();
        if (packTag == null || !packTag.contains(ITEMS_KEY, Tag.TAG_LIST)) {
            return slots;
        }
        ListTag itemTags = packTag.getList(ITEMS_KEY, Tag.TAG_COMPOUND);
        for (int entry = 0; entry < itemTags.size(); entry++) {
            CompoundTag itemTag = itemTags.getCompound(entry);
            int slotNumber = itemTag.getByte(SLOT_KEY) & SLOT_NUMBER_MASK;
            ItemStack stack = ItemStack.of(itemTag);
            if (slotNumber < slotCount) {
                slots.set(slotNumber, stack);
            } else if (!stack.isEmpty()) {
                itemsBeyondSize.add(stack);
            }
        }
        return slots;
    }

    /** Writes the slots back into the pack's NBT. An empty pack has no {@code Items} key at all, so empty packs stack-compare equal. */
    public static void write(ItemStack pack, NonNullList<ItemStack> slots) {
        boolean isEmpty = slots.stream().allMatch(ItemStack::isEmpty);
        if (isEmpty) {
            pack.removeTagKey(ITEMS_KEY);
            return;
        }
        ContainerHelper.saveAllItems(pack.getOrCreateTag(), slots, false);
    }

    /** The highest slot number that holds an item, or -1 for an empty pack. */
    public static int highestUsedSlot(ItemStack pack) {
        CompoundTag packTag = pack.getTag();
        if (packTag == null || !packTag.contains(ITEMS_KEY, Tag.TAG_LIST)) {
            return -1;
        }
        ListTag itemTags = packTag.getList(ITEMS_KEY, Tag.TAG_COMPOUND);
        int highestSlot = -1;
        for (int entry = 0; entry < itemTags.size(); entry++) {
            highestSlot = Math.max(highestSlot, itemTags.getCompound(entry).getByte(SLOT_KEY) & SLOT_NUMBER_MASK);
        }
        return highestSlot;
    }

    /** Every stack in the pack, empty slots skipped. */
    public static List<ItemStack> stacksIn(ItemStack pack) {
        List<ItemStack> ignoredBeyondSize = new ArrayList<>();
        List<ItemStack> stacks = new ArrayList<>(read(pack, PackRules.MAXIMUM_SLOTS, ignoredBeyondSize));
        stacks.removeIf(ItemStack::isEmpty);
        return stacks;
    }
}
