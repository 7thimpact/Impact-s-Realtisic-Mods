package com.impact.realisticinventory.pack;

import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticinventory.InventoryMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

/** Inventory's recipe types: the pack upgrade, which keeps a pack's contents and color. */
public final class PackRecipes {
    public static final String PACK_UPGRADE = "pack_upgrade";
    private static Supplier<PackUpgradeRecipe.Serializer> packUpgradeSerializer;

    private PackRecipes() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        packUpgradeSerializer = LoaderPlatform.current().registrar(InventoryMod.MOD_ID)
                .register(Registries.RECIPE_SERIALIZER, PACK_UPGRADE, PackUpgradeRecipe.Serializer::new);
    }

    public static PackUpgradeRecipe.Serializer packUpgradeSerializer() {
        return packUpgradeSerializer.get();
    }
}
