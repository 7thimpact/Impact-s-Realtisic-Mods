package com.impact.realisticvitality.registry;

import com.impact.lib.ResourceIds;
import com.impact.realisticvitality.VitalityMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

import java.util.List;

/** Vitality's damage types. Their JSON comes from data generation; cold damage uses vanilla's {@code freeze}. */
public final class VitalityDamageTypes {
    public static final ResourceKey<DamageType> DEHYDRATION = key("dehydration");
    public static final ResourceKey<DamageType> HEATSTROKE = key("heatstroke");
    public static final ResourceKey<DamageType> THIN_AIR = key("thin_air");
    public static final ResourceKey<DamageType> FUMES = key("fumes");
    public static final List<ResourceKey<DamageType>> ALL = List.of(DEHYDRATION, HEATSTROKE, THIN_AIR, FUMES);

    private VitalityDamageTypes() {
    }

    public static DamageSource source(Entity victim, ResourceKey<DamageType> type) {
        return new DamageSource(victim.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type));
    }

    /** The death message key is {@code death.attack.<message id>}. */
    public static String messageId(ResourceKey<DamageType> type) {
        return VitalityMod.MOD_ID + "." + type.location().getPath();
    }

    private static ResourceKey<DamageType> key(String path) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceIds.of(VitalityMod.MOD_ID, path));
    }
}
