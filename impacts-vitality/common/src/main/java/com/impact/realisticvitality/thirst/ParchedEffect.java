package com.impact.realisticvitality.thirst;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Parched: drains thirst every tick, as Hunger drains food. Milk clears it, as it clears every effect. */
public final class ParchedEffect extends MobEffect {
    private static final int DRY_GREEN = 0x8DA35A;

    public ParchedEffect() {
        super(MobEffectCategory.HARMFUL, DRY_GREEN);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            ThirstDrain.addParchedExhaustion(player, amplifier);
        }
    }

    @Override
    public boolean isDurationEffectTick(int remainingTicks, int amplifier) {
        return true;
    }
}
