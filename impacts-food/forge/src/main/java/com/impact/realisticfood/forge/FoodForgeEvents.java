package com.impact.realisticfood.forge;

import com.impact.realisticfood.boiling.CauldronPots;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

/** Food's game events on Forge's event bus. */
final class FoodForgeEvents {
    private FoodForgeEvents() {
    }

    static void register() {
        MinecraftForge.EVENT_BUS.addListener(FoodForgeEvents::onLevelTick);
    }

    private static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel) {
            CauldronPots.tickAll(serverLevel);
        }
    }
}
