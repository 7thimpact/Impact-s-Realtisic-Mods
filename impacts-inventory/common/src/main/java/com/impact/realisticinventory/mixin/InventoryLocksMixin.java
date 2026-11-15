package com.impact.realisticinventory.mixin;

import com.impact.realisticinventory.inventory.InventoryLocks;
import com.impact.realisticinventory.inventory.LockedSlotRedirect;
import com.impact.realisticinventory.inventory.PickBlock;
import com.impact.realisticinventory.inventory.PocketPlacement;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Locks vanilla inventory indexes 1 to 8 and 18 to 35: placement skips them, writes into them are redirected, and
 * pick block fills the main hand. Vanilla's 36-slot size is unchanged, so mods that index into it don't break.
 */
@Mixin(Inventory.class)
public abstract class InventoryLocksMixin {
    @Shadow
    @Final
    public Player player;

    @Inject(method = "getFreeSlot", at = @At("HEAD"), cancellable = true)
    private void impactsinventory$freeUnlockedSlot(CallbackInfoReturnable<Integer> callback) {
        if (InventoryLocks.isActiveFor(player)) {
            callback.setReturnValue(PocketPlacement.freeSlot((Inventory) (Object) this));
        }
    }

    @Inject(method = "getSlotWithRemainingSpace", at = @At("HEAD"), cancellable = true)
    private void impactsinventory$unlockedSlotWithSpace(ItemStack stack, CallbackInfoReturnable<Integer> callback) {
        if (InventoryLocks.isActiveFor(player)) {
            callback.setReturnValue(PocketPlacement.slotWithRemainingSpace((Inventory) (Object) this, stack));
        }
    }

    @ModifyVariable(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), argsOnly = true)
    private int impactsinventory$neverAddIntoLockedIndex(int requestedIndex) {
        return LockedSlotRedirect.placementIndexFor((Inventory) (Object) this, requestedIndex);
    }

    @Inject(method = "setItem", at = @At("HEAD"), cancellable = true)
    private void impactsinventory$redirectLockedWrite(int inventoryIndex, ItemStack stack, CallbackInfo callback) {
        Inventory inventory = (Inventory) (Object) this;
        if (!LockedSlotRedirect.isRedirected(inventory, inventoryIndex, stack)) {
            return;
        }
        callback.cancel();
        LockedSlotRedirect.redirect(inventory, inventoryIndex, stack);
    }

    @Inject(method = "pickSlot", at = @At("HEAD"), cancellable = true)
    private void impactsinventory$pickIntoMainHand(int pocketIndex, CallbackInfo callback) {
        if (!InventoryLocks.isActiveFor(player)) {
            return;
        }
        callback.cancel();
        PickBlock.pickFromPocket((Inventory) (Object) this, pocketIndex);
    }
}
