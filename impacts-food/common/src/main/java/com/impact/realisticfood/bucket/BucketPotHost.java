package com.impact.realisticfood.bucket;

import com.impact.lib.ImpactsLib;
import com.impact.realisticfood.boiling.PotHost;
import com.impact.realisticfood.boiling.PotKind;
import com.impact.realisticfood.config.FoodServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** A campfire bucket as a pot: small, quick to boil, and its water counted in boils rather than levels. */
public record BucketPotHost(ServerLevel level, BlockPos position, CampfireBucketBlockEntity bucket) implements PotHost {
    /** The water's surface, just under the bucket's rim (the bucket sits down on the campfire's logs). */
    private static final double SURFACE_HEIGHT = -0.05;
    private static final double POP_UP_SPEED = 0.3;
    private static final double POP_OUT_SPEED = 0.12;

    @Override
    public PotKind kind() {
        return PotKind.BUCKET;
    }

    @Override
    public boolean hasWater() {
        return bucket.hasWater();
    }

    @Override
    public boolean isHeated() {
        return ImpactsLib.isHeated(level, position.below());
    }

    @Override
    public int heatUpTicks() {
        return FoodServerConfig.get().boiling.bucketHeatUpTicks.get();
    }

    @Override
    public int coolDownTicks() {
        return FoodServerConfig.get().boiling.coolDownTicks.get();
    }

    @Override
    public int itemSlots() {
        return FoodServerConfig.get().boiling.bucketItemSlots.get();
    }

    @Override
    public int soupIngredientSlots() {
        return FoodServerConfig.get().soup.bucketIngredientSlots.get();
    }

    @Override
    public int maxServings() {
        return FoodServerConfig.get().soup.bucketBowls.get();
    }

    @Override
    public void useWaterForServings() {
        bucket.useOneBoil(level);
    }

    @Override
    public void useWaterForBoil() {
        bucket.useOneBoil(level);
    }

    // A bucket's water lasts a number of boils instead of evaporating.
    @Override
    public int evaporationTicks() {
        return 0;
    }

    @Override
    public boolean evaporateOneLevel() {
        return bucket.hasWater();
    }

    @Override
    public Vec3 surfaceCenter() {
        return Vec3.atBottomCenterOf(position).add(0.0, SURFACE_HEIGHT, 0.0);
    }

    @Override
    public void release(ItemStack boiled) {
        Vec3 surface = surfaceCenter();
        ItemEntity popped = new ItemEntity(level, surface.x, surface.y + 0.1, surface.z, boiled);
        double angle = level.random.nextDouble() * Math.PI * 2.0;
        popped.setDeltaMovement(Math.cos(angle) * POP_OUT_SPEED, POP_UP_SPEED, Math.sin(angle) * POP_OUT_SPEED);
        popped.setDefaultPickUpDelay();
        level.addFreshEntity(popped);
    }
}
