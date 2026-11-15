package com.impact.lib.advancement;

import com.google.gson.JsonObject;
import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** {@code impactslib:joined_alone}: a player joined a world where Impact's Lib is the only Impact mod. */
public final class JoinedAloneTrigger extends SimpleCriterionTrigger<JoinedAloneTrigger.TriggerInstance> {
    public static final ResourceLocation ID = ResourceIds.of(ImpactsLib.MOD_ID, "joined_alone");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new TriggerInstance(player);
    }

    public void trigger(ServerPlayer player) {
        trigger(player, instance -> true);
    }

    public static final class TriggerInstance extends AbstractCriterionTriggerInstance {
        public TriggerInstance(ContextAwarePredicate player) {
            super(ID, player);
        }

        public static TriggerInstance anyPlayer() {
            return new TriggerInstance(ContextAwarePredicate.ANY);
        }
    }
}
