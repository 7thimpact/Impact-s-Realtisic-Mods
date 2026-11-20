package com.impact.realisticcrafting.forge;

import com.impact.realisticcrafting.loot.PebbleDrops;
import com.impact.realisticcrafting.recipe.Hammering;
import com.impact.realisticcrafting.registry.CraftingItems;
import com.impact.realisticcrafting.timing.CraftDepthTable;
import com.impact.realisticcrafting.timing.CraftTimers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;

/** Crafting's game events on Forge's event bus. */
final class CraftingForgeEvents {
    private CraftingForgeEvents() {
    }

    static void register() {
        // Last, so a break another mod cancels never drops a pebble.
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, CraftingForgeEvents::onBlockBroken);
        MinecraftForge.EVENT_BUS.addListener(CraftingForgeEvents::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(CraftingForgeEvents::onDatapackSync);
        MinecraftForge.EVENT_BUS.addListener(CraftingForgeEvents::onPlayerTick);
    }

    // The depth table and other mods' loose items follow the live recipes: at start and after every /reload.
    private static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        CraftDepthTable.rebuild(server.getRecipeManager(), server.registryAccess());
        Hammering.registerHoldActions(server.getRecipeManager());
    }

    // A player joining gets the recipes synced too; only a reload (no player) changes them.
    private static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            return;
        }
        MinecraftServer server = event.getPlayerList().getServer();
        CraftDepthTable.rebuild(server.getRecipeManager(), server.registryAccess());
        Hammering.registerHoldActions(server.getRecipeManager());
    }

    private static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            CraftTimers.tick(player);
        }
    }

    // Stone broken by hand rolls no loot at all, so its pebble comes from here instead of the loot modifier.
    private static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof Level level)) {
            return;
        }
        int pebbles = PebbleDrops.fromHandBreak(event.getState(), event.getPlayer());
        if (pebbles > 0) {
            Block.popResource(level, event.getPos(), new ItemStack(CraftingItems.stonePebble(), pebbles));
        }
    }
}
