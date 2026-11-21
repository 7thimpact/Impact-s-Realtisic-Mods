package com.impact.realisticfood;

import com.impact.lib.ImpactsLib;
import com.impact.lib.StartupSummary;
import com.impact.lib.fuel.CompostRegistry;
import com.impact.lib.fuel.FuelRegistry;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.water.WaterVessels;
import com.impact.realisticfood.advancement.FoodTriggers;
import com.impact.realisticfood.boiling.BoilingRecipes;
import com.impact.realisticfood.boiling.PotBoiling;
import com.impact.realisticfood.bucket.CampfireBucketWaterVessel;
import com.impact.realisticfood.bucket.CampfireBuckets;
import com.impact.realisticfood.config.FoodClientConfig;
import com.impact.realisticfood.config.FoodCommonConfig;
import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.cooking.PieceCooking;
import com.impact.realisticfood.cooking.StyleCooking;
import com.impact.realisticfood.cutting.Cutting;
import com.impact.realisticfood.debug.FoodLogCategories;
import com.impact.realisticfood.food.FixedFood;
import com.impact.realisticfood.network.FoodNetwork;
import com.impact.realisticfood.registry.FoodItems;
import com.impact.realisticfood.registry.FoodSounds;
import com.impact.realisticfood.soup.SoupRecipes;
import com.impact.realisticfood.spit.Spits;
import com.impact.realisticfood.stoneboiling.StoneBoilingPotWaterVessel;
import com.impact.realisticfood.stoneboiling.StoneBoilingPots;

/** Food's startup steps that don't depend on the mod loader. */
public final class FoodStartup {
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);
    /** The spit burns like a stick. */
    private static final int SPIT_BURN_TICKS = 100;

    private FoodStartup() {
    }

    /**
     * Mod construction: registers with Lib, then the log categories, config files, items, cutting, cooking times,
     * boiling recipes, the network channel, Food's answer to Lib's boiling question and its campfire bucket as a water vessel.
     */
    public static void onModConstruction() {
        ImpactsLib.registerImpactMod(FoodMod.MOD_ID);
        LOG.registerCategories(FoodLogCategories.ALL);
        FoodCommonConfig.register();
        FoodServerConfig.register();
        FoodClientConfig.register();
        FoodItems.register();
        FoodSounds.register();
        Cutting.register();
        PieceCooking.register();
        StyleCooking.register();
        registerFuelAndCompost();
        BoilingRecipes.register();
        SoupRecipes.register();
        CampfireBuckets.register();
        StoneBoilingPots.register();
        Spits.register();
        FoodNetwork.create();
        ImpactsLib.registerBoilingProvider(FoodMod.MOD_ID, PotBoiling::isBoiling);
        WaterVessels.registerProvider(FoodMod.MOD_ID, new CampfireBucketWaterVessel());
        WaterVessels.registerProvider(FoodMod.MOD_ID, new StoneBoilingPotWaterVessel());
        ImpactsLib.onModsReady(impactMods -> {
            FuelRegistry.register(FoodItems.fixedFood(FixedFood.BURNT_FOOD), stack -> FoodServerConfig.get().burntFood.burnTicks.get());
            FuelRegistry.register(Spits.spitItem(), SPIT_BURN_TICKS);
            FoodTriggers.registerAll();
            logStartup();
        });
    }

    /** Which recipe set Food uses: its own vanilla-material recipes, or the versions made from Crafting's parts. */
    public static RecipeSet activeRecipeSet() {
        return ImpactsLib.isLoaded(FoodMod.CRAFTING_MOD_ID) ? RecipeSet.WITH_CRAFTING : RecipeSet.ALONE;
    }

    // Compost chances apply once every mod is ready and again at every server start, with the world's settings.
    private static void registerFuelAndCompost() {
        CompostRegistry.register(() -> FoodItems.fixedFood(FixedFood.BURNT_FOOD), () -> FoodServerConfig.get().burntFood.compostChance.get().floatValue());
        CompostRegistry.register(() -> FoodItems.fixedFood(FixedFood.MUSH), () -> FoodServerConfig.get().mush.compostChance.get().floatValue());
    }

    private static void logStartup() {
        StartupSummary.log(FoodMod.MOD_ID);
        LOG.debug(FoodLogCategories.STARTUP, "recipe set", "set", activeRecipeSet().logName);
        LOG.debug(FoodLogCategories.COMPAT, "srf", "found", ImpactsLib.isLoaded(StartupSummary.SALTYS_REALISTIC_FORGING_MOD_ID));
    }

    public enum RecipeSet {
        ALONE("alone"),
        WITH_CRAFTING("crafting");

        public final String logName;

        RecipeSet(String logName) {
            this.logName = logName;
        }
    }
}
