package com.impact.realisticcrafting.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.worldgen.CraftingWorldgen;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Worldgen: the loose pebble features, placed with no modifiers (each makes its own tries per chunk), and a biome
 * modifier per feature that adds it to every Overworld biome.
 */
final class CraftingWorldgenData {
    private CraftingWorldgenData() {
    }

    static DatapackBuiltinEntriesProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        RegistrySetBuilder entries = new RegistrySetBuilder()
                .add(Registries.CONFIGURED_FEATURE, CraftingWorldgenData::configuredFeatures)
                .add(Registries.PLACED_FEATURE, CraftingWorldgenData::placedFeatures)
                .add(ForgeRegistries.Keys.BIOME_MODIFIERS, CraftingWorldgenData::biomeModifiers);
        return new DatapackBuiltinEntriesProvider(output, lookupProvider, entries, Set.of(CraftingMod.MOD_ID));
    }

    private static void configuredFeatures(BootstapContext<ConfiguredFeature<?, ?>> context) {
        context.register(CraftingWorldgen.SURFACE_PEBBLES_CONFIGURED,
                new ConfiguredFeature<>(CraftingWorldgen.surfacePebbles(), NoneFeatureConfiguration.INSTANCE));
        context.register(CraftingWorldgen.CAVE_PEBBLES_CONFIGURED,
                new ConfiguredFeature<>(CraftingWorldgen.cavePebbles(), NoneFeatureConfiguration.INSTANCE));
    }

    private static void placedFeatures(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
        context.register(CraftingWorldgen.SURFACE_PEBBLES_PLACED, new PlacedFeature(configured.getOrThrow(CraftingWorldgen.SURFACE_PEBBLES_CONFIGURED), List.of()));
        context.register(CraftingWorldgen.CAVE_PEBBLES_PLACED, new PlacedFeature(configured.getOrThrow(CraftingWorldgen.CAVE_PEBBLES_CONFIGURED), List.of()));
    }

    private static void biomeModifiers(BootstapContext<BiomeModifier> context) {
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        HolderSet<Biome> overworldBiomes = context.lookup(Registries.BIOME).getOrThrow(BiomeTags.IS_OVERWORLD);
        // Surface pebbles go in just before vegetation, so grass and flowers don't take the spots first.
        context.register(biomeModifier("surface_loose_pebbles"), new ForgeBiomeModifiers.AddFeaturesBiomeModifier(overworldBiomes,
                HolderSet.direct(placed.getOrThrow(CraftingWorldgen.SURFACE_PEBBLES_PLACED)), GenerationStep.Decoration.FLUID_SPRINGS));
        context.register(biomeModifier("cave_loose_pebbles"), new ForgeBiomeModifiers.AddFeaturesBiomeModifier(overworldBiomes,
                HolderSet.direct(placed.getOrThrow(CraftingWorldgen.CAVE_PEBBLES_PLACED)), GenerationStep.Decoration.UNDERGROUND_DECORATION));
    }

    private static ResourceKey<BiomeModifier> biomeModifier(String path) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, ResourceIds.of(CraftingMod.MOD_ID, path));
    }
}
