package com.impact.realisticvitality.thirst;

import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.VitalityCommonConfig;
import com.impact.realisticvitality.config.VitalityNeed;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.player.ThirstState;
import com.impact.realisticvitality.player.VitalityState;
import com.impact.realisticvitality.registry.VitalityDamageTypes;
import com.impact.realisticvitality.registry.VitalityEffects;
import com.impact.realisticvitality.temperature.BodyState;
import com.impact.realisticvitality.temperature.TemperatureTicks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;

/**
 * Thirst's rules, mirroring hunger's: exertion adds thirst exhaustion at hunger's rates, a small drain runs even at
 * rest, every few points of exhaustion cost 1 Hydration (or 1 thirst once Hydration is empty), and an empty bar deals
 * dehydration damage on vanilla's starvation rules. On Peaceful thirst never drops and refills like hunger.
 */
public final class ThirstDrain {
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);
    /** Vanilla caps food exhaustion at 40; thirst exhaustion follows. */
    private static final float MAXIMUM_EXHAUSTION = 40.0F;
    /** Vanilla's starvation stops at 10 health on Easy and 1 on Normal. */
    private static final float EASY_HEALTH_FLOOR = 10.0F;
    private static final float NORMAL_HEALTH_FLOOR = 1.0F;
    /** On Peaceful, vanilla refills hunger 1 point every 10 ticks. */
    private static final int PEACEFUL_REFILL_TICKS = 10;
    private static final int LOGGED_STEP_POINTS = 2;
    private static volatile boolean isApplyingHungerEffect;

    private ThirstDrain() {
    }

    public static boolean isOn() {
        return VitalityCommonConfig.get().isOn(VitalityNeed.THIRST);
    }

    /** Called whenever the player pays food exhaustion for an action (sprinting, jumping, attacking, mining, being hurt). */
    public static void onFoodExhaustion(Player player, float hungerExhaustion) {
        boolean isPayingForAnAction = !isApplyingHungerEffect && !player.getAbilities().invulnerable;
        if (!isPayingForAnAction || player.level().isClientSide || !isOn()) {
            return;
        }
        double multiplier = VitalityServerConfig.get().thirst.exhaustionMultiplier.get();
        addExhaustion(player, (float) (hungerExhaustion * multiplier));
    }

    /** Marks the Hunger effect's own food drain, which is not an action and so costs no thirst. */
    public static void setApplyingHungerEffect(boolean isApplying) {
        isApplyingHungerEffect = isApplying;
    }

    static void addParchedExhaustion(Player player, int amplifier) {
        if (player.level().isClientSide || !isOn()) {
            return;
        }
        double perLevel = VitalityServerConfig.get().thirst.parchedExhaustionPerTick.get();
        addExhaustion(player, (float) (perLevel * (amplifier + 1)));
    }

    /** Adds thirst exhaustion, made heavier by heat: 1.5 times while Hot, twice while Very Hot or Overheating. */
    public static void addExhaustion(Player player, float amount) {
        VitalityState state = VitalityState.of(player);
        double heat = TemperatureTicks.isOn() ? BodyState.of(state.temperature.body()).thirstMultiplier() : 1.0;
        ThirstState thirst = state.thirst;
        thirst.setExhaustion(Math.min(MAXIMUM_EXHAUSTION, thirst.exhaustion() + (float) (amount * heat)));
    }

    /** One server tick of thirst for one player. */
    public static void tick(ServerPlayer player) {
        if (!isOn()) {
            return;
        }
        ThirstState thirst = VitalityState.of(player).thirst;
        boolean isPeaceful = player.level().getDifficulty() == Difficulty.PEACEFUL;
        addRestingDrain(player, thirst);
        spendExhaustion(thirst, isPeaceful);
        if (isPeaceful) {
            refillOnPeaceful(player, thirst);
        }
        dehydrate(player, thirst);
        logChanges(player, thirst);
    }

    private static void addRestingDrain(ServerPlayer player, ThirstState thirst) {
        VitalityServerConfig.ThirstSettings settings = VitalityServerConfig.get().thirst;
        thirst.setRestingTicks(thirst.restingTicks() + 1);
        if (thirst.restingTicks() < settings.restingDrainTicks.get() || player.getAbilities().invulnerable) {
            return;
        }
        thirst.setRestingTicks(0);
        addExhaustion(player, settings.exhaustionPerPoint.get().floatValue());
    }

    private static void spendExhaustion(ThirstState thirst, boolean isPeaceful) {
        float exhaustionPerPoint = VitalityServerConfig.get().thirst.exhaustionPerPoint.get().floatValue();
        if (thirst.exhaustion() < exhaustionPerPoint) {
            return;
        }
        thirst.setExhaustion(thirst.exhaustion() - exhaustionPerPoint);
        if (thirst.hydration() > 0.0F) {
            thirst.setHydration(Math.max(thirst.hydration() - 1.0F, 0.0F));
        } else if (!isPeaceful) {
            thirst.setLevel(thirst.level() - 1);
        }
    }

    private static void refillOnPeaceful(ServerPlayer player, ThirstState thirst) {
        if (thirst.level() < ThirstState.FULL && player.tickCount % PEACEFUL_REFILL_TICKS == 0) {
            thirst.setLevel(thirst.level() + 1);
        }
    }

    private static void dehydrate(ServerPlayer player, ThirstState thirst) {
        if (thirst.level() > 0) {
            thirst.setDamageTicks(0);
            return;
        }
        VitalityServerConfig.ThirstSettings settings = VitalityServerConfig.get().thirst;
        thirst.setDamageTicks(thirst.damageTicks() + 1);
        if (thirst.damageTicks() < settings.dehydrationIntervalTicks.get()) {
            return;
        }
        thirst.setDamageTicks(0);
        if (!canDehydrationHurt(player)) {
            return;
        }
        float damage = settings.dehydrationDamage.get().floatValue();
        player.hurt(VitalityDamageTypes.source(player, VitalityDamageTypes.DEHYDRATION), damage);
        LOG.debug(VitalityLogCategories.THIRST, "dehydration damage", "player", player.getScoreboardName(),
                "damage", damage, "health", player.getHealth());
    }

    private static boolean canDehydrationHurt(ServerPlayer player) {
        Difficulty difficulty = player.level().getDifficulty();
        float health = player.getHealth();
        return health > EASY_HEALTH_FLOOR || difficulty == Difficulty.HARD
                || health > NORMAL_HEALTH_FLOOR && difficulty == Difficulty.NORMAL;
    }

    private static void logChanges(ServerPlayer player, ThirstState thirst) {
        int step = thirst.level() / LOGGED_STEP_POINTS;
        if (step != thirst.loggedStep()) {
            thirst.setLoggedStep(step);
            LOG.debug(VitalityLogCategories.THIRST, "thirst step", "player", player.getScoreboardName(),
                    "thirst", thirst.level(), "hydration", thirst.hydration());
        }
        boolean isParched = player.hasEffect(VitalityEffects.parched());
        if (isParched != thirst.wasParched()) {
            thirst.setWasParched(isParched);
            LOG.debug(VitalityLogCategories.THIRST, isParched ? "parched given" : "parched cleared", "player", player.getScoreboardName());
        }
    }
}
