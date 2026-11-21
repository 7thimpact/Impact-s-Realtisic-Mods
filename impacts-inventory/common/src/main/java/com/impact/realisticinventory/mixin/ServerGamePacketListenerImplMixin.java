package com.impact.realisticinventory.mixin;

import com.impact.realisticinventory.hotbar.SelectedSlotGuard;
import com.impact.realisticinventory.inventory.InventoryLocks;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Once the packet runs on the server thread: refuses clients' requests to select another hotbar index, since the
 * main hand is always index 0.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    private static final String ENSURE_RUNNING_ON_SERVER_THREAD =
            "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V";

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleSetCarriedItem", cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = ENSURE_RUNNING_ON_SERVER_THREAD))
    private void impactsinventory$refuseHotbarSelection(ServerboundSetCarriedItemPacket packet, CallbackInfo callback) {
        if (!InventoryLocks.isActiveFor(player)) {
            return;
        }
        SelectedSlotGuard.logRefusedClientSelection(player, packet.getSlot());
        callback.cancel();
    }
}
