package com.impact.realisticinventory.hotbar;

import com.impact.realisticinventory.config.InventoryServerConfig;
import com.impact.realisticinventory.network.InventoryNetwork;
import net.minecraft.server.level.ServerPlayer;

/** Hotbar state after joining, respawning or changing dimension. */
public final class HotbarRespawn {
    private HotbarRespawn() {
    }

    /**
     * After death the hotbar state is only kept with keepInventory (it is attached like the inventory); the server
     * config can drop the active slot even then.
     */
    public static void afterRespawn(ServerPlayer player, boolean wasDeath) {
        boolean isActiveSlotDropped = wasDeath && !InventoryServerConfig.get().hotbar.isActiveSlotKeptWithKeepInventory.get();
        if (isActiveSlotDropped) {
            HandSwitch.stateOf(player).reset();
        }
    }

    /** Sends the active slot to the player's client, whose player object is new after joining or respawning. */
    public static void syncToClient(ServerPlayer player) {
        InventoryNetwork.sendHotbarState(player);
    }
}
