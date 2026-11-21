package com.impact.lib.grinding;

import com.impact.lib.ImpactsLib;
import com.impact.lib.hold.HoldToUse;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.ModRegistrar;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

/**
 * Lib's grinder: the {@code impactslib:grinding} recipe type and the four grinder items. The mods ship the recipes
 * (Crafting's, or Food's simpler ones without Crafting); Lib ships none.
 */
public final class Grinding {
    private static final int DEFAULT_WOODEN_DURABILITY = 64;
    private static final int DEFAULT_STONE_DURABILITY = 256;
    private static Supplier<GrinderBowlItem> woodenBowl;
    private static Supplier<GrinderBowlItem> stoneBowl;
    private static Supplier<GrindingStickItem> woodenStick;
    private static Supplier<GrindingStickItem> stoneStick;
    private static Supplier<RecipeType<GrindingRecipe>> recipeType;
    private static Supplier<GrindingRecipe.Serializer> serializer;

    private Grinding() {
    }

    /** Called once, during Lib's construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(ImpactsLib.MOD_ID);
        woodenBowl = registrar.register(Registries.ITEM, "wooden_grinder_bowl", () -> new GrinderBowlItem(GrinderTier.WOOD, toolProperties(GrinderTier.WOOD)));
        stoneBowl = registrar.register(Registries.ITEM, "stone_grinder_bowl", () -> new GrinderBowlItem(GrinderTier.STONE, toolProperties(GrinderTier.STONE)));
        woodenStick = registrar.register(Registries.ITEM, "wooden_grinding_stick", () -> new GrindingStickItem(GrinderTier.WOOD, toolProperties(GrinderTier.WOOD)));
        stoneStick = registrar.register(Registries.ITEM, "stone_grinding_stick", () -> new GrindingStickItem(GrinderTier.STONE, toolProperties(GrinderTier.STONE)));
        recipeType = registrar.register(Registries.RECIPE_TYPE, "grinding", Grinding::newRecipeType);
        serializer = registrar.register(Registries.RECIPE_SERIALIZER, "grinding", GrindingRecipe.Serializer::new);
    }

    /** Once every mod is ready: the sticks become hold-to-use items. */
    public static void registerHoldActions() {
        GrindingAction action = new GrindingAction();
        HoldToUse.register(woodenStick.get(), action);
        HoldToUse.register(stoneStick.get(), action);
    }

    public static GrinderBowlItem woodenBowl() {
        return woodenBowl.get();
    }

    public static GrinderBowlItem stoneBowl() {
        return stoneBowl.get();
    }

    public static GrindingStickItem woodenStick() {
        return woodenStick.get();
    }

    public static GrindingStickItem stoneStick() {
        return stoneStick.get();
    }

    public static List<Item> grinderItems() {
        return List.of(woodenBowl(), stoneBowl(), woodenStick(), stoneStick());
    }

    public static RecipeType<GrindingRecipe> recipeType() {
        return recipeType.get();
    }

    public static GrindingRecipe.Serializer serializer() {
        return serializer.get();
    }

    /** Whether any grinding recipe takes the item, so a bowl accepts it. */
    public static boolean isGrindable(Level level, ItemStack stack) {
        return !stack.isEmpty() && level.getRecipeManager().getAllRecipesFor(recipeType()).stream().anyMatch(recipe -> recipe.uses(stack));
    }

    // Vanilla's own recipe types are this simple too; Forge's RecipeType.simple isn't available to loader-independent code.
    private static RecipeType<GrindingRecipe> newRecipeType() {
        return new RecipeType<>() {
            @Override
            public String toString() {
                return GrindingAction.ID.toString();
            }
        };
    }

    // Registered with the default durability, so vanilla treats them as damageable; the config value is read on use.
    private static Item.Properties toolProperties(GrinderTier tier) {
        return new Item.Properties().durability(tier == GrinderTier.WOOD ? DEFAULT_WOODEN_DURABILITY : DEFAULT_STONE_DURABILITY);
    }
}
