package com.impact.realisticcrafting.worldgen;

import com.impact.lib.ResourceIds;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticcrafting.CraftingMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.function.Supplier;

/** Crafting's worldgen: the loose pebble features, and the keys of their configured and placed features (written by datagen). */
public final class CraftingWorldgen {
    public static final ResourceKey<ConfiguredFeature<?, ?>> SURFACE_PEBBLES_CONFIGURED = configured("surface_loose_pebbles");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CAVE_PEBBLES_CONFIGURED = configured("cave_loose_pebbles");
    public static final ResourceKey<PlacedFeature> SURFACE_PEBBLES_PLACED = placed("surface_loose_pebbles");
    public static final ResourceKey<PlacedFeature> CAVE_PEBBLES_PLACED = placed("cave_loose_pebbles");
    private static volatile Supplier<Feature<NoneFeatureConfiguration>> surfacePebbles;
    private static volatile Supplier<Feature<NoneFeatureConfiguration>> cavePebbles;

    private CraftingWorldgen() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(CraftingMod.MOD_ID);
        surfacePebbles = registrar.register(Registries.FEATURE, "surface_loose_pebbles", () -> new LoosePebbleFeature(LoosePebbleFeature.Spot.SURFACE));
        cavePebbles = registrar.register(Registries.FEATURE, "cave_loose_pebbles", () -> new LoosePebbleFeature(LoosePebbleFeature.Spot.CAVE));
    }

    public static Feature<NoneFeatureConfiguration> surfacePebbles() {
        return surfacePebbles.get();
    }

    public static Feature<NoneFeatureConfiguration> cavePebbles() {
        return cavePebbles.get();
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> configured(String path) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceIds.of(CraftingMod.MOD_ID, path));
    }

    private static ResourceKey<PlacedFeature> placed(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceIds.of(CraftingMod.MOD_ID, path));
    }
}
