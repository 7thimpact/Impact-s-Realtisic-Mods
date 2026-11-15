package com.impact.lib.advancement;

import com.google.gson.JsonObject;
import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.counter.ImpactsCounters;
import com.impact.lib.counter.PlayerCounters;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

import java.util.Arrays;
import java.util.Locale;

/**
 * {@code impactslib:count}: a player's counter reached a value. JSON fields: {@code counter}, {@code measure}
 * ({@code total}, {@code item}, {@code any_item} or {@code distinct_items}), {@code item} (for {@code item}) and
 * {@code value}.
 */
public final class CountTrigger extends SimpleCriterionTrigger<CountTrigger.TriggerInstance> {
    public static final ResourceLocation ID = ResourceIds.of(ImpactsLib.MOD_ID, "count");
    private static final String COUNTER_FIELD = "counter";
    private static final String MEASURE_FIELD = "measure";
    private static final String ITEM_FIELD = "item";
    private static final String VALUE_FIELD = "value";

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        Measure measure = Measure.byName(GsonHelper.getAsString(json, MEASURE_FIELD, Measure.TOTAL.serializedName()));
        String itemId = GsonHelper.getAsString(json, ITEM_FIELD, "");
        return new TriggerInstance(player, GsonHelper.getAsString(json, COUNTER_FIELD), measure, itemId, GsonHelper.getAsInt(json, VALUE_FIELD));
    }

    public void trigger(ServerPlayer player, String counter) {
        PlayerCounters counters = ImpactsCounters.all(player);
        trigger(player, instance -> instance.matches(counters, counter));
    }

    /** What part of a counter a condition compares with its value. */
    public enum Measure {
        TOTAL, ITEM, ANY_ITEM, DISTINCT_ITEMS;

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        static Measure byName(String serializedName) {
            return Arrays.stream(values())
                    .filter(measure -> measure.serializedName().equals(serializedName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown count measure: " + serializedName));
        }
    }

    public static final class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final String counter;
        private final Measure measure;
        private final String itemId;
        private final int value;

        public TriggerInstance(ContextAwarePredicate player, String counter, Measure measure, String itemId, int value) {
            super(ID, player);
            this.counter = counter;
            this.measure = measure;
            this.itemId = itemId;
            this.value = value;
        }

        public static TriggerInstance total(String counter, int value) {
            return new TriggerInstance(ContextAwarePredicate.ANY, counter, Measure.TOTAL, "", value);
        }

        public static TriggerInstance item(String counter, ResourceLocation itemId, int value) {
            return new TriggerInstance(ContextAwarePredicate.ANY, counter, Measure.ITEM, itemId.toString(), value);
        }

        public static TriggerInstance anyItem(String counter, int value) {
            return new TriggerInstance(ContextAwarePredicate.ANY, counter, Measure.ANY_ITEM, "", value);
        }

        public static TriggerInstance distinctItems(String counter, int value) {
            return new TriggerInstance(ContextAwarePredicate.ANY, counter, Measure.DISTINCT_ITEMS, "", value);
        }

        public boolean matches(PlayerCounters counters, String changedCounter) {
            return counter.equals(changedCounter) && measuredCount(counters) >= value;
        }

        private int measuredCount(PlayerCounters counters) {
            return switch (measure) {
                case TOTAL -> counters.total(counter);
                case ITEM -> counters.itemCount(counter, itemId);
                case ANY_ITEM -> counters.highestItemCount(counter);
                case DISTINCT_ITEMS -> counters.distinctItemCount(counter);
            };
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.addProperty(COUNTER_FIELD, counter);
            json.addProperty(MEASURE_FIELD, measure.serializedName());
            if (measure == Measure.ITEM) {
                json.addProperty(ITEM_FIELD, itemId);
            }
            json.addProperty(VALUE_FIELD, value);
            return json;
        }
    }
}
