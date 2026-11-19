package com.impact.realisticfood.spit;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.debug.FoodLogCategories;
import com.impact.realisticfood.food.StyleLog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A spit mounted across a campfire: its own row of slots, cooking with the campfire's recipes at
 * {@code spit.speed} times the campfire's speed, or {@code spit.turnSpeed} for a while after it's turned. Saved and
 * synced with the campfire block entity; cooking happens only while the campfire is lit.
 */
public final class Spit {
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);
    private static final String SPIT_ITEM_KEY = "SpitItem";
    private static final String SLOTS_KEY = "Slots";
    private static final String PROGRESS_KEY = "Progress";
    private static final String COOK_TIMES_KEY = "CookTimes";
    private static final String TURN_ENDS_AT_KEY = "TurnEndsAt";
    private static final String TURNS_KEY = "Turns";
    private static final String ITEMS_KEY = "Items";

    private final ItemStack spitItem;
    private final NonNullList<ItemStack> items;
    private final float[] progress;
    private final int[] cookTimes;
    private long turnEndsAt;
    private int turns;

    public Spit(ItemStack spitItem, int slots) {
        this.spitItem = spitItem;
        this.items = NonNullList.withSize(slots, ItemStack.EMPTY);
        this.progress = new float[slots];
        this.cookTimes = new int[slots];
    }

    public ItemStack spitItem() {
        return spitItem;
    }

    public List<ItemStack> items() {
        return List.copyOf(items);
    }

    public int turns() {
        return turns;
    }

    public long turnEndsAt() {
        return turnEndsAt;
    }

    public boolean isTurning(long gameTime) {
        return gameTime < turnEndsAt;
    }

    /** Skewers one of the stack's items, when a campfire recipe cooks it and a slot is free. */
    public boolean addFood(Level level, BlockPos position, ItemStack stack) {
        int freeSlot = items.indexOf(ItemStack.EMPTY);
        Optional<CampfireCookingRecipe> recipe = recipeFor(level, stack);
        if (freeSlot < 0 || recipe.isEmpty()) {
            return false;
        }
        items.set(freeSlot, stack.copyWithCount(1));
        progress[freeSlot] = 0.0F;
        cookTimes[freeSlot] = recipe.get().getCookingTime();
        LOG.debug(FoodLogCategories.SPIT, "item added", "pos", position, "item", ResourceIds.itemId(stack.getItem()), "slot", freeSlot);
        return true;
    }

    /** Turns the spit, unless it's still turning from the last time. */
    public boolean turn(Level level, BlockPos position) {
        long gameTime = level.getGameTime();
        if (isTurning(gameTime)) {
            return false;
        }
        turnEndsAt = gameTime + FoodServerConfig.get().spit.turnTicks.get();
        turns++;
        LOG.debug(FoodLogCategories.SPIT, "turned", "pos", position, "turns", turns);
        return true;
    }

    /** Takes everything off: the spit item and whatever is still on it. */
    public List<ItemStack> takeEverything() {
        List<ItemStack> everything = new ArrayList<>();
        everything.add(spitItem.copy());
        items.stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy).forEach(everything::add);
        items.replaceAll(stack -> ItemStack.EMPTY);
        return everything;
    }

    /** One tick of cooking on a lit campfire. True when something finished (and dropped off), so the campfire resyncs. */
    public boolean cookTick(Level level, BlockPos position) {
        FoodServerConfig.SpitSettings settings = FoodServerConfig.get().spit;
        double speed = isTurning(level.getGameTime()) ? settings.turnSpeed.get() : settings.speed.get();
        boolean hasFinished = false;
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack food = items.get(slot);
            if (food.isEmpty()) {
                continue;
            }
            progress[slot] += (float) speed;
            if (progress[slot] >= cookTimes[slot]) {
                finish(level, position, slot);
                hasFinished = true;
            }
        }
        return hasFinished;
    }

    // Cooked food drops off the spit, as it does off the campfire.
    private void finish(Level level, BlockPos position, int slot) {
        ItemStack raw = items.get(slot);
        ItemStack cooked = recipeFor(level, raw).map(recipe -> recipe.assemble(new SimpleContainer(raw), level.registryAccess())).orElse(raw);
        items.set(slot, ItemStack.EMPTY);
        LOG.debug(FoodLogCategories.SPIT, "finished", "pos", position, "input", ResourceIds.itemId(raw.getItem()),
                "result", ResourceIds.itemId(cooked.getItem()), "ticks", cookTimes[slot]);
        StyleLog.onCooked("spit", raw, cooked);
        Containers.dropItemStack(level, position.getX(), position.getY(), position.getZ(), cooked);
    }

    private static Optional<CampfireCookingRecipe> recipeFor(Level level, ItemStack stack) {
        return level.getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, new SimpleContainer(stack), level);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put(SPIT_ITEM_KEY, spitItem.save(new CompoundTag()));
        tag.putInt(SLOTS_KEY, items.size());
        CompoundTag itemsTag = new CompoundTag();
        ContainerHelper.saveAllItems(itemsTag, items, true);
        tag.put(ITEMS_KEY, itemsTag);
        tag.putIntArray(COOK_TIMES_KEY, cookTimes);
        int[] progressTicks = new int[progress.length];
        for (int slot = 0; slot < progress.length; slot++) {
            progressTicks[slot] = Math.round(progress[slot]);
        }
        tag.putIntArray(PROGRESS_KEY, progressTicks);
        tag.putLong(TURN_ENDS_AT_KEY, turnEndsAt);
        tag.putInt(TURNS_KEY, turns);
        return tag;
    }

    public static Spit load(CompoundTag tag) {
        Spit spit = new Spit(ItemStack.of(tag.getCompound(SPIT_ITEM_KEY)), tag.getInt(SLOTS_KEY));
        ContainerHelper.loadAllItems(tag.getCompound(ITEMS_KEY), spit.items);
        int[] savedCookTimes = tag.getIntArray(COOK_TIMES_KEY);
        int[] savedProgress = tag.getIntArray(PROGRESS_KEY);
        for (int slot = 0; slot < spit.items.size(); slot++) {
            spit.cookTimes[slot] = slot < savedCookTimes.length ? savedCookTimes[slot] : 0;
            spit.progress[slot] = slot < savedProgress.length ? savedProgress[slot] : 0.0F;
        }
        spit.turnEndsAt = tag.getLong(TURN_ENDS_AT_KEY);
        spit.turns = tag.getInt(TURNS_KEY);
        return spit;
    }
}
