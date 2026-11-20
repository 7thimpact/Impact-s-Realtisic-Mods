package com.impact.realisticvitality.thirst.block;

import com.impact.realisticvitality.thirst.WaterPurity;
import net.minecraft.nbt.CompoundTag;

/** Drinks of water held inside a block, with one purity for all of it (adding water mixes it down to the worst). */
public final class WaterTank {
    private static final String DRINKS_KEY = "Drinks";
    private static final String PURITY_KEY = "Purity";

    private int drinks;
    private WaterPurity purity = WaterPurity.PURIFIED;

    public int drinks() {
        return drinks;
    }

    public WaterPurity purity() {
        return purity;
    }

    public boolean isEmpty() {
        return drinks <= 0;
    }

    public void add(int addedDrinks, WaterPurity addedPurity) {
        purity = isEmpty() ? addedPurity : purity.mixedWith(addedPurity);
        drinks += addedDrinks;
    }

    public void take(int takenDrinks) {
        drinks = Math.max(0, drinks - takenDrinks);
        if (drinks == 0) {
            purity = WaterPurity.PURIFIED;
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(DRINKS_KEY, drinks);
        tag.putString(PURITY_KEY, purity.serializedName);
        return tag;
    }

    public void load(CompoundTag tag) {
        drinks = tag.getInt(DRINKS_KEY);
        purity = WaterPurity.bySerializedName(tag.getString(PURITY_KEY)).orElse(WaterPurity.PURIFIED);
    }
}
