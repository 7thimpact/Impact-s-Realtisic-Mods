package com.impact.realisticfood.mixin;

import com.impact.realisticfood.boiling.CauldronInteractions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Items dropped into a hot water cauldron go into its pot when a boiling recipe takes them. */
@Mixin(LayeredCauldronBlock.class)
public abstract class LayeredCauldronBlockMixin {
    @Inject(method = "entityInside", at = @At("HEAD"))
    private void impactsfood$takeDroppedItems(BlockState state, Level level, BlockPos position, Entity entity, CallbackInfo callback) {
        if (level instanceof ServerLevel serverLevel && entity instanceof ItemEntity itemEntity && itemEntity.isAlive()) {
            CauldronInteractions.onItemInWater(serverLevel, position, state, itemEntity);
        }
    }
}
