package com.impact.lib.forge.config;

import com.impact.lib.ImpactsLib;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.network.MessageCodec;
import com.impact.lib.network.NetworkChannel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Sends a SERVER config file's new contents to connected players when it changes. Forge only sends SERVER files
 * at login, so without this a change made with {@code /impacts config} would reach players on their next join.
 */
public final class ServerConfigResync {
    public static final MessageCodec<ConfigFileContents> CODEC = new MessageCodec<>() {
        @Override
        public void encode(ConfigFileContents message, FriendlyByteBuf buffer) {
            buffer.writeUtf(message.fileName());
            buffer.writeByteArray(message.contents());
        }

        @Override
        public ConfigFileContents decode(FriendlyByteBuf buffer) {
            return new ConfigFileContents(buffer.readUtf(), buffer.readByteArray());
        }
    };

    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);
    private static volatile NetworkChannel channel;

    private ServerConfigResync() {
    }

    /** One SERVER config file, as Forge stores it on disk. */
    public record ConfigFileContents(String fileName, byte[] contents) {
    }

    public static void register(NetworkChannel libChannel) {
        channel = libChannel;
        libChannel.registerClientbound(ConfigFileContents.class, CODEC, ServerConfigResync::applyOnClient);
    }

    /**
     * Sends a changed SERVER file to every remote player. Does nothing for other file types, on a client without a
     * server, or for the single-player host, who already shares the server's config.
     */
    static void broadcastIfServerFile(ModConfig changedConfig) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        boolean isServerFile = changedConfig.getType() == ModConfig.Type.SERVER;
        if (!isServerFile || server == null || channel == null) {
            return;
        }
        server.execute(() -> sendToRemotePlayers(server, changedConfig));
    }

    private static void sendToRemotePlayers(MinecraftServer server, ModConfig changedConfig) {
        ConfigFileContents message = readContents(changedConfig);
        if (message == null) {
            return;
        }
        server.getPlayerList().getPlayers().stream()
                .filter(player -> !player.connection.connection.isMemoryConnection())
                .forEach(player -> channel.sendToPlayer(player, message));
        LOG.debug(ImpactsLog.CONFIG_CATEGORY, "server config sent to players", "file", changedConfig.getFileName());
    }

    private static ConfigFileContents readContents(ModConfig config) {
        try {
            return new ConfigFileContents(config.getFileName(), Files.readAllBytes(config.getFullPath()));
        } catch (IOException readFailure) {
            LOG.warn(ImpactsLog.CONFIG_CATEGORY, "server config not sent, file unreadable", "file", config.getFileName(),
                    "reason", readFailure.getMessage());
            return null;
        }
    }

    private static void applyOnClient(ConfigFileContents message) {
        ModConfig config = ConfigTracker.INSTANCE.fileMap().get(message.fileName());
        if (config == null) {
            return;
        }
        config.acceptSyncedConfig(message.contents());
        LOG.debug(ImpactsLog.CONFIG_CATEGORY, "server config received", "file", message.fileName());
    }
}
