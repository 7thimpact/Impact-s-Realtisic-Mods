package com.impact.lib.mixin;

import com.impact.lib.attachment.PlayerAttachmentData;
import com.impact.lib.attachment.PlayerAttachmentHolder;
import com.impact.lib.attachment.PlayerAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Gives every player a store for Impact attachments and saves it with the player. */
@Mixin(Player.class)
public abstract class PlayerAttachmentsMixin implements PlayerAttachmentHolder {
    @Unique
    private final Map<String, PlayerAttachmentData> impactslib$attachmentsById = new ConcurrentHashMap<>();

    @Override
    public Map<String, PlayerAttachmentData> impactslib$attachments() {
        return impactslib$attachmentsById;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void impactslib$saveAttachments(CompoundTag playerTag, CallbackInfo callback) {
        PlayerAttachments.save((Player) (Object) this, playerTag);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void impactslib$loadAttachments(CompoundTag playerTag, CallbackInfo callback) {
        PlayerAttachments.load((Player) (Object) this, playerTag);
    }
}
