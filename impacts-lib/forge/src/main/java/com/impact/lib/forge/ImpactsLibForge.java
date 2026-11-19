package com.impact.lib.forge;

import com.impact.lib.ImpactsLib;
import com.impact.lib.LibStartup;
import com.impact.lib.advancement.LoneLibAchievement;
import com.impact.lib.audit.RecipeChecks;
import com.impact.lib.command.ImpactsCommands;
import com.impact.lib.forge.client.LibForgeClient;
import com.impact.lib.forge.condition.NoImpactModsCondition;
import com.impact.lib.forge.config.ServerConfigResync;
import com.impact.lib.forge.crafting.WaterBottleIngredient;
import com.impact.lib.forge.datagen.LibDataGenerators;
import com.impact.lib.forge.event.ImpactsModsReadyEvent;
import com.impact.lib.fuel.CompostRegistry;
import com.impact.lib.fuel.FuelRegistry;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.network.LibNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/** Impact's Lib on Forge: connects the loader-independent parts of Lib to Forge's events. */
@Mod(ImpactsLib.MOD_ID)
public final class ImpactsLibForge {
    public ImpactsLibForge(FMLJavaModLoadingContext context) {
        ForgeModConstruction.run(context, LibStartup::onModConstruction);
        ServerConfigResync.register(LibNetwork.channel());
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(ImpactsLibForge::onCommonSetup);
        modEventBus.addListener(ImpactsLibForge::onRegister);
        modEventBus.addListener(LibDataGenerators::onGatherData);
        if (FMLEnvironment.dist.isClient()) {
            LibForgeClient.register(modEventBus);
        }
        MinecraftForge.EVENT_BUS.addListener(ImpactsLibForge::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(ImpactsLibForge::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(ImpactsLibForge::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(ImpactsLibForge::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(ImpactsLibForge::onServerStopped);
        MinecraftForge.EVENT_BUS.addListener(ImpactsLibForge::onFuelBurnTime);
        MinecraftForge.EVENT_BUS.addListener(ImpactsLibForge::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(ImpactsLibForge::onDatapackSync);
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
        CraftingHelper.register(WaterBottleIngredient.ID, WaterBottleIngredient.Serializer.INSTANCE);
    }

    /** The recipe audit and conflict check run on the live recipe set at every start... */
    private static void onServerStarted(ServerStartedEvent event) {
        CompostRegistry.applyAll();
        RecipeChecks.runAndLog(event.getServer());
    }

    /** ...and after every /reload (the sync without a player is the one sent to everyone). */
    private static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            RecipeChecks.runAndLog(event.getPlayerList().getServer());
        }
    }

    /** Impact items burn for the time Lib's fuel registry decides; anything it doesn't know is left to others. */
    private static void onFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        FuelRegistry.burnTicks(event.getItemStack()).ifPresent(event::setBurnTime);
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
