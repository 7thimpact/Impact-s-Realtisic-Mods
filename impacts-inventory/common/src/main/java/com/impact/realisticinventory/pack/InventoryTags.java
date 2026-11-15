package com.impact.realisticinventory.pack;

import com.impact.lib.ResourceIds;
import com.impact.realisticinventory.InventoryMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/** Inventory's item tags. */
public final class InventoryTags {
    /** Inventory's own packs: they fit the back slot and can't go inside another pack. */
    public static final TagKey<Item> PACKS = TagKey.create(Registries.ITEM, ResourceIds.of(InventoryMod.MOD_ID, "packs"));

    private InventoryTags() {
    }
}
