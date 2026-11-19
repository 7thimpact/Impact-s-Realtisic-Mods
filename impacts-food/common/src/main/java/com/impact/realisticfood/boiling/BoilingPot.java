package com.impact.realisticfood.boiling;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.debug.FoodLogCategories;
import com.impact.realisticfood.food.StyleLog;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * The water in one pot: it warms up over a heat source, boils, keeps boiling for a while once the heat is gone,
 * boils single items, and (in a cauldron) slowly boils away. Ticked by the block that holds it, through {@link PotHost}.
 */
public final class BoilingPot {
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);
    private static final String HEAT_TICKS_KEY = "HeatTicks";
    private static final String BOILING_KEY = "Boiling";
    private static final String COOL_TICKS_KEY = "CoolTicks";
    private static final String EVAPORATION_TICKS_KEY = "EvaporationTicks";
    private static final String ITEMS_KEY = "Items";

    private int heatTicks;
    private boolean isBoiling;
    private int coolTicks;
    private int evaporationTicks;
    private final List<BoilingItem> items = new ArrayList<>();

    public boolean isBoiling() {
        return isBoiling;
    }

    /** Whether the pot is warming up or boiling, so it takes items. */
    public boolean isHot(PotHost host) {
        return isBoiling || host.isHeated();
    }

    public List<BoilingItem> items() {
        return List.copyOf(items);
    }

    /** Nothing left to track: cold, not boiling and empty. */
    public boolean isIdle(PotHost host) {
        return !isBoiling && heatTicks == 0 && items.isEmpty() && !host.isHeated();
    }

    /** Brings the water straight to a boil (a Hot Pebble does this). */
    public void boilNow(PotHost host) {
        if (!isBoiling) {
            isBoiling = true;
            logHeat(host, "boiling");
        }
        heatTicks = host.heatUpTicks();
        coolTicks = 0;
    }

    /** Adds one of the stack's items to boil, when the pot is hot, has room, and a recipe takes it. */
    public Optional<BoilingRecipe> tryAdd(PotHost host, ItemStack stack) {
        if (!isHot(host) || items.size() >= host.itemSlots()) {
            return Optional.empty();
        }
        Optional<BoilingRecipe> recipe = BoilingRecipes.find(host.level(), stack, host.kind());
        recipe.ifPresent(found -> {
            ItemStack input = stack.copyWithCount(1);
            items.add(new BoilingItem(input, found.result().copy(), BoilingRecipes.boilTicks(found, input)));
            LOG.debug(FoodLogCategories.BOILING, "item added", "pot", host.kind().logName, "pos", host.position(),
                    "input", ResourceIds.itemId(input.getItem()), "items", items.size());
        });
        return recipe;
    }

    /** Takes every item out unboiled, for when the pot is emptied or broken. */
    public List<ItemStack> takeAllInputs() {
        List<ItemStack> inputs = items.stream().map(item -> item.input().copy()).toList();
        items.clear();
        return inputs;
    }

    public void tick(PotHost host) {
        updateHeat(host);
        if (!isBoiling) {
            return;
        }
        boilItems(host);
        evaporate(host);
    }

    private void updateHeat(PotHost host) {
        if (host.isHeated()) {
            warmUp(host);
            return;
        }
        if (isBoiling) {
            coolOff(host);
            return;
        }
        heatTicks = 0;
    }

    private void warmUp(PotHost host) {
        coolTicks = 0;
        if (isBoiling) {
            return;
        }
        if (heatTicks == 0) {
            logHeat(host, "heating");
        }
        heatTicks++;
        if (heatTicks >= host.heatUpTicks()) {
            isBoiling = true;
            logHeat(host, "boiling");
        }
    }

    private void coolOff(PotHost host) {
        coolTicks++;
        if (coolTicks < host.coolDownTicks()) {
            return;
        }
        isBoiling = false;
        heatTicks = 0;
        coolTicks = 0;
        logHeat(host, "cooled off");
    }

    private void boilItems(PotHost host) {
        Iterator<BoilingItem> boiling = items.iterator();
        while (boiling.hasNext()) {
            BoilingItem item = boiling.next();
            if (item.boilOneTick()) {
                boiling.remove();
                LOG.debug(FoodLogCategories.BOILING, "boiled", "pot", host.kind().logName, "pos", host.position(),
                        "input", ResourceIds.itemId(item.input().getItem()), "result", ResourceIds.itemId(item.result().getItem()),
                        "ticks", item.ticksNeeded());
                StyleLog.onCooked(BoilingRecipes.ID.toString(), item.input(), item.result());
                host.release(item.result().copy());
            }
        }
    }

    private void evaporate(PotHost host) {
        int interval = host.evaporationTicks();
        if (interval <= 0) {
            return;
        }
        evaporationTicks++;
        if (evaporationTicks < interval) {
            return;
        }
        evaporationTicks = 0;
        boolean hasWaterLeft = host.evaporateOneLevel();
        LOG.debug(FoodLogCategories.EVAPORATION, hasWaterLeft ? "level lost" : "boiled dry", "pot", host.kind().logName, "pos", host.position());
    }

    private void logHeat(PotHost host, String event) {
        if (LOG.isEnabled(FoodLogCategories.HEAT)) {
            LOG.debug(FoodLogCategories.HEAT, event, "pot", host.kind().logName, "pos", host.position(),
                    "source", ResourceIds.blockId(host.level().getBlockState(host.position().below()).getBlock()));
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(HEAT_TICKS_KEY, heatTicks);
        tag.putBoolean(BOILING_KEY, isBoiling);
        tag.putInt(COOL_TICKS_KEY, coolTicks);
        tag.putInt(EVAPORATION_TICKS_KEY, evaporationTicks);
        ListTag itemList = new ListTag();
        items.forEach(item -> itemList.add(item.save()));
        tag.put(ITEMS_KEY, itemList);
        return tag;
    }

    public static BoilingPot load(CompoundTag tag) {
        BoilingPot pot = new BoilingPot();
        pot.heatTicks = tag.getInt(HEAT_TICKS_KEY);
        pot.isBoiling = tag.getBoolean(BOILING_KEY);
        pot.coolTicks = tag.getInt(COOL_TICKS_KEY);
        pot.evaporationTicks = tag.getInt(EVAPORATION_TICKS_KEY);
        tag.getList(ITEMS_KEY, Tag.TAG_COMPOUND).forEach(itemTag -> pot.items.add(BoilingItem.load((CompoundTag) itemTag)));
        return pot;
    }
}
