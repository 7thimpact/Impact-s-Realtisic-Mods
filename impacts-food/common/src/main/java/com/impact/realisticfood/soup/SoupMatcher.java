package com.impact.realisticfood.soup;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Finds which soup a pot's ingredients make: the recipe and number of sets that use the most ingredients, so rabbit
 * stew (4 ingredients) wins over the hearty stew inside it (3). Each set's ingredients go in any order; ingredients
 * left over stay in the pot.
 */
public final class SoupMatcher {
    private static final int NO_ITEM = -1;

    private SoupMatcher() {
    }

    /** One way the ingredients make a soup: the recipe, how many sets, and which ingredients (by index) it uses. */
    public record SoupMatch(SoupRecipe recipe, int sets, List<Integer> usedIndexes) {
        int ingredientsUsed() {
            return usedIndexes.size();
        }
    }

    /** The best match among the recipes, with at most {@code maximumServings} servings. */
    public static Optional<SoupMatch> bestMatch(List<SoupRecipe> recipes, List<ItemStack> ingredients, int maximumServings) {
        SoupMatch best = null;
        for (SoupRecipe recipe : recipes) {
            Optional<SoupMatch> match = mostSets(recipe, ingredients, maximumServings / recipe.bowlsPerSet());
            if (match.isPresent() && (best == null || match.get().ingredientsUsed() > best.ingredientsUsed())) {
                best = match.get();
            }
        }
        return Optional.ofNullable(best);
    }

    /** The most sets of this one recipe the ingredients make, up to {@code maximumSets}. */
    public static Optional<SoupMatch> mostSets(SoupRecipe recipe, List<ItemStack> ingredients, int maximumSets) {
        for (int sets = maximumSets; sets >= 1; sets--) {
            Optional<List<Integer>> used = assign(recipe.getIngredients(), sets, ingredients);
            if (used.isPresent()) {
                return Optional.of(new SoupMatch(recipe, sets, used.get()));
            }
        }
        return Optional.empty();
    }

    // Each recipe slot (every ingredient, once per set) needs its own item: a bipartite matching, by augmenting paths.
    private static Optional<List<Integer>> assign(List<Ingredient> recipeIngredients, int sets, List<ItemStack> items) {
        List<Ingredient> slots = new ArrayList<>();
        for (int set = 0; set < sets; set++) {
            slots.addAll(recipeIngredients);
        }
        if (slots.size() > items.size()) {
            return Optional.empty();
        }
        int[] slotOfItem = new int[items.size()];
        Arrays.fill(slotOfItem, NO_ITEM);
        for (int slot = 0; slot < slots.size(); slot++) {
            if (!findItemFor(slot, slots, items, slotOfItem, new boolean[items.size()])) {
                return Optional.empty();
            }
        }
        List<Integer> used = new ArrayList<>();
        for (int item = 0; item < items.size(); item++) {
            if (slotOfItem[item] != NO_ITEM) {
                used.add(item);
            }
        }
        return Optional.of(used);
    }

    private static boolean findItemFor(int slot, List<Ingredient> slots, List<ItemStack> items, int[] slotOfItem, boolean[] isVisited) {
        for (int item = 0; item < items.size(); item++) {
            if (isVisited[item] || !slots.get(slot).test(items.get(item))) {
                continue;
            }
            isVisited[item] = true;
            boolean isFree = slotOfItem[item] == NO_ITEM;
            if (isFree || findItemFor(slotOfItem[item], slots, items, slotOfItem, isVisited)) {
                slotOfItem[item] = slot;
                return true;
            }
        }
        return false;
    }
}
