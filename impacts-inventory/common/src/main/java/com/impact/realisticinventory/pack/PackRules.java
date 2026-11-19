package com.impact.realisticinventory.pack;

import com.impact.lib.ResourceIds;
import com.impact.realisticinventory.config.InventoryServerConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.List;

/**
 * What may go inside a pack. Packs can't (the default {@code packs.nestingBlacklist} lists {@code #impactsinventory:packs}),
 * shulker boxes can't unless {@code packs.allowShulkers} is on, and the blacklist can name more items or tags.
 */
public final class PackRules {
    /** The most slots a pack can be configured to have. */
    public static final int MAXIMUM_SLOTS = 54;
    private static final String TAG_PREFIX = "#";

    private PackRules() {
    }

    public static boolean canGoInPack(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        if (isShulkerBox(stack) && !InventoryServerConfig.get().packs.isShulkerAllowed.get()) {
            return false;
        }
        List<String> blacklist = InventoryServerConfig.get().packs.nestingBlacklist.get();
        return blacklist.stream().noneMatch(entry -> matchesEntry(stack, entry));
    }

    public static boolean isPack(ItemStack stack) {
        return stack.getItem() instanceof PackItem;
    }

    private static boolean isShulkerBox(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock;
    }

    // Entries that aren't valid IDs match nothing.
    private static boolean matchesEntry(ItemStack stack, String entry) {
        if (entry.startsWith(TAG_PREFIX)) {
            return ResourceIds.tryParse(entry.substring(TAG_PREFIX.length()))
                    .map(tagId -> stack.is(TagKey.create(Registries.ITEM, tagId)))
                    .orElse(false);
        }
        return ResourceIds.tryParse(entry).map(itemId -> itemId.equals(ResourceIds.itemId(stack.getItem()))).orElse(false);
    }
}
