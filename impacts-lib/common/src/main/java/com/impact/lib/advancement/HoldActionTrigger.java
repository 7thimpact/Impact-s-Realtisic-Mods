package com.impact.lib.advancement;

import com.google.gson.JsonObject;
import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

import java.util.Optional;

/** {@code impactslib:hold_action}: a hold-to-use cycle finished (one item ground, one hammer blow). Optional {@code action} ID. */
public final class HoldActionTrigger extends SimpleCriterionTrigger<HoldActionTrigger.TriggerInstance> {
    public static final ResourceLocation ID = ResourceIds.of(ImpactsLib.MOD_ID, "hold_action");
    private static final String ACTION_FIELD = "action";

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        Optional<ResourceLocation> action = json.has(ACTION_FIELD) ? ResourceIds.tryParse(GsonHelper.getAsString(json, ACTION_FIELD)) : Optional.empty();
        return new TriggerInstance(player, action);
    }

    public void trigger(ServerPlayer player, ResourceLocation actionId) {
        trigger(player, instance -> instance.action.map(actionId::equals).orElse(true));
    }

    public static final class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ResourceLocation> action;

        public TriggerInstance(ContextAwarePredicate player, Optional<ResourceLocation> action) {
            super(ID, player);
            this.action = action;
        }

        public static TriggerInstance of(ResourceLocation actionId) {
            return new TriggerInstance(ContextAwarePredicate.ANY, Optional.of(actionId));
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            action.ifPresent(actionId -> json.addProperty(ACTION_FIELD, actionId.toString()));
            return json;
        }
    }
}
