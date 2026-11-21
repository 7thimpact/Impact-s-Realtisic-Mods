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
    public static final ResourceLocation VAT_DYEING_ID = ResourceIds.of(CraftingMod.MOD_ID, "vat_dyeing");
    private static volatile Supplier<RecipeType<VatDyeingRecipe>> vatDyeing;
    private static volatile Supplier<VatDyeingRecipe.Serializer> vatDyeingSerializer;

    private CraftingRecipeTypes() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(CraftingMod.MOD_ID);
        hammering = registrar.register(Registries.RECIPE_TYPE, HAMMERING_ID.getPath(), () -> named(HAMMERING_ID));
        hammeringSerializer = registrar.register(Registries.RECIPE_SERIALIZER, HAMMERING_ID.getPath(), HammeringRecipe.Serializer::new);
        vatDyeing = registrar.register(Registries.RECIPE_TYPE, VAT_DYEING_ID.getPath(), () -> named(VAT_DYEING_ID));
        vatDyeingSerializer = registrar.register(Registries.RECIPE_SERIALIZER, VAT_DYEING_ID.getPath(), VatDyeingRecipe.Serializer::new);
    }

    public static RecipeType<HammeringRecipe> hammering() {
        return hammering.get();
    }

    public static HammeringRecipe.Serializer hammeringSerializer() {
        return hammeringSerializer.get();
    }

    public static RecipeType<VatDyeingRecipe> vatDyeing() {
        return vatDyeing.get();
    }

    public static VatDyeingRecipe.Serializer vatDyeingSerializer() {
        return vatDyeingSerializer.get();
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
