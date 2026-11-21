package com.impact.realisticfood.stoneboiling;

import com.impact.lib.water.WaterVesselProvider;
import com.impact.realisticfood.boiling.CauldronInteractions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The stone-boiling pot as a water vessel for Lib: a bottle filled from it takes one of its boils of water (purified
 * by Vitality while the pot boils). Never while it holds a soup or soup ingredients.
 */
public final class StoneBoilingPotWaterVessel implements WaterVesselProvider {
    private static final ItemStack GLASS_BOTTLE = new ItemStack(Items.GLASS_BOTTLE);

    @Override
    public boolean canFillBottle(Level level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        boolean hasWater = state.is(StoneBoilingPots.block()) && state.getValue(StoneBoilingPotBlock.HAS_WATER);
        return hasWater && !CauldronInteractions.isBottleRefused(level, position, GLASS_BOTTLE);
    }

    @Override
    public boolean takeBottle(Level level, BlockPos position) {
        if (!(level instanceof ServerLevel serverLevel) || !canFillBottle(level, position)) {
            return false;
        }
        if (!(level.getBlockEntity(position) instanceof StoneBoilingPotBlockEntity potEntity) || !potEntity.hasWater()) {
            return false;
        }
        potEntity.useOneBoil(serverLevel);
        return true;
    }
}
