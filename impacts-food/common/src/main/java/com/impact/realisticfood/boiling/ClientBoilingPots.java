package com.impact.realisticfood.boiling;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The boiling pots a client knows about, from the server's bubble updates. An entry lasts a little longer than the
 * update interval, so a pot that stops boiling (or goes out of sight) drops out by itself.
 */
public final class ClientBoilingPots {
    private static final int ENTRY_LIFETIME_TICKS = 45;
    private static final Map<BlockPos, BoilingPotView> POTS_BY_POSITION = new ConcurrentHashMap<>();

    private ClientBoilingPots() {
    }

    public record BoilingPotView(Vec3 surfaceCenter, long expiresAtGameTime) {
    }

    public static void update(BlockPos position, Vec3 surfaceCenter, long gameTime) {
        POTS_BY_POSITION.put(position.immutable(), new BoilingPotView(surfaceCenter, gameTime + ENTRY_LIFETIME_TICKS));
    }

    public static boolean isBoiling(Level level, BlockPos position) {
        BoilingPotView view = POTS_BY_POSITION.get(position);
        return view != null && level.getGameTime() <= view.expiresAtGameTime();
    }

    /** The pots still boiling at this game time; expired ones are forgotten. */
    public static Map<BlockPos, BoilingPotView> activePots(long gameTime) {
        POTS_BY_POSITION.values().removeIf(view -> gameTime > view.expiresAtGameTime());
        return Map.copyOf(POTS_BY_POSITION);
    }

    public static void clear() {
        POTS_BY_POSITION.clear();
    }
}
