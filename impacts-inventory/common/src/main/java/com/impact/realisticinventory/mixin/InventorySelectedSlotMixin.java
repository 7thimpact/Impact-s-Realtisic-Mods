package com.impact.realisticinventory.mixin;

import com.impact.realisticinventory.hotbar.SelectedSlotGuard;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Undoes changes to the selected hotbar index every tick. */
@Mixin(Inventory.class)
public abstract class InventorySelectedSlotMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void impactsinventory$keepMainHandSelected(CallbackInfo callback) {
        SelectedSlotGuard.enforce((Inventory) (Object) this);
    }
}
