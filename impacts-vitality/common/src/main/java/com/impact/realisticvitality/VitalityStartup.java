package com.impact.realisticvitality;

import com.impact.lib.ImpactsLib;
import com.impact.lib.StartupSummary;
import com.impact.lib.command.ImpactsCommands;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.advancement.VitalityTriggers;
import com.impact.realisticvitality.command.VitalityCommand;
import com.impact.realisticvitality.config.VitalityClientConfig;
import com.impact.realisticvitality.config.VitalityCommonConfig;
import com.impact.realisticvitality.config.VitalityNeed;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.network.VitalityNetwork;
import com.impact.realisticvitality.player.VitalityState;
import com.impact.realisticvitality.registry.VitalityBlocks;
import com.impact.realisticvitality.registry.VitalityEffects;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.registry.VitalityRecipeSerializers;
import com.impact.realisticvitality.thirst.BottleFilling;

/** Vitality's startup steps that don't depend on the mod loader. */
public final class VitalityStartup {
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);

    private VitalityStartup() {
    }

    /** Mod construction: registers with Lib, then the log categories, config files, the player state, effects, blocks, items, the network channel and the {@code /impacts vitality} command. */
    public static void onModConstruction() {
        ImpactsLib.registerImpactMod(VitalityMod.MOD_ID);
        LOG.registerCategories(VitalityLogCategories.ALL);
        VitalityCommonConfig.register();
        VitalityServerConfig.register();
        VitalityClientConfig.register();
        VitalityState.register();
        VitalityEffects.register();
        VitalityBlocks.register();
        VitalityItems.register();
        VitalityRecipeSerializers.register();
        VitalityNetwork.create();
        ImpactsCommands.addSubcommand(VitalityCommand::build);
        ImpactsLib.onModsReady(impactMods -> {
            BottleFilling.registerInteractions();
            VitalityTriggers.registerAll();
            logStartup();
        });
    }

    private static void logStartup() {
        StartupSummary.log(VitalityMod.MOD_ID);
        VitalityCommonConfig config = VitalityCommonConfig.get();
        LOG.debug(VitalityLogCategories.STARTUP, "needs",
                "thirst", config.isOn(VitalityNeed.THIRST),
                "temperature", config.isOn(VitalityNeed.TEMPERATURE),
                "air", config.isOn(VitalityNeed.AIR));
        LOG.debug(VitalityLogCategories.COMPAT, "srf", "found", ImpactsLib.isLoaded(StartupSummary.SALTYS_REALISTIC_FORGING_MOD_ID));
    }
}
