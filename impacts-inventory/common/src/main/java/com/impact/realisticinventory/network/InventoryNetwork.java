package com.impact.realisticinventory.network;

import com.impact.lib.ResourceIds;
import com.impact.lib.network.NetworkChannel;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.pack.PackMenus;

/** Inventory's {@code impactsinventory:main} channel. */
public final class InventoryNetwork {
    private static final String PROTOCOL_VERSION = "2";
    private static volatile NetworkChannel channel;

    private InventoryNetwork() {
    }

    /** Creates the channel and registers every message. Called once, during mod construction. */
    public static void create() {
        channel = LoaderPlatform.current().createChannel(ResourceIds.of(InventoryMod.MOD_ID, "main"), PROTOCOL_VERSION);
        channel.registerServerbound(OpenPackMessage.class, OpenPackMessage.CODEC, (message, sender) -> PackMenus.openWornPack(sender));
    }

    /** Client side: asks the server to open the worn pack. */
    public static void requestOpenPack() {
        channel.sendToServer(new OpenPackMessage());
    }
}
