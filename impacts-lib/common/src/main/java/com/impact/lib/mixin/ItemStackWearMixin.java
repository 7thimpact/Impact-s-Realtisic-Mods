package com.impact.lib.mixin;

import com.impact.lib.item.ConfiguredDurabilityItem;
import com.impact.lib.item.UsesDurability;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Two kinds of wear vanilla can't express per stack: uses-tracked items stack to 1 once used, and items with
 * config-driven durability report the configured maximum.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackWearMixin {
    private static final int WORN_STACK_SIZE = 1;

    @Shadow
    public abstract Item getItem();

    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void impactslib$wornItemsStackToOne(CallbackInfoReturnable<Integer> callback) {
        ItemStack stack = (ItemStack) (Object) this;
        if (UsesDurability.isTracked(stack) && !UsesDurability.isUnused(stack)) {
            callback.setReturnValue(WORN_STACK_SIZE);
        }
    }

    @Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
    private void impactslib$configuredDurability(CallbackInfoReturnable<Integer> callback) {
        if (getItem() instanceof ConfiguredDurabilityItem configured) {
            callback.setReturnValue(configured.configuredMaxDamage());
        }
    }
}
