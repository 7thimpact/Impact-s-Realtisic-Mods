package com.impact.lib.item;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.log.ImpactsLog;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * Uses-based durability: the number of uses spent is stored as {@code uses} in the item's NBT, and there's no tag at
 * all while the item is unused, so fresh items stack together. Every mod changes uses only through this class.
 */
public final class UsesDurability {
    public static final String USES_KEY = "uses";
    private static final int FULL_BAR_WIDTH = 13;
    private static final float GREEN_HUE = 1.0F / 3.0F;
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);

    private UsesDurability() {
    }

    public static boolean isTracked(ItemStack stack) {
        return stack.getItem() instanceof UsesTrackedItem;
    }

    /** Uses left, or 0 for an item that isn't uses-tracked. */
    public static int get(ItemStack stack) {
        if (!(stack.getItem() instanceof UsesTrackedItem tracked)) {
            return 0;
        }
        return Math.max(0, tracked.maxUses(stack) - usesSpent(stack));
    }

    public static boolean isUnused(ItemStack stack) {
        return usesSpent(stack) == 0;
    }

    /** Spends uses. Returns true when the item is used up; the caller removes it then. */
    public static boolean use(ItemStack stack, int amount) {
        if (!isTracked(stack) || amount <= 0) {
            return false;
        }
        int spent = usesSpent(stack) + amount;
        stack.getOrCreateTag().putInt(USES_KEY, spent);
        int usesLeft = get(stack);
        LOG.debug(LibLogCategories.USES, "used", "item", ResourceIds.itemId(stack.getItem()), "amount", amount, "usesLeft", usesLeft);
        return usesLeft <= 0;
    }

    /** Copies the uses spent from one stack to another (Crafting's bucket swaps, Food's Hot Pebble). */
    public static void copyUses(ItemStack from, ItemStack to) {
        int spent = usesSpent(from);
        if (spent > 0) {
            to.getOrCreateTag().putInt(USES_KEY, spent);
        }
    }

    public static int usesSpent(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(USES_KEY);
    }

    // Durability bar helpers for UsesTrackedItem classes: they override vanilla's bar methods with these.

    public static boolean isBarVisible(ItemStack stack) {
        return isTracked(stack) && !isUnused(stack);
    }

    public static int barWidth(ItemStack stack) {
        int maxUses = ((UsesTrackedItem) stack.getItem()).maxUses(stack);
        return maxUses <= 0 ? 0 : Math.round(FULL_BAR_WIDTH * (float) get(stack) / maxUses);
    }

    public static int barColor(ItemStack stack) {
        int maxUses = ((UsesTrackedItem) stack.getItem()).maxUses(stack);
        float fraction = maxUses <= 0 ? 0.0F : (float) get(stack) / maxUses;
        return Mth.hsvToRgb(GREEN_HUE * fraction, 1.0F, 1.0F);
    }
}
