package com.impact.realisticinventory.advancement;

import com.google.gson.JsonObject;
import com.impact.lib.ResourceIds;
import com.impact.realisticinventory.InventoryMod;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

/**
 * {@code impactsinventory:slot_switched}: a player switched hotbar slots. The optional {@code recent_switches} field
 * asks for at least that many switches within the last 5 seconds, this one included ("Juggler").
 */
public final class SlotSwitchedTrigger extends SimpleCriterionTrigger<SlotSwitchedTrigger.TriggerInstance> {
    public static final ResourceLocation ID = ResourceIds.of(InventoryMod.MOD_ID, "slot_switched");
    private static final String RECENT_SWITCHES_FIELD = "recent_switches";
    private static final int ANY_SWITCH = 1;

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new TriggerInstance(player, GsonHelper.getAsInt(json, RECENT_SWITCHES_FIELD, ANY_SWITCH));
    }

    public void trigger(ServerPlayer player, int recentSwitches) {
        trigger(player, instance -> recentSwitches >= instance.minimumRecentSwitches);
    }

    public static final class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final int minimumRecentSwitches;

        public TriggerInstance(ContextAwarePredicate player, int minimumRecentSwitches) {
            super(ID, player);
            this.minimumRecentSwitches = minimumRecentSwitches;
        }

        public static TriggerInstance anySwitch() {
            return new TriggerInstance(ContextAwarePredicate.ANY, ANY_SWITCH);
        }

        public static TriggerInstance recentSwitches(int minimumRecentSwitches) {
            return new TriggerInstance(ContextAwarePredicate.ANY, minimumRecentSwitches);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            if (minimumRecentSwitches > ANY_SWITCH) {
                json.addProperty(RECENT_SWITCHES_FIELD, minimumRecentSwitches);
            }
            return json;
        }
    }
}
