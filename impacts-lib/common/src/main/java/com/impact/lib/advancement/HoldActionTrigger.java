package com.impact.lib.advancement;

import com.google.gson.JsonObject;
import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * {@code impactslib:hold_action}: a hold-to-use cycle finished (one item ground, one hammer blow). Optional {@code action}
 * ID, and an optional {@code item} predicate on the item held as the cycle started (the loose item being hammered).
 */
public final class HoldActionTrigger extends SimpleCriterionTrigger<HoldActionTrigger.TriggerInstance> {
    public static final ResourceLocation ID = ResourceIds.of(ImpactsLib.MOD_ID, "hold_action");
    private static final String ACTION_FIELD = "action";
    private static final String ITEM_FIELD = "item";

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        Optional<ResourceLocation> action = json.has(ACTION_FIELD) ? ResourceIds.tryParse(GsonHelper.getAsString(json, ACTION_FIELD)) : Optional.empty();
        return new TriggerInstance(player, action, ItemPredicate.fromJson(json.get(ITEM_FIELD)));
    }

    /** For actions with no held item worth matching (Food's spit turns). */
    public void trigger(ServerPlayer player, ResourceLocation actionId) {
        trigger(player, actionId, ItemStack.EMPTY);
    }

    public void trigger(ServerPlayer player, ResourceLocation actionId, ItemStack held) {
        trigger(player, instance -> instance.matches(actionId, held));
    }

    public static final class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ResourceLocation> action;
        private final ItemPredicate item;

        public TriggerInstance(ContextAwarePredicate player, Optional<ResourceLocation> action, ItemPredicate item) {
            super(ID, player);
            this.action = action;
            this.item = item;
        }

        public static TriggerInstance of(ResourceLocation actionId) {
            return new TriggerInstance(ContextAwarePredicate.ANY, Optional.of(actionId), ItemPredicate.ANY);
        }

        /** The action finished a cycle on a held item the predicate accepts. */
        public static TriggerInstance holding(ResourceLocation actionId, ItemPredicate held) {
            return new TriggerInstance(ContextAwarePredicate.ANY, Optional.of(actionId), held);
        }

        boolean matches(ResourceLocation actionId, ItemStack held) {
            return action.map(actionId::equals).orElse(true) && (item == ItemPredicate.ANY || item.matches(held));
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            action.ifPresent(actionId -> json.addProperty(ACTION_FIELD, actionId.toString()));
            if (item != ItemPredicate.ANY) {
                json.add(ITEM_FIELD, item.serializeToJson());
            }
            return json;
        }
    }
}
