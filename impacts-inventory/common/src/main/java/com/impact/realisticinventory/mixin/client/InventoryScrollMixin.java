package com.impact.realisticinventory.mixin.client;

import com.impact.realisticinventory.client.LocalHands;
import com.impact.realisticinventory.inventory.InventoryLocks;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The scroll wheel swaps the hands, in either direction, exactly like vanilla's swap-hands key. Client only: the method is client side. */
@Mixin(Inventory.class)
public abstract class InventoryScrollMixin {
    @Inject(method = "swapPaint", at = @At("HEAD"), cancellable = true)
    private void impactsinventory$switchOnScroll(double scrollAmount, CallbackInfo callback) {
        Inventory inventory = (Inventory) (Object) this;
        if (!InventoryLocks.isActiveFor(inventory.player)) {
            return;
        }
        LocalHands.requestSwap();
        callback.cancel();
    }
}
