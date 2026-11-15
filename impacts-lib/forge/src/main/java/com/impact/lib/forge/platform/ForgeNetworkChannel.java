package com.impact.lib.forge.platform;

import com.impact.lib.network.ClientboundHandler;
import com.impact.lib.network.MessageCodec;
import com.impact.lib.network.NetworkChannel;
import com.impact.lib.network.ServerboundHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/** A {@link NetworkChannel} on Forge's {@link SimpleChannel}. Messages run on the receiving side's main thread. */
final class ForgeNetworkChannel implements NetworkChannel {
    private final SimpleChannel channel;
    private int nextMessageIndex;

    ForgeNetworkChannel(ResourceLocation channelId, String protocolVersion) {
        this.channel = NetworkRegistry.ChannelBuilder.named(channelId)
                .networkProtocolVersion(() -> protocolVersion)
                .clientAcceptedVersions(protocolVersion::equals)
                .serverAcceptedVersions(protocolVersion::equals)
                .simpleChannel();
    }

    @Override
    public <M> void registerClientbound(Class<M> messageType, MessageCodec<M> codec, ClientboundHandler<M> handler) {
        channel.messageBuilder(messageType, nextMessageIndex++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(codec::encode)
                .decoder(codec::decode)
                .consumerMainThread((message, context) -> handler.handle(message))
                .add();
    }

    @Override
    public <M> void registerServerbound(Class<M> messageType, MessageCodec<M> codec, ServerboundHandler<M> handler) {
        channel.messageBuilder(messageType, nextMessageIndex++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(codec::encode)
                .decoder(codec::decode)
                .consumerMainThread((message, context) -> {
                    ServerPlayer sender = context.get().getSender();
                    if (sender != null) {
                        handler.handle(message, sender);
                    }
                })
                .add();
    }

    @Override
    public <M> void sendToPlayer(ServerPlayer player, M message) {
        channel.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    @Override
    public <M> void sendToAllPlayers(M message) {
        channel.send(PacketDistributor.ALL.noArg(), message);
    }

    @Override
    public <M> void sendToServer(M message) {
        channel.sendToServer(message);
    }
}
