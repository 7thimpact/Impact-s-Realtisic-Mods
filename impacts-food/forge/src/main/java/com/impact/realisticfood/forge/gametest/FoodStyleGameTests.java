package com.impact.realisticfood.forge.gametest;

import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.boiling.BoilingRecipe;
import com.impact.realisticfood.boiling.BoilingRecipes;
import com.impact.realisticfood.boiling.PotKind;
import com.impact.realisticfood.food.FixedFood;
import com.impact.realisticfood.food.FoodBase;
import com.impact.realisticfood.food.FoodPiece;
import com.impact.realisticfood.food.FoodStyle;
import com.impact.realisticfood.registry.FoodItems;
import com.impact.realisticfood.registry.FoodTags;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

/** Food milestone 4: styled food values, every style's recipes, the boiled dead end, Burnt Food and Mush, tags. */
@GameTestHolder(FoodMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FoodStyleGameTests {
    private static final float SATURATION_TOLERANCE = 0.001F;

    private FoodStyleGameTests() {
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void threeStylesOfOneFood(GameTestHelper helper) {
        FoodProperties cookedBeef = FoodCuttingGameTests.foodOf(Items.COOKED_BEEF);
        assertStyle(helper, FoodStyle.DRIED, cookedBeef, 0, 0.75F, 16);
        assertStyle(helper, FoodStyle.TOASTED, cookedBeef, 1, 1.25F, 32);
        assertStyle(helper, FoodStyle.BOILED, cookedBeef, 0, 0.9F, 24);
        helper.assertTrue(FoodCuttingGameTests.foodOf(styled(FoodBase.COOKED_BEEF, FoodStyle.DRIED)).isMeat(), "Beef Jerky is still meat");
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void breadDriesToastsAndBoils(GameTestHelper helper) {
        Item dryBread = cookingResult(helper, RecipeType.SMELTING, Items.BREAD).orElseThrow();
        helper.assertTrue(dryBread == styled(FoodBase.BREAD, FoodStyle.DRIED), "Bread in a furnace gives Dry Bread");
        helper.assertTrue(new ItemStack(dryBread).getUseDuration() == 16, "Dry Bread is eaten in 16 ticks");
        Item toastedBread = cookingResult(helper, RecipeType.CAMPFIRE_COOKING, Items.BREAD).orElseThrow();
        helper.assertTrue(FoodCuttingGameTests.foodOf(toastedBread).getNutrition() == 6, "Toasted Bread restores 6 hunger");
        helper.assertTrue(boilingResult(helper, Items.BREAD).orElseThrow() == styled(FoodBase.BREAD, FoodStyle.BOILED), "Boiled, bread gives Boiled Bread");
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void cookedFoodTakesEveryStyle(GameTestHelper helper) {
        helper.assertTrue(cookingResult(helper, RecipeType.SMELTING, Items.BEEF).orElseThrow() == Items.COOKED_BEEF,
                "Raw beef in a furnace still gives vanilla cooked beef");
        helper.assertTrue(cookingResult(helper, RecipeType.CAMPFIRE_COOKING, Items.BEEF).orElseThrow() == Items.COOKED_BEEF,
                "Raw beef on a campfire still gives vanilla cooked beef");
        helper.assertTrue(cookingResult(helper, RecipeType.SMELTING, Items.COOKED_BEEF).orElseThrow() == styled(FoodBase.COOKED_BEEF, FoodStyle.DRIED),
                "Cooked beef in a furnace gives Beef Jerky");
        helper.assertTrue(cookingResult(helper, RecipeType.CAMPFIRE_COOKING, Items.COOKED_BEEF).orElseThrow() == styled(FoodBase.COOKED_BEEF, FoodStyle.TOASTED),
                "Cooked beef on a campfire gives Fire-Roasted Beef");
        helper.assertTrue(cookingRecipe(helper, RecipeType.CAMPFIRE_COOKING, Items.COOKED_BEEF).orElseThrow().getCookingTime() == 300,
                "Toasting takes 300 ticks");
        helper.assertTrue(cookingRecipe(helper, RecipeType.SMOKING, Items.COOKED_BEEF).orElseThrow().getCookingTime() == 100,
                "Drying in a smoker takes 100 ticks");
        BoilingRecipe rawBoiling = boilingRecipe(helper, Items.BEEF).orElseThrow();
        helper.assertTrue(rawBoiling.result().is(styled(FoodBase.COOKED_BEEF, FoodStyle.BOILED)) && BoilingRecipes.boilTicks(rawBoiling, new ItemStack(Items.BEEF)) == 300,
                "Raw beef boils straight into Boiled Beef in 300 ticks");
        helper.assertTrue(boilingResult(helper, Items.COOKED_BEEF).orElseThrow() == styled(FoodBase.COOKED_BEEF, FoodStyle.BOILED),
                "Cooked beef boils into Boiled Beef");
        helper.assertTrue(cookingResult(helper, RecipeType.CAMPFIRE_COOKING, FoodItems.piece(FoodPiece.COOKED_BEEF_CUT)).orElseThrow()
                == styled(FoodBase.COOKED_BEEF_CUT, FoodStyle.TOASTED), "A Cooked Beef Cut toasts too");
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void boiledFoodIsADeadEnd(GameTestHelper helper) {
        Item boiledBeef = styled(FoodBase.COOKED_BEEF, FoodStyle.BOILED);
        helper.assertTrue(cookingResult(helper, RecipeType.SMELTING, boiledBeef).isEmpty(), "A furnace won't cook Boiled Beef");
        helper.assertTrue(cookingResult(helper, RecipeType.SMOKING, boiledBeef).isEmpty(), "Nor will a smoker");
        helper.assertTrue(cookingResult(helper, RecipeType.CAMPFIRE_COOKING, boiledBeef).isEmpty(), "A campfire won't take it");
        Item mush = FoodItems.fixedFood(FixedFood.MUSH);
        helper.assertTrue(boilingResult(helper, boiledBeef).orElseThrow() == mush, "Boiling it again gives Mush");
        helper.assertTrue(boilingResult(helper, mush).isEmpty(), "Mush itself doesn't change");
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void finishedFoodBurnsAndTurnsToMush(GameTestHelper helper) {
        Item burntFood = FoodItems.fixedFood(FixedFood.BURNT_FOOD);
        helper.assertTrue(cookingResult(helper, RecipeType.SMELTING, styled(FoodBase.BREAD, FoodStyle.DRIED)).orElseThrow() == burntFood,
                "Dry Bread in a furnace burns");
        helper.assertTrue(cookingResult(helper, RecipeType.CAMPFIRE_COOKING, styled(FoodBase.BREAD, FoodStyle.TOASTED)).orElseThrow() == burntFood,
                "Toasted Bread on a campfire burns");
        helper.assertTrue(cookingResult(helper, RecipeType.SMELTING, Items.DRIED_KELP).orElseThrow() == burntFood, "Dried kelp is already dried: it burns");
        helper.assertTrue(cookingResult(helper, RecipeType.CAMPFIRE_COOKING, Items.DRIED_KELP).orElseThrow() == styled(FoodBase.DRIED_KELP, FoodStyle.TOASTED),
                "Dried kelp roasts on a campfire");
        helper.assertTrue(boilingResult(helper, styled(FoodBase.APPLE, FoodStyle.TOASTED)).orElseThrow() == FoodItems.fixedFood(FixedFood.MUSH),
                "A Roasted Apple boils into Mush");
        helper.assertTrue(boilingResult(helper, burntFood).orElseThrow() == FoodItems.fixedFood(FixedFood.MUSH), "Burnt Food boils into Mush");
        helper.assertTrue(ForgeHooks.getBurnTime(new ItemStack(burntFood), null) == 400, "Burnt Food burns for 400 ticks");
        helper.assertTrue(ComposterBlock.COMPOSTABLES.getFloat(FoodItems.fixedFood(FixedFood.MUSH)) == 0.65F, "Mush composts at 65%");
        helper.assertTrue(FoodCuttingGameTests.foodOf(burntFood).getEffects().stream().anyMatch(effect -> effect.getFirst().getEffect() == MobEffects.CONFUSION),
                "Burnt Food can give Nausea");
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void newRawFoodsAndEggsCook(GameTestHelper helper) {
        helper.assertTrue(cookingResult(helper, RecipeType.SMELTING, Items.TROPICAL_FISH).orElseThrow() == FoodItems.fixedFood(FixedFood.COOKED_TROPICAL_FISH),
                "Tropical fish cooks into Cooked Tropical Fish");
        helper.assertTrue(cookingResult(helper, RecipeType.CAMPFIRE_COOKING, Items.ROTTEN_FLESH).orElseThrow() == FoodItems.fixedFood(FixedFood.COOKED_ROTTEN_FLESH),
                "Rotten flesh cooks into Cooked Rotten Flesh");
        helper.assertTrue(cookingResult(helper, RecipeType.SMELTING, Items.EGG).isEmpty(), "An egg would crack in a furnace");
        helper.assertTrue(cookingResult(helper, RecipeType.CAMPFIRE_COOKING, Items.EGG).orElseThrow() == FoodItems.fixedFood(FixedFood.FRIED_EGG),
                "An egg fries on a campfire");
        float rottenChance = hungerChance(FoodCuttingGameTests.foodOf(FoodItems.fixedFood(FixedFood.COOKED_ROTTEN_FLESH)));
        float boiledRottenChance = hungerChance(FoodCuttingGameTests.foodOf(styled(FoodBase.COOKED_ROTTEN_FLESH, FoodStyle.BOILED)));
        helper.assertTrue(Math.abs(rottenChance - 0.1F) < SATURATION_TOLERANCE, "Cooked Rotten Flesh gives Hunger 10% of the time");
        helper.assertTrue(Math.abs(boiledRottenChance - 0.05F) < SATURATION_TOLERANCE, "Boiling halves that chance");
        helper.succeed();
    }

    @GameTest(templateNamespace = FoodSkeletonGameTests.LIB_NAMESPACE, template = FoodSkeletonGameTests.EMPTY_TEMPLATE)
    public static void styledFoodsHaveTheirTags(GameTestHelper helper) {
        Item toastedCod = styled(FoodBase.COOKED_COD, FoodStyle.TOASTED);
        helper.assertTrue(new ItemStack(toastedCod).is(FoodTags.TOASTED_FOODS) && new ItemStack(toastedCod).is(FoodTags.STYLED_FOODS),
                "Fire-Roasted Cod is a toasted, styled food");
        helper.assertTrue(new ItemStack(toastedCod).is(ItemTags.FISHES), "It joins its base food's tags, like minecraft:fishes");
        helper.assertTrue(new ItemStack(FoodItems.fixedFood(FixedFood.MUSH)).is(FoodTags.FINISHED_FOODS), "Mush is a finished food");
        helper.assertTrue(FoodItems.styledFoods(FoodStyle.DRIED).size() == FoodBase.values().length - 1, "Every base but dried kelp has a dried version");
        helper.assertTrue(FoodItems.styledFoods(FoodStyle.TOASTED).size() == FoodBase.values().length, "Every base has a toasted version");
        helper.succeed();
    }

    private static void assertStyle(GameTestHelper helper, FoodStyle style, FoodProperties base, int hungerBonus, float saturationMultiplier, int eatTicks) {
        Item item = styled(FoodBase.COOKED_BEEF, style);
        FoodProperties food = FoodCuttingGameTests.foodOf(item);
        float baseSaturation = base.getNutrition() * base.getSaturationModifier() * 2.0F;
        float saturation = food.getNutrition() * food.getSaturationModifier() * 2.0F;
        helper.assertTrue(food.getNutrition() == base.getNutrition() + hungerBonus, style.prefix + " hunger should be the base's plus " + hungerBonus);
        helper.assertTrue(Math.abs(saturation - baseSaturation * saturationMultiplier) < SATURATION_TOLERANCE,
                style.prefix + " saturation should be the base's times " + saturationMultiplier);
        helper.assertTrue(new ItemStack(item).getUseDuration() == eatTicks, style.prefix + " food is eaten in " + eatTicks + " ticks");
    }

    private static float hungerChance(FoodProperties food) {
        return food.getEffects().stream().filter(effect -> effect.getFirst().getEffect() == MobEffects.HUNGER)
                .map(effect -> effect.getSecond()).findFirst().orElse(0.0F);
    }

    private static Item styled(FoodBase base, FoodStyle style) {
        return FoodItems.styled(base, style).orElseThrow();
    }

    private static <R extends AbstractCookingRecipe> Optional<R> cookingRecipe(GameTestHelper helper, RecipeType<R> type, Item input) {
        return helper.getLevel().getRecipeManager().getRecipeFor(type, new SimpleContainer(new ItemStack(input)), helper.getLevel());
    }

    private static <R extends AbstractCookingRecipe> Optional<Item> cookingResult(GameTestHelper helper, RecipeType<R> type, Item input) {
        return cookingRecipe(helper, type, input).map(recipe -> recipe.getResultItem(helper.getLevel().registryAccess()).getItem());
    }

    private static Optional<BoilingRecipe> boilingRecipe(GameTestHelper helper, Item input) {
        return BoilingRecipes.find(helper.getLevel(), new ItemStack(input), PotKind.CAULDRON);
    }

    private static Optional<Item> boilingResult(GameTestHelper helper, Item input) {
        return boilingRecipe(helper, input).map(recipe -> recipe.result().getItem());
    }
}
