package com.impact.realisticvitality.air;

import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.VitalityCommonConfig;
import com.impact.realisticvitality.config.VitalityNeed;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.player.AirState;
import com.impact.realisticvitality.player.VitalityState;
import com.impact.realisticvitality.registry.VitalityDamageTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;

import java.util.OptionalInt;

/**
 * Air's per-tick work for a player: the air quality once a second, a bubble lost every so often in bad air (vanilla's
 * refill is held back meanwhile), and drowning-like damage once the bubbles run out. Underwater, vanilla's rules apply.
 */
public final class AirTicks {
    /** A full bubble row is 300 air; each of its 10 bubbles is 30. */
    public static final int AIR_PER_BUBBLE = 30;
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);
    private static final int TICKS_PER_READING = 20;

    private AirTicks() {
    }

    public static boolean isOn() {
        return VitalityCommonConfig.get().isOn(VitalityNeed.AIR);
    }

    public static void tick(ServerPlayer player) {
        if (!isOn()) {
            return;
        }
        AirState air = VitalityState.of(player).air;
        if ((player.tickCount + player.getId()) % TICKS_PER_READING == 0) {
            updateQuality(player, air, AirQualities.around(player.level(), player.getBoundingBox()));
        }
        OptionalInt bubbleTicks = player.isUnderWater() ? OptionalInt.empty() : AirProtection.bubbleTicks(player, air.quality());
        if (bubbleTicks.isEmpty()) {
            stopDraining(player, air);
            return;
        }
        drain(player, air, bubbleTicks.getAsInt());
        hurtWhenOut(player, air);
    }

    /** Whether vanilla's air refill is held back: in bad air you're not protected from, out of water. Asked on both sides. */
    public static boolean holdsBackRefill(Player player, AirQuality quality) {
        return isOn() && !player.isUnderWater() && AirProtection.bubbleTicks(player, quality).isPresent();
    }

    public static void updateQuality(ServerPlayer player, AirState air, AirQuality quality) {
        if (quality == air.quality()) {
            return;
        }
        LOG.debug(VitalityLogCategories.AIR, "air quality", "player", player.getScoreboardName(), "from", air.quality().serializedName,
                "to", quality.serializedName, "y", player.getBlockY());
        air.setQuality(quality);
    }

    private static void drain(ServerPlayer player, AirState air, int bubbleTicks) {
        if (!air.isDraining()) {
            air.setDraining(true);
            LOG.debug(VitalityLogCategories.AIR, "bubble drain started", "player", player.getScoreboardName(), "quality", air.quality().serializedName,
                    "ticksPerBubble", bubbleTicks);
        }
        air.setDrainTicks(air.drainTicks() + 1);
        if (air.drainTicks() < bubbleTicks) {
            return;
        }
        air.setDrainTicks(0);
        player.setAirSupply(Math.max(0, player.getAirSupply() - AIR_PER_BUBBLE));
    }

    private static void hurtWhenOut(ServerPlayer player, AirState air) {
        if (player.getAirSupply() > 0) {
            air.setDamageTicks(0);
            return;
        }
        VitalityServerConfig.AirSettings settings = VitalityServerConfig.get().air;
        air.setDamageTicks(air.damageTicks() + 1);
        if (air.damageTicks() < settings.damageIntervalTicks.get()) {
            return;
        }
        air.setDamageTicks(0);
        if (player.level().getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        player.hurt(VitalityDamageTypes.source(player, air.quality().damageType()), settings.damage.get().floatValue());
        LOG.debug(VitalityLogCategories.AIR, "air damage", "player", player.getScoreboardName(), "quality", air.quality().serializedName,
                "health", player.getHealth());
    }

    private static void stopDraining(ServerPlayer player, AirState air) {
        air.setDrainTicks(0);
        air.setDamageTicks(0);
        if (air.isDraining()) {
            air.setDraining(false);
            LOG.debug(VitalityLogCategories.AIR, "bubble drain stopped", "player", player.getScoreboardName(), "quality", air.quality().serializedName);
        }
    }
}
