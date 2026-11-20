package com.impact.realisticcrafting.item;

import com.impact.lib.event.ImpactsEvents;
import com.impact.lib.event.WaterFilled;
import com.impact.lib.item.UsesDurability;
import com.impact.lib.item.UsesTrackedItem;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.debug.CraftingLogCategories;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * The empty Wooden Bucket: a crafting part while unused (stacks to 16), and a bucket that scoops water (never lava).
 * 32 uses; a scoop costs 1. Scooping turns it into a Water-filled Wooden Bucket carrying its whole tag, then posts Lib's
 * water-filled event so other mods can add their own data.
 */
public final class WoodenBucketItem extends Item implements UsesTrackedItem {
    private static final int UNUSED_STACK_SIZE = 16;
    private static final ImpactsLog LOG = ImpactsLog.of(CraftingMod.MOD_ID);

    public WoodenBucketItem() {
        super(new Item.Properties().stacksTo(UNUSED_STACK_SIZE));
    }

    @Override
    public int maxUses(ItemStack stack) {
        return CraftingServerConfig.get().woodenBucketUses.get();
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return UsesDurability.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return UsesDurability.barWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return UsesDurability.barColor(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        BlockHitResult target = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (target.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(held);
        }
        BlockPos position = target.getBlockPos();
        BlockState state = level.getBlockState(position);
        boolean isWater = state.getFluidState().is(FluidTags.WATER) && state.getFluidState().isSource();
        if (!isWater || !(state.getBlock() instanceof BucketPickup pickup) || !level.mayInteract(player, position)
                || !player.mayUseItemAt(position, target.getDirection(), held)) {
            return InteractionResultHolder.pass(held);
        }
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(held, true);
        }
        pickup.pickupBlock(level, position, state);
        level.playSound(null, position, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.FLUID_PICKUP, position);
        ItemStack filled = scoop(held, player);
        ImpactsEvents.postWaterFilled(new WaterFilled(player, filled, level, position, false));
        LOG.debug(CraftingLogCategories.BUCKET, "scooped", "player", player.getScoreboardName(), "usesLeft", UsesDurability.get(filled),
                "pos", position.toShortString());
        LOG.debug(CraftingLogCategories.BUCKET, "water-filled event posted", "item", "impactscrafting:wooden_water_bucket");
        return InteractionResultHolder.sidedSuccess(ItemUtils.createFilledResult(held, player, filled), false);
    }

    /** The filled bucket for one scoop: this bucket's whole tag, one more use spent (none in creative). */
    private static ItemStack scoop(ItemStack empty, Player player) {
        ItemStack filled = new ItemStack(CraftingItems.woodenWaterBucket());
        if (empty.hasTag()) {
            filled.setTag(empty.getTag().copy());
        }
        if (!player.getAbilities().instabuild) {
            spendUse(filled);
        }
        return filled;
    }

    // The filled bucket counts uses against the empty bucket's maximum: it's the same bucket.
    static void spendUse(ItemStack bucket) {
        int spent = UsesDurability.usesSpent(bucket) + 1;
        bucket.getOrCreateTag().putInt(UsesDurability.USES_KEY, spent);
    }

    static int usesLeft(ItemStack bucket) {
        return Math.max(0, CraftingServerConfig.get().woodenBucketUses.get() - UsesDurability.usesSpent(bucket));
    }
}
