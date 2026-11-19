package com.impact.realisticfood.boiling;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The busy pots a client knows about, from the server's updates. An entry lasts a little longer than the update
 * interval, so a pot that goes quiet (or out of sight) drops out by itself.
 */
public final class ClientBoilingPots {
    private static final int ENTRY_LIFETIME_TICKS = 45;
    private static final Map<BlockPos, PotView> POTS_BY_POSITION = new ConcurrentHashMap<>();

    private ClientBoilingPots() {
    }

    public record PotView(Vec3 surfaceCenter, boolean isBoiling, SoupState soupState, long expiresAtGameTime) {
    }

    public static void update(BlockPos position, Vec3 surfaceCenter, boolean isBoiling, SoupState soupState, long gameTime) {
        POTS_BY_POSITION.put(position.immutable(), new PotView(surfaceCenter, isBoiling, soupState, gameTime + ENTRY_LIFETIME_TICKS));
    }

    public static boolean isBoiling(Level level, BlockPos position) {
        PotView view = POTS_BY_POSITION.get(position);
        return view != null && level.getGameTime() <= view.expiresAtGameTime() && view.isBoiling();
    }

    public static SoupState soupState(Level level, BlockPos position) {
        PotView view = POTS_BY_POSITION.get(position);
        boolean isCurrent = view != null && level.getGameTime() <= view.expiresAtGameTime();
        return isCurrent ? view.soupState() : SoupState.NONE;
    }

    /** The pots still current at this game time; expired ones are forgotten. */
    public static Map<BlockPos, PotView> activePots(long gameTime) {
        POTS_BY_POSITION.values().removeIf(view -> gameTime > view.expiresAtGameTime());
        return Map.copyOf(POTS_BY_POSITION);
    }

    public static void clear() {
        POTS_BY_POSITION.clear();
    }
}
