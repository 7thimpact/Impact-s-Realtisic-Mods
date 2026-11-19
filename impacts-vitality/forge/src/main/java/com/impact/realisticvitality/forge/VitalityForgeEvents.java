package com.impact.realisticvitality.forge;

import com.impact.realisticvitality.player.VitalityPlayerTicks;
import com.impact.realisticvitality.player.VitalitySync;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

/** Vitality's game events on Forge's event bus. */
final class VitalityForgeEvents {
    private VitalityForgeEvents() {
    }

    static void register() {
        MinecraftForge.EVENT_BUS.addListener(VitalityForgeEvents::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(VitalityForgeEvents::onPlayerLoggedIn);
    }

    private static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            VitalityPlayerTicks.tick(player);
        }
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            VitalitySync.onLogin(player);
        }
    }
}
