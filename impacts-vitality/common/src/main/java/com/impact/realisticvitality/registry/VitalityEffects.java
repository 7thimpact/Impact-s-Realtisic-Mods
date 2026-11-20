package com.impact.realisticvitality.registry;

import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.thirst.ParchedEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;

import java.util.function.Supplier;

/** Vitality's mob effects. */
public final class VitalityEffects {
    private static volatile Supplier<MobEffect> parched;

    private VitalityEffects() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(VitalityMod.MOD_ID);
        parched = registrar.register(Registries.MOB_EFFECT, "parched", ParchedEffect::new);
    }

    /** Drains thirst as the Hunger effect drains food. */
    public static MobEffect parched() {
        return parched.get();
    }
}
