package com.impact.lib.mixin;

import com.impact.lib.attachment.PlayerAttachments;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Copies Impact attachments to the new player object on respawn, following each attachment's death rule. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerAttachmentsMixin {
    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void impactslib$copyAttachments(ServerPlayer oldPlayer, boolean isKeepingEverything, CallbackInfo callback) {
        PlayerAttachments.copyOnRespawn(oldPlayer, (ServerPlayer) (Object) this, isKeepingEverything);
    }
}
