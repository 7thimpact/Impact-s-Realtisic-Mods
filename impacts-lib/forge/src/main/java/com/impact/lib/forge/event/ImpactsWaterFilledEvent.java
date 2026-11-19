package com.impact.lib.forge.event;

import com.impact.lib.event.WaterFilled;
import net.minecraftforge.eventbus.api.Event;

/**
 * Lib's water-filled event on Forge's event bus ({@code ImpactsEvents.WaterFilled} in Lib's spec). Listeners may add
 * their own namespaced NBT to {@code details().filledStack()}.
 */
public final class ImpactsWaterFilledEvent extends Event {
    private final WaterFilled details;

    public ImpactsWaterFilledEvent(WaterFilled details) {
        this.details = details;
    }

    public WaterFilled details() {
        return details;
    }
}
