package com.impact.realisticcrafting.mixin;

import com.impact.realisticcrafting.timing.CraftTimers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A crafting output can't be taken (clicked, shift-clicked, thrown or swapped) while its craft runs. */
@Mixin(Slot.class)
public abstract class SlotMixin {
    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void impactscrafting$lockTheRunningCraft(Player player, CallbackInfoReturnable<Boolean> callback) {
        Slot slot = (Slot) (Object) this;
        if (slot instanceof ResultSlot && CraftTimers.isOutputLocked(player, slot)) {
            callback.setReturnValue(false);
        }
    }
}
