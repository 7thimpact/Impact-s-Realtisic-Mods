package com.impact.realisticvitality.thirst.block;

import com.impact.realisticvitality.registry.VitalityBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/** The Rain Barrel: collects clean rainwater under open sky. Fill bottles and containers from it; comparators read how full it is. */
public final class RainBarrelBlock extends BaseEntityBlock {
    /** How full it looks, in quarters (0 is empty). */
    public static final IntegerProperty FILL = IntegerProperty.create("fill", 0, 4);

    public RainBarrelBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FILL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FILL);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new RainBarrelBlockEntity(position, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, VitalityBlocks.rainBarrelEntity(), RainBarrelBlockEntity::serverTick);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(position) instanceof RainBarrelBlockEntity barrel)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return barrel.canFill(player.getItemInHand(hand)) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return barrel.fillHeld(player, hand) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos position) {
        return level.getBlockEntity(position) instanceof RainBarrelBlockEntity barrel ? barrel.comparatorSignal() : 0;
    }
}
