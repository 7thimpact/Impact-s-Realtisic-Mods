package com.impact.realisticinventory.pack;

import com.impact.realisticinventory.network.InventoryNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Keeps clients' copies of every back slot current. The server compares each player's back slot once a tick with
 * what it last sent, so any change is noticed, including mods changing the worn stack in place.
 */
public final class BackSlotSync {
    private BackSlotSync() {
    }

    /** Server, once a tick per player: tells the player and everyone watching them when the worn item changed. */
    public static void tick(ServerPlayer player) {
        BackSlotContents contents = BackSlotStorage.contentsOf(player);
        if (!contents.hasChangedSinceSync()) {
            return;
        }
        contents.markSynced();
        InventoryNetwork.sendBackSlotToTrackingAndSelf(player, contents.stack());
    }

    /** A player started watching the wearer: send what the wearer has on their back. */
    public static void sendTo(ServerPlayer receiver, Player wearer) {
        InventoryNetwork.sendBackSlot(receiver, wearer, BackSlotStorage.get(wearer));
    }
}
