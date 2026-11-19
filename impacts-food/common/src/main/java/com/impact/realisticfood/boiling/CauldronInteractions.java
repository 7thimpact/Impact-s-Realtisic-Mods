package com.impact.realisticfood.boiling;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.config.FoodFeature;
import com.impact.realisticfood.debug.FoodLogCategories;
import com.impact.realisticfood.mixin.ItemEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Putting items into a water cauldron: by hand, or by dropping them in. Food acts only on items a boiling recipe takes,
 * in a hot cauldron; everything else (Salty's Realistic Forging's ores, bottles, buckets, ...) passes on untouched.
 */
public final class CauldronInteractions {
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);
    private static final float SPLASH_VOLUME = 0.4F;

    private CauldronInteractions() {
    }

    /** Right-clicking a cauldron. PASS leaves the click to vanilla's cauldron interactions and other mods. */
    public static InteractionResult use(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!CauldronPotHost.isWaterCauldron(state) || held.isEmpty() || !FoodFeature.BOILING.isOn()) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return wouldClientAdd(level, position, held) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        CauldronPotHost host = new CauldronPotHost(serverLevel, position);
        if (!host.isHeated() && !CauldronPots.isBoiling(serverLevel, position)) {
            logPassedOn(held, position, "cold");
            return InteractionResult.PASS;
        }
        if (CauldronPots.of(serverLevel).track(position).tryAdd(host, held).isEmpty()) {
            logPassedOn(held, position, "no recipe or no room");
            return InteractionResult.PASS;
        }
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        level.playSound(null, position, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, SPLASH_VOLUME, 1.0F);
        return InteractionResult.CONSUME;
    }

    /** An item entity in a cauldron's water: a hot pot takes every item it has a recipe and room for. */
    public static void onItemInWater(ServerLevel level, BlockPos position, BlockState state, ItemEntity itemEntity) {
        boolean isPopped = CauldronPotHost.POT_THROWER.equals(((ItemEntityAccessor) itemEntity).impactsfood$getThrower());
        if (isPopped || !FoodFeature.BOILING.isOn() || !isBelowSurface(state, position, itemEntity)) {
            return;
        }
        CauldronPotHost host = new CauldronPotHost(level, position);
        if (!host.isHeated() && !CauldronPots.isBoiling(level, position)) {
            return;
        }
        BoilingPot pot = CauldronPots.of(level).track(position);
        ItemStack dropped = itemEntity.getItem().copy();
        while (!dropped.isEmpty() && pot.tryAdd(host, dropped).isPresent()) {
            dropped.shrink(1);
        }
        if (dropped.getCount() == itemEntity.getItem().getCount()) {
            return;
        }
        if (dropped.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(dropped);
        }
        level.playSound(null, position, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, SPLASH_VOLUME, 1.0F);
    }

    // The client can't see the server's pot, so it guesses from the heat source and the bubbles it was sent.
    private static boolean wouldClientAdd(Level level, BlockPos position, ItemStack held) {
        boolean isHot = ImpactsLib.isHeated(level, position.below()) || ClientBoilingPots.isBoiling(level, position);
        return isHot && BoilingRecipes.find(level, held, PotKind.CAULDRON).isPresent();
    }

    private static boolean isBelowSurface(BlockState state, BlockPos position, ItemEntity itemEntity) {
        return itemEntity.getY() < position.getY() + CauldronPotHost.waterHeight(state);
    }

    private static void logPassedOn(ItemStack held, BlockPos position, String reason) {
        if (LOG.isVerbose(FoodLogCategories.BOILING)) {
            LOG.debug(FoodLogCategories.BOILING, "passed on", "item", ResourceIds.itemId(held.getItem()), "pos", position, "reason", reason);
        }
    }
}
