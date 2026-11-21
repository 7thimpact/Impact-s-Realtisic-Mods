package com.impact.realisticinventory.forge.mixin;

import com.impact.realisticinventory.inventory.InventoryLayout;
import com.impact.realisticinventory.inventory.InventoryLocks;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mods that insert through the player's item-handler capability (including {@code ItemHandlerHelper.giveItemToPlayer})
 * skip locked indexes and fall back to their own handling, usually dropping the item.
 */
@Mixin(PlayerMainInvWrapper.class)
public abstract class PlayerMainInvWrapperMixin extends RangedWrapper {
    @Shadow(remap = false)
    @Final
    private Inventory inventoryPlayer;

    private PlayerMainInvWrapperMixin(IItemHandlerModifiable compose, int minSlot, int maxSlotExclusive) {
        super(compose, minSlot, maxSlotExclusive);
    }

    @Inject(method = "insertItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void impactsinventory$refuseLockedInsert(int slot, ItemStack stack, boolean isSimulated, CallbackInfoReturnable<ItemStack> callback) {
        if (isLockedSlot(slot)) {
            callback.setReturnValue(stack);
        }
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return !isLockedSlot(slot) && super.isItemValid(slot, stack);
    }

    private boolean isLockedSlot(int slot) {
        return InventoryLayout.isLocked(slot) && InventoryLocks.isActiveFor(inventoryPlayer.player);
    }
}
