package com.impact.realisticcrafting.recipe;

import com.impact.lib.ResourceIds;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticcrafting.CraftingMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.function.Supplier;

/** Crafting's recipe types and serializers. */
public final class CraftingRecipeTypes {
    public static final ResourceLocation HAMMERING_ID = ResourceIds.of(CraftingMod.MOD_ID, "hammering");
    private static volatile Supplier<RecipeType<HammeringRecipe>> hammering;
    private static volatile Supplier<HammeringRecipe.Serializer> hammeringSerializer;

    private CraftingRecipeTypes() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(CraftingMod.MOD_ID);
        hammering = registrar.register(Registries.RECIPE_TYPE, HAMMERING_ID.getPath(), () -> named(HAMMERING_ID));
        hammeringSerializer = registrar.register(Registries.RECIPE_SERIALIZER, HAMMERING_ID.getPath(), HammeringRecipe.Serializer::new);
    }

    public static RecipeType<HammeringRecipe> hammering() {
        return hammering.get();
    }

    public static HammeringRecipe.Serializer hammeringSerializer() {
        return hammeringSerializer.get();
    }

    // Vanilla's own recipe types are this simple; Forge's RecipeType.simple isn't available to loader-independent code.
    private static <T extends Recipe<?>> RecipeType<T> named(ResourceLocation id) {
        return new RecipeType<>() {
            @Override
            public String toString() {
                return id.toString();
            }
        };
    }
}
