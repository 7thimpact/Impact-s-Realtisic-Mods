package com.impact.lib.hold;

import com.impact.lib.ImpactsLib;
import com.impact.lib.client.WorkMotionReceiver;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.network.MessageCodec;
import com.impact.lib.network.NetworkChannel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Motions a mod shows on a player for work that isn't an item's hold-to-use, like Crafting's timed crafts. The server
 * says what each player is doing; Lib sends each change to everyone who can see the player (and again every couple of
 * seconds while it lasts, for players who walk up later), and every client draws it like a hold motion.
 */
public final class WorkMotions {
    static final int RESEND_INTERVAL_TICKS = 40;
    private static final int NO_MOTION = -1;
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);
    private static final Map<UUID, HoldMotion> SHOWN_ON_SERVER = new ConcurrentHashMap<>();
    // Keyed by the client's player object: a player who leaves and comes back is a new object, so nothing goes stale.
    private static final Map<Player, ClientWork> SHOWN_ON_CLIENT = Collections.synchronizedMap(new WeakHashMap<>());
    private static volatile NetworkChannel channel;

    private WorkMotions() {
    }

    /** On the client: the motion a player's work shows, and the level's game time when it began. */
    public record ClientWork(HoldMotion motion, long startGameTime) {
    }

    /** Lib's network message: the player (by entity ID) and the motion shown, if any. */
    public record WorkMotionMessage(int entityId, Optional<HoldMotion> motion) {
    }

    /** Registers the message on Lib's channel. Called once, during Lib's mod construction. */
    public static void register(NetworkChannel libChannel) {
        channel = libChannel;
        // A lambda, not a method reference: the client class loads only when a message arrives, never on a server.
        libChannel.registerClientbound(WorkMotionMessage.class, new Codec(), message -> WorkMotionReceiver.receive(message));
    }

    /**
     * Server, as often as every tick: the motion this player's work shows now, or none. Only changes go out (plus the
     * repeats for players who come into view), so a mod can call this from its player tick.
     */
    public static void show(ServerPlayer player, Optional<HoldMotion> motion) {
        HoldMotion previous = motion.isPresent() ? SHOWN_ON_SERVER.put(player.getUUID(), motion.get()) : SHOWN_ON_SERVER.remove(player.getUUID());
        boolean hasChanged = !Objects.equals(previous, motion.orElse(null));
        boolean isRepeatDue = motion.isPresent() && player.tickCount % RESEND_INTERVAL_TICKS == 0;
        if (!hasChanged && !isRepeatDue) {
            return;
        }
        if (channel != null) {
            channel.sendToTrackingAndSelf(player, new WorkMotionMessage(player.getId(), motion));
        }
        if (hasChanged) {
            String loggedMotion = motion.or(() -> Optional.ofNullable(previous)).map(shown -> shown.name().toLowerCase(Locale.ROOT)).orElse("none");
            LOG.debug(LibLogCategories.HOLD_TO_USE, motion.isPresent() ? "work motion started" : "work motion stopped",
                    "player", player.getGameProfile().getName(), "motion", loggedMotion);
        }
    }

    /** Server: what this player's work shows right now. */
    public static Optional<HoldMotion> shownMotion(Player player) {
        return Optional.ofNullable(SHOWN_ON_SERVER.get(player.getUUID()));
    }

    /** Server: the player left, so their motion is forgotten (a client that still shows it drops it with the player). */
    public static void forget(Player player) {
        SHOWN_ON_SERVER.remove(player.getUUID());
    }

    /** Client, from the server's message: a repeat of the motion already shown keeps its start, so it doesn't jump. */
    public static void onClientMessage(Player player, Optional<HoldMotion> motion) {
        if (motion.isEmpty()) {
            SHOWN_ON_CLIENT.remove(player);
            return;
        }
        ClientWork current = SHOWN_ON_CLIENT.get(player);
        if (current == null || current.motion() != motion.get()) {
            SHOWN_ON_CLIENT.put(player, new ClientWork(motion.get(), player.level().getGameTime()));
        }
    }

    /** Client: the work motion this player shows, if any. */
    public static Optional<ClientWork> clientWork(Player player) {
        return Optional.ofNullable(SHOWN_ON_CLIENT.get(player));
    }

    private static final class Codec implements MessageCodec<WorkMotionMessage> {
        @Override
        public void encode(WorkMotionMessage message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.entityId());
            buffer.writeVarInt(message.motion().map(Enum::ordinal).orElse(NO_MOTION));
        }

        @Override
        public WorkMotionMessage decode(FriendlyByteBuf buffer) {
            int entityId = buffer.readVarInt();
            int motionIndex = buffer.readVarInt();
            HoldMotion[] motions = HoldMotion.values();
            boolean isKnownMotion = motionIndex >= 0 && motionIndex < motions.length;
            return new WorkMotionMessage(entityId, isKnownMotion ? Optional.of(motions[motionIndex]) : Optional.empty());
        }
    }
}
