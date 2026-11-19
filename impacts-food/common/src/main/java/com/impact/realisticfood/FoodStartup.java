package com.impact.realisticfood;

import com.impact.lib.ImpactsLib;
import com.impact.lib.StartupSummary;
import com.impact.lib.fuel.CompostRegistry;
import com.impact.lib.fuel.FuelRegistry;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticfood.boiling.BoilingRecipes;
import com.impact.realisticfood.boiling.PotBoiling;
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

/** Food's startup steps that don't depend on the mod loader. */
public final class FoodStartup {
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);

    private FoodStartup() {
    }

    /**
     * Mod construction: registers with Lib, then the log categories, config files, items, cutting, cooking times,
     * boiling recipes, the network channel and Food's answer to Lib's boiling question.
     */
    public static void onModConstruction() {
        ImpactsLib.registerImpactMod(FoodMod.MOD_ID);
        LOG.registerCategories(FoodLogCategories.ALL);
        FoodCommonConfig.register();
        FoodServerConfig.register();
        FoodClientConfig.register();
        FoodItems.register();
        Cutting.register();
        PieceCooking.register();
        StyleCooking.register();
        registerFuelAndCompost();
        BoilingRecipes.register();
        FoodNetwork.create();
        ImpactsLib.registerBoilingProvider(FoodMod.MOD_ID, PotBoiling::isBoiling);
        ImpactsLib.onModsReady(impactMods -> {
            FuelRegistry.register(FoodItems.fixedFood(FixedFood.BURNT_FOOD), stack -> FoodServerConfig.get().burntFood.burnTicks.get());
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
