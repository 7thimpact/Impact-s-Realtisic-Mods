package com.impact.lib.fuel;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.config.LibServerConfig;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.log.ImpactsLog;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToIntFunction;

/**
 * Furnace burn times for Impact items, read from the item and its NBT (a crimson panel doesn't burn, an oak one does).
 * {@code fuel.overrides} in Lib's server config ({@code item=ticks}) wins over every registered value. The loader
 * module asks this class whenever a furnace looks for fuel.
 */
public final class FuelRegistry {
    private static final String OVERRIDE_SEPARATOR = "=";
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);
    private static final Map<Item, ToIntFunction<ItemStack>> BURN_TICKS_BY_ITEM = new ConcurrentHashMap<>();

    private FuelRegistry() {
    }

    /** A fixed burn time. 200 ticks smelt one item. */
    public static void register(Item item, int burnTicks) {
        register(item, stack -> burnTicks);
    }

    /** A burn time that depends on the stack, for items whose material is in their NBT. Return 0 for "doesn't burn". */
    public static void register(Item item, ToIntFunction<ItemStack> burnTicks) {
        BURN_TICKS_BY_ITEM.put(item, burnTicks);
        LOG.debug(LibLogCategories.REGISTRY, "fuel registered", "item", ResourceIds.itemId(item));
    }

    /** The burn time Lib decides for this stack, or nothing to leave it to vanilla and other mods. */
    public static OptionalInt burnTicks(ItemStack stack) {
        OptionalInt override = configuredOverride(stack.getItem());
        if (override.isPresent()) {
            return override;
        }
        ToIntFunction<ItemStack> registered = BURN_TICKS_BY_ITEM.get(stack.getItem());
        return registered == null ? OptionalInt.empty() : OptionalInt.of(registered.applyAsInt(stack));
    }

    private static OptionalInt configuredOverride(Item item) {
        String itemId = ResourceIds.itemId(item).toString();
        return LibServerConfig.get().fuel.overrides.get().stream()
                .map(entry -> entry.split(OVERRIDE_SEPARATOR, 2))
                .filter(parts -> parts.length == 2 && parts[0].trim().equals(itemId))
                .map(parts -> parseTicks(parts[1]))
                .filter(OptionalInt::isPresent)
                .findFirst()
                .orElse(OptionalInt.empty());
    }

    private static OptionalInt parseTicks(String text) {
        try {
            return OptionalInt.of(Math.max(0, Integer.parseInt(text.trim())));
        } catch (NumberFormatException notANumber) {
            return OptionalInt.empty();
        }
    }
}
