package com.impact.realisticinventory.mixin;

import com.impact.realisticinventory.inventory.SlotLocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Locked player slots are hidden in every container screen and refuse every kind of click. */
@Mixin(Slot.class)
public abstract class SlotLocksMixin {
    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
    private void impactsinventory$hideLockedSlot(CallbackInfoReturnable<Boolean> callback) {
        if (SlotLocks.isLocked((Slot) (Object) this)) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void impactsinventory$refusePlacing(ItemStack stack, CallbackInfoReturnable<Boolean> callback) {
        if (SlotLocks.isLocked((Slot) (Object) this)) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void impactsinventory$refusePickingUp(Player player, CallbackInfoReturnable<Boolean> callback) {
        if (SlotLocks.isLocked((Slot) (Object) this)) {
            callback.setReturnValue(false);
        }
    }
}
