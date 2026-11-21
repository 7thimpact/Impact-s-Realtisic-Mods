package com.impact.realisticcrafting.recipe;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.OverhaulCategory;
import com.impact.realisticcrafting.debug.CraftingLogCategories;
import com.impact.realisticcrafting.menu.CraftingStation;
import com.impact.realisticcrafting.menu.CraftingTier;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Labels every vanilla crafting recipe: replaced by Crafting, gated (kept, but above the Makeshift Table's tier),
 * untouched (kept, with the reason) or food (Impact's Realistic Food's). Anything left unlabeled is a recipe the
 * overhaul forgot, and the audit GameTest fails on it. Runs at every server start and reload, where tags (and so
 * tiers) are known.
 */
public final class CraftingRecipeAudit {
    private static final ImpactsLog LOG = ImpactsLog.of(CraftingMod.MOD_ID);
    private static final String RECIPES_FOLDER = "recipes";
    private static final String JSON_SUFFIX = ".json";
    private static final String MINECRAFT_NAMESPACE = "minecraft";
    private static final String FOOD_MOD_ID = "impactsfood";
    private static final int SMALL_STORAGE_CELLS = 4;
    private static final int STORAGE_CELLS = 9;

    private CraftingRecipeAudit() {
    }

    public enum Label {
        REPLACED, GATED, UNTOUCHED, FOOD
    }

    /** One vanilla crafting recipe and what Crafting does with it. */
    public record Entry(ResourceLocation recipeId, Label label, String reason) {
    }

    /** The labeled recipes, and any the audit couldn't label. */
    public record Result(List<Entry> labeled, List<ResourceLocation> unlabeled) {
        public long count(Label label) {
            return labeled.stream().filter(entry -> entry.label() == label).count();
        }
    }

    public static Result audit(ResourceManager resources, RecipeManager recipes, RegistryAccess registryAccess) {
        Map<ResourceLocation, OverhaulCategory> replaced = VanillaRecipeReplacements.all();
        List<Entry> labeled = new ArrayList<>();
        List<ResourceLocation> unlabeled = new ArrayList<>();
        for (ResourceLocation fileLocation : vanillaRecipeFiles(resources)) {
            ResourceLocation recipeId = recipeIdOf(fileLocation);
            Optional<? extends Recipe<?>> recipe = recipes.byKey(recipeId);
            boolean isOtherType = recipe.map(loaded -> loaded.getType() != RecipeType.CRAFTING).orElse(false);
            if (isOtherType) {
                continue;
            }
            Optional<Entry> entry = replaced.containsKey(recipeId)
                    ? Optional.of(new Entry(recipeId, Label.REPLACED, replaced.get(recipeId).configKey))
                    : labelKept(recipeId, fileLocation, recipe, registryAccess);
            entry.ifPresentOrElse(labeled::add, () -> unlabeled.add(recipeId));
        }
        labeled.sort(Comparator.comparing(entry -> entry.recipeId().toString()));
        unlabeled.sort(Comparator.comparing(ResourceLocation::toString));
        return new Result(labeled, unlabeled);
    }

    /** Server start and reload: the label counts, and each unlabeled recipe as a warning. */
    public static void logAudit(ResourceManager resources, RecipeManager recipes, RegistryAccess registryAccess) {
        Result result = audit(resources, recipes, registryAccess);
        LOG.debug(CraftingLogCategories.AUDIT, "vanilla crafting recipes", "replaced", result.count(Label.REPLACED), "gated", result.count(Label.GATED),
                "untouched", result.count(Label.UNTOUCHED), "food", result.count(Label.FOOD), "unlabeled", result.unlabeled().size());
        result.unlabeled().forEach(recipeId -> LOG.warn(CraftingLogCategories.AUDIT, "unlabeled vanilla recipe", "recipe", recipeId));
    }

    private static Optional<Entry> labelKept(ResourceLocation recipeId, ResourceLocation fileLocation, Optional<? extends Recipe<?>> recipe,
                                             RegistryAccess registryAccess) {
        if (recipe.isPresent()) {
            return labelKeptCrafting(recipeId, recipe.get(), registryAccess);
        }
        boolean isFoods = LoaderPlatform.current().modsWithDataFile(fileLocation).contains(FOOD_MOD_ID);
        return Optional.of(new Entry(recipeId, isFoods ? Label.FOOD : Label.UNTOUCHED, isFoods ? "switched off by Food" : "switched off by a data pack"));
    }

    private static Optional<Entry> labelKeptCrafting(ResourceLocation recipeId, Recipe<?> recipe, RegistryAccess registryAccess) {
        ItemStack result = recipe.getResultItem(registryAccess);
        if (KeptVanillaRecipes.isFood(result)) {
            return Optional.of(new Entry(recipeId, Label.FOOD, "food"));
        }
        if (recipe.isSpecial() || !(recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe)) {
            return Optional.of(new Entry(recipeId, Label.UNTOUCHED, "special recipe"));
        }
        CraftingTier tier = TierResolver.tierOf(recipe);
        if (tier != CraftingTier.NON_METAL) {
            return Optional.of(new Entry(recipeId, Label.GATED, CraftingStation.lowestAccepting(tier).serializedName));
        }
        Optional<String> keptReason = KeptVanillaRecipes.reasonFor(recipeId);
        if (keptReason.isPresent()) {
            return Optional.of(new Entry(recipeId, Label.UNTOUCHED, keptReason.get()));
        }
        return isStoragePacking(recipe) ? Optional.of(new Entry(recipeId, Label.UNTOUCHED, "storage block")) : Optional.empty();
    }

    // Packing 4 or 9 of one item into a block (shaped as a full grid, or shapeless), or unpacking one item into several.
    private static boolean isStoragePacking(Recipe<?> recipe) {
        List<Ingredient> ingredients = recipe.getIngredients().stream().filter(ingredient -> !ingredient.isEmpty()).toList();
        boolean isOneOfEachCell = ingredients.size() == recipe.getIngredients().size();
        boolean isUnpacking = recipe instanceof ShapelessRecipe && ingredients.size() == 1;
        boolean isPacking = isOneOfEachCell && (ingredients.size() == SMALL_STORAGE_CELLS || ingredients.size() == STORAGE_CELLS)
                && ingredients.stream().allMatch(ingredient -> sameItems(ingredient, ingredients.get(0)));
        return isUnpacking || isPacking;
    }

    private static boolean sameItems(Ingredient first, Ingredient second) {
        ItemStack[] firstItems = first.getItems();
        ItemStack[] secondItems = second.getItems();
        if (firstItems.length != secondItems.length) {
            return false;
        }
        for (int index = 0; index < firstItems.length; index++) {
            if (!firstItems[index].is(secondItems[index].getItem())) {
                return false;
            }
        }
        return true;
    }

    private static List<ResourceLocation> vanillaRecipeFiles(ResourceManager resources) {
        return List.copyOf(resources.listResources(RECIPES_FOLDER,
                location -> location.getNamespace().equals(MINECRAFT_NAMESPACE) && location.getPath().endsWith(JSON_SUFFIX)).keySet());
    }

    // data/minecraft/recipes/bread.json is the recipe minecraft:bread.
    private static ResourceLocation recipeIdOf(ResourceLocation fileLocation) {
        String path = fileLocation.getPath();
        return ResourceIds.of(fileLocation.getNamespace(), path.substring(RECIPES_FOLDER.length() + 1, path.length() - JSON_SUFFIX.length()));
    }
}
