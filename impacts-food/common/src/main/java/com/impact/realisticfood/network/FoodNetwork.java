package com.impact.realisticfood.network;

import com.impact.lib.ResourceIds;
import com.impact.lib.network.NetworkChannel;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.boiling.SoupState;
import com.impact.realisticfood.client.BoilingEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

/** Food's {@code impactsfood:main} channel. */
public final class FoodNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static final boolean ONLY_PLAYERS_AT_THE_EDGE = false;
    private static volatile NetworkChannel channel;

    private FoodNetwork() {
    }

    /** Creates the channel and registers every message. Called once, during mod construction. */
    public static void create() {
        channel = LoaderPlatform.current().createChannel(ResourceIds.of(FoodMod.MOD_ID, "main"), PROTOCOL_VERSION);
        channel.registerClientbound(PotStateMessage.class, PotStateMessage.CODEC, message -> BoilingEffects.onPotState(message));
    }

    /** Tells every player who can see the pot's chunk how the pot is doing. */
    public static void sendPotState(ServerLevel level, BlockPos position, Vec3 surfaceCenter, boolean isBoiling, SoupState soupState) {
        PotStateMessage message = new PotStateMessage(position, surfaceCenter, isBoiling, soupState);
        level.getChunkSource().chunkMap.getPlayers(new ChunkPos(position), ONLY_PLAYERS_AT_THE_EDGE)
                .forEach(player -> channel.sendToPlayer(player, message));
    }
}
