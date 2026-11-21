package com.impact.realisticfood.stoneboiling;

import com.impact.lib.ResourceIds;
import com.impact.lib.event.ImpactsEvents;
import com.impact.lib.event.WaterFilled;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.registry.ModRegistrar;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.advancement.FoodAchievements;
import com.impact.realisticfood.boiling.ClientBoilingPots;
import com.impact.realisticfood.boiling.PotInteractions;
import com.impact.realisticfood.boiling.PotKind;
import com.impact.realisticfood.config.FoodFeature;
import com.impact.realisticfood.debug.FoodLogCategories;
import com.impact.realisticfood.item.HotPebbleItem;
import com.impact.realisticfood.registry.FoodSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Stone boiling's pot (only with Impact's Realistic Crafting): sneak-use Crafting's Water-filled Wooden Bucket on the
 * top of a solid block to set it down, drop Hot Pebbles in to boil it, and sneak-use it with an empty hand to take the
 * bucket back. Crafting's items are found by registry ID, never by its classes.
 */
public final class StoneBoilingPots {
    public static final ResourceLocation CRAFTING_WATER_BUCKET = ResourceIds.of(FoodMod.CRAFTING_MOD_ID, "wooden_water_bucket");
    public static final ResourceLocation CRAFTING_WOODEN_BUCKET = ResourceIds.of(FoodMod.CRAFTING_MOD_ID, "wooden_bucket");
    private static final ImpactsLog LOG = ImpactsLog.of(FoodMod.MOD_ID);
    private static final float POT_STRENGTH = 0.5F;
    private static final float SPLASH_VOLUME = 0.6F;
    private static Supplier<StoneBoilingPotBlock> block;
    private static Supplier<BlockEntityType<StoneBoilingPotBlockEntity>> blockEntityType;

    private StoneBoilingPots() {
    }

    /** Called once, during mod construction. */
    public static void register() {
        ModRegistrar registrar = LoaderPlatform.current().registrar(FoodMod.MOD_ID);
        block = registrar.register(Registries.BLOCK, "stone_boiling_pot", () -> new StoneBoilingPotBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD).strength(POT_STRENGTH).sound(SoundType.WOOD).noOcclusion()));
        blockEntityType = registrar.register(Registries.BLOCK_ENTITY_TYPE, "stone_boiling_pot",
                () -> LoaderPlatform.current().blockEntityType(StoneBoilingPotBlockEntity::new, block.get()));
    }

    public static StoneBoilingPotBlock block() {
        return block.get();
    }

    public static BlockEntityType<StoneBoilingPotBlockEntity> blockEntityType() {
        return blockEntityType.get();
    }

    /** A pot stands on any block with a solid top; a campfire's isn't, so a wooden bucket never sits in flames. */
    public static boolean canStandOn(LevelReader level, BlockPos below) {
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    public static boolean isWaterBucket(ItemStack stack) {
        return !stack.isEmpty() && ResourceIds.itemId(stack.getItem()).equals(CRAFTING_WATER_BUCKET);
    }

    /**
     * A click on a block with an item: sets Crafting's Water-filled Wooden Bucket down on a solid top when the player
     * sneaks (Lib's shared interactions give Crafting the plain click, which pours). Every other click passes.
     */
    public static InteractionResult tryPlace(Level level, BlockPos clicked, Direction face, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        boolean isPlacing = isWaterBucket(held) && player.isSecondaryUseActive() && face == Direction.UP
                && canStandOn(level, clicked) && FoodFeature.STONE_BOILING.isOn();
        BlockPos above = clicked.above();
        if (!isPlacing || !level.getBlockState(above).canBeReplaced()) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        serverLevel.setBlockAndUpdate(above, block().defaultBlockState());
        if (serverLevel.getBlockEntity(above) instanceof StoneBoilingPotBlockEntity potEntity) {
            potEntity.holdBucket(held);
        }
        if (!player.getAbilities().instabuild) {
            player.setItemInHand(hand, ItemStack.EMPTY);
        }
        serverLevel.playSound(null, above, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        LOG.debug(FoodLogCategories.STONE_BOILING, "pot placed", "player", player.getGameProfile().getName(), "pos", above);
        return InteractionResult.CONSUME;
    }

    /** Clicking the pot: sneak with an empty hand to take the bucket back, a Hot Pebble to boil it, or use it as a pot. */
    static InteractionResult use(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand) {
        if (!(level.getBlockEntity(position) instanceof StoneBoilingPotBlockEntity potEntity)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        boolean isTakingBack = player.isSecondaryUseActive() && held.isEmpty() && hand == InteractionHand.MAIN_HAND;
        boolean isHotPebble = held.getItem() instanceof HotPebbleItem;
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            boolean takesPebble = isHotPebble && state.getValue(StoneBoilingPotBlock.HAS_WATER);
            return isTakingBack || takesPebble ? InteractionResult.SUCCESS
                    : PotInteractions.predictOnClient(level, position, held, hand, PotKind.BUCKET, ClientBoilingPots.isBoiling(level, position));
        }
        if (isTakingBack) {
            return takeBack(serverLevel, position, serverPlayer, potEntity);
        }
        if (isHotPebble) {
            return dropPebbleIn(serverLevel, position, serverPlayer, hand, potEntity);
        }
        StonePotHost host = new StonePotHost(serverLevel, position, potEntity);
        return PotInteractions.serveOrAdd(serverPlayer, hand, host, Optional.of(potEntity.pot()), potEntity::pot);
    }

    /** Drops the held Hot Pebble into the pot. The pot breaks if that was its bucket's last use. */
    static InteractionResult dropPebbleIn(ServerLevel level, BlockPos position, ServerPlayer player, InteractionHand hand,
                                          StoneBoilingPotBlockEntity potEntity) {
        ItemStack hotPebble = player.getItemInHand(hand);
        StoneBoilingPotBlockEntity.PebbleResult result = potEntity.addHotPebble(level, hotPebble);
        if (result == StoneBoilingPotBlockEntity.PebbleResult.NO_WATER) {
            return InteractionResult.PASS;
        }
        player.setItemInHand(hand, ItemStack.EMPTY);
        level.playSound(null, position, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, SPLASH_VOLUME, 1.4F);
        level.playSound(null, position, FoodSounds.potSplash(), SoundSource.BLOCKS, SPLASH_VOLUME, 1.0F);
        FoodAchievements.onStoneBoiled(level, position);
        if (result == StoneBoilingPotBlockEntity.PebbleResult.BUCKET_BROKE) {
            level.playSound(null, position, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.8F, 0.8F);
            LOG.debug(FoodLogCategories.STONE_BOILING, "bucket broke", "player", player.getScoreboardName(), "pos", position);
            level.destroyBlock(position, false);
        }
        return InteractionResult.CONSUME;
    }

    private static InteractionResult takeBack(ServerLevel level, BlockPos position, ServerPlayer player, StoneBoilingPotBlockEntity potEntity) {
        ItemStack bucket = potEntity.bucketToReturn();
        boolean hasBoiledWater = potEntity.hasWater() && potEntity.hasBoiled();
        int boilsLeft = potEntity.boilsLeft();
        potEntity.emptyOut().forEach(content -> Block.popResource(level, position, content));
        level.removeBlock(position, false);
        if (hasBoiledWater) {
            // Vitality may mark the water purified.
            ImpactsEvents.postWaterFilled(new WaterFilled(player, bucket, level, position, true));
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, bucket);
        level.playSound(null, position, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        LOG.debug(FoodLogCategories.STONE_BOILING, "bucket returned", "player", player.getGameProfile().getName(), "pos", position,
                "bucket", ResourceIds.itemId(bucket.getItem()), "boilsLeft", boilsLeft, "boiled", hasBoiledWater);
        return InteractionResult.CONSUME;
    }
}
