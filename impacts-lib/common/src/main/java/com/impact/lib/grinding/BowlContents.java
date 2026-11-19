package com.impact.lib.grinding;

import com.impact.lib.config.LibServerConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * What a grinder bowl holds: up to 2 kinds of items (2 dyes for mixing), at most {@code grinder.bowlCapacity} (16) items
 * in all, stored as {@code Contents} in the bowl's NBT. No tag means an empty bowl.
 */
public final class BowlContents {
    public static final String CONTENTS_KEY = "Contents";
    public static final int MAXIMUM_KINDS = 2;

    private BowlContents() {
    }

    public static List<ItemStack> read(ItemStack bowl) {
        List<ItemStack> contents = new ArrayList<>();
        CompoundTag tag = bowl.getTag();
        if (tag == null || !tag.contains(CONTENTS_KEY, Tag.TAG_LIST)) {
            return contents;
        }
        ListTag entries = tag.getList(CONTENTS_KEY, Tag.TAG_COMPOUND);
        for (int entry = 0; entry < entries.size(); entry++) {
            ItemStack stack = ItemStack.of(entries.getCompound(entry));
            if (!stack.isEmpty()) {
                contents.add(stack);
            }
        }
        return contents;
    }

    public static void write(ItemStack bowl, List<ItemStack> contents) {
        List<ItemStack> kept = contents.stream().filter(stack -> !stack.isEmpty()).toList();
        if (kept.isEmpty()) {
            bowl.removeTagKey(CONTENTS_KEY);
            return;
        }
        ListTag entries = new ListTag();
        kept.forEach(stack -> entries.add(stack.save(new CompoundTag())));
        bowl.getOrCreateTag().put(CONTENTS_KEY, entries);
    }

    /** The contents as a recipe container, one stack per kind. */
    public static SimpleContainer asContainer(ItemStack bowl) {
        return new SimpleContainer(read(bowl).toArray(ItemStack[]::new));
    }

    public static int totalCount(List<ItemStack> contents) {
        return contents.stream().mapToInt(ItemStack::getCount).sum();
    }

    /** Moves as much of the stack into the bowl as fits. Returns how many items moved. */
    public static int load(ItemStack bowl, ItemStack incoming) {
        List<ItemStack> contents = read(bowl);
        int room = capacity() - totalCount(contents);
        int movedCount = Math.min(room, incoming.getCount());
        if (movedCount <= 0) {
            return 0;
        }
        ItemStack sameKind = contents.stream().filter(stack -> ItemStack.isSameItemSameTags(stack, incoming)).findFirst().orElse(ItemStack.EMPTY);
        if (sameKind.isEmpty() && contents.size() >= MAXIMUM_KINDS) {
            return 0;
        }
        if (sameKind.isEmpty()) {
            contents.add(incoming.copyWithCount(movedCount));
        } else {
            sameKind.grow(movedCount);
        }
        incoming.shrink(movedCount);
        write(bowl, contents);
        return movedCount;
    }

    /** Takes one item of each kind the recipe used. */
    public static void consumeOneOfEach(ItemStack bowl) {
        List<ItemStack> contents = read(bowl);
        contents.forEach(stack -> stack.shrink(1));
        write(bowl, contents);
    }

    public static int capacity() {
        return LibServerConfig.get().grinder.bowlCapacity.get();
    }
}
