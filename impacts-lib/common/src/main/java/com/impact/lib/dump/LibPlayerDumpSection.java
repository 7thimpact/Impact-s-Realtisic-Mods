package com.impact.lib.dump;

import com.impact.lib.ImpactsLib;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;
import java.util.Map;

/** Lib's part of the player dump: the vanilla basics that every bug report needs. */
public final class LibPlayerDumpSection implements PlayerDumpSection {
    public static final String SECTION_NAME = "lib";
    private static final String DECIMAL_FORMAT = "%.1f";
    private static final String RATIO_FORMAT = "%s/%s";

    @Override
    public Map<String, Object> describe(ServerPlayer player) {
        return ImpactsDump.entries(
                "uuid", player.getUUID(),
                "dimension", player.level().dimension().location(),
                "position", player.blockPosition().toShortString(),
                "gameMode", player.gameMode.getGameModeForPlayer().getName(),
                "health", ratio(oneDecimal(player.getHealth()), oneDecimal(player.getMaxHealth())),
                "food", player.getFoodData().getFoodLevel(),
                "saturation", oneDecimal(player.getFoodData().getSaturationLevel()),
                "air", ratio(player.getAirSupply(), player.getMaxAirSupply()),
                "impactMods", ImpactsLib.registeredImpactMods());
    }

    private static String oneDecimal(float value) {
        return String.format(Locale.ROOT, DECIMAL_FORMAT, value);
    }

    private static String ratio(Object current, Object maximum) {
        return String.format(Locale.ROOT, RATIO_FORMAT, current, maximum);
    }
}
