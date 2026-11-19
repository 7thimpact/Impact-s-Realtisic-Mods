package com.impact.realisticfood.mixin;

import com.impact.realisticfood.spit.Spits;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Spit clicks run before vanilla's campfire click, and a broken campfire drops its spit. */
@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void impactsfood$spitClick(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand, BlockHitResult hit,
                                       CallbackInfoReturnable<InteractionResult> callback) {
        InteractionResult result = Spits.use(state, level, position, player, hand);
        if (result != InteractionResult.PASS) {
            callback.setReturnValue(result);
        }
    }

    @Inject(method = "onRemove", at = @At("HEAD"))
    private void impactsfood$dropSpit(BlockState state, Level level, BlockPos position, BlockState newState, boolean isMoving, CallbackInfo callback) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(position) instanceof CampfireBlockEntity campfire) {
            Spits.onCampfireRemoved(level, position, campfire);
        }
    }
}
