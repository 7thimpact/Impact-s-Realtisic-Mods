package com.impact.realisticfood.stoneboiling;

import com.impact.realisticfood.boiling.PotHost;
import com.impact.realisticfood.boiling.PotKind;
import com.impact.realisticfood.config.FoodServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * A stone-boiling pot as a pot: bucket-sized, heated only by its Hot Pebbles. The water boils as soon as a pebble goes
 * in and stops the moment the last pebble's heat is spent.
 */
public record StonePotHost(ServerLevel level, BlockPos position, StoneBoilingPotBlockEntity potEntity) implements PotHost {
    /** The water's surface, just under the bucket's rim. */
    private static final double SURFACE_HEIGHT = 9.0 / 16.0;
    private static final double POP_UP_SPEED = 0.3;
    private static final double POP_OUT_SPEED = 0.12;

    @Override
    public PotKind kind() {
        return PotKind.BUCKET;
    }

    @Override
    public boolean hasWater() {
        return potEntity.hasWater();
    }

    @Override
    public boolean isHeated() {
        return potEntity.heatTicksLeft() > 0;
    }

    @Override
    public int heatUpTicks() {
        return 0;
    }

    @Override
    public int coolDownTicks() {
        return 0;
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
        potEntity.useOneBoil(level);
    }

    @Override
    public void useWaterForBoil() {
        potEntity.useOneBoil(level);
    }

    // Like a campfire bucket, its water lasts a number of boils instead of evaporating.
    @Override
    public int evaporationTicks() {
        return 0;
    }

    @Override
    public boolean evaporateOneLevel() {
        return potEntity.hasWater();
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
