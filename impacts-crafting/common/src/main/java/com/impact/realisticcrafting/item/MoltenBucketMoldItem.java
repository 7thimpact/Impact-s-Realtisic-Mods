package com.impact.realisticcrafting.item;

import com.impact.lib.item.ImpactsItems;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.debug.CraftingLogCategories;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.Optional;

/**
 * A bucket mold full of molten iron. Used on water (a water block, a water cauldron, or while standing in water) it
 * cools with a hiss of steam into a bucket, and the mold comes back for the next one.
 */
public final class MoltenBucketMoldItem extends Item {
    public static final String HOT_HINT_KEY = "tooltip." + CraftingMod.MOD_ID + ".molten_bucket_mold";
    private static final ImpactsLog LOG = ImpactsLog.of(CraftingMod.MOD_ID);
    private static final int STEAM_PARTICLES = 12;
    private static final double STEAM_SPREAD = 0.3;

    public MoltenBucketMoldItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack molten = player.getItemInHand(hand);
        Optional<BlockPos> water = waterToCoolIn(level, player);
        if (water.isEmpty()) {
            return InteractionResultHolder.pass(molten);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.success(molten);
        }
        BlockState cooledIn = level.getBlockState(water.get());
        if (cooledIn.is(Blocks.WATER_CAULDRON)) {
            LayeredCauldronBlock.lowerFillLevel(cooledIn, level, water.get());
        }
        BlockPos at = water.get();
        level.playSound(null, at, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1.0F);
        ((ServerLevel) level).sendParticles(ParticleTypes.CLOUD, at.getX() + 0.5, at.getY() + 1.0, at.getZ() + 0.5, STEAM_PARTICLES,
                STEAM_SPREAD, 0.1, STEAM_SPREAD, 0.02);
        ImpactsItems.giveOrDrop(player, new ItemStack(CraftingItems.bucketMold(BucketMold.FIRED)));
        LOG.debug(CraftingLogCategories.BUCKET, "bucket cast", "player", player.getScoreboardName(), "cooledIn",
                cooledIn.is(Blocks.WATER_CAULDRON) ? "cauldron" : "water", "pos", at.toShortString());
        return InteractionResultHolder.consume(new ItemStack(Items.BUCKET));
    }

    @Override
    public void appendHoverText(ItemStack molten, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(HOT_HINT_KEY).withStyle(ChatFormatting.GOLD));
    }

    // The water block or water cauldron looked at, or the block the player stands in when that's water.
    private static Optional<BlockPos> waterToCoolIn(Level level, Player player) {
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos target = hit.getBlockPos();
            boolean isWater = level.getFluidState(target).is(FluidTags.WATER) || level.getBlockState(target).is(Blocks.WATER_CAULDRON);
            if (isWater) {
                return Optional.of(target);
            }
        }
        return player.isInWater() ? Optional.of(player.blockPosition()) : Optional.empty();
    }
}
