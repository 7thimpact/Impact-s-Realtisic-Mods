package com.impact.realisticvitality.registry;

import com.impact.lib.ResourceIds;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticvitality.VitalityMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

/** Vitality's sound events: panting when thirsty, shivering when cold, coughing in fumes. */
public final class VitalitySounds {
    public static final String PANT = "player.pant";
    public static final String SHIVER = "player.shiver";
    public static final String COUGH = "player.cough";
    private static volatile Supplier<SoundEvent> pant;
    private static volatile Supplier<SoundEvent> shiver;
    private static volatile Supplier<SoundEvent> cough;

    private VitalitySounds() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(VitalityMod.MOD_ID);
        pant = register(registrar, PANT);
        shiver = register(registrar, SHIVER);
        cough = register(registrar, COUGH);
    }

    public static SoundEvent pant() {
        return pant.get();
    }

    public static SoundEvent shiver() {
        return shiver.get();
    }

    public static SoundEvent cough() {
        return cough.get();
    }

    private static Supplier<SoundEvent> register(ModRegistrar registrar, String path) {
        return registrar.register(Registries.SOUND_EVENT, path, () -> SoundEvent.createVariableRangeEvent(ResourceIds.of(VitalityMod.MOD_ID, path)));
    }
}
