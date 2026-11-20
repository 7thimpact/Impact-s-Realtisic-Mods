package com.impact.realisticvitality.air;

import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.item.CharcoalFilterItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** The Charcoal Filter fitted in a Respirator or Rebreather, saved on the mask as {@code impactsvitality:filter}. */
public final class MaskFilters {
    public static final String FILTER_KEY = VitalityMod.MOD_ID + ":filter";

    private MaskFilters() {
    }

    public static ItemStack filterIn(ItemStack mask) {
        CompoundTag tag = mask.getTag();
        return tag == null || !tag.contains(FILTER_KEY) ? ItemStack.EMPTY : ItemStack.of(tag.getCompound(FILTER_KEY));
    }

    public static void setFilter(ItemStack mask, ItemStack filter) {
        if (filter.isEmpty()) {
            mask.removeTagKey(FILTER_KEY);
            return;
        }
        mask.getOrCreateTag().put(FILTER_KEY, filter.save(new CompoundTag()));
    }

    public static boolean hasWorkingFilter(ItemStack mask) {
        ItemStack filter = filterIn(mask);
        return filter.getItem() instanceof CharcoalFilterItem && filter.getDamageValue() < filter.getMaxDamage();
    }
}
