package com.impact.realisticinventory.mixin;

import com.impact.realisticinventory.inventory.PickupRefusal;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Notices when a touched item can't be picked up at all, for the "Pockets full" message: the stack's count just before
 * and just after vanilla tries to add it to the inventory. Plain injections, so other mods' mixins on the same call
 * still apply.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityPickupMixin {
    private static final String INVENTORY_ADD = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z";

    @Unique
    private int impactsinventory$countBeforeAdd;

    @Shadow
    public abstract ItemStack getItem();

    @Inject(method = "playerTouch", at = @At(value = "INVOKE", target = INVENTORY_ADD))
    private void impactsinventory$countBeforeAdd(Player player, CallbackInfo callback) {
        impactsinventory$countBeforeAdd = getItem().getCount();
    }

    @Inject(method = "playerTouch", at = @At(value = "INVOKE", target = INVENTORY_ADD, shift = At.Shift.AFTER))
    private void impactsinventory$noticeFullPockets(Player player, CallbackInfo callback) {
        ItemStack stack = getItem();
        boolean isNothingPickedUp = !stack.isEmpty() && stack.getCount() == impactsinventory$countBeforeAdd;
        if (isNothingPickedUp && player instanceof ServerPlayer serverPlayer) {
            PickupRefusal.onRefused(serverPlayer, stack);
        }
    }
}
