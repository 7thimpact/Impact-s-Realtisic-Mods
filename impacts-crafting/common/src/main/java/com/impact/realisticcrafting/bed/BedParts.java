package com.impact.realisticcrafting.bed;

import com.impact.lib.log.ImpactsLog;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.CraftingFeature;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.debug.CraftingLogCategories;
import com.impact.realisticcrafting.registry.CraftingBlocks;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** A broken bed drops its three parts instead of itself: the frame, the comforter and its color's sheet (village beds too). */
public final class BedParts {
    private static final ImpactsLog LOG = ImpactsLog.of(CraftingMod.MOD_ID);

    private BedParts() {
    }

    /** Replaces the bed item in a broken bed's loot with its parts, when beds drop parts. */
    public static void replaceBedDrop(BlockState brokenState, List<ItemStack> loot) {
        boolean dropsParts = CraftingFeature.BEDS.isOn() && CraftingServerConfig.get().bedsDropParts.get();
        if (!dropsParts || !(brokenState.getBlock() instanceof BedBlock bed)) {
            return;
        }
        boolean hadBed = loot.removeIf(stack -> stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof BedBlock);
        if (!hadBed) {
            return;
        }
        loot.add(new ItemStack(CraftingBlocks.bedFrame()));
        loot.add(new ItemStack(CraftingItems.comforter()));
        loot.add(new ItemStack(CraftingItems.sheet(bed.getColor())));
        LOG.debug(CraftingLogCategories.BEDS, "parts dropped", "color", bed.getColor().getName());
    }
}
