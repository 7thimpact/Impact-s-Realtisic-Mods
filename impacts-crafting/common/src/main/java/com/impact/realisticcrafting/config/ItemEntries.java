package com.impact.realisticcrafting.config;

import com.impact.lib.ResourceIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Config lists of items written as item IDs ({@code minecraft:iron_ingot}) or item tags ({@code #forge:ingots/iron}). */
public final class ItemEntries {
    private static final String TAG_PREFIX = "#";

    private ItemEntries() {
    }

    public static boolean contains(List<String> entries, ItemStack stack) {
        return entries.stream().anyMatch(entry -> matches(entry, stack));
    }

    private static boolean matches(String entry, ItemStack stack) {
        if (entry.startsWith(TAG_PREFIX)) {
            return ResourceIds.tryParse(entry.substring(TAG_PREFIX.length()))
                    .map(tagId -> stack.is(TagKey.create(Registries.ITEM, tagId)))
                    .orElse(false);
        }
        return ResourceIds.tryParse(entry).map(itemId -> itemId.equals(ResourceIds.itemId(stack.getItem()))).orElse(false);
    }
}
