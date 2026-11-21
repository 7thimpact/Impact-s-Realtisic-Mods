package com.impact.realisticinventory.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.pack.InventoryItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Milestone 8, Crafting part: with Impact's Realistic Crafting installed, the packs are sewn from Crafting's parts and
 * the leather-and-string recipes switch off. Crafting's items are looked up by registry ID, never by its classes, so
 * these tests also run (and check the switch the other way) when Crafting isn't installed.
 */
@GameTestHolder(InventoryMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InventoryCraftingGameTests {
    static final String CRAFTING_MOD_ID = "impactscrafting";
    private static final List<String> PACK_NAMES = List.of("satchel", "backpack", "frame_pack");
    private static final String WITH_CRAFTING_SUFFIX = "_with_" + CRAFTING_MOD_ID;
    private static final TagKey<Item> STONE_TIER = TagKey.create(Registries.ITEM, ResourceIds.of(CRAFTING_MOD_ID, "tier/stone"));

    private InventoryCraftingGameTests() {
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void packRecipesSwitchToCraftingParts(GameTestHelper helper) {
        boolean hasCrafting = ImpactsLib.isLoaded(CRAFTING_MOD_ID);
        RecipeManager recipes = helper.getLevel().getRecipeManager();
        for (String packName : PACK_NAMES) {
            boolean hasAloneRecipe = recipes.byKey(ResourceIds.of(InventoryMod.MOD_ID, packName)).isPresent();
            boolean hasCraftingRecipe = recipes.byKey(withCraftingRecipeId(packName)).isPresent();
            helper.assertTrue(hasAloneRecipe != hasCrafting, "The " + packName + " recipe from leather and string loads only without Crafting");
            helper.assertTrue(hasCraftingRecipe == hasCrafting, "The " + packName + " recipe from Crafting's parts loads only with Crafting");
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void sewingASatchelTakesPanelsTwineAndTheNeedle(GameTestHelper helper) {
        if (!ImpactsLib.isLoaded(CRAFTING_MOD_ID)) {
            helper.succeed();
            return;
        }
        CraftingRecipe recipe = craftingRecipe(helper, "satchel");
        CraftingContainer grid = TestCraftingGrids.holding(List.of(crafting("leather_panel"), crafting("leather_panel"), crafting("twine"), crafting("twine"),
                crafting("bone_needle")));
        helper.assertTrue(recipe.matches(grid, helper.getLevel()), "2 Leather Panels and 2 Twine, with the Bone Needle, make a satchel");
        helper.assertTrue(recipe.assemble(grid, helper.getLevel().registryAccess()).is(InventoryItems.satchel()), "The result is a satchel");
        CraftingContainer withoutNeedle = TestCraftingGrids.holding(List.of(crafting("leather_panel"), crafting("leather_panel"), crafting("twine"),
                crafting("twine")));
        helper.assertFalse(recipe.matches(withoutNeedle, helper.getLevel()), "The satchel can't be sewn without the Bone Needle");
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void sewnUpgradesKeepThePacksContentsAndColor(GameTestHelper helper) {
        if (!ImpactsLib.isLoaded(CRAFTING_MOD_ID)) {
            helper.succeed();
            return;
        }
        CraftingContainer backpackGrid = TestCraftingGrids.holding(List.of(InventoryPackGameTests.blueSatchelOfApples(), crafting("leather_panel"),
                crafting("leather_panel"), crafting("chest_top"), crafting("twine"), crafting("bone_needle")));
        CraftingRecipe backpackRecipe = craftingRecipe(helper, "backpack");
        helper.assertTrue(backpackRecipe.matches(backpackGrid, helper.getLevel()), "A satchel, 2 panels, a Chest Top and Twine make a backpack");
        ItemStack backpack = backpackRecipe.assemble(backpackGrid, helper.getLevel().registryAccess());
        helper.assertTrue(backpack.is(InventoryItems.backpack()), "The result is a backpack");
        helper.assertTrue(InventoryPackGameTests.keepsTheSatchelsDyeAndApples(backpack), "The backpack keeps the satchel's dye and contents");
        CraftingContainer framePackGrid = TestCraftingGrids.holding(List.of(backpack, crafting("wooden_frame"), crafting("iron_rod"), crafting("iron_rod"),
                crafting("iron_rivets")));
        CraftingRecipe framePackRecipe = craftingRecipe(helper, "frame_pack");
        helper.assertTrue(framePackRecipe.matches(framePackGrid, helper.getLevel()), "A backpack, a Wooden Frame, 2 Iron Rods and Iron Rivets make a frame pack");
        ItemStack framePack = framePackRecipe.assemble(framePackGrid, helper.getLevel().registryAccess());
        helper.assertTrue(framePack.is(InventoryItems.framePack()), "The result is a frame pack");
        helper.assertTrue(InventoryPackGameTests.keepsTheSatchelsDyeAndApples(framePack), "The frame pack keeps the backpack's dye and contents");
        helper.succeed();
    }

    // Crafting gives a recipe the highest tier among its ingredients, so one stone-tier part puts the recipe on the Stone Table.
    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void onlyTheFramePackNeedsTheStoneTable(GameTestHelper helper) {
        if (!ImpactsLib.isLoaded(CRAFTING_MOD_ID)) {
            helper.succeed();
            return;
        }
        helper.assertTrue(hasStoneTierIngredient(craftingRecipe(helper, "frame_pack")), "The frame pack's Iron Rods and Rivets are stone-tier parts");
        helper.assertFalse(hasStoneTierIngredient(craftingRecipe(helper, "satchel")), "The satchel is sewn at any table");
        helper.assertFalse(hasStoneTierIngredient(craftingRecipe(helper, "backpack")), "The backpack is sewn at any table");
        helper.succeed();
    }

    private static boolean hasStoneTierIngredient(CraftingRecipe recipe) {
        return recipe.getIngredients().stream().filter(ingredient -> !ingredient.isEmpty()).anyMatch(InventoryCraftingGameTests::isStoneTierOnly);
    }

    // An ingredient is as cheap as its cheapest item, so it's stone tier only when every item it accepts is.
    private static boolean isStoneTierOnly(Ingredient ingredient) {
        ItemStack[] accepted = ingredient.getItems();
        return accepted.length > 0 && Arrays.stream(accepted).allMatch(stack -> stack.is(STONE_TIER));
    }

    private static CraftingRecipe craftingRecipe(GameTestHelper helper, String packName) {
        Optional<? extends Recipe<?>> recipe = helper.getLevel().getRecipeManager().byKey(withCraftingRecipeId(packName));
        if (recipe.isEmpty() || !(recipe.get() instanceof CraftingRecipe craftingRecipe)) {
            throw new IllegalStateException("The " + packName + " recipe from Crafting's parts didn't load");
        }
        return craftingRecipe;
    }

    private static ResourceLocation withCraftingRecipeId(String packName) {
        return ResourceIds.of(InventoryMod.MOD_ID, packName + WITH_CRAFTING_SUFFIX);
    }

    private static ItemStack crafting(String itemPath) {
        ResourceLocation itemId = ResourceIds.of(CRAFTING_MOD_ID, itemPath);
        Item item = ResourceIds.item(itemId).orElseThrow(() -> new IllegalStateException("Crafting has no item " + itemId));
        return new ItemStack(item);
    }
}
