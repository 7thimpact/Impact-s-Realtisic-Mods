package com.impact.realisticcrafting;

import com.impact.lib.ImpactsLib;
import com.impact.lib.StartupSummary;
import com.impact.lib.crafting.CraftingProgress;
import com.impact.lib.crafting.ToolCrafting;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticcrafting.advancement.CraftingAchievements;
import com.impact.realisticcrafting.advancement.CraftingTriggers;
import com.impact.realisticcrafting.config.CraftingClientConfig;
import com.impact.realisticcrafting.config.CraftingCommonConfig;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.config.OverhaulCategory;
import com.impact.realisticcrafting.debug.CraftingDumpSections;
import com.impact.realisticcrafting.debug.CraftingLogCategories;
import com.impact.realisticcrafting.item.CraftingFuels;
import com.impact.realisticcrafting.menu.CraftingMenus;
import com.impact.realisticcrafting.recipe.CraftingRecipeTypes;
import com.impact.realisticcrafting.recipe.CraftingToolCraftingOverrides;
import com.impact.realisticcrafting.recipe.Hammering;
import com.impact.realisticcrafting.registry.CraftingBlocks;
import com.impact.realisticcrafting.registry.CraftingItems;
import com.impact.realisticcrafting.registry.CraftingSounds;
import com.impact.realisticcrafting.timing.CraftTimers;
import com.impact.realisticcrafting.worldgen.CraftingWorldgen;

import java.util.Arrays;

/** Crafting's startup steps that don't depend on the mod loader. */
public final class CraftingStartup {
    private static final ImpactsLog LOG = ImpactsLog.of(CraftingMod.MOD_ID);

    private CraftingStartup() {
    }

    /** Mod construction: registers with Lib, then the log categories, config files, blocks, items and worldgen features. */
    public static void onModConstruction() {
        ImpactsLib.registerImpactMod(CraftingMod.MOD_ID);
        LOG.registerCategories(CraftingLogCategories.ALL);
        CraftingCommonConfig.register();
        CraftingServerConfig.register();
        CraftingClientConfig.register();
        CraftingBlocks.register();
        CraftingItems.register();
        CraftingMenus.register();
        CraftingRecipeTypes.register();
        CraftingSounds.register();
        CraftingWorldgen.register();
        CraftingDumpSections.register();
        ToolCrafting.registerOverrides(CraftingMod.MOD_ID, new CraftingToolCraftingOverrides());
        CraftingProgress.registerSource(CraftingMod.MOD_ID, CraftTimers::progressOf);
        CraftingAchievements.register();
        ImpactsLib.onModsReady(impactMods -> {
            CraftingFuels.register();
            CraftingTriggers.registerAll();
            CraftingItems.looseItems().forEach(Hammering::registerHoldAction);
            logStartup();
        });
    }

    private static void logStartup() {
        StartupSummary.log(CraftingMod.MOD_ID);
        LOG.debug(CraftingLogCategories.STARTUP, "catalog toggles", "on",
                Arrays.stream(OverhaulCategory.values()).filter(OverhaulCategory::isOn).map(category -> category.configKey).toList());
        LOG.debug(CraftingLogCategories.COMPAT, "srf", "found", ImpactsLib.isLoaded(StartupSummary.SALTYS_REALISTIC_FORGING_MOD_ID));
    }
}
