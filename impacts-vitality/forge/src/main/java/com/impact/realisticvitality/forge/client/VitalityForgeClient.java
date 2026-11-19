package com.impact.realisticvitality.forge.client;

import com.impact.realisticvitality.client.ClientVitality;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

/** Vitality's client-only hooks on Forge. Loaded only on the physical client. */
public final class VitalityForgeClient {
    private VitalityForgeClient() {
    }

    public static void register(IEventBus modEventBus) {
        MinecraftForge.EVENT_BUS.addListener(VitalityForgeClient::onLoggingOut);
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientVitality.reset();
    }
}
