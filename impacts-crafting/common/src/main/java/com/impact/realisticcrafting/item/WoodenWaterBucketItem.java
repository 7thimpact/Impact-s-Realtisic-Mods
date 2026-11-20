package com.impact.realisticcrafting.item;

import com.impact.lib.item.UsesDurability;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.debug.CraftingLogCategories;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * The Water-filled Wooden Bucket. Used on a block (not sneaking) it pours its water like vanilla's bucket, costing 1
 * use, and gives back the empty bucket with only its uses tag: other mods' water data leaves with the water. Sneaking,
 * or aiming at the air, is left to other mods (Food's stone-boiling pot, Vitality's drinking).
 */
public final class WoodenWaterBucketItem extends Item {
    private static final ImpactsLog LOG = ImpactsLog.of(CraftingMod.MOD_ID);
    private static final int FULL_BAR_WIDTH = 13;
    // Water blue, so a filled bucket's wear reads apart from the empty bucket's green-to-red bar.
    private static final int BAR_COLOR = 0x3F76E4;

    public WoodenWaterBucketItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !UsesDurability.isUnused(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(FULL_BAR_WIDTH * (float) WoodenBucketItem.usesLeft(stack) / CraftingServerConfig.get().woodenBucketUses.get());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BAR_COLOR;
    }

    // Forge deprecates vanilla's emptyContents for its own overload; loader-independent code calls vanilla's.
    @Override
    @SuppressWarnings("deprecation")
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        BlockHitResult target = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (player.isShiftKeyDown() || target.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(held);
        }
        BlockPos clicked = target.getBlockPos();
        BlockState clickedState = level.getBlockState(clicked);
        boolean fillsClicked = clickedState.getBlock() instanceof LiquidBlockContainer container
                && container.canPlaceLiquid(level, clicked, clickedState, Fluids.WATER);
        BlockPos pourAt = fillsClicked ? clicked : clicked.relative(target.getDirection());
        if (!level.mayInteract(player, clicked) || !player.mayUseItemAt(pourAt, target.getDirection(), held)) {
            return InteractionResultHolder.fail(held);
        }
        // Vanilla's water bucket places water exactly as vanilla does (waterlogging, the Nether boiling it away).
        boolean hasPoured = ((BucketItem) Items.WATER_BUCKET).emptyContents(player, level, pourAt, target);
        if (!hasPoured) {
            return InteractionResultHolder.fail(held);
        }
        if (player.getAbilities().instabuild) {
            return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
        }
        return InteractionResultHolder.sidedSuccess(emptied(held, player, level, pourAt), level.isClientSide());
    }

    /** The bucket after pouring: one more use spent, only the uses tag kept, or nothing when it wore out. */
    static ItemStack emptied(ItemStack filled, Player player, Level level, BlockPos pouredAt) {
        int spent = UsesDurability.usesSpent(filled) + 1;
        boolean isWornOut = spent >= CraftingServerConfig.get().woodenBucketUses.get();
        if (isWornOut) {
            level.playSound(null, pouredAt, SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8F, 0.8F);
            LOG.debug(CraftingLogCategories.BUCKET, "poured", "player", player.getScoreboardName(), "usesLeft", 0, "broke", true);
            return ItemStack.EMPTY;
        }
        ItemStack empty = new ItemStack(CraftingItems.woodenBucket());
        empty.getOrCreateTag().putInt(UsesDurability.USES_KEY, spent);
        LOG.debug(CraftingLogCategories.BUCKET, "poured", "player", player.getScoreboardName(), "usesLeft", UsesDurability.get(empty),
                "pos", pouredAt.toShortString());
        return empty;
    }
}
