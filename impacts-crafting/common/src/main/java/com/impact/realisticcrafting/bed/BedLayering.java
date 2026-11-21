package com.impact.realisticcrafting.bed;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.CraftingFeature;
import com.impact.realisticcrafting.debug.CraftingLogCategories;
import com.impact.realisticcrafting.item.SheetItem;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

import java.util.Optional;

/**
 * Building a bed in the world: a Comforter on a Bed Frame, then a Sheet, which turns both halves into the vanilla bed of
 * the sheet's color. A sheet on a bare frame is refused with a hint. A different sheet on a finished bed recolors it, and
 * the old sheet drops.
 */
public final class BedLayering {
    public static final String COMFORTER_FIRST_KEY = "message." + CraftingMod.MOD_ID + ".comforter_first";
    private static final ImpactsLog LOG = ImpactsLog.of(CraftingMod.MOD_ID);
    // Both halves are swapped before either reshapes its neighbor, so neither breaks the other.
    private static final int SWAP_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    private BedLayering() {
    }

    public static InteractionResult onFrameUsed(Level level, BlockPos position, BlockState frame, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        boolean isComforter = held.is(CraftingItems.comforter());
        if (!CraftingFeature.BEDS.isOn() || !isComforter && !(held.getItem() instanceof SheetItem)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        return isComforter ? layComforter(level, position, frame, player, held) : laySheet(level, position, frame, player, held);
    }

    /** A sheet used on a finished vanilla bed of another color: the bed takes the new color and the old sheet drops. */
    public static InteractionResult onBedUsed(Level level, BlockPos position, BlockState bed, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!CraftingFeature.BEDS.isOn() || !(held.getItem() instanceof SheetItem sheet) || !(bed.getBlock() instanceof BedBlock bedBlock)
                || bedBlock.getColor() == sheet.color()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        DyeColor oldColor = bedBlock.getColor();
        BlockPos otherHalf = position.relative(BedBlock.getConnectedDirection(bed));
        finishBed(level, position, bed.getValue(BedBlock.FACING), bed.getValue(BedBlock.PART), otherHalf, sheet.color());
        Block.popResource(level, position, new ItemStack(CraftingItems.sheet(oldColor)));
        consumeOne(held, player);
        LOG.debug(CraftingLogCategories.BEDS, "bed recolored", "player", player.getScoreboardName(), "from", oldColor.getName(),
                "to", sheet.color().getName(), "pos", position.toShortString());
        BedEvents.onBedAssembled(player, sheet.color());
        return InteractionResult.CONSUME;
    }

    private static InteractionResult layComforter(Level level, BlockPos position, BlockState frame, Player player, ItemStack comforter) {
        if (frame.getValue(BedFrameBlock.COMFORTER)) {
            return InteractionResult.PASS;
        }
        BlockPos otherHalf = position.relative(BedFrameBlock.towardOtherHalf(frame));
        level.setBlock(position, frame.setValue(BedFrameBlock.COMFORTER, true), Block.UPDATE_ALL);
        BlockState other = level.getBlockState(otherHalf);
        if (other.getBlock() instanceof BedFrameBlock) {
            level.setBlock(otherHalf, other.setValue(BedFrameBlock.COMFORTER, true), Block.UPDATE_ALL);
        }
        consumeOne(comforter, player);
        level.playSound(null, position, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        LOG.debug(CraftingLogCategories.BEDS, "comforter laid", "player", player.getScoreboardName(), "pos", position.toShortString());
        return InteractionResult.CONSUME;
    }

    private static InteractionResult laySheet(Level level, BlockPos position, BlockState frame, Player player, ItemStack sheetStack) {
        if (!frame.getValue(BedFrameBlock.COMFORTER)) {
            player.displayClientMessage(Component.translatable(COMFORTER_FIRST_KEY), true);
            LOG.debug(CraftingLogCategories.BEDS, "sheet refused", "player", player.getScoreboardName(), "reason", "no comforter");
            return InteractionResult.FAIL;
        }
        DyeColor color = ((SheetItem) sheetStack.getItem()).color();
        BlockPos otherHalf = position.relative(BedFrameBlock.towardOtherHalf(frame));
        finishBed(level, position, frame.getValue(BedFrameBlock.FACING), frame.getValue(BedFrameBlock.PART), otherHalf, color);
        consumeOne(sheetStack, player);
        level.playSound(null, position, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
        LOG.debug(CraftingLogCategories.BEDS, "bed finished", "player", player.getScoreboardName(), "color", color.getName(),
                "pos", position.toShortString());
        BedEvents.onBedAssembled(player, color);
        return InteractionResult.CONSUME;
    }

    // Both halves become the color's vanilla bed, facing the same way.
    private static void finishBed(Level level, BlockPos position, Direction facing, BedPart part, BlockPos otherHalf, DyeColor color) {
        Optional<Block> bed = ResourceIds.block(ResourceIds.of("minecraft", color.getName() + "_bed"));
        if (bed.isEmpty()) {
            return;
        }
        BlockState clicked = bed.get().defaultBlockState().setValue(BedBlock.FACING, facing).setValue(BedBlock.PART, part)
                .setValue(BedBlock.OCCUPIED, false);
        BlockState other = clicked.setValue(BedBlock.PART, part == BedPart.FOOT ? BedPart.HEAD : BedPart.FOOT);
        level.setBlock(otherHalf, other, SWAP_FLAGS);
        level.setBlock(position, clicked, SWAP_FLAGS);
        clicked.updateNeighbourShapes(level, position, Block.UPDATE_ALL);
        other.updateNeighbourShapes(level, otherHalf, Block.UPDATE_ALL);
    }

    private static void consumeOne(ItemStack held, Player player) {
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
    }
}
