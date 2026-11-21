package com.impact.realisticcrafting.forge.gametest;

import com.impact.lib.ResourceIds;
import com.impact.lib.crafting.ToolCraftingRecipe;
import com.impact.lib.grinding.Grinding;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.item.BucketMold;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.CraftingTool;
import com.impact.realisticcrafting.item.ToolKind;
import com.impact.realisticcrafting.item.ToolMaterial;
import com.impact.realisticcrafting.item.WoodPart;
import com.impact.realisticcrafting.recipe.CraftingRecipeAudit;
import com.impact.realisticcrafting.recipe.CraftingRecipeTypes;
import com.impact.realisticcrafting.recipe.HammeringRecipe;
import com.impact.realisticcrafting.registry.CraftingItems;
import com.impact.realisticcrafting.timing.CraftDepthTable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Crafting milestone 14: the recipe audit labels every vanilla crafting recipe, and the tree check holds. */
@GameTestHolder(CraftingMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CraftingAuditGameTests {
    private static final String LIB_NAMESPACE = CraftingSkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = CraftingSkeletonGameTests.EMPTY_TEMPLATE;

    private CraftingAuditGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void everyVanillaRecipeIsLabeled(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        CraftingRecipeAudit.Result audit = CraftingRecipeAudit.audit(server.getResourceManager(), server.getRecipeManager(), server.registryAccess());
        helper.assertTrue(audit.count(CraftingRecipeAudit.Label.REPLACED) > 0, "The audit finds Crafting's replacements");
        helper.assertTrue(audit.unlabeled().isEmpty(), audit.unlabeled().size() + " vanilla crafting recipes are unlabeled: " + audit.unlabeled());
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void theCraftingTreeHoldsTogether(GameTestHelper helper) {
        RecipeManager recipes = helper.getLevel().getRecipeManager();
        List<String> onFallback = recipes.getAllRecipesFor(RecipeType.CRAFTING).stream()
                .filter(recipe -> recipe.getId().getNamespace().equals(CraftingMod.MOD_ID))
                .filter(recipe -> CraftDepthTable.recipeDepth(recipe).isEmpty())
                .map(recipe -> recipe.getId().getPath())
                .toList();
        helper.assertTrue(onFallback.isEmpty(), "No Crafting recipe falls back to the default time: " + onFallback);
        Set<Item> usedItems = usedItems(recipes);
        List<String> unused = libraryParts().stream().filter(part -> !usedItems.contains(part)).map(part -> ResourceIds.itemId(part).getPath()).toList();
        helper.assertTrue(unused.isEmpty(), "Every library part goes on to make something: " + unused);
        Set<Item> library = new HashSet<>(libraryParts());
        library.addAll(knownNonParts());
        List<String> strangers = usedItems.stream()
                .filter(item -> ResourceIds.itemId(item).getNamespace().equals(CraftingMod.MOD_ID) && !library.contains(item))
                .map(item -> ResourceIds.itemId(item).getPath())
                .toList();
        helper.assertTrue(strangers.isEmpty(), "Recipes use only parts from the library: " + strangers);
        helper.succeed();
    }

    // Every ingredient and tool of every crafting, hammering, vat, grinding and furnace recipe.
    private static Set<Item> usedItems(RecipeManager recipes) {
        List<Recipe<?>> everyRecipe = new ArrayList<>(recipes.getAllRecipesFor(RecipeType.CRAFTING));
        everyRecipe.addAll(recipes.getAllRecipesFor(CraftingRecipeTypes.hammering()));
        everyRecipe.addAll(recipes.getAllRecipesFor(CraftingRecipeTypes.vatDyeing()));
        everyRecipe.addAll(recipes.getAllRecipesFor(Grinding.recipeType()));
        everyRecipe.addAll(recipes.getAllRecipesFor(RecipeType.SMELTING));
        everyRecipe.addAll(recipes.getAllRecipesFor(RecipeType.BLASTING));
        Set<Item> used = new HashSet<>();
        everyRecipe.forEach(recipe -> addIngredients(used, recipe));
        return used;
    }

    private static void addIngredients(Set<Item> used, Recipe<?> recipe) {
        List<Ingredient> ingredients = new ArrayList<>(recipe.getIngredients());
        if (recipe instanceof ToolCraftingRecipe toolRecipe) {
            ingredients.addAll(toolRecipe.tools());
        }
        if (recipe instanceof HammeringRecipe hammering) {
            ingredients.add(hammering.input());
            ingredients.add(hammering.hammer());
        }
        ingredients.forEach(ingredient -> Arrays.stream(ingredient.getItems()).forEach(stack -> used.add(stack.getItem())));
    }

    // The Parts and tools library: every part (Wood Scraps are a byproduct, not a part), per-wood part, tool and textile.
    private static List<Item> libraryParts() {
        List<Item> parts = new ArrayList<>();
        Arrays.stream(CraftingPart.values()).filter(part -> part != CraftingPart.WOOD_SCRAPS).forEach(part -> parts.add(CraftingItems.part(part)));
        Arrays.stream(WoodPart.values()).forEach(part -> parts.add(CraftingItems.woodPart(part)));
        Arrays.stream(CraftingTool.values()).forEach(tool -> parts.add(CraftingItems.tool(tool)));
        parts.addAll(CraftingItems.pebbles());
        Arrays.stream(BucketMold.values()).filter(stage -> stage != BucketMold.MOLTEN).forEach(stage -> parts.add(CraftingItems.bucketMold(stage)));
        parts.add(CraftingItems.woodenBucket());
        parts.add(CraftingItems.bannerCloth(DyeColor.WHITE));
        for (ToolMaterial material : ToolMaterial.values()) {
            Arrays.stream(ToolKind.values()).forEach(kind -> parts.add(CraftingItems.toolPart(material, kind)));
        }
        return parts;
    }

    // Crafting items a recipe may take that aren't parts: loose items, filled buckets, the table being upgraded, every color of cloth.
    private static List<Item> knownNonParts() {
        List<Item> items = new ArrayList<>(CraftingItems.looseItems());
        items.add(CraftingItems.woodenWaterBucket());
        items.add(CraftingItems.comforter());
        items.add(CraftingItems.bedFrame());
        items.addAll(CraftingItems.sheets());
        items.addAll(CraftingItems.bannerCloths());
        Arrays.stream(MakeshiftTable.values()).forEach(table -> items.add(CraftingItems.table(table)));
        items.add(CraftingItems.part(CraftingPart.WOOD_SCRAPS));
        // Cooled in water in the world, not in a recipe.
        items.add(CraftingItems.bucketMold(BucketMold.MOLTEN));
        return items;
    }
}
