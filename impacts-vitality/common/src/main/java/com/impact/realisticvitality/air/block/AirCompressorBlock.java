package com.impact.realisticvitality.air.block;

import com.impact.realisticvitality.registry.VitalityBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The Air Compressor: burns furnace fuel to fill an Air Tank in 5 seconds, even in thin air. Use a tank on it to put it
 * in, fuel to feed it, and an empty hand to take the tank back.
 */
public final class AirCompressorBlock extends BaseEntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public AirCompressorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new AirCompressorBlockEntity(position, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, VitalityBlocks.airCompressorEntity(), AirCompressorBlockEntity::serverTick);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(position) instanceof AirCompressorBlockEntity compressor)) {
            return InteractionResult.PASS;
        }
        return compressor.use(player, hand) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos position, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(position) instanceof AirCompressorBlockEntity compressor) {
            Containers.dropItemStack(level, position.getX(), position.getY(), position.getZ(), compressor.takeTank());
        }
        super.onRemove(state, level, position, newState, isMoving);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos position, RandomSource random) {
        if (state.getValue(LIT) && random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.SMOKE, position.getX() + 0.5, position.getY() + 1.05, position.getZ() + 0.5, 0.0, 0.02, 0.0);
        }
    }
}
