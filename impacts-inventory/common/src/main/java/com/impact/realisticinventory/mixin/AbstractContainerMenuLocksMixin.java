package com.impact.realisticinventory.mixin;

import com.impact.realisticinventory.inventory.MenuClickLocks;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/** Refuses clicks into locked slots in every menu, and makes shift-click fill the pockets before the main hand. */
@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuLocksMixin {
    @Shadow
    @Final
    public NonNullList<Slot> slots;

    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void impactsinventory$refuseLockedClicks(int slotId, int button, ClickType clickType, Player player, CallbackInfo callback) {
        if (MenuClickLocks.isRefused((AbstractContainerMenu) (Object) this, slotId, button, clickType, player)) {
            callback.cancel();
        }
    }

    @Inject(method = "moveItemStackTo", at = @At("HEAD"), cancellable = true)
    private void impactsinventory$fillPocketsFirst(ItemStack stack, int startIndex, int endIndex, boolean isReversed,
                                                  CallbackInfoReturnable<Boolean> callback) {
        Optional<Boolean> hasMoved = MenuClickLocks.moveIntoPlayerSlots(slots, stack, startIndex, endIndex);
        hasMoved.ifPresent(callback::setReturnValue);
    }
}
