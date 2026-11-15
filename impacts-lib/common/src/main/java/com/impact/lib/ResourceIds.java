package com.impact.lib;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/** Creates resource IDs in one place, so later ports change only this class. */
public final class ResourceIds {
    private ResourceIds() {
    }

    // Forge 47.4 marks this constructor for removal in favor of 1.21's fromNamespaceAndPath, which vanilla 1.20.1
    // (the common module) doesn't have yet.
    @SuppressWarnings("removal")
    public static ResourceLocation of(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    // Forge deprecates vanilla's registries in favor of its own. Both hold the same entries, and vanilla's is the
    // one loader-independent code can reach.
    @SuppressWarnings("deprecation")
    public static ResourceLocation itemId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }
}
