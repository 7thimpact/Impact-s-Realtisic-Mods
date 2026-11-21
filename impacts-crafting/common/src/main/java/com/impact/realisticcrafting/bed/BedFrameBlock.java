package com.impact.realisticcrafting.bed;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Bed Frame: a bed's first layer, placed like a bed (2 blocks, facing away from the player). A Comforter goes on
 * top, then a Sheet turns both halves into that color's vanilla bed. You can't sleep in a frame.
 */
public final class BedFrameBlock extends HorizontalDirectionalBlock {
    public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;
    public static final BooleanProperty COMFORTER = BooleanProperty.create("comforter");
    private static final VoxelShape BARE_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0);
    private static final VoxelShape COMFORTER_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 9.0, 16.0);
    // Like vanilla's beds: the other half goes in without reshaping its neighbors yet.
    private static final int QUIET_PLACEMENT = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;

    public BedFrameBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, BedPart.FOOT).setValue(COMFORTER, false));
    }

    /** From one half, the direction of the other half. */
    public static Direction towardOtherHalf(BlockState state) {
        Direction facing = state.getValue(FACING);
        return state.getValue(PART) == BedPart.FOOT ? facing : facing.getOpposite();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, COMFORTER);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockPos headPosition = context.getClickedPos().relative(facing);
        Level level = context.getLevel();
        boolean hasRoom = level.getBlockState(headPosition).canBeReplaced(context) && level.getWorldBorder().isWithinBounds(headPosition);
        return hasRoom ? defaultBlockState().setValue(FACING, facing) : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos position, BlockState state, LivingEntity placer, ItemStack stack) {
        if (level.isClientSide) {
            return;
        }
        BlockPos headPosition = position.relative(state.getValue(FACING));
        level.setBlock(headPosition, state.setValue(PART, BedPart.HEAD), Block.UPDATE_ALL);
        level.blockUpdated(position, Blocks.AIR);
        state.updateNeighbourShapes(level, position, Block.UPDATE_ALL);
    }

    // Each half lives only with its other half; the comforter shows on both.
    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos position,
                                  BlockPos neighborPosition) {
        if (direction != towardOtherHalf(state)) {
            return super.updateShape(state, direction, neighbor, level, position, neighborPosition);
        }
        boolean isOtherHalf = neighbor.is(this) && neighbor.getValue(PART) != state.getValue(PART);
        return isOtherHalf ? state.setValue(COMFORTER, neighbor.getValue(COMFORTER)) : Blocks.AIR.defaultBlockState();
    }

    // Like vanilla's beds: a creative player breaking the foot doesn't drop the head's items.
    @Override
    public void playerWillDestroy(Level level, BlockPos position, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative() && state.getValue(PART) == BedPart.FOOT) {
            BlockPos headPosition = position.relative(towardOtherHalf(state));
            BlockState head = level.getBlockState(headPosition);
            if (head.is(this) && head.getValue(PART) == BedPart.HEAD) {
                level.setBlock(headPosition, Blocks.AIR.defaultBlockState(), QUIET_PLACEMENT | Block.UPDATE_NEIGHBORS);
                level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, headPosition, Block.getId(head));
            }
        }
        super.playerWillDestroy(level, position, state, player);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos position, CollisionContext context) {
        return state.getValue(COMFORTER) ? COMFORTER_SHAPE : BARE_SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand, BlockHitResult hit) {
        return BedLayering.onFrameUsed(level, position, state, player, hand);
    }
}
