package com.impact.realisticfood.stoneboiling;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * {@code impactsfood:stone_boiling_pot}: Crafting's Water-filled Wooden Bucket set down on a solid block. Hot Pebbles
 * dropped in bring its water to a boil (a wooden bucket would burn on a fire). It has no item: it is placed with the
 * bucket and taken back as one, and it pops off, dropping the bucket and what's in it, when the block under it goes.
 * Vanilla deprecates calling these block methods directly, not overriding them.
 */
@SuppressWarnings("deprecation")
public final class StoneBoilingPotBlock extends BaseEntityBlock {
    public static final BooleanProperty HAS_WATER = BooleanProperty.create("has_water");
    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 10.0, 12.0);

    public StoneBoilingPotBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HAS_WATER, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_WATER);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos position, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new StoneBoilingPotBlockEntity(position, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, StoneBoilingPots.blockEntityType(), StoneBoilingPotBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand, BlockHitResult hit) {
        return StoneBoilingPots.use(state, level, position, player, hand);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos position) {
        return StoneBoilingPots.canStandOn(level, position.below());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos position,
                                  BlockPos neighborPosition) {
        if (direction == Direction.DOWN && !canSurvive(state, level, position)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, position, neighborPosition);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos position, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(position) instanceof StoneBoilingPotBlockEntity potEntity) {
            potEntity.drops().forEach(drop -> Block.popResource(level, position, drop));
        }
        super.onRemove(state, level, position, newState, isMoving);
    }
}
