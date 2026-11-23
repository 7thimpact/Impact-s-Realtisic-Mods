package com.impact.realisticinventory.pack;

import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.CreativeTabs;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticinventory.InventoryMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Inventory's items: the packs, and the creative tab that lists them. */
public final class InventoryItems {
    private static final Map<PackKind, Supplier<PackItem>> PACKS = new EnumMap<>(PackKind.class);

    private InventoryItems() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(InventoryMod.MOD_ID);
        for (PackKind kind : PackKind.values()) {
            PACKS.put(kind, registrar.register(Registries.ITEM, kind.itemPath(), () -> new PackItem(kind)));
        }
        CreativeTabs.register(InventoryMod.MOD_ID, () -> new ItemStack(backpack()), InventoryItems::packs);
    }

    public static PackItem satchel() {
        return pack(PackKind.SATCHEL);
    }

    public static PackItem backpack() {
        return pack(PackKind.BACKPACK);
    }

    public static PackItem framePack() {
        return pack(PackKind.FRAME_PACK);
    }

    public static PackItem pack(PackKind kind) {
        return PACKS.get(kind).get();
    }

    public static List<Item> packs() {
        return Arrays.stream(PackKind.values()).<Item>map(InventoryItems::pack).toList();
    }
}
