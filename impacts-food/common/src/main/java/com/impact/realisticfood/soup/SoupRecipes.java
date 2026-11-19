package com.impact.realisticfood.soup;

import com.impact.lib.ResourceIds;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.boiling.PotKind;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/** The {@code impactsfood:soup} recipe type, and the soups a kind of pot can make. */
public final class SoupRecipes {
    public static final ResourceLocation ID = ResourceIds.of(FoodMod.MOD_ID, "soup");
    private static Supplier<RecipeType<SoupRecipe>> recipeType;
    private static Supplier<SoupRecipe.Serializer> serializer;

    private SoupRecipes() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(FoodMod.MOD_ID);
        recipeType = registrar.register(Registries.RECIPE_TYPE, ID.getPath(), SoupRecipes::newRecipeType);
        serializer = registrar.register(Registries.RECIPE_SERIALIZER, ID.getPath(), SoupRecipe.Serializer::new);
    }

    public static RecipeType<SoupRecipe> recipeType() {
        return recipeType.get();
    }

    public static SoupRecipe.Serializer serializer() {
        return serializer.get();
    }

    public static List<SoupRecipe> recipesFor(Level level, PotKind pot) {
        return level.getRecipeManager().getAllRecipesFor(recipeType()).stream().filter(recipe -> recipe.vessel().accepts(pot)).toList();
    }

    /** Whether any soup this kind of pot can make uses the item. */
    public static boolean isIngredient(Level level, ItemStack stack, PotKind pot) {
        return !stack.isEmpty() && recipesFor(level, pot).stream().anyMatch(recipe -> recipe.uses(stack));
    }

    public static Optional<SoupRecipe> byId(Level level, ResourceLocation recipeId) {
        return level.getRecipeManager().byKey(recipeId).filter(SoupRecipe.class::isInstance).map(SoupRecipe.class::cast);
    }

    // Forge's RecipeType.simple isn't available to loader-independent code; vanilla's own types are this simple.
    private static RecipeType<SoupRecipe> newRecipeType() {
        return new RecipeType<>() {
            @Override
            public String toString() {
                return ID.toString();
            }
        };
    }
}
