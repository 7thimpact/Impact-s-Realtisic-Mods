package com.impact.realisticinventory.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A chest placed at the player's feet for items that fit neither the pockets nor a worn pack. It goes beside the player
 * if there's room (a spot a player could build in: air, grass, snow, water), else where the player stands.
 */
final class OverflowChest {
    private static final int CHEST_SIZE = 27;

    private OverflowChest() {
    }

    /** Places a chest and moves as many of the stacks into it as fit. Returns where, or nothing when there's no room. */
    static Optional<BlockPos> placeWith(ServerPlayer player, List<ItemStack> stacks) {
        ServerLevel level = player.serverLevel();
        Optional<BlockPos> chestPosition = candidatePositions(player.blockPosition()).stream()
                .filter(position -> canHoldChest(level, position))
                .findFirst();
        chestPosition.ifPresent(position -> fillNewChest(level, position, player.getDirection().getOpposite(), stacks));
        return chestPosition;
    }

    private static List<BlockPos> candidatePositions(BlockPos feet) {
        List<BlockPos> candidates = new ArrayList<>();
        Direction.Plane.HORIZONTAL.forEach(direction -> candidates.add(feet.relative(direction)));
        candidates.add(feet);
        return candidates;
    }

    private static boolean canHoldChest(ServerLevel level, BlockPos position) {
        if (!level.isInWorldBounds(position) || !level.isLoaded(position)) {
            return false;
        }
        BlockState existing = level.getBlockState(position);
        FluidState fluid = existing.getFluidState();
        return existing.canBeReplaced() && (fluid.isEmpty() || fluid.is(FluidTags.WATER));
    }

    private static void fillNewChest(ServerLevel level, BlockPos position, Direction facing, List<ItemStack> stacks) {
        boolean isInWaterSource = level.getFluidState(position).isSourceOfType(Fluids.WATER);
        BlockState chestState = Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, facing)
                .setValue(ChestBlock.WATERLOGGED, isInWaterSource);
        level.setBlock(position, chestState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(position) instanceof ChestBlockEntity chest)) {
            return;
        }
        for (int chestSlot = 0; chestSlot < CHEST_SIZE && !stacks.isEmpty(); chestSlot++) {
            chest.setItem(chestSlot, stacks.remove(0));
        }
    }
}
