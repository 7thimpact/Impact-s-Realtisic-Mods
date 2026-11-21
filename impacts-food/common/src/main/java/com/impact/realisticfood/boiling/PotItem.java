package com.impact.realisticfood.boiling;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * One item in a pot. It boils on its own into its {@link #boiledResult} when it has a boiling recipe, unless a soup
 * takes it first; a soup ingredient with no boiling recipe waits for a soup (or turns into Watery Soup).
 */
public final class PotItem {
    private static final String INPUT_KEY = "Input";
    private static final String RESULT_KEY = "Result";
    private static final String TICKS_NEEDED_KEY = "TicksNeeded";
    private static final String TICKS_BOILED_KEY = "TicksBoiled";
    private static final String SOUP_INGREDIENT_KEY = "SoupIngredient";

    private final ItemStack input;
    private final Optional<ItemStack> boiledResult;
    private final int ticksNeeded;
    private final boolean isSoupIngredient;
    private int ticksBoiled;

    public PotItem(ItemStack input, Optional<ItemStack> boiledResult, int ticksNeeded, boolean isSoupIngredient) {
        this(input, boiledResult, ticksNeeded, isSoupIngredient, 0);
    }

    private PotItem(ItemStack input, Optional<ItemStack> boiledResult, int ticksNeeded, boolean isSoupIngredient, int ticksBoiled) {
        this.input = input;
        this.boiledResult = boiledResult;
        this.ticksNeeded = ticksNeeded;
        this.isSoupIngredient = isSoupIngredient;
        this.ticksBoiled = ticksBoiled;
    }

    public ItemStack input() {
        return input;
    }

    public Optional<ItemStack> boiledResult() {
        return boiledResult;
    }

    public int ticksNeeded() {
        return ticksNeeded;
    }

    public boolean isSoupIngredient() {
        return isSoupIngredient;
    }

    /** A soup ingredient with no boiling recipe of its own: only a soup (or Watery Soup) uses it. */
    public boolean isOnlySoupIngredient() {
        return isSoupIngredient && boiledResult.isEmpty();
    }

    /** One tick of boiling. True once an item with a boiling recipe is done. */
    boolean boilOneTick() {
        if (boiledResult.isEmpty()) {
            return false;
        }
        ticksBoiled++;
        return ticksBoiled >= ticksNeeded;
    }

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put(INPUT_KEY, input.save(new CompoundTag()));
        boiledResult.ifPresent(result -> tag.put(RESULT_KEY, result.save(new CompoundTag())));
        tag.putInt(TICKS_NEEDED_KEY, ticksNeeded);
        tag.putInt(TICKS_BOILED_KEY, ticksBoiled);
        tag.putBoolean(SOUP_INGREDIENT_KEY, isSoupIngredient);
        return tag;
    }

    static PotItem load(CompoundTag tag) {
        Optional<ItemStack> result = tag.contains(RESULT_KEY) ? Optional.of(ItemStack.of(tag.getCompound(RESULT_KEY))) : Optional.empty();
        return new PotItem(ItemStack.of(tag.getCompound(INPUT_KEY)), result, tag.getInt(TICKS_NEEDED_KEY), tag.getBoolean(SOUP_INGREDIENT_KEY),
                tag.getInt(TICKS_BOILED_KEY));
    }
}
