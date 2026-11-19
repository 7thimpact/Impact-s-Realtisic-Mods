package com.impact.realisticfood.registry;

import com.impact.lib.ResourceIds;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticfood.FoodMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

/**
 * Food's sound events. Each plays a vanilla sound by default (see the generated sounds.json), so resource packs can
 * give Food's pots and spits their own sounds without touching vanilla's.
 */
public final class FoodSounds {
    public static final String POT_SIMMER = "block.pot.simmer";
    public static final String POT_SPLASH = "block.pot.splash";
    public static final String SOUP_LADLE = "block.pot.ladle";
    public static final String SPIT_SIZZLE = "block.spit.sizzle";
    private static Supplier<SoundEvent> potSimmer;
    private static Supplier<SoundEvent> potSplash;
    private static Supplier<SoundEvent> soupLadle;
    private static Supplier<SoundEvent> spitSizzle;

    private FoodSounds() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(FoodMod.MOD_ID);
        potSimmer = register(registrar, POT_SIMMER);
        potSplash = register(registrar, POT_SPLASH);
        soupLadle = register(registrar, SOUP_LADLE);
        spitSizzle = register(registrar, SPIT_SIZZLE);
    }

    public static SoundEvent potSimmer() {
        return potSimmer.get();
    }

    public static SoundEvent potSplash() {
        return potSplash.get();
    }

    public static SoundEvent soupLadle() {
        return soupLadle.get();
    }

    public static SoundEvent spitSizzle() {
        return spitSizzle.get();
    }

    private static Supplier<SoundEvent> register(ModRegistrar registrar, String path) {
        return registrar.register(Registries.SOUND_EVENT, path, () -> SoundEvent.createVariableRangeEvent(ResourceIds.of(FoodMod.MOD_ID, path)));
    }
}
