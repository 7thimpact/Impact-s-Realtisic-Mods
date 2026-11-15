package com.impact.lib.counter;

import com.impact.lib.attachment.PlayerAttachmentData;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

/** One player's named counters. Each counter keeps a total and, optionally, a count per item. */
public final class PlayerCounters implements PlayerAttachmentData {
    private static final String TOTAL_KEY = "total";
    private static final String ITEMS_KEY = "items";

    private final Map<String, CounterValues> valuesByCounter = new HashMap<>();

    /** Adds to the counter (and to the item's own count, when an item is given) and returns the new total. */
    public int add(String counter, String itemIdOrNull, int amount) {
        CounterValues values = valuesByCounter.computeIfAbsent(counter, name -> new CounterValues());
        values.total += amount;
        if (itemIdOrNull != null) {
            values.countsByItemId.merge(itemIdOrNull, amount, Integer::sum);
        }
        return values.total;
    }

    public int total(String counter) {
        CounterValues values = valuesByCounter.get(counter);
        return values == null ? 0 : values.total;
    }

    public int itemCount(String counter, String itemId) {
        CounterValues values = valuesByCounter.get(counter);
        return values == null ? 0 : values.countsByItemId.getOrDefault(itemId, 0);
    }

    /** The highest count of any single item, for "do the same thing N times" achievements. */
    public int highestItemCount(String counter) {
        CounterValues values = valuesByCounter.get(counter);
        return values == null ? 0 : values.countsByItemId.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    /** How many different items have a count of one or more. */
    public int distinctItemCount(String counter) {
        CounterValues values = valuesByCounter.get(counter);
        return values == null ? 0 : (int) values.countsByItemId.values().stream().filter(count -> count > 0).count();
    }

    public void reset(String counter) {
        valuesByCounter.remove(counter);
    }

    @Override
    public CompoundTag save() {
        CompoundTag countersTag = new CompoundTag();
        valuesByCounter.forEach((counter, values) -> countersTag.put(counter, values.save()));
        return countersTag;
    }

    @Override
    public void load(CompoundTag countersTag) {
        valuesByCounter.clear();
        for (String counter : countersTag.getAllKeys()) {
            valuesByCounter.put(counter, CounterValues.load(countersTag.getCompound(counter)));
        }
    }

    private static final class CounterValues {
        private int total;
        private final Map<String, Integer> countsByItemId = new HashMap<>();

        private CompoundTag save() {
            CompoundTag valuesTag = new CompoundTag();
            valuesTag.putInt(TOTAL_KEY, total);
            CompoundTag itemsTag = new CompoundTag();
            countsByItemId.forEach(itemsTag::putInt);
            valuesTag.put(ITEMS_KEY, itemsTag);
            return valuesTag;
        }

        private static CounterValues load(CompoundTag valuesTag) {
            CounterValues values = new CounterValues();
            values.total = valuesTag.getInt(TOTAL_KEY);
            CompoundTag itemsTag = valuesTag.getCompound(ITEMS_KEY);
            itemsTag.getAllKeys().forEach(itemId -> values.countsByItemId.put(itemId, itemsTag.getInt(itemId)));
            return values;
        }
    }
}
