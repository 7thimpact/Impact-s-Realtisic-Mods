package com.impact.realisticvitality.registry;

import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.crafting.ContainerFillingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

import java.util.function.Supplier;

/** Vitality's recipe serializers. */
public final class VitalityRecipeSerializers {
    private static volatile Supplier<SimpleCraftingRecipeSerializer<ContainerFillingRecipe>> containerFilling;

    private VitalityRecipeSerializers() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        containerFilling = LoaderPlatform.current().registrar(VitalityMod.MOD_ID)
                .register(Registries.RECIPE_SERIALIZER, "container_filling", () -> new SimpleCraftingRecipeSerializer<>(ContainerFillingRecipe::new));
    }

    public static RecipeSerializer<ContainerFillingRecipe> containerFilling() {
        return containerFilling.get();
    }
}
