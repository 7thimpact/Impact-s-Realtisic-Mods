package com.impact.realisticfood.bucket;

import com.impact.lib.water.WaterVesselProvider;
import com.impact.realisticfood.boiling.CauldronInteractions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * The campfire bucket as a water vessel for Lib: a bottle filled from it takes one of its boils of water. Never while it
 * holds a soup or soup ingredients (those bottles are refused, as at a cauldron).
 */
public final class CampfireBucketWaterVessel implements WaterVesselProvider {
    private static final ItemStack GLASS_BOTTLE = new ItemStack(Items.GLASS_BOTTLE);

    @Override
    public boolean canFillBottle(Level level, BlockPos position) {
        boolean hasWater = level.getBlockState(position).is(CampfireBuckets.block())
                && level.getBlockState(position).getValue(CampfireBucketBlock.HAS_WATER);
        return hasWater && !CauldronInteractions.isBottleRefused(level, position, GLASS_BOTTLE);
    }

    @Override
    public boolean takeBottle(Level level, BlockPos position) {
        if (!(level instanceof ServerLevel serverLevel) || !canFillBottle(level, position)) {
            return false;
        }
        if (!(level.getBlockEntity(position) instanceof CampfireBucketBlockEntity bucket) || !bucket.hasWater()) {
            return false;
        }
        bucket.useOneBoil(serverLevel);
        return true;
    }
}
