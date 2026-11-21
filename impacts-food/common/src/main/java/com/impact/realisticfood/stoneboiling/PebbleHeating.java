package com.impact.realisticfood.stoneboiling;

import com.impact.lib.ResourceIds;
import com.impact.lib.item.ImpactsItems;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.config.FoodFeature;
import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.debug.FoodLogCategories;
import com.impact.realisticfood.item.HotPebbleItem;
import com.impact.realisticfood.registry.FoodItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Heating a pebble (any of Crafting's {@code #impactscrafting:pebbles}) on a lit campfire: hold use on the campfire for
 * {@code stoneBoiling.heatTicks} and one pebble from the stack turns into a Hot Pebble. The pebble is Crafting's item,
 * so this times the hold from the clicks themselves: a held use button clicks the block again every 4 ticks, and a gap
 * longer than {@link #MAX_CLICK_GAP_TICKS} starts the hold over.
 */
public final class PebbleHeating {
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);
    private static final TagKey<Item> PEBBLES = TagKey.create(Registries.ITEM, ResourceIds.of(FoodMod.CRAFTING_MOD_ID, "pebbles"));
    private static final long MAX_CLICK_GAP_TICKS = 8;
    private static final double SMOKE_HEIGHT = 0.5;
    private static final Map<UUID, Hold> HOLDS = new ConcurrentHashMap<>();

    private PebbleHeating() {
    }

    /** A player holding use on one campfire with a pebble since {@code startTick}. */
    private record Hold(BlockPos campfire, InteractionHand hand, long startTick, long lastClickTick) {
        boolean continuesWith(BlockPos clicked, InteractionHand clickHand, long now) {
            return campfire.equals(clicked) && hand == clickHand && now - lastClickTick <= MAX_CLICK_GAP_TICKS;
        }
    }

    public static boolean isPebble(ItemStack stack) {
        return stack.is(PEBBLES);
    }

    /** A click on a block: a pebble on a lit campfire heats while the click is held. Every other click passes. */
    public static InteractionResult tryHeat(Level level, BlockPos clicked, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        boolean isHeating = isPebble(held) && !player.isSecondaryUseActive() && CampfireBlock.isLitCampfire(level.getBlockState(clicked))
                && FoodFeature.STONE_BOILING.isOn();
        if (!isHeating) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        long now = serverLevel.getGameTime();
        Hold previous = HOLDS.get(player.getUUID());
        long startTick = previous != null && previous.continuesWith(clicked, hand, now) ? previous.startTick() : now;
        serverLevel.sendParticles(ParticleTypes.SMOKE, clicked.getX() + 0.5, clicked.getY() + SMOKE_HEIGHT, clicked.getZ() + 0.5, 2, 0.1, 0.05, 0.1, 0.0);
        if (now - startTick < FoodServerConfig.get().stoneBoiling.heatTicks.get()) {
            HOLDS.put(player.getUUID(), new Hold(clicked.immutable(), hand, startTick, now));
            return InteractionResult.CONSUME;
        }
        HOLDS.remove(player.getUUID());
        heatOnePebble(serverLevel, clicked, player, hand);
        return InteractionResult.CONSUME;
    }

    /** Forgets a player's hold (they left). */
    public static void forget(Player player) {
        HOLDS.remove(player.getUUID());
    }

    private static void heatOnePebble(ServerLevel level, BlockPos campfire, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        ItemStack pebble = held.split(1);
        ItemStack hotPebble = HotPebbleItem.heatedFrom(FoodItems.hotPebble(), pebble);
        if (held.isEmpty()) {
            player.setItemInHand(hand, hotPebble);
        } else {
            ImpactsItems.giveOrDrop(player, hotPebble);
        }
        level.playSound(null, campfire, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.4F, 1.6F);
        LOG.debug(FoodLogCategories.STONE_BOILING, "pebble heated", "player", player.getGameProfile().getName(), "pos", campfire,
                "pebble", ResourceIds.itemId(pebble.getItem()));
    }
}
