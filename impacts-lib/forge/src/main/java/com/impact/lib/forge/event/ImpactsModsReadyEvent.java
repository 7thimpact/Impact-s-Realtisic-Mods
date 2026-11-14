package com.impact.lib.forge.event;

import net.minecraftforge.eventbus.api.Event;

import java.util.Set;

/**
 * Posted on the Forge event bus once every mod has finished common setup, with the Impact mods that registered
 * with Lib. Each Impact mod turns its integration features on from here, in one place.
 */
public final class ImpactsModsReadyEvent extends Event {
    private final Set<String> impactMods;

    public ImpactsModsReadyEvent(Set<String> impactMods) {
        this.impactMods = Set.copyOf(impactMods);
    }

    public Set<String> impactMods() {
        return impactMods;
    }
}
