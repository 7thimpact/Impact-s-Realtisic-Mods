package com.impact.realisticinventory.client;

import com.impact.realisticinventory.network.BackSlotMessage;
import com.impact.realisticinventory.pack.BackSlotStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** Applies the server's back-slot updates to the client's copy of each player. */
public final class ClientBackSlot {
    private ClientBackSlot() {
    }

    public static void apply(BackSlotMessage message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Entity wearer = level.getEntity(message.wearerEntityId());
        if (wearer instanceof Player player) {
            BackSlotStorage.set(player, message.worn());
        }
    }
}
