package com.impact.realisticinventory.forge.mixin;

import com.impact.realisticinventory.forge.ForgeGiveItem;
import com.impact.realisticinventory.inventory.InventoryLocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** {@code giveItemToPlayer} reaches the worn pack too: on the server it goes through Lib's Give or drop under the locks. */
@Mixin(value = ItemHandlerHelper.class, remap = false)
public abstract class ItemHandlerHelperMixin {
    @Inject(method = "giveItemToPlayer(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V",
            at = @At("HEAD"), cancellable = true)
    private static void impactsinventory$giveThroughGiveOrDrop(Player player, ItemStack stack, int preferredSlot, CallbackInfo callback) {
        if (stack.isEmpty() || player.level().isClientSide() || !InventoryLocks.isActiveFor(player)) {
            return;
        }
        callback.cancel();
        ForgeGiveItem.give(player, stack, preferredSlot);
    }
}
