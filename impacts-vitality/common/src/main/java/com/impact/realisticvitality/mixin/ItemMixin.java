package com.impact.realisticvitality.mixin;

import com.impact.realisticvitality.thirst.WoodenBuckets;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Drinking from Crafting's Water-filled Wooden Bucket, which Vitality doesn't own and Crafting gives no use time: these
 * vanilla defaults (the bucket overrides none of them) make it drink like a bottle once Vitality starts the use.
 */
@Mixin(Item.class)
abstract class ItemMixin {
    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void impactsvitality$woodenBucketDrinkTicks(ItemStack stack, CallbackInfoReturnable<Integer> callback) {
        if (WoodenBuckets.isWaterBucket(stack)) {
            callback.setReturnValue(WoodenBuckets.DRINK_TICKS);
        }
    }

    @Inject(method = "getUseAnimation", at = @At("HEAD"), cancellable = true)
    private void impactsvitality$woodenBucketDrinkAnimation(ItemStack stack, CallbackInfoReturnable<UseAnim> callback) {
        if (WoodenBuckets.isWaterBucket(stack)) {
            callback.setReturnValue(UseAnim.DRINK);
        }
    }

    @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
    private void impactsvitality$woodenBucketDrinkTaken(ItemStack stack, Level level, LivingEntity drinker, CallbackInfoReturnable<ItemStack> callback) {
        if (WoodenBuckets.isWaterBucket(stack) && drinker instanceof Player player) {
            callback.setReturnValue(WoodenBuckets.finishDrink(stack, player));
        }
    }
}
