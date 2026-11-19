package com.impact.realisticfood.forge.client;

import com.impact.realisticfood.client.BoilingEffects;
import com.impact.realisticfood.client.StyledOverlaySource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

/** Food's client-only events on Forge. Loaded only on the physical client. */
public final class FoodForgeClient {
    private FoodForgeClient() {
    }

    public static void register() {
        StyledOverlaySource.registerType();
        MinecraftForge.EVENT_BUS.addListener(FoodForgeClient::onClientTick);
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            BoilingEffects.tick();
        }
    }
}
