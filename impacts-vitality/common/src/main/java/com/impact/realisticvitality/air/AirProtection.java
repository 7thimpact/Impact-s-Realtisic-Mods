package com.impact.realisticvitality.air;

import com.impact.realisticvitality.config.VitalityServerConfig;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.OptionalInt;

/**
 * How fast a player actually loses bubbles in bad air, after what protects them: Water Breathing and Conduit Power stop
 * the loss (config {@code air.waterBreathingProtects}); each level of Respiration slows it by a third; breathing gear
 * (masks, filters, tanks) answers what's left. Both sides ask this, so the client holds back vanilla's refill exactly as the server does.
 */
public final class AirProtection {
    private AirProtection() {
    }

    /** Ticks per bubble lost, or nothing when the player doesn't lose air here (breathing gear included). */
    public static OptionalInt bubbleTicks(Player player, AirQuality quality) {
        OptionalInt withoutGear = bubbleTicksWithoutGear(player, quality);
        if (withoutGear.isEmpty()) {
            return withoutGear;
        }
        if (quality.isSmoke && BreathingGear.blocksSmoke(player) || !quality.isSmoke && BreathingGear.hasTankAir(player)) {
            return OptionalInt.empty();
        }
        boolean isDampened = quality.isSmoke && BreathingGear.isDampMaskWorn(player);
        double slowing = isDampened ? VitalityServerConfig.get().breathing.dampMaskSmokeSlowing.get() : 1.0;
        return OptionalInt.of((int) Math.ceil(withoutGear.getAsInt() * slowing));
    }

    /** Ticks per bubble lost counting only effects and Respiration: what the gear would have to answer. */
    public static OptionalInt bubbleTicksWithoutGear(Player player, AirQuality quality) {
        OptionalInt baseTicks = quality.bubbleTicks();
        if (baseTicks.isEmpty() || hasBreathingEffect(player) || player.getAbilities().invulnerable) {
            return OptionalInt.empty();
        }
        double slowing = EnchantmentHelper.getRespiration(player) * VitalityServerConfig.get().air.respirationSlowingPerLevel.get();
        if (slowing >= 1.0) {
            return OptionalInt.empty();
        }
        return OptionalInt.of((int) Math.ceil(baseTicks.getAsInt() / (1.0 - slowing)));
    }

    public static boolean hasBreathingEffect(Player player) {
        boolean doesEffectProtect = VitalityServerConfig.get().air.doesWaterBreathingProtect.get();
        return doesEffectProtect && (player.hasEffect(MobEffects.WATER_BREATHING) || player.hasEffect(MobEffects.CONDUIT_POWER));
    }
}
