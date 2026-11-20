package com.impact.realisticvitality.mixin.client;

import com.impact.realisticvitality.client.ClientThirst;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Low thirst stops sprinting the way low hunger does: vanilla asks this both to start a sprint and to keep one going. */
@Mixin(LocalPlayer.class)
abstract class LocalPlayerMixin {
    @Inject(method = "hasEnoughFoodToStartSprinting", at = @At("RETURN"), cancellable = true)
    private void impactsvitality$checkThirst(CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValueZ() && ClientThirst.isTooThirstyToSprint((LocalPlayer) (Object) this)) {
            callback.setReturnValue(false);
        }
    }
}
