package com.impact.lib.forge;

import com.impact.lib.ImpactsLib;
import com.impact.lib.LibStartup;
import com.impact.lib.advancement.LoneLibAchievement;
import com.impact.lib.command.ImpactsCommands;
import com.impact.lib.forge.condition.NoImpactModsCondition;
import com.impact.lib.forge.datagen.LibDataGenerators;
import com.impact.lib.forge.event.ImpactsModsReadyEvent;
import com.impact.lib.log.ImpactsLog;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/** Impact's Lib on Forge: connects the loader-independent parts of Lib to Forge's events. */
@Mod(ImpactsLib.MOD_ID)
public final class ImpactsLibForge {
    public ImpactsLibForge(FMLJavaModLoadingContext context) {
        ForgeModConstruction.run(context, LibStartup::onModConstruction);
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(ImpactsLibForge::onCommonSetup);
        modEventBus.addListener(ImpactsLibForge::onRegister);
        modEventBus.addListener(LibDataGenerators::onGatherData);
        MinecraftForge.EVENT_BUS.addListener(ImpactsLibForge::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(ImpactsLibForge::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(ImpactsLibForge::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(ImpactsLibForge::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(ImpactsLibForge::onServerStopped);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        // Enqueued work runs on the main thread after every mod's common setup has finished.
        event.enqueueWork(ImpactsLibForge::announceModsReady);
    }

    private static void announceModsReady() {
        LibStartup.onModsReady();
        MinecraftForge.EVENT_BUS.post(new ImpactsModsReadyEvent(ImpactsLib.registeredImpactMods()));
    }

    private static void onRegister(RegisterEvent event) {
        // Forge asks for condition serializers to be registered while recipe serializers register.
        if (!event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS)) {
            return;
        }
        CraftingHelper.register(NoImpactModsCondition.Serializer.INSTANCE);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        ImpactsCommands.register(event.getDispatcher());
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        LoneLibAchievement.grantOnLogin(player);
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
