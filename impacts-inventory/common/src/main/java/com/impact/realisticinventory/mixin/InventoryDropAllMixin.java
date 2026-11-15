package com.impact.realisticinventory.mixin;

import com.impact.realisticinventory.pack.BackSlotStorage;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Vanilla drops the whole inventory on death (without keepInventory); the back slot drops with it. */
@Mixin(Inventory.class)
public abstract class InventoryDropAllMixin {
    @Shadow
    @Final
    public Player player;

    @Inject(method = "dropAll", at = @At("TAIL"))
    private void impactsinventory$dropBackSlot(CallbackInfo callback) {
        BackSlotStorage.dropOnDeath(player);
    }
}
