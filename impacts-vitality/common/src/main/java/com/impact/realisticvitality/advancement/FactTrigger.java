package com.impact.realisticvitality.advancement;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A trigger fired with named facts about what happened, such as {@code purity=salt} or {@code state=overheating}. A
 * criterion lists the facts it needs in its conditions, and matches when every one of them is the same; the usual
 * {@code player} predicate (location, effects, ...) applies too. Vitality's drank, temperature_state, air_lost and
 * gear_used triggers are all of this kind.
 */
public final class FactTrigger extends SimpleCriterionTrigger<FactTrigger.TriggerInstance> {
    private static final String PLAYER_FIELD = "player";

    private final ResourceLocation id;

    public FactTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        Map<String, String> requiredFacts = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> field : json.entrySet()) {
            if (!field.getKey().equals(PLAYER_FIELD) && field.getValue().isJsonPrimitive()) {
                requiredFacts.put(field.getKey(), field.getValue().getAsString());
            }
        }
        return new TriggerInstance(id, player, requiredFacts);
    }

    public void trigger(ServerPlayer player, Map<String, String> facts) {
        trigger(player, instance -> instance.matches(facts));
    }

    /** A criterion needing these facts, from any player. */
    public TriggerInstance needing(Map<String, String> requiredFacts) {
        return new TriggerInstance(id, ContextAwarePredicate.ANY, requiredFacts);
    }

    /** A criterion needing these facts, from a player matching the predicate (where they are, for example). */
    public TriggerInstance needing(ContextAwarePredicate player, Map<String, String> requiredFacts) {
        return new TriggerInstance(id, player, requiredFacts);
    }

    public static final class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Map<String, String> requiredFacts;

        public TriggerInstance(ResourceLocation id, ContextAwarePredicate player, Map<String, String> requiredFacts) {
            super(id, player);
            this.requiredFacts = Map.copyOf(requiredFacts);
        }

        public boolean matches(Map<String, String> facts) {
            return requiredFacts.entrySet().stream().allMatch(required -> required.getValue().equals(facts.get(required.getKey())));
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            requiredFacts.forEach(json::addProperty);
            return json;
        }
    }
}
