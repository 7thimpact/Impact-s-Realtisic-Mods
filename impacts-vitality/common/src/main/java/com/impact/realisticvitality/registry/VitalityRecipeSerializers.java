package com.impact.realisticvitality.registry;

import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.crafting.ArmorLiningRecipe;
import com.impact.realisticvitality.crafting.ContainerFillingRecipe;
import com.impact.realisticvitality.crafting.MaskFilterRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

import java.util.function.Supplier;

/** Vitality's recipe serializers. */
public final class VitalityRecipeSerializers {
    private static volatile Supplier<SimpleCraftingRecipeSerializer<ContainerFillingRecipe>> containerFilling;
    private static volatile Supplier<SimpleCraftingRecipeSerializer<MaskFilterRecipe>> maskFilter;
    private static volatile Supplier<SimpleCraftingRecipeSerializer<ArmorLiningRecipe>> armorLining;

    private VitalityRecipeSerializers() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(VitalityMod.MOD_ID);
        containerFilling = registrar.register(Registries.RECIPE_SERIALIZER, "container_filling",
                () -> new SimpleCraftingRecipeSerializer<>(ContainerFillingRecipe::new));
        maskFilter = registrar.register(Registries.RECIPE_SERIALIZER, "mask_filter", () -> new SimpleCraftingRecipeSerializer<>(MaskFilterRecipe::new));
        armorLining = registrar.register(Registries.RECIPE_SERIALIZER, "armor_lining", () -> new SimpleCraftingRecipeSerializer<>(ArmorLiningRecipe::new));
    }

    public static RecipeSerializer<MaskFilterRecipe> maskFilter() {
        return maskFilter.get();
    }

    public static RecipeSerializer<ArmorLiningRecipe> armorLining() {
        return armorLining.get();
    }

    public static RecipeSerializer<ContainerFillingRecipe> containerFilling() {
        return containerFilling.get();
    }
}
