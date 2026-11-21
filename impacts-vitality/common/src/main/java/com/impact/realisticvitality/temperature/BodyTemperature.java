package com.impact.realisticvitality.temperature;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.TaggedEntries;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.player.TemperatureState;
import com.impact.realisticvitality.player.VitalityState;
import com.impact.realisticvitality.registry.VitalityDamageTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

import java.util.OptionalDouble;

/**
 * Body temperature, once a second: it drifts back to 0 inside the comfortable ambient range (50–80°F, widened by
 * insulation) and toward ±100 outside it, faster the further outside. Each band has its effects: Slowness when cold,
 * Weakness and Nausea when hot, and damage at the extremes (never on Peaceful).
 */
public final class BodyTemperature {
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);
    /** Status effects last a little past the next reading, so they never flicker off between readings. */
    private static final int EFFECT_TICKS = 50;
    private static final int SLOWNESS_ONE = 0;
    private static final int SLOWNESS_TWO = 1;
    private static final int LEVEL_ONE = 0;

    private BodyTemperature() {
    }

    /** One second's drift toward what the ambient temperature calls for, then the band's effects. */
    public static void tickSecond(ServerPlayer player, float ambient) {
        TemperatureState temperature = VitalityState.of(player).temperature;
        temperature.setBody(temperature.body() + driftPerSecond(temperature.body(), ambient, Insulations.of(player)));
        BodyState state = BodyState.of(temperature.body());
        logStateChange(player, temperature, state);
        applyEffects(player, state);
        applyDamage(player, temperature, state);
    }

    /** How far body temperature moves in one second, toward 0 inside the range and toward ±100 outside it. */
    public static float driftPerSecond(float body, float ambient, Insulation insulation) {
        VitalityServerConfig.TemperatureSettings settings = VitalityServerConfig.get().temperature;
        ComfortRange range = ComfortRange.of(insulation);
        double degreesPerPoint = settings.driftDegreesPerPoint.get();
        if (ambient < range.coldEdge()) {
            return (float) -((range.coldEdge() - ambient) / degreesPerPoint);
        }
        if (ambient > range.hotEdge()) {
            return (float) ((ambient - range.hotEdge()) / degreesPerPoint);
        }
        float recovery = settings.recoveryPerSecond.get().floatValue();
        return Math.abs(body) <= recovery ? -body : -Math.signum(body) * recovery;
    }

    /** A drink or food that cools or warms right away: Cold Water and melon −15, soups +15 (from {@code temperature.foodTemperatures}). */
    public static void onConsumed(ServerPlayer player, ItemStack consumed) {
        OptionalDouble change = TaggedEntries.findNumber(VitalityServerConfig.get().temperature.foodTemperatures.get(),
                ResourceIds.itemId(consumed.getItem())::equals, tagId -> consumed.is(TagKey.create(Registries.ITEM, tagId)));
        if (change.isEmpty() || consumed.isEmpty()) {
            return;
        }
        TemperatureState temperature = VitalityState.of(player).temperature;
        temperature.setBody(temperature.body() + (float) change.getAsDouble());
        LOG.debug(VitalityLogCategories.FOOD, "food temperature", "player", player.getScoreboardName(),
                "item", ResourceIds.itemId(consumed.getItem()), "change", change.getAsDouble(), "body", temperature.body());
    }

    private static void applyEffects(ServerPlayer player, BodyState state) {
        if (state.isVeryColdOrWorse()) {
            refresh(player, MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_TWO);
        } else if (state == BodyState.COLD) {
            refresh(player, MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_ONE);
        }
        if (state == BodyState.VERY_HOT) {
            refresh(player, MobEffects.WEAKNESS, LEVEL_ONE);
        }
        if (state == BodyState.OVERHEATING) {
            refresh(player, MobEffects.WEAKNESS, LEVEL_ONE);
            refresh(player, MobEffects.CONFUSION, LEVEL_ONE);
        }
    }

    private static void applyDamage(ServerPlayer player, TemperatureState temperature, BodyState state) {
        boolean isExtreme = state == BodyState.FREEZING || state == BodyState.OVERHEATING;
        if (!isExtreme) {
            temperature.setDamageSeconds(0);
            return;
        }
        VitalityServerConfig.TemperatureSettings settings = VitalityServerConfig.get().temperature;
        temperature.setDamageSeconds(temperature.damageSeconds() + 1);
        boolean isPeaceful = player.level().getDifficulty() == Difficulty.PEACEFUL;
        if (temperature.damageSeconds() < settings.damageIntervalSeconds.get() || isPeaceful) {
            return;
        }
        temperature.setDamageSeconds(0);
        if (state == BodyState.FREEZING) {
            player.hurt(player.damageSources().freeze(), settings.freezingDamage.get().floatValue());
        } else {
            player.hurt(VitalityDamageTypes.source(player, VitalityDamageTypes.HEATSTROKE), settings.overheatingDamage.get().floatValue());
        }
        LOG.debug(VitalityLogCategories.TEMPERATURE, "temperature damage", "player", player.getScoreboardName(), "state", state.serializedName,
                "health", player.getHealth());
    }

    private static void refresh(ServerPlayer player, MobEffect effect, int amplifier) {
        boolean isAmbient = true;
        boolean showsParticles = false;
        player.addEffect(new MobEffectInstance(effect, EFFECT_TICKS, amplifier, isAmbient, showsParticles, true));
    }

    private static void logStateChange(ServerPlayer player, TemperatureState temperature, BodyState state) {
        if (state.serializedName.equals(temperature.loggedState())) {
            return;
        }
        LOG.debug(VitalityLogCategories.TEMPERATURE, "body state", "player", player.getScoreboardName(),
                "from", temperature.loggedState(), "to", state.serializedName, "body", Math.round(temperature.body()));
        temperature.setLoggedState(state.serializedName);
    }
}
