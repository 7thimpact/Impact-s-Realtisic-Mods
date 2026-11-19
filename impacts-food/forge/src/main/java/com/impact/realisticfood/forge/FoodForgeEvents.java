package com.impact.realisticfood.forge;

import com.impact.realisticfood.boiling.CauldronInteractions;
import com.impact.realisticfood.boiling.CauldronPots;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;

/** Food's game events on Forge's event bus. */
final class FoodForgeEvents {
    private FoodForgeEvents() {
    }

    static void register() {
        MinecraftForge.EVENT_BUS.addListener(FoodForgeEvents::onLevelTick);
        // Early, so no other mod fills a bottle from a soup; every other click is left alone.
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, FoodForgeEvents::onRightClickBlock);
    }

    private static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel) {
            CauldronPots.tickAll(serverLevel);
        }
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (CauldronInteractions.isBottleRefused(event.getLevel(), event.getPos(), event.getItemStack())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }
}
