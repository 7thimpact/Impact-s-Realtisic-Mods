package com.impact.realisticcrafting.timing;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.debug.CraftingLogCategories;
import com.impact.realisticcrafting.recipe.CraftingRecipeTypes;
import com.impact.realisticcrafting.recipe.HammeringRecipe;
import com.impact.realisticcrafting.registry.CraftingTags;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;

/**
 * How deep each item sits in the crafting tree, built from the live recipe set on the server (at start and after every
 * reload). Base materials are depth 0: {@code #impactscrafting:base_materials}, anything a cooking recipe makes, and
 * anything no crafting or hammering recipe makes. A crafting recipe's output is 1 deeper than its deepest ingredient (an
 * ingredient counts its shallowest item; tools and items that come back from the craft don't count); a hammering recipe
 * passes its loose item's depth on. The table settles as a fixpoint, so loops (ingot and block) take the shortest path.
 */
public final class CraftDepthTable {
    private static final ImpactsLog LOG = ImpactsLog.of(CraftingMod.MOD_ID);
    private static final List<RecipeType<?>> COOKING_TYPES = List.of(RecipeType.SMELTING, RecipeType.BLASTING, RecipeType.SMOKING,
            RecipeType.CAMPFIRE_COOKING);
    private static volatile Map<Item, Integer> depths = Map.of();

    private CraftDepthTable() {
    }

    public static OptionalInt depthOf(Item item) {
        Integer depth = depths.get(item);
        return depth == null ? OptionalInt.empty() : OptionalInt.of(depth);
    }

    /** 1 + the deepest counted ingredient, or empty when the table can't place one (no ingredients, or stuck in a loop). */
    public static OptionalInt recipeDepth(Recipe<?> recipe) {
        return deepestIngredient(recipe, depths).stream().map(deepest -> deepest + 1).findFirst();
    }

    public static void rebuild(RecipeManager recipes, RegistryAccess registryAccess) {
        long startNanos = System.nanoTime();
        List<CraftingRecipe> craftingRecipes = recipes.getAllRecipesFor(RecipeType.CRAFTING);
        List<HammeringRecipe> hammeringRecipes = recipes.getAllRecipesFor(CraftingRecipeTypes.hammering());
        Map<Item, Integer> solved = new HashMap<>(baseMaterials(recipes, registryAccess, craftingRecipes, hammeringRecipes));
        boolean hasChanged = true;
        while (hasChanged) {
            hasChanged = false;
            for (CraftingRecipe recipe : craftingRecipes) {
                OptionalInt deepest = deepestIngredient(recipe, solved);
                if (deepest.isPresent()) {
                    hasChanged |= propose(solved, recipe.getResultItem(registryAccess).getItem(), deepest.getAsInt() + 1);
                }
            }
            for (HammeringRecipe recipe : hammeringRecipes) {
                OptionalInt looseDepth = shallowest(recipe.input(), solved);
                if (looseDepth.isPresent()) {
                    hasChanged |= propose(solved, recipe.result().getItem(), looseDepth.getAsInt());
                }
            }
        }
        depths = Collections.unmodifiableMap(solved);
        logRebuild(craftingRecipes, solved, (System.nanoTime() - startNanos) / 1_000_000L);
    }

    private static Map<Item, Integer> baseMaterials(RecipeManager recipes, RegistryAccess registryAccess, List<CraftingRecipe> craftingRecipes,
                                                    List<HammeringRecipe> hammeringRecipes) {
        Set<Item> crafted = new HashSet<>();
        craftingRecipes.forEach(recipe -> crafted.add(recipe.getResultItem(registryAccess).getItem()));
        hammeringRecipes.forEach(recipe -> crafted.add(recipe.result().getItem()));
        Set<Item> cooked = new HashSet<>();
        COOKING_TYPES.forEach(type -> recipes.getRecipes().stream()
                .filter(recipe -> recipe.getType() == type)
                .forEach(recipe -> cooked.add(recipe.getResultItem(registryAccess).getItem())));
        Map<Item, Integer> base = new HashMap<>();
        registryAccess.registryOrThrow(Registries.ITEM).forEach(item -> {
            boolean isBase = !crafted.contains(item) || cooked.contains(item) || new ItemStack(item).is(CraftingTags.BASE_MATERIALS);
            if (isBase) {
                base.put(item, 0);
            }
        });
        return base;
    }

    private static boolean propose(Map<Item, Integer> solved, Item output, int depth) {
        Integer known = solved.get(output);
        if (known != null && known <= depth) {
            return false;
        }
        solved.put(output, depth);
        return true;
    }

    private static OptionalInt deepestIngredient(Recipe<?> recipe, Map<Item, Integer> solved) {
        List<Ingredient> counted = recipe.getIngredients().stream().filter(CraftDepthTable::counts).toList();
        if (counted.isEmpty()) {
            return OptionalInt.empty();
        }
        int deepest = 0;
        for (Ingredient ingredient : counted) {
            OptionalInt ingredientDepth = shallowest(ingredient, solved);
            if (ingredientDepth.isEmpty()) {
                return OptionalInt.empty();
            }
            deepest = Math.max(deepest, ingredientDepth.getAsInt());
        }
        return OptionalInt.of(deepest);
    }

    private static OptionalInt shallowest(Ingredient ingredient, Map<Item, Integer> solved) {
        return Arrays.stream(ingredient.getItems())
                .map(stack -> solved.get(stack.getItem()))
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .min();
    }

    // Tools in the grid don't count: Lib's tools aren't ingredients at all, and items that come back from the craft are skipped.
    private static boolean counts(Ingredient ingredient) {
        ItemStack[] items = ingredient.getItems();
        if (ingredient.isEmpty() || items.length == 0) {
            return false;
        }
        return !Arrays.stream(items).allMatch(stack -> LoaderPlatform.current().craftingRemainder(stack).is(stack.getItem()));
    }

    private static void logRebuild(List<CraftingRecipe> craftingRecipes, Map<Item, Integer> solved, long milliseconds) {
        long unplaced = craftingRecipes.stream().filter(recipe -> deepestIngredient(recipe, solved).isEmpty()).count();
        Map.Entry<Item, Integer> deepestItem = solved.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        LOG.debug(CraftingLogCategories.DEPTH, "depth table rebuilt", "recipes", craftingRecipes.size(), "items", solved.size(),
                "deepest", deepestItem == null ? "none" : ResourceIds.itemId(deepestItem.getKey()) + "=" + deepestItem.getValue(),
                "onFallback", unplaced, "ms", milliseconds);
        if (LOG.isVerbose(CraftingLogCategories.DEPTH)) {
            solved.forEach((item, depth) -> LOG.verbose(CraftingLogCategories.DEPTH, "depth", "item", ResourceIds.itemId(item), "depth", depth));
        }
    }
}
