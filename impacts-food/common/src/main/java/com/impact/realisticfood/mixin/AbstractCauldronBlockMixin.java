package com.impact.realisticfood.mixin;

import com.impact.realisticfood.boiling.CauldronInteractions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Food's cauldron clicks run before vanilla's cauldron interactions, and only take over for items Food boils. */
@Mixin(AbstractCauldronBlock.class)
public abstract class AbstractCauldronBlockMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void impactsfood$boilOnClick(BlockState state, Level level, BlockPos position, Player player, InteractionHand hand, BlockHitResult hit,
                                         CallbackInfoReturnable<InteractionResult> callback) {
        InteractionResult result = CauldronInteractions.use(state, level, position, player, hand);
        if (result != InteractionResult.PASS) {
            callback.setReturnValue(result);
        }
    }
}
