package com.impact.lib.audit;

import com.impact.lib.ImpactsLib;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Finds crafting recipes that could match the same grid, where at least one of them is an Impact recipe (an Impact
 * recipe against vanilla, SRF or another Impact mod). Shaped recipes compare cell by cell (mirrored too), and shaped tool
 * recipes also need tools that collide; anything shapeless compares as a bag of ingredients. Two ingredients collide
 * when some item fits both.
 */
public final class RecipeConflictChecker {
    private RecipeConflictChecker() {
    }

    public static List<RecipeConflict> findGridConflicts(RecipeManager recipes) {
        List<GridSignature> signatures = recipes.getAllRecipesFor(RecipeType.CRAFTING).stream()
                .map(GridSignature::of)
                .flatMap(Optional::stream)
                .toList();
        Map<Integer, List<GridSignature>> bySize = signatures.stream().collect(Collectors.groupingBy(signature -> signature.filledChoices().size()));
        List<RecipeConflict> conflicts = new ArrayList<>();
        bySize.values().forEach(sameSize -> addConflictsWithin(sameSize, conflicts));
        return conflicts;
    }

    public static boolean isImpactRecipe(GridSignature signature) {
        return signature.id().getNamespace().startsWith(ImpactsLib.MOD_ID_PREFIX);
    }

    private static void addConflictsWithin(List<GridSignature> sameSize, List<RecipeConflict> conflicts) {
        for (int firstIndex = 0; firstIndex < sameSize.size(); firstIndex++) {
            GridSignature first = sameSize.get(firstIndex);
            for (int secondIndex = firstIndex + 1; secondIndex < sameSize.size(); secondIndex++) {
                addIfConflicting(first, sameSize.get(secondIndex), conflicts);
            }
        }
    }

    private static void addIfConflicting(GridSignature first, GridSignature second, List<RecipeConflict> conflicts) {
        boolean involvesImpact = isImpactRecipe(first) || isImpactRecipe(second);
        if (involvesImpact && canMatchSameGrid(first, second)) {
            conflicts.add(new RecipeConflict(first.id(), second.id(), first.isShaped() && second.isShaped() ? "same shape" : "same ingredients"));
        }
    }

    static boolean canMatchSameGrid(GridSignature first, GridSignature second) {
        if (first.isShaped() && second.isShaped()) {
            boolean shapesCollide = sameSizeGrid(first, second) && (cellsCollide(first, second, false) || cellsCollide(first, second, true));
            boolean toolsCollide = first.tools().size() == second.tools().size()
                    && bagsCollide(first.tools(), second.tools(), new boolean[second.tools().size()], 0);
            return shapesCollide && toolsCollide;
        }
        return bagsCollide(first.filledChoices(), second.filledChoices(), new boolean[second.filledChoices().size()], 0);
    }

    private static boolean sameSizeGrid(GridSignature first, GridSignature second) {
        return first.width() == second.width() && first.height() == second.height();
    }

    private static boolean cellsCollide(GridSignature first, GridSignature second, boolean isMirrored) {
        for (int row = 0; row < first.height(); row++) {
            for (int column = 0; column < first.width(); column++) {
                int secondColumn = isMirrored ? first.width() - 1 - column : column;
                List<ItemStack> firstCell = first.cells().get(row * first.width() + column);
                List<ItemStack> secondCell = second.cells().get(row * second.width() + secondColumn);
                if (firstCell.isEmpty() != secondCell.isEmpty() || !firstCell.isEmpty() && !choicesCollide(firstCell, secondCell)) {
                    return false;
                }
            }
        }
        return true;
    }

    // Backtracking match of one bag onto the other; bags hold at most 9 ingredients.
    private static boolean bagsCollide(List<List<ItemStack>> first, List<List<ItemStack>> second, boolean[] secondUsed, int firstIndex) {
        if (firstIndex == first.size()) {
            return true;
        }
        for (int secondIndex = 0; secondIndex < second.size(); secondIndex++) {
            if (secondUsed[secondIndex] || !choicesCollide(first.get(firstIndex), second.get(secondIndex))) {
                continue;
            }
            secondUsed[secondIndex] = true;
            if (bagsCollide(first, second, secondUsed, firstIndex + 1)) {
                return true;
            }
            secondUsed[secondIndex] = false;
        }
        return false;
    }

    private static boolean choicesCollide(List<ItemStack> first, List<ItemStack> second) {
        return first.stream().anyMatch(firstStack -> second.stream().anyMatch(secondStack -> stacksCollide(firstStack, secondStack)));
    }

    // An ingredient without NBT accepts any NBT; two NBT-specific ones collide only on equal NBT (per-wood parts).
    private static boolean stacksCollide(ItemStack first, ItemStack second) {
        if (!ItemStack.isSameItem(first, second)) {
            return false;
        }
        return !first.hasTag() || !second.hasTag() || first.getTag().equals(second.getTag());
    }
}
