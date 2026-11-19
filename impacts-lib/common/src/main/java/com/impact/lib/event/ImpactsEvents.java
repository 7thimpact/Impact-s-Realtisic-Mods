package com.impact.lib.event;

import com.impact.lib.platform.LoaderPlatform;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Lib's shared events. Each one reaches loader-independent listeners registered here, and the loader's own event bus
 * (on Forge, {@code ImpactsWaterFilledEvent}), so any mod can listen the way it prefers.
 */
public final class ImpactsEvents {
    private static final List<Consumer<WaterFilled>> WATER_FILLED_LISTENERS = new CopyOnWriteArrayList<>();

    private ImpactsEvents() {
    }

    public static void onWaterFilled(Consumer<WaterFilled> listener) {
        WATER_FILLED_LISTENERS.add(listener);
    }

    /** Posted by the mod that filled the container, after filling it and before handing it over. */
    public static void postWaterFilled(WaterFilled event) {
        WATER_FILLED_LISTENERS.forEach(listener -> listener.accept(event));
        LoaderPlatform.current().postWaterFilled(event);
    }
}
