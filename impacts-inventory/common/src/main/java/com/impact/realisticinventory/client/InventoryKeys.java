package com.impact.realisticinventory.client;

import com.impact.realisticinventory.network.InventoryNetwork;
import com.impact.realisticinventory.pack.WornPack;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/** Inventory's key bindings: B opens the worn pack. Players rebind it under Controls, in the Inventory category. */
public final class InventoryKeys {
    public static final KeyMapping OPEN_PACK = new KeyMapping("key.impactsinventory.open_pack", InputConstants.KEY_B, "key.categories.inventory");

    private InventoryKeys() {
    }

    /** Once a client tick, outside screens. */
    public static void onClientTick() {
        while (OPEN_PACK.consumeClick()) {
            requestOpenPack();
        }
    }

    /** Asks the server to open the worn pack. Nothing is sent when no pack is worn. */
    public static void requestOpenPack() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || WornPack.of(player).isEmpty()) {
            return;
        }
        InventoryNetwork.requestOpenPack();
    }

    public static boolean isOpenPackKey(int keyCode, int scanCode) {
        return OPEN_PACK.matches(keyCode, scanCode);
    }
}
