package com.impact.realisticinventory.pack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/**
 * A pack set down on the ground. Right-click opens it like a small chest; sneak-right-click with an empty hand (handled
 * by the loader's click event, since vanilla skips blocks while sneaking with anything in hand) picks it back up.
 * Breaking it, or a piston, drops the pack with its contents. The pack's kind sets its size and shape; the block
 * entity draws it in the pack's dye color.
 */
// Vanilla marks block behavior methods deprecated to mean "call them through BlockState"; overriding them is intended.
@SuppressWarnings("deprecation")
public final class PlacedPackBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<PackKind> KIND = EnumProperty.create("kind", PackKind.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final Map<PackKind, VoxelShape> SOUTH_FACING_SHAPES = new EnumMap<>(Map.of(
            PackKind.SATCHEL, Block.box(5, 0, 7, 11, 5, 9),
            PackKind.BACKPACK, Block.box(4, 0, 5.5, 12, 10, 10.5),
            PackKind.FRAME_PACK, Block.box(3, 0, 6, 13, 16, 10)));
    private static final Map<PackKind, VoxelShape> EAST_FACING_SHAPES = new EnumMap<>(Map.of(
            PackKind.SATCHEL, Block.box(7, 0, 5, 9, 5, 11),
            PackKind.BACKPACK, Block.box(5.5, 0, 4, 10.5, 10, 12),
            PackKind.FRAME_PACK, Block.box(6, 0, 3, 10, 16, 13)));

    public PlacedPackBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(KIND, PackKind.BACKPACK)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, KIND, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos position, CollisionContext context) {
        boolean isAlongX = state.getValue(FACING).getAxis() == Direction.Axis.X;
        return (isAlongX ? EAST_FACING_SHAPES : SOUTH_FACING_SHAPES).get(state.getValue(KIND));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new PlacedPackBlockEntity(position, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(position) instanceof PlacedPackBlockEntity placedPack) {
            PackMenus.openPlacedPack(serverPlayer, placedPack);
        }
        return InteractionResult.CONSUME;
    }

    /** The block went away some other way than being picked up (broken, pushed, exploded): the pack drops whole. */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos position, BlockState newState, boolean isMoving) {
        boolean isStillThisBlock = state.is(newState.getBlock());
        if (!isStillThisBlock && level.getBlockEntity(position) instanceof PlacedPackBlockEntity placedPack && !placedPack.pack().isEmpty()) {
            ItemStack packWithContents = placedPack.takePack();
            // Logged first: dropping empties the stack.
            PlacedPacks.logDropped(level, position, packWithContents);
            Containers.dropItemStack(level, position.getX(), position.getY(), position.getZ(), packWithContents);
        }
        super.onRemove(state, level, position, newState, isMoving);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos position, BlockState state) {
        return new ItemStack(InventoryItems.pack(state.getValue(KIND)));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos position,
                                  BlockPos neighborPosition) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(position, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, position, neighborPosition);
    }
}
