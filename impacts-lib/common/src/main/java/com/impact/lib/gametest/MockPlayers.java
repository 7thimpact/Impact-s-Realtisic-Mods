package com.impact.lib.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Mock players for GameTests that join through a real login, so login hooks run. Vanilla's
 * {@code GameTestHelper.makeMockServerPlayerInLevel} leaves the connection without a network channel, which
 * Forge's login code needs, so these players get an in-memory one.
 */
public final class MockPlayers {
    // Commands only accept player names of up to 16 characters.
    public static final String MOCK_PLAYER_NAME = "impacts_mock";

    private MockPlayers() {
    }

    public static ServerPlayer joinWorld(ServerLevel level) {
        MinecraftServer server = level.getServer();
        ServerPlayer player = new ServerPlayer(server, level, new GameProfile(UUID.randomUUID(), MOCK_PLAYER_NAME));
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        // Registering the connection on an in-memory channel gives it the channel that login code expects.
        new EmbeddedChannel(connection);
        server.getPlayerList().placeNewPlayer(connection, player);
        return player;
    }

    public static void leaveWorld(ServerPlayer player) {
        player.server.getPlayerList().remove(player);
    }
}
