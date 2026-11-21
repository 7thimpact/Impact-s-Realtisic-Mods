package com.impact.realisticfood.food;

import net.minecraft.world.food.FoodProperties;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Food values worked out from config settings, worked out again only when those settings change. {@code S} is a value
 * type (a number or a record) holding every setting the values depend on.
 */
public final class ConfiguredFoodValues<S> {
    private final Supplier<S> currentSettings;
    private final Function<S, FoodProperties> computeFood;
    private volatile Computed<S> computed;

    public ConfiguredFoodValues(Supplier<S> currentSettings, Function<S, FoodProperties> computeFood) {
        this.currentSettings = currentSettings;
        this.computeFood = computeFood;
    }

    public FoodProperties get() {
        S settings = currentSettings.get();
        Computed<S> latest = computed;
        if (latest == null || !latest.settings().equals(settings)) {
            latest = new Computed<>(settings, computeFood.apply(settings));
            computed = latest;
        }
        return latest.food();
    }

    private record Computed<S>(S settings, FoodProperties food) {
    }
}
