package com.impact.realisticinventory.mixin;

import com.impact.realisticinventory.inventory.PickupRefusal;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Notices when a touched item can't be picked up at all, for the "Pockets full" message. */
@Mixin(ItemEntity.class)
public abstract class ItemEntityPickupMixin {
    @Redirect(method = "playerTouch", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean impactsinventory$noticeFullPockets(Inventory inventory, ItemStack stack, Player player) {
        int countBefore = stack.getCount();
        boolean hasAddedAny = inventory.add(stack);
        boolean isNothingPickedUp = !hasAddedAny && stack.getCount() == countBefore;
        if (isNothingPickedUp && player instanceof ServerPlayer serverPlayer) {
            PickupRefusal.onRefused(serverPlayer, stack);
        }
        return hasAddedAny;
    }
}
