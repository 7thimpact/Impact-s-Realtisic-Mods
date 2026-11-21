package com.impact.realisticvitality.mixin;

import com.impact.realisticvitality.health.HealthCap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Natural regeneration under the health cap: once vanilla has paid the tick's exhaustion (just before it reads the
 * naturalRegeneration rule), Vitality runs regeneration and starvation itself and skips vanilla's.
 */
@Mixin(FoodData.class)
abstract class FoodDataMixin {
    @Shadow
    private int tickTimer;

    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"), cancellable = true)
    private void impactsvitality$shareRegeneration(Player player, CallbackInfo callback) {
        if (!HealthCap.isActive()) {
            return;
        }
        tickTimer = HealthCap.tickRegeneration(player, (FoodData) (Object) this, tickTimer);
        callback.cancel();
    }
}
