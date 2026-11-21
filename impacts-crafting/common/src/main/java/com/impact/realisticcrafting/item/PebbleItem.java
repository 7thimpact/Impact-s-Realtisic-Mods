package com.impact.realisticcrafting.item;

import com.impact.lib.item.UsesDurability;
import com.impact.lib.item.UsesTrackedItem;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The Stone Pebble: the first tool, for trimming and early hammering. Unused pebbles carry no tag and stack to 64; once
 * used, Lib counts its uses in NBT, it stacks to 1 and shows a durability bar.
 */
public final class PebbleItem extends Item implements UsesTrackedItem {
    public PebbleItem() {
        super(new Item.Properties());
    }

    @Override
    public int maxUses(ItemStack stack) {
        return CraftingServerConfig.get().durability.pebbleUses.get();
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return UsesDurability.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return UsesDurability.barWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return UsesDurability.barColor(stack);
    }
}
