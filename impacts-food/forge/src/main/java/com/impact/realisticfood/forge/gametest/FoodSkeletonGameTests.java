package com.impact.realisticfood.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.FoodStartup;
import com.impact.realisticfood.config.FoodCommonConfig;
import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.debug.FoodLogCategories;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Food milestone 1: the mod registers with Lib, its config files load with the spec's defaults, and Crafting is detected. */
@GameTestHolder(FoodMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FoodSkeletonGameTests {
    static final String LIB_NAMESPACE = ImpactsLib.MOD_ID;
    static final String EMPTY_TEMPLATE = "empty";

    private FoodSkeletonGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void registersWithLib(GameTestHelper helper) {
        helper.assertTrue(ImpactsLib.registeredImpactMods().contains(FoodMod.MOD_ID), "Food should register itself with Lib");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void registersEveryLogCategory(GameTestHelper helper) {
        ImpactsLog log = ImpactsLog.of(FoodMod.MOD_ID);
        FoodLogCategories.ALL.forEach(category ->
                helper.assertTrue(log.registeredCategories().contains(category), "Food should register the log category " + category));
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void detectsCrafting(GameTestHelper helper) {
        FoodStartup.RecipeSet expectedSet = ImpactsLib.isLoaded(FoodMod.CRAFTING_MOD_ID)
                ? FoodStartup.RecipeSet.WITH_CRAFTING
                : FoodStartup.RecipeSet.ALONE;
        helper.assertTrue(FoodStartup.activeRecipeSet() == expectedSet, "Food should pick its recipe set from whether Crafting is installed");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void commonConfigSwitchesDefaultOn(GameTestHelper helper) {
        FoodCommonConfig config = FoodCommonConfig.get();
        helper.assertTrue(config.overhaul.isCuttingOn.defaultValue(), "overhaul.cutting should default to true");
        helper.assertTrue(config.overhaul.isBoilingOn.defaultValue(), "overhaul.boiling should default to true");
        helper.assertTrue(config.overhaul.isSpitOn.defaultValue(), "overhaul.spit should default to true");
        helper.assertTrue(config.overhaul.isBakingOn.defaultValue(), "overhaul.baking should default to true");
        helper.assertTrue(config.overhaul.isStewOverhaulOn.defaultValue(), "overhaul.stews should default to true");
        helper.assertTrue(config.rawFood.isOn.defaultValue(), "rawFood.enabled should default to true");
        helper.assertTrue(config.cooking.areStylesOn.defaultValue(), "cooking.styles should default to true");
        helper.assertTrue(config.cooking.isOvercookingOn.defaultValue(), "cooking.overcooking should default to true");
        helper.assertTrue(config.debug.isDebugEnabled(), "debug.enabled should default to true");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void serverConfigUsesSpecDefaults(GameTestHelper helper) {
        FoodServerConfig config = FoodServerConfig.get();
        helper.assertTrue(config.knives.flintDurability.defaultValue() == 64, "A Flint Knife lasts 64 cuts");
        helper.assertTrue(config.knives.ironDurability.defaultValue() == 250, "An Iron Knife lasts 250 cuts");
        helper.assertTrue(config.cutting.areByproductsOn.defaultValue(), "cutting.byproducts should default to true");
        helper.assertTrue(config.boiling.heatUpTicks.defaultValue() == 200, "boiling.heatUpTicks should default to 200");
        helper.assertTrue(config.boiling.bucketBoils.defaultValue() == 8, "boiling.bucketBoils should default to 8");
        helper.assertTrue(config.boiling.evaporationTicks.defaultValue() == 2400, "boiling.evaporationTicks should default to 2400");
        helper.assertTrue(config.soup.maxBowls.defaultValue() == 6, "soup.maxBowls should default to 6");
        helper.assertTrue(config.spit.speed.defaultValue() == 1.25, "spit.speed should default to 1.25");
        helper.assertTrue(config.spit.turnSpeed.defaultValue() == 1.5, "spit.turnSpeed should default to 1.5");
        helper.assertTrue(config.rawFood.chance.defaultValue() == 0.3, "rawFood.chance should default to 0.3");
        helper.assertTrue(config.rawFood.durationTicks.defaultValue() == 300, "rawFood.durationTicks should default to 300");
        helper.assertTrue(config.styles.dried.eatTicks.defaultValue() == 16, "Dried food eats in 16 ticks");
        helper.assertTrue(config.styles.toasted.hungerBonus.defaultValue() == 1, "Toasted food restores 1 more hunger");
        helper.assertTrue(config.burntFood.burnTicks.defaultValue() == 400, "Burnt Food burns for 400 ticks");
        helper.assertTrue(config.counters.dayInAndDayOutEats.defaultValue() == 100, "Day in and Day out counts to 100");
        helper.succeed();
    }
}
