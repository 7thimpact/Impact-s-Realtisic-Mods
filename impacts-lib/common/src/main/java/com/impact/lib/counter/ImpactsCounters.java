package com.impact.lib.counter;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.advancement.LibTriggers;
import com.impact.lib.attachment.DeathCopyRule;
import com.impact.lib.attachment.PlayerAttachment;
import com.impact.lib.attachment.PlayerAttachments;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.log.ImpactsLog;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/**
 * Named per-player counters, optionally per item, saved with the player and kept through death. Counter names are
 * namespaced: {@code "<modid>:<counter>"}. Every change fires the {@code impactslib:count} trigger.
 */
public final class ImpactsCounters {
    /** Registered by Lib during mod construction. */
    public static final PlayerAttachment<PlayerCounters> COUNTERS =
            new PlayerAttachment<>(ResourceIds.of(ImpactsLib.MOD_ID, "counters"), PlayerCounters::new, DeathCopyRule.ALWAYS);

    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);
    private static final int MILESTONE_FACTOR = 10;

    private ImpactsCounters() {
    }

    /** Adds to a counter, and to the item's own count when an item is given. Returns the counter's new total. */
    public static int add(ServerPlayer player, String counter, Item itemOrNull, int amount) {
        String itemId = itemOrNull == null ? null : ResourceIds.itemId(itemOrNull).toString();
        PlayerCounters counters = PlayerAttachments.get(player, COUNTERS);
        int previousTotal = counters.total(counter);
        int newTotal = counters.add(counter, itemId, amount);
        logMilestonesPassed(player, counter, previousTotal, newTotal);
        LibTriggers.COUNT.trigger(player, counter);
        return newTotal;
    }

    /** The counter's total, or the item's own count when an item is given. */
    public static int get(Player player, String counter, Item itemOrNull) {
        PlayerCounters counters = PlayerAttachments.get(player, COUNTERS);
        if (itemOrNull == null) {
            return counters.total(counter);
        }
        return counters.itemCount(counter, ResourceIds.itemId(itemOrNull).toString());
    }

    public static PlayerCounters all(Player player) {
        return PlayerAttachments.get(player, COUNTERS);
    }

    /** Clears a counter, for achievements whose progress breaks (Vitality's "In Balance"). */
    public static void reset(ServerPlayer player, String counter) {
        PlayerAttachments.get(player, COUNTERS).reset(counter);
        LOG.debug(LibLogCategories.COUNTERS, "counter reset", "player", player.getGameProfile().getName(), "counter", counter);
    }

    // Logs only when the total passes 1, 10, 100, ...: counters change too often to log every step.
    private static void logMilestonesPassed(ServerPlayer player, String counter, int previousTotal, int newTotal) {
        for (long milestone = 1; milestone <= newTotal; milestone *= MILESTONE_FACTOR) {
            if (milestone > previousTotal) {
                LOG.debug(LibLogCategories.COUNTERS, "milestone", "player", player.getGameProfile().getName(),
                        "counter", counter, "total", milestone);
            }
        }
    }
}
