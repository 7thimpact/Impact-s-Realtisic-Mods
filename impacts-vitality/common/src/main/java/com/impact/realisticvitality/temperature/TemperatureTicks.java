package com.impact.realisticvitality.temperature;

import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.VitalityCommonConfig;
import com.impact.realisticvitality.config.VitalityNeed;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.player.TemperatureState;
import com.impact.realisticvitality.player.VitalityState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

/** Temperature's per-tick work for a player: the Wet timer every tick, and the ambient reading once a second. */
public final class TemperatureTicks {
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);
    private static final int TICKS_PER_READING = 20;

    private TemperatureTicks() {
    }

    public static boolean isOn() {
        return VitalityCommonConfig.get().isOn(VitalityNeed.TEMPERATURE);
    }

    public static void tick(ServerPlayer player) {
        if (!isOn()) {
            return;
        }
        TemperatureState temperature = VitalityState.of(player).temperature;
        updateWet(temperature, player.isInWaterRainOrBubble());
        // Players are spread over the second, so a full server doesn't scan every player on the same tick.
        if ((player.tickCount + player.getId()) % TICKS_PER_READING != 0) {
            return;
        }
        AmbientReading reading = AmbientTemperature.at(player.level(), bodyPosition(player), player.isInWater());
        temperature.setAmbient((float) reading.total());
        logReading(player, reading);
    }

    /** Water, rain or a bubble column soak you; out of them you dry off over {@code temperature.wetTicks}. */
    public static void updateWet(TemperatureState temperature, boolean isSoaking) {
        if (isSoaking) {
            temperature.setWetTicks(VitalityServerConfig.get().temperature.wetTicks.get());
        } else if (temperature.isWet()) {
            temperature.setWetTicks(temperature.wetTicks() - 1);
        }
    }

    // The block at chest height: what the player's body is in.
    private static BlockPos bodyPosition(ServerPlayer player) {
        return BlockPos.containing(player.getX(), player.getY() + 1.0, player.getZ());
    }

    private static void logReading(ServerPlayer player, AmbientReading reading) {
        if (!LOG.isVerbose(VitalityLogCategories.TEMPERATURE)) {
            return;
        }
        LOG.verbose(VitalityLogCategories.TEMPERATURE, "ambient", "player", player.getScoreboardName(), "total", round(reading.total()),
                "biome", round(reading.biome()), "dimension", round(reading.dimension()), "height", round(reading.height()),
                "time", round(reading.time()), "weather", round(reading.weather()), "sun", round(reading.sun()),
                "underground", round(reading.underground()), "blocks", round(reading.blocks()), "water", round(reading.water()));
    }

    private static double round(double degrees) {
        return Math.round(degrees * 10.0) / 10.0;
    }
}
