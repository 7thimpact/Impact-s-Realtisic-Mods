package com.impact.lib;

import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.platform.LoaderPlatform;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Public entry point of Impact's Lib: which mods are installed, and which Impact mods registered themselves.
 */
public final class ImpactsLib {
    public static final String MOD_ID = "impactslib";
    /** Every Impact mod's ID starts with this, for example {@code impactsinventory}. */
    public static final String MOD_ID_PREFIX = "impacts";

    private static final Map<String, Boolean> LOAD_STATES_BY_MOD_ID = new ConcurrentHashMap<>();
    private static final Set<String> REGISTERED_IMPACT_MODS = ConcurrentHashMap.newKeySet();
    private static final List<Consumer<Set<String>>> MODS_READY_LISTENERS = new CopyOnWriteArrayList<>();

    private ImpactsLib() {
    }

    /** Whether a mod with this ID is installed. The answer is looked up once and then cached. */
    public static boolean isLoaded(String modId) {
        return LOAD_STATES_BY_MOD_ID.computeIfAbsent(modId, LoaderPlatform.current()::isModLoaded);
    }

    /**
     * Registers an Impact mod with Lib. Every Impact mod calls this during mod construction, so Lib knows
     * which family members are present without hard-coding their IDs.
     */
    public static void registerImpactMod(String modId) {
        REGISTERED_IMPACT_MODS.add(modId);
        ImpactsLog.of(MOD_ID).debug(LibLogCategories.STARTUP, "impact mod registered", "mod", modId);
    }

    /** The Impact mods that registered themselves. Lib itself is not one of them. */
    public static Set<String> registeredImpactMods() {
        return Set.copyOf(REGISTERED_IMPACT_MODS);
    }

    /** Runs the listener once every mod has finished common setup, with the registered Impact mods. */
    public static void onModsReady(Consumer<Set<String>> listener) {
        MODS_READY_LISTENERS.add(listener);
    }

    /** Called once by Lib's loader module after every mod's common setup. Other mods must not call it. */
    public static void announceModsReady() {
        Set<String> impactMods = registeredImpactMods();
        MODS_READY_LISTENERS.forEach(listener -> listener.accept(impactMods));
    }
}
