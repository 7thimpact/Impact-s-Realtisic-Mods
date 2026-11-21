package com.impact.realisticvitality.health;

import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.advancement.VitalityTriggers;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.player.ThirstState;
import com.impact.realisticvitality.player.VitalityState;
import com.impact.realisticvitality.thirst.ThirstDrain;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.GameRules;

/**
 * Hunger and thirst share healing. The fuller bar must be at 18 or more for any natural regeneration; the emptier bar
 * then caps it: full health when both are at 18 or more, three-quarters when the emptier is between, half when it's at
 * 6 or less. Regeneration only stops at the cap and never takes health away. Fast regeneration needs both bars full and
 * both saturation and Hydration above 0, and every regeneration's exhaustion is split evenly between hunger and thirst.
 */
public final class HealthCap {
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);
    private static final int FULL_BAR = 20;
    private static final int FAST_REGENERATION_TICKS = 10;
    private static final int SLOW_REGENERATION_TICKS = 80;
    private static final float SLOW_REGENERATION_HEALTH = 1.0F;
    private static final float SLOW_REGENERATION_EXHAUSTION = 6.0F;
    private static final float MAXIMUM_FAST_SATURATION = 6.0F;
    private static final float STARVATION_DAMAGE = 1.0F;
    private static final float EASY_STARVATION_FLOOR = 10.0F;
    private static final float NORMAL_STARVATION_FLOOR = 1.0F;

    private HealthCap() {
    }

    /** Whether Vitality takes over natural regeneration: the cap is on and thirst is on. */
    public static boolean isActive() {
        return VitalityServerConfig.get().healthCap.isOn.get() && ThirstDrain.isOn();
    }

    /** The most health natural regeneration may reach now, from the emptier of the two bars. */
    public static float limit(Player player) {
        VitalityServerConfig.HealthCapSettings settings = VitalityServerConfig.get().healthCap;
        int emptierBar = Math.min(player.getFoodData().getFoodLevel(), VitalityState.of(player).thirst.level());
        double share = 1.0;
        if (emptierBar <= settings.lowBarAtOrBelow.get()) {
            share = settings.lowShare.get();
        } else if (emptierBar < settings.fullBarAtOrAbove.get()) {
            share = settings.middleShare.get();
        }
        return (float) (player.getMaxHealth() * share);
    }

    /** Whether natural regeneration may run at all: the fuller bar is at 18 or more. */
    public static boolean canRegenerate(Player player) {
        int fullerBar = Math.max(player.getFoodData().getFoodLevel(), VitalityState.of(player).thirst.level());
        return fullerBar >= VitalityServerConfig.get().healthCap.fullBarAtOrAbove.get();
    }

    /**
     * Replaces the regeneration part of {@code FoodData.tick} (after exhaustion has been paid). Takes and returns
     * vanilla's regeneration timer.
     */
    public static int tickRegeneration(Player player, FoodData food, int tickTimer) {
        boolean isNaturalRegenerationOn = player.level().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);
        ThirstState thirst = VitalityState.of(player).thirst;
        if (isNaturalRegenerationOn && player.isHurt() && canRegenerateFast(food, thirst)) {
            return tickFastRegeneration(player, food, tickTimer + 1);
        }
        if (isNaturalRegenerationOn && player.isHurt() && canRegenerate(player)) {
            return tickSlowRegeneration(player, food, tickTimer + 1);
        }
        clearCapped(player);
        if (food.getFoodLevel() <= 0) {
            return tickStarvation(player, tickTimer + 1);
        }
        return 0;
    }

    /** With {@code healthCap.capAllHealing} on, every heal (potions, golden apples, beacons) stops at the cap too. */
    public static float limitHealing(LivingEntity entity, float amount) {
        boolean capsAllHealing = VitalityServerConfig.get().healthCap.capsAllHealing.get();
        if (!(entity instanceof Player player) || !capsAllHealing || !isActive() || player.level().isClientSide) {
            return amount;
        }
        return Math.max(0.0F, Math.min(amount, limit(player) - player.getHealth()));
    }

    private static boolean canRegenerateFast(FoodData food, ThirstState thirst) {
        boolean areBothFull = food.getFoodLevel() >= FULL_BAR && thirst.level() >= FULL_BAR;
        return areBothFull && food.getSaturationLevel() > 0.0F && thirst.hydration() > 0.0F;
    }

    private static int tickFastRegeneration(Player player, FoodData food, int tickTimer) {
        if (tickTimer < FAST_REGENERATION_TICKS) {
            return tickTimer;
        }
        float spentSaturation = Math.min(food.getSaturationLevel(), MAXIMUM_FAST_SATURATION);
        player.heal(spentSaturation / MAXIMUM_FAST_SATURATION);
        splitExhaustion(player, food, spentSaturation);
        return 0;
    }

    private static int tickSlowRegeneration(Player player, FoodData food, int tickTimer) {
        float limit = limit(player);
        if (player.getHealth() >= limit) {
            markCapped(player, food, limit);
            return 0;
        }
        clearCapped(player);
        if (tickTimer < SLOW_REGENERATION_TICKS) {
            return tickTimer;
        }
        player.heal(Math.min(SLOW_REGENERATION_HEALTH, limit - player.getHealth()));
        splitExhaustion(player, food, SLOW_REGENERATION_EXHAUSTION);
        return 0;
    }

    private static int tickStarvation(Player player, int tickTimer) {
        if (tickTimer < SLOW_REGENERATION_TICKS) {
            return tickTimer;
        }
        Difficulty difficulty = player.level().getDifficulty();
        float health = player.getHealth();
        boolean canStarve = health > EASY_STARVATION_FLOOR || difficulty == Difficulty.HARD
                || health > NORMAL_STARVATION_FLOOR && difficulty == Difficulty.NORMAL;
        if (canStarve) {
            player.hurt(player.damageSources().starve(), STARVATION_DAMAGE);
        }
        return 0;
    }

    private static void splitExhaustion(Player player, FoodData food, float exhaustion) {
        food.addExhaustion(exhaustion / 2);
        ThirstDrain.addExhaustion(player, exhaustion / 2);
    }

    private static void markCapped(Player player, FoodData food, float limit) {
        VitalityState state = VitalityState.of(player);
        if (state.health.isCapped()) {
            return;
        }
        state.health.setCapped(true);
        if (player instanceof ServerPlayer serverPlayer) {
            VitalityTriggers.HEALTH_CAPPED.trigger(serverPlayer);
        }
        LOG.debug(VitalityLogCategories.HEALTH, "regeneration capped", "player", player.getScoreboardName(),
                "hunger", food.getFoodLevel(), "thirst", state.thirst.level(), "cap", limit, "health", player.getHealth());
    }

    private static void clearCapped(Player player) {
        VitalityState.of(player).health.setCapped(false);
    }
}
