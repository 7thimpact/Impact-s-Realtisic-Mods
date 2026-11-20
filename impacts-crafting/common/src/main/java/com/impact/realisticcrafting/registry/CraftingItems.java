package com.impact.realisticcrafting.registry;

import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.CreativeTabs;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.item.PebbleItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Crafting's items and its creative tab. */
public final class CraftingItems {
    private static volatile Supplier<PebbleItem> stonePebble;

    private CraftingItems() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(CraftingMod.MOD_ID);
        stonePebble = registrar.register(Registries.ITEM, "stone_pebble", PebbleItem::new);
        CreativeTabs.register(CraftingMod.MOD_ID, () -> new ItemStack(stonePebble()), CraftingItems::creativeTabItems);
    }

    public static PebbleItem stonePebble() {
        return stonePebble.get();
    }

    public static List<Item> creativeTabItems() {
        List<Item> items = new ArrayList<>();
        items.add(stonePebble());
        return items;
    }
}
