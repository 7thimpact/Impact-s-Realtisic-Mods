package com.impact.realisticfood.boiling;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** One item boiling in a pot: what went in, what it turns into, and how far along it is. */
public final class BoilingItem {
    private static final String INPUT_KEY = "Input";
    private static final String RESULT_KEY = "Result";
    private static final String TICKS_NEEDED_KEY = "TicksNeeded";
    private static final String TICKS_BOILED_KEY = "TicksBoiled";

    private final ItemStack input;
    private final ItemStack result;
    private final int ticksNeeded;
    private int ticksBoiled;

    public BoilingItem(ItemStack input, ItemStack result, int ticksNeeded) {
        this(input, result, ticksNeeded, 0);
    }

    private BoilingItem(ItemStack input, ItemStack result, int ticksNeeded, int ticksBoiled) {
        this.input = input;
        this.result = result;
        this.ticksNeeded = ticksNeeded;
        this.ticksBoiled = ticksBoiled;
    }

    public ItemStack input() {
        return input;
    }

    public ItemStack result() {
        return result;
    }

    public int ticksNeeded() {
        return ticksNeeded;
    }

    /** One tick of boiling. True once the item is done. */
    boolean boilOneTick() {
        ticksBoiled++;
        return ticksBoiled >= ticksNeeded;
    }

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put(INPUT_KEY, input.save(new CompoundTag()));
        tag.put(RESULT_KEY, result.save(new CompoundTag()));
        tag.putInt(TICKS_NEEDED_KEY, ticksNeeded);
        tag.putInt(TICKS_BOILED_KEY, ticksBoiled);
        return tag;
    }

    static BoilingItem load(CompoundTag tag) {
        return new BoilingItem(ItemStack.of(tag.getCompound(INPUT_KEY)), ItemStack.of(tag.getCompound(RESULT_KEY)),
                tag.getInt(TICKS_NEEDED_KEY), tag.getInt(TICKS_BOILED_KEY));
    }
}
