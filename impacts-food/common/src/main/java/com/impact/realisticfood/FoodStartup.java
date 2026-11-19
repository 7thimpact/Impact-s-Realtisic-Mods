package com.impact.realisticfood;

import com.impact.lib.ImpactsLib;
import com.impact.lib.StartupSummary;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticfood.config.FoodClientConfig;
import com.impact.realisticfood.config.FoodCommonConfig;
import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.debug.FoodLogCategories;

/** Food's startup steps that don't depend on the mod loader. */
public final class FoodStartup {
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);

    private FoodStartup() {
    }

    /** Mod construction: registers with Lib, then the log categories and config files. */
    public static void onModConstruction() {
        ImpactsLib.registerImpactMod(FoodMod.MOD_ID);
        LOG.registerCategories(FoodLogCategories.ALL);
        FoodCommonConfig.register();
        FoodServerConfig.register();
        FoodClientConfig.register();
        ImpactsLib.onModsReady(impactMods -> logStartup());
    }

    /** Which recipe set Food uses: its own vanilla-material recipes, or the versions made from Crafting's parts. */
    public static RecipeSet activeRecipeSet() {
        return ImpactsLib.isLoaded(FoodMod.CRAFTING_MOD_ID) ? RecipeSet.WITH_CRAFTING : RecipeSet.ALONE;
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
