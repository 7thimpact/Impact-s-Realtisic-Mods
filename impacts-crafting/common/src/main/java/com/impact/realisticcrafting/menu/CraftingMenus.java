package com.impact.realisticcrafting.menu;

import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticcrafting.CraftingMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

/** Crafting's menu types. */
public final class CraftingMenus {
    private static volatile Supplier<MenuType<TieredCraftingMenu>> tieredCrafting;

    private CraftingMenus() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        tieredCrafting = LoaderPlatform.current().registrar(CraftingMod.MOD_ID)
                .register(Registries.MENU, "tiered_crafting", () -> LoaderPlatform.current().menuTypeWithData(TieredCraftingMenu::fromNetwork));
    }

    public static MenuType<TieredCraftingMenu> tieredCrafting() {
        return tieredCrafting.get();
    }
}
