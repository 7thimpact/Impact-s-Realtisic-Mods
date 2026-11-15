package com.impact.realisticinventory.network;

import com.impact.lib.ResourceIds;
import com.impact.lib.network.NetworkChannel;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.client.ClientHotbar;
import com.impact.realisticinventory.hotbar.HandSwitch;
import com.impact.realisticinventory.hotbar.SwitchSource;
import net.minecraft.server.level.ServerPlayer;

/** Inventory's {@code impactsinventory:main} channel. */
public final class InventoryNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static volatile NetworkChannel channel;

    private InventoryNetwork() {
    }

    /** Creates the channel and registers every message. Called once, during mod construction. */
    public static void create() {
        channel = LoaderPlatform.current().createChannel(ResourceIds.of(InventoryMod.MOD_ID, "main"), PROTOCOL_VERSION);
        channel.registerServerbound(SelectHotbarSlotMessage.class, SelectHotbarSlotMessage.CODEC, HandSwitch::handleRequest);
        channel.registerClientbound(HotbarStateMessage.class, HotbarStateMessage.CODEC, message -> ClientHotbar.applyState(message));
    }

    public static void sendHotbarState(ServerPlayer player) {
        channel.sendToPlayer(player, new HotbarStateMessage(HandSwitch.stateOf(player).activeSlot()));
    }

    /** Client side: asks the server to switch slots. */
    public static void requestSlot(int requestedSlot, SwitchSource source) {
        channel.sendToServer(new SelectHotbarSlotMessage(requestedSlot, source));
    }
}
