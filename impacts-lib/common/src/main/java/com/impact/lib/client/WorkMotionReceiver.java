package com.impact.lib.client;

import com.impact.lib.hold.WorkMotions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** Client side of {@link WorkMotions}: finds the player a work-motion message is about. */
public final class WorkMotionReceiver {
    private WorkMotionReceiver() {
    }

    public static void receive(WorkMotions.WorkMotionMessage message) {
        ClientLevel level = Minecraft.getInstance().level;
        Entity entity = level == null ? null : level.getEntity(message.entityId());
        if (entity instanceof Player player) {
            WorkMotions.onClientMessage(player, message.motion());
        }
    }
}
