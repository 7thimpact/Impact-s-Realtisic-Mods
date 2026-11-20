package com.impact.realisticcrafting.registry;

import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.CreativeTabs;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.item.MakeshiftTableItem;
import com.impact.realisticcrafting.item.PebbleItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Crafting's items and its creative tab. */
public final class CraftingItems {
    private static final Map<MakeshiftTable, Supplier<MakeshiftTableItem>> TABLES = new EnumMap<>(MakeshiftTable.class);
    private static volatile Supplier<PebbleItem> stonePebble;

    private CraftingItems() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(CraftingMod.MOD_ID);
        stonePebble = registrar.register(Registries.ITEM, "stone_pebble", PebbleItem::new);
        for (MakeshiftTable table : MakeshiftTable.values()) {
            TABLES.put(table, registrar.register(Registries.ITEM, table.path, () -> new MakeshiftTableItem(CraftingBlocks.table(table))));
        }
        CreativeTabs.register(CraftingMod.MOD_ID, () -> new ItemStack(stonePebble()), CraftingItems::creativeTabItems);
    }

    public static PebbleItem stonePebble() {
        return stonePebble.get();
    }

    public static MakeshiftTableItem table(MakeshiftTable table) {
        return TABLES.get(table).get();
    }

    public static List<Item> creativeTabItems() {
        List<Item> items = new ArrayList<>();
        items.add(stonePebble());
        Arrays.stream(MakeshiftTable.values()).map(CraftingItems::table).forEach(items::add);
        return items;
    }
}
