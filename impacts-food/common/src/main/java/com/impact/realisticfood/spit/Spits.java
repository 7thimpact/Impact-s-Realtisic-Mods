package com.impact.realisticfood.spit;

import com.impact.lib.ResourceIds;
import com.impact.lib.advancement.LibTriggers;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.config.FoodFeature;
import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.debug.FoodLogCategories;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Spit roasting on a vanilla campfire. Clicks on a campfire: the Spit item mounts a spit (lit campfires only), food
 * skewers onto it (the campfire's own slots take the rest), an empty hand turns it, and sneaking with an empty hand
 * takes it down. Anything else passes to vanilla's campfire.
 */
public final class Spits {
    /** The spit's turn as Lib's hold action, for Food's "Round and Round" achievement. */
    public static final ResourceLocation TURN_ACTION = ResourceIds.of(FoodMod.MOD_ID, "spit_turn");
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);
    private static final int TURN_SMOKE = 6;
    private static final double SMOKE_SPREAD = 0.25;
    private static final double SMOKE_RISE = 0.05;
    private static final float SIZZLE_VOLUME = 0.5F;
    private static Supplier<Item> spitItem;

    private Spits() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        spitItem = LoaderPlatform.current().registrar(FoodMod.MOD_ID).register(Registries.ITEM, "spit", () -> new Item(new Item.Properties()));
    }

    public static Item spitItem() {
        return spitItem.get();
    }

    /** A click on a campfire. PASS leaves it to vanilla (placing food in the campfire's slots) and other mods. */
    public static InteractionResult use(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand) {
        if (!FoodFeature.SPIT.isOn() || !(level.getBlockEntity(position) instanceof CampfireBlockEntity campfire)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        Optional<Spit> spit = SpitHolder.spitOf(campfire);
        if (held.is(spitItem()) && spit.isEmpty() && CampfireBlock.isLitCampfire(state)) {
            return level.isClientSide ? InteractionResult.SUCCESS : mount(level, position, player, held, campfire);
        }
        if (spit.isEmpty()) {
            return InteractionResult.PASS;
        }
        boolean isEmptyMainHand = held.isEmpty() && hand == InteractionHand.MAIN_HAND;
        if (isEmptyMainHand && player.isSecondaryUseActive()) {
            return level.isClientSide ? InteractionResult.SUCCESS : takeDown(level, position, campfire);
        }
        if (isEmptyMainHand) {
            return level.isClientSide ? InteractionResult.SUCCESS : turn(level, position, player, spit.get(), campfire);
        }
        if (level.isClientSide) {
            return InteractionResult.PASS;
        }
        return skewer(level, position, player, held, spit.get(), campfire);
    }

    /** Called from the campfire's cooking tick, so a spit cooks only while the campfire is lit. */
    public static void cookTick(Level level, BlockPos position, BlockState state, CampfireBlockEntity campfire) {
        SpitHolder.spitOf(campfire).filter(spit -> spit.cookTick(level, position)).ifPresent(spit -> markUpdated(level, position, state, campfire));
    }

    /** A campfire broken (or replaced) drops its spit and what's on it. */
    public static void onCampfireRemoved(Level level, BlockPos position, CampfireBlockEntity campfire) {
        if (level.isClientSide) {
            return;
        }
        SpitHolder.spitOf(campfire).ifPresent(spit -> {
            spit.takeEverything().forEach(stack -> Containers.dropItemStack(level, position.getX(), position.getY(), position.getZ(), stack));
            ((SpitHolder) campfire).impactsfood$setSpit(null);
            LOG.debug(FoodLogCategories.SPIT, "dropped with campfire", "pos", position);
        });
    }

    private static InteractionResult mount(Level level, BlockPos position, Player player, ItemStack held, CampfireBlockEntity campfire) {
        ((SpitHolder) campfire).impactsfood$setSpit(new Spit(held.copyWithCount(1), FoodServerConfig.get().spit.slots.get()));
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        level.playSound(null, position, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        markUpdated(level, position, campfire.getBlockState(), campfire);
        LOG.debug(FoodLogCategories.SPIT, "mounted", "player", player.getGameProfile().getName(), "pos", position);
        return InteractionResult.CONSUME;
    }

    private static InteractionResult skewer(Level level, BlockPos position, Player player, ItemStack held, Spit spit, CampfireBlockEntity campfire) {
        if (!spit.addFood(level, position, held)) {
            return InteractionResult.PASS;
        }
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        markUpdated(level, position, campfire.getBlockState(), campfire);
        return InteractionResult.CONSUME;
    }

    private static InteractionResult turn(Level level, BlockPos position, Player player, Spit spit, CampfireBlockEntity campfire) {
        if (!spit.turn(level, position)) {
            return InteractionResult.CONSUME;
        }
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, position.getX() + 0.5, position.getY() + 0.9, position.getZ() + 0.5,
                    TURN_SMOKE, SMOKE_SPREAD, 0.1, SMOKE_SPREAD, SMOKE_RISE);
        }
        level.playSound(null, position, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, SIZZLE_VOLUME, 1.6F);
        if (player instanceof ServerPlayer serverPlayer) {
            LibTriggers.HOLD_ACTION.trigger(serverPlayer, TURN_ACTION);
        }
        markUpdated(level, position, campfire.getBlockState(), campfire);
        return InteractionResult.CONSUME;
    }

    private static InteractionResult takeDown(Level level, BlockPos position, CampfireBlockEntity campfire) {
        onCampfireRemoved(level, position, campfire);
        markUpdated(level, position, campfire.getBlockState(), campfire);
        LOG.debug(FoodLogCategories.SPIT, "taken down", "pos", position);
        return InteractionResult.CONSUME;
    }

    private static void markUpdated(Level level, BlockPos position, BlockState state, CampfireBlockEntity campfire) {
        campfire.setChanged();
        level.sendBlockUpdated(position, state, state, CampfireBlock.UPDATE_ALL);
    }
}
