package com.impact.realisticvitality.thirst.block;

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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The Water Filter: pour unclean or salt water in, and with a Charcoal Filter inside it drips out purified water.
 * Use a Charcoal Filter on it to put one in, and sneak-use it with an empty hand to take the filter out.
 */
public final class WaterFilterBlock extends BaseEntityBlock {
    public static final BooleanProperty FILTERING = BooleanProperty.create("filtering");
    private static final double DRIP_INSET = 0.3;

    public WaterFilterBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FILTERING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FILTERING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new WaterFilterBlockEntity(position, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, VitalityBlocks.waterFilterEntity(), WaterFilterBlockEntity::serverTick);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(position) instanceof WaterFilterBlockEntity filter)) {
            return InteractionResult.PASS;
        }
        return filter.use(player, hand) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos position, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(position) instanceof WaterFilterBlockEntity filter) {
            Containers.dropItemStack(level, position.getX(), position.getY(), position.getZ(), filter.removeCharcoalFilter());
        }
        super.onRemove(state, level, position, newState, isMoving);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos position, RandomSource random) {
        if (!state.getValue(FILTERING)) {
            return;
        }
        double x = position.getX() + DRIP_INSET + random.nextDouble() * (1 - 2 * DRIP_INSET);
        double z = position.getZ() + DRIP_INSET + random.nextDouble() * (1 - 2 * DRIP_INSET);
        level.addParticle(ParticleTypes.DRIPPING_WATER, x, position.getY() - 0.05, z, 0.0, 0.0, 0.0);
    }
}
