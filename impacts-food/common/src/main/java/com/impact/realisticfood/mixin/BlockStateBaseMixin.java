package com.impact.realisticfood.mixin;

import com.impact.realisticfood.boiling.CauldronPots;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla cauldrons have no block entity and don't tick, so Food watches their block changes: a water cauldron filled
 * or refilled, a heat source changing under one, and a water cauldron emptied or broken.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Inject(method = "onPlace", at = @At("TAIL"))
    private void impactsfood$waterCauldronPlaced(Level level, BlockPos position, BlockState oldState, boolean isMoving, CallbackInfo callback) {
        if (level instanceof ServerLevel serverLevel && CauldronPots.isWaterCauldron(self())) {
            CauldronPots.onWaterCauldronUpdated(serverLevel, position);
        }
    }

    @Inject(method = "neighborChanged", at = @At("HEAD"))
    private void impactsfood$heatChangedBelow(Level level, BlockPos position, Block neighborBlock, BlockPos neighborPosition, boolean isMoving,
                                               CallbackInfo callback) {
        boolean isFromBelow = neighborPosition.equals(position.below());
        if (isFromBelow && level instanceof ServerLevel serverLevel && CauldronPots.isWaterCauldron(self())) {
            CauldronPots.onWaterCauldronUpdated(serverLevel, position);
        }
    }

    @Inject(method = "onRemove", at = @At("HEAD"))
    private void impactsfood$waterCauldronRemoved(Level level, BlockPos position, BlockState newState, boolean isMoving, CallbackInfo callback) {
        boolean isStillWater = CauldronPots.isWaterCauldron(newState);
        if (!isStillWater && level instanceof ServerLevel serverLevel && CauldronPots.isWaterCauldron(self())) {
            CauldronPots.onWaterCauldronRemoved(serverLevel, position);
        }
    }

    private BlockState self() {
        return (BlockState) (Object) this;
    }
}
