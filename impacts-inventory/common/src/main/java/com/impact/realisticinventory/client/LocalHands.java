package com.impact.realisticinventory.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;

/** The local player's hands: the scroll wheel asks for the same swap as vanilla's swap-hands key, so vanilla moves and syncs the items. */
public final class LocalHands {
    private LocalHands() {
    }

    public static void requestSwap() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener connection = minecraft.getConnection();
        if (connection == null || minecraft.player == null || minecraft.player.isSpectator()) {
            return;
        }
        connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
    }
}
