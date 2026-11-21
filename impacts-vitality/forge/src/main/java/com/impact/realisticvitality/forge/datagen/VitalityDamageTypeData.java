package com.impact.realisticvitality.forge.datagen;

import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.registry.VitalityDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.DataPackRegistriesHooks;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Vitality's damage types and their vanilla tags. Dehydration is like starving; heatstroke like freezing; thin air and
 * fumes like drowning. None of them is reduced by armor.
 */
final class VitalityDamageTypeData {
    private static final float NO_EXHAUSTION = 0.0F;

    private VitalityDamageTypeData() {
    }

    /** The world's registries with Vitality's damage types added, for both the damage type files and their tags. */
    static CompletableFuture<HolderLookup.Provider> withDamageTypes(CompletableFuture<HolderLookup.Provider> lookupProvider) {
        return lookupProvider.thenApply(VitalityDamageTypeData::addDamageTypes);
    }

    static DatapackBuiltinEntriesProvider damageTypes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupWithDamageTypes) {
        return new DatapackBuiltinEntriesProvider(output, lookupWithDamageTypes, Set.of(VitalityMod.MOD_ID));
    }

    // Every other data pack registry gets an empty entry so its existing objects aren't written out again.
    private static HolderLookup.Provider addDamageTypes(HolderLookup.Provider original) {
        RegistrySetBuilder registries = new RegistrySetBuilder().add(Registries.DAMAGE_TYPE, VitalityDamageTypeData::registerDamageTypes);
        DataPackRegistriesHooks.getDataPackRegistriesWithDimensions()
                .filter(registryData -> !registryData.key().equals(Registries.DAMAGE_TYPE))
                .forEach(registryData -> registries.add(registryData.key(), context -> { }));
        return registries.buildPatch(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), original);
    }

    private static void registerDamageTypes(BootstapContext<DamageType> context) {
        context.register(VitalityDamageTypes.DEHYDRATION, damageType(VitalityDamageTypes.DEHYDRATION, DamageEffects.HURT));
        context.register(VitalityDamageTypes.HEATSTROKE, damageType(VitalityDamageTypes.HEATSTROKE, DamageEffects.BURNING));
        context.register(VitalityDamageTypes.THIN_AIR, damageType(VitalityDamageTypes.THIN_AIR, DamageEffects.DROWNING));
        context.register(VitalityDamageTypes.FUMES, damageType(VitalityDamageTypes.FUMES, DamageEffects.DROWNING));
    }

    private static DamageType damageType(ResourceKey<DamageType> key, DamageEffects effects) {
        return new DamageType(VitalityDamageTypes.messageId(key), DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, NO_EXHAUSTION, effects);
    }

    static final class Tags extends TagsProvider<DamageType> {
        Tags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFiles) {
            super(output, Registries.DAMAGE_TYPE, lookupProvider, VitalityMod.MOD_ID, existingFiles);
        }

        @Override
        protected void addTags(HolderLookup.Provider lookup) {
            tag(DamageTypeTags.BYPASSES_ARMOR).add(VitalityDamageTypes.DEHYDRATION, VitalityDamageTypes.HEATSTROKE,
                    VitalityDamageTypes.THIN_AIR, VitalityDamageTypes.FUMES);
            tag(DamageTypeTags.BYPASSES_EFFECTS).add(VitalityDamageTypes.DEHYDRATION);
            tag(DamageTypeTags.NO_IMPACT).add(VitalityDamageTypes.THIN_AIR, VitalityDamageTypes.FUMES);
        }
    }
}
