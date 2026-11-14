package com.impact.lib.forge;

import com.impact.lib.ImpactsLib;
import com.impact.lib.LibStartup;
import com.impact.lib.forge.event.ImpactsModsReadyEvent;
import com.impact.lib.log.ImpactsLog;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/** Impact's Lib on Forge: connects the loader-independent startup steps to Forge's events. */
@Mod(ImpactsLib.MOD_ID)
public final class ImpactsLibForge {
    public ImpactsLibForge(FMLJavaModLoadingContext context) {
        ForgeModConstruction.run(context, LibStartup::onModConstruction);
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(ImpactsLibForge::onCommonSetup);
        MinecraftForge.EVENT_BUS.addListener(ImpactsLibForge::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(ImpactsLibForge::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(ImpactsLibForge::onServerStopped);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        // Enqueued work runs after every mod's common setup has finished.
        event.enqueueWork(ImpactsLibForge::announceModsReady);
    }

    private static void announceModsReady() {
        LibStartup.onModsReady();
        MinecraftForge.EVENT_BUS.post(new ImpactsModsReadyEvent(ImpactsLib.registeredImpactMods()));
    }

    private static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ImpactsLog.flushExpiredRepeats();
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ImpactsLog.flushExpiredRepeats();
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        ImpactsLog.flushAllRepeats();
    }
}
