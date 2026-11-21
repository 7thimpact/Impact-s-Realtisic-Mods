package com.impact.lib.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * An advancement trigger for something that just happened, with no conditions beyond vanilla's player predicate
 * (Inventory's {@code pack_placed}, for example). Each mod makes one per event with its own ID and registers it.
 */
public final class EventTrigger extends SimpleCriterionTrigger<EventTrigger.TriggerInstance> {
    private final ResourceLocation id;

    public EventTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new TriggerInstance(id, player);
    }

    public void trigger(ServerPlayer player) {
        trigger(player, instance -> true);
    }

    /** A criterion met by any player when the event happens. */
    public TriggerInstance anyPlayer() {
        return new TriggerInstance(id, ContextAwarePredicate.ANY);
    }

    public static final class TriggerInstance extends AbstractCriterionTriggerInstance {
        TriggerInstance(ResourceLocation triggerId, ContextAwarePredicate player) {
            super(triggerId, player);
        }
    }
}
