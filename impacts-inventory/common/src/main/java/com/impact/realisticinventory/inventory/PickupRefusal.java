package com.impact.realisticinventory.inventory;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.config.InventoryServerConfig;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import com.impact.realisticinventory.lang.InventoryTranslations;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * When everything is full, items stay on the ground and the action bar says "Pockets full", at most once per
 * configured interval: a player standing on items touches them every tick.
 */
public final class PickupRefusal {
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);
    private static final Map<UUID, Long> LAST_MESSAGE_GAME_TIME_BY_PLAYER = new ConcurrentHashMap<>();

    private PickupRefusal() {
    }

    public static void onRefused(ServerPlayer player, ItemStack refusedStack) {
        if (!InventoryLocks.isActiveFor(player) || !isMessageDue(player)) {
            return;
        }
        LOG.debug(InventoryLogCategories.PICKUP, "pickup refused, everything full", "player", player.getGameProfile().getName(),
                "item", ResourceIds.itemId(refusedStack.getItem()), "count", refusedStack.getCount());
        if (InventoryServerConfig.get().pickup.isFullMessageShown.get()) {
            player.displayClientMessage(Component.translatable(InventoryTranslations.POCKETS_FULL), true);
        }
    }

    /** Whether the interval since this player's last message has passed; records the message when it has. */
    public static boolean isMessageDue(ServerPlayer player) {
        long gameTime = player.level().getGameTime();
        long intervalTicks = InventoryServerConfig.get().pickup.fullMessageIntervalTicks.get();
        Long lastMessageGameTime = LAST_MESSAGE_GAME_TIME_BY_PLAYER.get(player.getUUID());
        boolean isDue = lastMessageGameTime == null || gameTime - lastMessageGameTime >= intervalTicks;
        if (isDue) {
            LAST_MESSAGE_GAME_TIME_BY_PLAYER.put(player.getUUID(), gameTime);
        }
        return isDue;
    }

    public static void forget(UUID playerId) {
        LAST_MESSAGE_GAME_TIME_BY_PLAYER.remove(playerId);
    }
}
