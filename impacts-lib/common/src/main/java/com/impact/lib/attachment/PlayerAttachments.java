package com.impact.lib.attachment;

import com.impact.lib.ImpactsLib;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.log.ImpactsLog;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player data that Impact mods save with the player in vanilla NBT, under the key {@code impactslib:attachments}.
 * Each mod registers its own attachment during mod construction and reads it with {@link #get}. Syncing to the
 * client is each mod's own job, on its own network channel.
 */
public final class PlayerAttachments {
    public static final String NBT_KEY = "impactslib:attachments";
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);
    private static final Map<String, PlayerAttachment<?>> ATTACHMENTS_BY_ID = new ConcurrentHashMap<>();

    private PlayerAttachments() {
    }

    public static <T extends PlayerAttachmentData> PlayerAttachment<T> register(PlayerAttachment<T> attachment) {
        ATTACHMENTS_BY_ID.put(attachment.id().toString(), attachment);
        LOG.debug(LibLogCategories.REGISTRY, "player attachment registered", "id", attachment.id(), "onDeath", attachment.deathCopyRule());
        return attachment;
    }

    /** The player's data for this attachment, created empty on first use. */
    @SuppressWarnings("unchecked")
    public static <T extends PlayerAttachmentData> T get(Player player, PlayerAttachment<T> attachment) {
        Map<String, PlayerAttachmentData> attachments = ((PlayerAttachmentHolder) player).impactslib$attachments();
        return (T) attachments.computeIfAbsent(attachment.id().toString(), key -> attachment.factory().get());
    }

    /** Called by Lib's mixin when the player is saved. */
    public static void save(Player player, CompoundTag playerTag) {
        CompoundTag attachmentsTag = new CompoundTag();
        ((PlayerAttachmentHolder) player).impactslib$attachments()
                .forEach((attachmentId, data) -> attachmentsTag.put(attachmentId, data.save()));
        playerTag.put(NBT_KEY, attachmentsTag);
    }

    /** Called by Lib's mixin when the player is loaded. Data of attachments no longer registered is kept as is. */
    public static void load(Player player, CompoundTag playerTag) {
        CompoundTag attachmentsTag = playerTag.getCompound(NBT_KEY);
        for (String attachmentId : attachmentsTag.getAllKeys()) {
            loadOne(player, attachmentId, attachmentsTag.getCompound(attachmentId));
        }
    }

    /** Called by Lib's mixin when a player respawns or returns from the End, with the old player's data. */
    public static void copyOnRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean isKeepingEverything) {
        boolean isKeepingInventory = newPlayer.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
        ((PlayerAttachmentHolder) oldPlayer).impactslib$attachments().forEach((attachmentId, oldData) -> {
            PlayerAttachment<?> attachment = ATTACHMENTS_BY_ID.get(attachmentId);
            if (attachment == null || !isCopied(attachment.deathCopyRule(), isKeepingEverything, isKeepingInventory)) {
                return;
            }
            get(newPlayer, attachment).copyFrom(oldData);
        });
    }

    private static boolean isCopied(DeathCopyRule rule, boolean isKeepingEverything, boolean isKeepingInventory) {
        return switch (rule) {
            case ALWAYS -> true;
            case LIKE_INVENTORY -> isKeepingEverything || isKeepingInventory;
            case NEVER -> isKeepingEverything;
        };
    }

    private static void loadOne(Player player, String attachmentId, CompoundTag dataTag) {
        PlayerAttachment<?> attachment = ATTACHMENTS_BY_ID.get(attachmentId);
        if (attachment == null) {
            return;
        }
        get(player, attachment).load(dataTag);
    }
}
