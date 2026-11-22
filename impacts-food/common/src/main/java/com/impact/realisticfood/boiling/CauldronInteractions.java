package com.impact.realisticfood.boiling;

import com.impact.lib.ImpactsLib;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.config.FoodFeature;
import com.impact.realisticfood.debug.FoodLogCategories;
import com.impact.realisticfood.mixin.ItemEntityAccessor;
import com.impact.realisticfood.registry.FoodSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Using a water cauldron: putting items in (by hand or by dropping them), ladling a ready soup into a bowl, eating it
 * from the pot with an empty hand, and refusing bottles at a soup. Everything else (Salty's Realistic Forging's ores,
 * buckets, ...) passes on untouched.
 */
public final class CauldronInteractions {
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);
    private static final float SPLASH_VOLUME = 0.4F;

    private CauldronInteractions() {
    }

    /** Right-clicking a cauldron. PASS leaves the click to vanilla's cauldron interactions and other mods. */
    public static InteractionResult use(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!CauldronPotHost.isWaterCauldron(state) || !FoodFeature.BOILING.isOn()) {
            return InteractionResult.PASS;
        }
        if (isBottleRefused(level, position, held)) {
            return InteractionResult.FAIL;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return PotInteractions.predictOnClient(level, position, held, hand, PotKind.CAULDRON, ImpactsLib.isHeated(level, position.below()));
        }
        CauldronPots pots = CauldronPots.of(serverLevel);
        return PotInteractions.serveOrAdd(serverPlayer, hand, new CauldronPotHost(serverLevel, position), pots.pot(position), () -> pots.track(position));
    }

    /**
     * Whether Food refuses a bottle here: a cauldron, campfire bucket or stone-boiling pot holding a soup or its ingredients mustn't give water. Checked by
     * the loader's early click event too, so no other mod fills a bottle from a soup.
     */
    public static boolean isBottleRefused(Level level, BlockPos position, ItemStack held) {
        if (!held.is(Items.GLASS_BOTTLE) || !FoodFeature.BOILING.isOn()) {
            return false;
        }
        boolean holdsSoup = level instanceof ServerLevel serverLevel
                ? serverPot(serverLevel, position).map(BoilingPot::holdsSoup).orElse(false)
                : ClientBoilingPots.soupState(level, position) != SoupState.NONE;
        if (holdsSoup && level instanceof ServerLevel) {
            LOG.debug(FoodLogCategories.SOUP, "bottle refused", "pos", position);
        }
        return holdsSoup;
    }

    // A water cauldron's pot, or a campfire bucket's or stone-boiling pot's.
    private static Optional<BoilingPot> serverPot(ServerLevel level, BlockPos position) {
        if (level.getBlockEntity(position) instanceof PotBlockEntity potEntity) {
            return Optional.of(potEntity.pot());
        }
        boolean isWaterCauldron = CauldronPotHost.isWaterCauldron(level.getBlockState(position));
        return isWaterCauldron ? CauldronPots.of(level).pot(position) : Optional.empty();
    }

    /** An item entity in a cauldron's water: a hot pot takes every item it has a recipe and room for. */
    public static void onItemInWater(ServerLevel level, BlockPos position, BlockState state, ItemEntity itemEntity) {
        boolean isPopped = CauldronPotHost.POT_THROWER.equals(((ItemEntityAccessor) itemEntity).impactsfood$getThrower());
        // Powder snow cauldrons are layered cauldrons too, but hold no water.
        if (isPopped || !CauldronPotHost.isWaterCauldron(state) || !FoodFeature.BOILING.isOn() || !isBelowSurface(state, position, itemEntity)) {
            return;
        }
        CauldronPotHost host = new CauldronPotHost(level, position);
        if (!host.isHeated() && !CauldronPots.isBoiling(level, position)) {
            return;
        }
        BoilingPot pot = CauldronPots.of(level).track(position);
        ItemStack dropped = itemEntity.getItem().copy();
        int taken = 0;
        while (!dropped.isEmpty() && pot.tryAdd(host, dropped)) {
            dropRemainder(level, position, dropped);
            dropped.shrink(1);
            taken++;
        }
        if (taken == 0) {
            return;
        }
        if (dropped.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(dropped);
        }
        level.playSound(null, position, FoodSounds.potSplash(), SoundSource.BLOCKS, SPLASH_VOLUME, 1.0F);
    }

    // A milk bucket leaves its bucket behind, as it does in a crafting grid.
    private static void dropRemainder(ServerLevel level, BlockPos position, ItemStack dropped) {
        ItemStack remainder = LoaderPlatform.current().craftingRemainder(dropped);
        if (!remainder.isEmpty()) {
            Block.popResource(level, position.above(), remainder);
        }
    }

    private static boolean isBelowSurface(BlockState state, BlockPos position, ItemEntity itemEntity) {
        return itemEntity.getY() < position.getY() + CauldronPotHost.waterHeight(state);
    }
}
