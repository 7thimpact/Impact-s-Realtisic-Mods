package com.impact.realisticfood.boiling;

import com.impact.lib.ImpactsLib;
import com.impact.realisticfood.config.FoodServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/** A vanilla water cauldron as a pot: heated by the block below it, it loses water levels as it boils. */
public record CauldronPotHost(ServerLevel level, BlockPos position) implements PotHost {
    /**
     * The thrower Food gives the items it pops out of a pot, so the pot doesn't take them straight back. Saved with
     * the item entity, so it holds across restarts too.
     */
    public static final UUID POT_THROWER = UUID.fromString("6f6f6474-0000-4000-8000-706f74746564");
    private static final double FLOOR_HEIGHT = 6.0 / 16.0;
    private static final double LEVEL_HEIGHT = 3.0 / 16.0;
    private static final double POP_UP_SPEED = 0.3;
    private static final double POP_OUT_SPEED = 0.12;

    public static boolean isWaterCauldron(BlockState state) {
        return state.is(Blocks.WATER_CAULDRON);
    }

    /** The height of a water cauldron's surface above its block's bottom, from its level (1 to 3). */
    public static double waterHeight(BlockState state) {
        return FLOOR_HEIGHT + LEVEL_HEIGHT * state.getValue(LayeredCauldronBlock.LEVEL);
    }

    @Override
    public PotKind kind() {
        return PotKind.CAULDRON;
    }

    @Override
    public boolean isHeated() {
        return ImpactsLib.isHeated(level, position.below());
    }

    @Override
    public int heatUpTicks() {
        return FoodServerConfig.get().boiling.heatUpTicks.get();
    }

    @Override
    public int coolDownTicks() {
        return FoodServerConfig.get().boiling.coolDownTicks.get();
    }

    @Override
    public int itemSlots() {
        return FoodServerConfig.get().boiling.cauldronItemSlots.get();
    }

    @Override
    public int soupIngredientSlots() {
        return FoodServerConfig.get().soup.cauldronIngredientSlots.get();
    }

    @Override
    public int maxServings() {
        return FoodServerConfig.get().soup.maxBowls.get();
    }

    @Override
    public void useWaterForServings() {
        BlockState state = level.getBlockState(position);
        if (isWaterCauldron(state)) {
            LayeredCauldronBlock.lowerFillLevel(state, level, position);
        }
    }

    @Override
    public int evaporationTicks() {
        return FoodServerConfig.get().boiling.evaporationTicks.get();
    }

    @Override
    public boolean evaporateOneLevel() {
        BlockState state = level.getBlockState(position);
        if (isWaterCauldron(state)) {
            LayeredCauldronBlock.lowerFillLevel(state, level, position);
        }
        return isWaterCauldron(level.getBlockState(position));
    }

    @Override
    public Vec3 surfaceCenter() {
        BlockState state = level.getBlockState(position);
        double height = isWaterCauldron(state) ? waterHeight(state) : FLOOR_HEIGHT;
        return Vec3.atBottomCenterOf(position).add(0.0, height, 0.0);
    }

    /** The item pops up out of the water and over the rim, in a random direction. */
    @Override
    public void release(ItemStack boiled) {
        Vec3 surface = surfaceCenter();
        ItemEntity popped = new ItemEntity(level, surface.x, surface.y, surface.z, boiled);
        double angle = level.random.nextDouble() * Math.PI * 2.0;
        popped.setDeltaMovement(Math.cos(angle) * POP_OUT_SPEED, POP_UP_SPEED, Math.sin(angle) * POP_OUT_SPEED);
        popped.setThrower(POT_THROWER);
        popped.setDefaultPickUpDelay();
        level.addFreshEntity(popped);
    }
}
