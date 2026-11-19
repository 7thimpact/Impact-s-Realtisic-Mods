package com.impact.realisticinventory.pack;

import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.CreativeTabs;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticinventory.InventoryMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

/** Inventory's items: the three packs, and the creative tab that lists them. */
public final class InventoryItems {
    private static Supplier<PackItem> satchel;
    private static Supplier<PackItem> backpack;
    private static Supplier<PackItem> framePack;

    private InventoryItems() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(InventoryMod.MOD_ID);
        satchel = registerPack(registrar, PackKind.SATCHEL);
        backpack = registerPack(registrar, PackKind.BACKPACK);
        framePack = registerPack(registrar, PackKind.FRAME_PACK);
        CreativeTabs.register(InventoryMod.MOD_ID, () -> new ItemStack(backpack()), InventoryItems::packs);
    }

    public static PackItem satchel() {
        return satchel.get();
    }

    public static PackItem backpack() {
        return backpack.get();
    }

    public static PackItem framePack() {
        return framePack.get();
    }

    public static PackItem pack(PackKind kind) {
        return switch (kind) {
            case SATCHEL -> satchel();
            case BACKPACK -> backpack();
            case FRAME_PACK -> framePack();
        };
    }

    public static List<Item> packs() {
        return List.of(satchel(), backpack(), framePack());
    }

    private static Supplier<PackItem> registerPack(ModRegistrar registrar, PackKind kind) {
        return registrar.register(Registries.ITEM, kind.itemPath(), () -> new PackItem(kind));
    }
}
