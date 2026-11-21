package com.impact.realisticvitality.mixin;

import com.impact.realisticvitality.thirst.ThirstDrain;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Every action that costs food exhaustion costs thirst exhaustion too. */
@Mixin(Player.class)
abstract class PlayerMixin {
    @Inject(method = "causeFoodExhaustion", at = @At("HEAD"))
    private void impactsvitality$addThirstExhaustion(float exhaustion, CallbackInfo callback) {
        ThirstDrain.onFoodExhaustion((Player) (Object) this, exhaustion);
    }
}
