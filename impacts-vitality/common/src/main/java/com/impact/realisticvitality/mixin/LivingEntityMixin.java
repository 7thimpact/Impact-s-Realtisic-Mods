package com.impact.realisticvitality.mixin;

import com.impact.realisticvitality.air.AirQuality;
import com.impact.realisticvitality.air.AirTicks;
import com.impact.realisticvitality.client.ClientVitality;
import com.impact.realisticvitality.player.VitalityState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Out of water vanilla refills air every tick; in bad air a player isn't protected from, it doesn't. */
@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
    @Inject(method = "increaseAirSupply", at = @At("HEAD"), cancellable = true)
    private void impactsvitality$holdBackRefill(int currentAir, CallbackInfoReturnable<Integer> callback) {
        if (!((Object) this instanceof Player player)) {
            return;
        }
        AirQuality quality = player.level().isClientSide ? ClientVitality.state().airQuality() : VitalityState.of(player).air.quality();
        if (AirTicks.holdsBackRefill(player, quality)) {
            callback.setReturnValue(currentAir);
        }
    }
}
