package com.impact.realisticfood.forge.client;

import com.impact.realisticfood.bucket.CampfireBuckets;
import com.impact.realisticfood.client.BoilingEffects;
import com.impact.realisticfood.client.FoodTooltips;
import com.impact.realisticfood.client.StyledOverlaySource;
import com.impact.realisticfood.stoneboiling.StoneBoilingPots;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/** Food's client-only events on Forge. Loaded only on the physical client. */
public final class FoodForgeClient {
    /** Vanilla's plain water color, for a bucket with no level around it (an item frame, a screen). */
    private static final int DEFAULT_WATER_COLOR = 0x3F76E4;
    private static final int WATER_TINT = 0;

    private FoodForgeClient() {
    }

    public static void register(IEventBus modEventBus) {
        StyledOverlaySource.registerType();
        modEventBus.addListener(FoodForgeClient::onRegisterBlockColors);
        MinecraftForge.EVENT_BUS.addListener(FoodForgeClient::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(FoodForgeClient::onItemTooltip);
    }

    private static void onItemTooltip(ItemTooltipEvent event) {
        FoodTooltips.addLines(event.getItemStack(), event.getToolTip());
    }

    // A campfire bucket's or stone-boiling pot's water takes the biome's water color, as a cauldron's does.
    private static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, position, tintIndex) -> {
            boolean isWater = tintIndex == WATER_TINT && level != null && position != null;
            return isWater ? BiomeColors.getAverageWaterColor(level, position) : DEFAULT_WATER_COLOR;
        }, CampfireBuckets.block(), StoneBoilingPots.block());
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            BoilingEffects.tick();
        }
    }
}
