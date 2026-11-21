package com.impact.realisticcrafting.item;

import com.impact.lib.item.UsesDurability;
import com.impact.lib.item.UsesTrackedItem;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.MakeshiftTableBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** A makeshift table as an item. It carries the uses the placed table had left, so picking it up doesn't repair it. */
public final class MakeshiftTableItem extends BlockItem implements UsesTrackedItem {
    public static final String USES_LEFT_KEY = "tooltip." + CraftingMod.MOD_ID + ".uses_left";

    private final MakeshiftTableBlock tableBlock;

    public MakeshiftTableItem(MakeshiftTableBlock tableBlock) {
        super(tableBlock, new Properties());
        this.tableBlock = tableBlock;
    }

    @Override
    public int maxUses(ItemStack stack) {
        return tableBlock.table().maxUses();
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

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(USES_LEFT_KEY, UsesDurability.get(stack), maxUses(stack)).withStyle(ChatFormatting.GRAY));
    }
}
