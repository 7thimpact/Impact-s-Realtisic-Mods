package com.impact.realisticfood.boiling;

import com.impact.lib.ResourceIds;
import com.impact.lib.config.KeyValueEntries;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.config.FoodServerConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;

/** The {@code impactsfood:boiling} recipe type, and finding the recipe for an item in a pot. */
public final class BoilingRecipes {
    public static final ResourceLocation ID = ResourceIds.of(FoodMod.MOD_ID, "boiling");
    private static final int MINIMUM_BOIL_TICKS = 1;
    private static Supplier<RecipeType<BoilingRecipe>> recipeType;
    private static Supplier<BoilingRecipe.Serializer> serializer;

    private BoilingRecipes() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(FoodMod.MOD_ID);
        recipeType = registrar.register(Registries.RECIPE_TYPE, ID.getPath(), BoilingRecipes::newRecipeType);
        serializer = registrar.register(Registries.RECIPE_SERIALIZER, ID.getPath(), BoilingRecipe.Serializer::new);
    }

    public static RecipeType<BoilingRecipe> recipeType() {
        return recipeType.get();
    }

    public static BoilingRecipe.Serializer serializer() {
        return serializer.get();
    }

    /** The boiling recipe for one of these items in this kind of pot. */
    public static Optional<BoilingRecipe> find(Level level, ItemStack stack, PotKind pot) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return level.getRecipeManager().getAllRecipesFor(recipeType()).stream()
                .filter(recipe -> recipe.accepts(stack, pot))
                .findFirst();
    }

    /** The recipe's boil time, or the one {@code boiling.boilTickOverrides} sets for the input item. */
    public static int boilTicks(BoilingRecipe recipe, ItemStack input) {
        String inputId = ResourceIds.itemId(input.getItem()).toString();
        OptionalInt override = KeyValueEntries.findInt(FoodServerConfig.get().boiling.boilTickOverrides.get(), inputId);
        return Math.max(MINIMUM_BOIL_TICKS, override.orElse(recipe.recipeBoilTicks()));
    }

    // Forge's RecipeType.simple isn't available to loader-independent code; vanilla's own types are this simple.
    private static RecipeType<BoilingRecipe> newRecipeType() {
        return new RecipeType<>() {
            @Override
            public String toString() {
                return ID.toString();
            }
        };
    }
}
