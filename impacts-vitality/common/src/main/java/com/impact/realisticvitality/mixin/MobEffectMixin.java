package com.impact.realisticvitality.mixin;

import com.impact.realisticvitality.thirst.ThirstDrain;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The Hunger effect drains food through the same call as actions do; this marks it so it costs no thirst. */
@Mixin(MobEffect.class)
abstract class MobEffectMixin {
    @Inject(method = "applyEffectTick", at = @At("HEAD"))
    private void impactsvitality$markHungerEffect(LivingEntity entity, int amplifier, CallbackInfo callback) {
        ThirstDrain.setApplyingHungerEffect((Object) this == MobEffects.HUNGER);
    }

    @Inject(method = "applyEffectTick", at = @At("RETURN"))
    private void impactsvitality$clearHungerEffect(LivingEntity entity, int amplifier, CallbackInfo callback) {
        ThirstDrain.setApplyingHungerEffect(false);
    }
}
