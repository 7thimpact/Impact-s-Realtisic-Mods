package com.impact.realisticfood.bucket;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.event.ImpactsEvents;
import com.impact.lib.event.WaterFilled;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.boiling.PotInteractions;
import com.impact.realisticfood.boiling.PotKind;
import com.impact.realisticfood.config.FoodFeature;
import com.impact.realisticfood.debug.FoodLogCategories;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * The bucket on a campfire: sneak-use a water bucket on the top of a lit campfire to set it down as a small pot, and
 * sneak-use it with an empty hand to take the bucket back (still a water bucket if any water is left).
 */
public final class CampfireBuckets {
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);
    private static final float BUCKET_STRENGTH = 0.5F;
    private static Supplier<CampfireBucketBlock> block;
    private static Supplier<BlockEntityType<CampfireBucketBlockEntity>> blockEntityType;

    private CampfireBuckets() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(FoodMod.MOD_ID);
        block = registrar.register(Registries.BLOCK, "campfire_bucket", () -> new CampfireBucketBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(BUCKET_STRENGTH).sound(SoundType.METAL).noOcclusion()));
        blockEntityType = registrar.register(Registries.BLOCK_ENTITY_TYPE, "campfire_bucket",
                () -> LoaderPlatform.current().blockEntityType(CampfireBucketBlockEntity::new, block.get()));
    }

    public static CampfireBucketBlock block() {
        return block.get();
    }

    public static BlockEntityType<CampfireBucketBlockEntity> blockEntityType() {
        return blockEntityType.get();
    }

    public static boolean isOnLitCampfire(LevelReader level, BlockPos position) {
        return CampfireBlock.isLitCampfire(level.getBlockState(position.below()));
    }

    /**
     * A click on a block with an item: sets a water bucket down on a lit campfire's top when the player sneaks. Every
     * other click passes (Lib's shared interactions: the campfire's other inputs belong to the spit, cooking and pebbles).
     */
    public static InteractionResult tryPlace(Level level, BlockPos clicked, Direction face, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        boolean isPlacing = held.is(Items.WATER_BUCKET) && player.isSecondaryUseActive() && face == Direction.UP
                && CampfireBlock.isLitCampfire(level.getBlockState(clicked)) && FoodFeature.CAMPFIRE_BUCKET.isOn();
        BlockPos above = clicked.above();
        if (!isPlacing || !level.getBlockState(above).canBeReplaced()) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        serverLevel.setBlockAndUpdate(above, block().defaultBlockState());
        if (serverLevel.getBlockEntity(above) instanceof CampfireBucketBlockEntity bucketEntity) {
            bucketEntity.holdBucket(held);
        }
        if (!player.getAbilities().instabuild) {
            player.setItemInHand(hand, ItemStack.EMPTY);
        }
        serverLevel.playSound(null, above, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        LOG.debug(FoodLogCategories.CAMPFIRE_BUCKET, "placed", "player", player.getGameProfile().getName(), "pos", above);
        return InteractionResult.CONSUME;
    }

    /** Clicking the bucket: sneak with an empty hand to take it back, or use it as a pot. */
    static InteractionResult use(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand) {
        if (!(level.getBlockEntity(position) instanceof CampfireBucketBlockEntity bucketEntity)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        boolean isTakingBack = player.isSecondaryUseActive() && held.isEmpty() && hand == InteractionHand.MAIN_HAND;
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            boolean isHeated = state.getValue(CampfireBucketBlock.HAS_WATER) && ImpactsLib.isHeated(level, position.below());
            return isTakingBack ? InteractionResult.SUCCESS : PotInteractions.predictOnClient(level, position, held, hand, PotKind.BUCKET, isHeated);
        }
        if (isTakingBack) {
            return takeBack(serverLevel, position, serverPlayer, bucketEntity);
        }
        BucketPotHost host = new BucketPotHost(serverLevel, position, bucketEntity);
        return PotInteractions.serveOrAdd(serverPlayer, hand, host, Optional.of(bucketEntity.pot()), bucketEntity::pot);
    }

    /** Pops the bucket off its campfire, dropping it and what's in it. */
    static void popOff(ServerLevel level, BlockPos position, String reason) {
        LOG.debug(FoodLogCategories.CAMPFIRE_BUCKET, "popped off", "pos", position, "reason", reason);
        level.destroyBlock(position, false);
    }

    static void dropEverything(Level level, BlockPos position, CampfireBucketBlockEntity bucketEntity) {
        bucketEntity.drops().forEach(drop -> Block.popResource(level, position, drop));
    }

    private static InteractionResult takeBack(ServerLevel level, BlockPos position, ServerPlayer player, CampfireBucketBlockEntity bucketEntity) {
        ItemStack bucket = bucketEntity.bucketToReturn();
        boolean hasBoiledWater = bucketEntity.hasWater() && bucketEntity.hasBoiled();
        int boilsLeft = bucketEntity.boilsLeft();
        bucketEntity.emptyOut().forEach(input -> Block.popResource(level, position, input));
        level.removeBlock(position, false);
        if (hasBoiledWater) {
            // Vitality may mark the water purified.
            ImpactsEvents.postWaterFilled(new WaterFilled(player, bucket, level, position, true));
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, bucket);
        level.playSound(null, position, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        LOG.debug(FoodLogCategories.CAMPFIRE_BUCKET, "bucket returned", "player", player.getGameProfile().getName(), "pos", position,
                "bucket", ResourceIds.itemId(bucket.getItem()), "boilsLeft", boilsLeft, "boiled", hasBoiledWater);
        return InteractionResult.CONSUME;
    }
}
