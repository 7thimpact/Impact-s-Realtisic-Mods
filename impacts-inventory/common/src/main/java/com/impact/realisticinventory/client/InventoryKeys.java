package com.impact.realisticinventory.client;

import com.impact.realisticinventory.lang.InventoryTranslations;
import com.impact.realisticinventory.network.InventoryNetwork;
import com.impact.realisticinventory.pack.WornOn;
import com.impact.realisticinventory.pack.WornPack;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.Optional;

/**
 * Inventory's key bindings: B opens the pack on the back, N the pack on the chest. Players rebind them under Controls,
 * in the Inventory category.
 */
public final class InventoryKeys {
    public static final KeyMapping OPEN_PACK = new KeyMapping(InventoryTranslations.OPEN_PACK_KEY, InputConstants.KEY_B, "key.categories.inventory");
    public static final KeyMapping OPEN_CHEST_PACK = new KeyMapping(InventoryTranslations.OPEN_CHEST_PACK_KEY, InputConstants.KEY_N,
            "key.categories.inventory");

    private InventoryKeys() {
    }

    /** Once a client tick, outside screens. */
    public static void onClientTick() {
        while (OPEN_PACK.consumeClick()) {
            requestOpenPack(WornOn.BACK);
        }
        while (OPEN_CHEST_PACK.consumeClick()) {
            requestOpenPack(WornOn.CHEST);
        }
    }

    /** Asks the server to open the pack worn there. Nothing is sent when no pack is worn there. */
    public static void requestOpenPack(WornOn place) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || WornPack.of(player, place).isEmpty()) {
            return;
        }
        InventoryNetwork.requestOpenPack(place);
    }

    /** The worn pack a key opens, if it's one of the open-pack keys. */
    public static Optional<WornOn> placeOpenedBy(int keyCode, int scanCode) {
        if (OPEN_PACK.matches(keyCode, scanCode)) {
            return Optional.of(WornOn.BACK);
        }
        return OPEN_CHEST_PACK.matches(keyCode, scanCode) ? Optional.of(WornOn.CHEST) : Optional.empty();
    }

    /** The key that opens the pack worn there. */
    public static KeyMapping keyFor(WornOn place) {
        return place == WornOn.BACK ? OPEN_PACK : OPEN_CHEST_PACK;
    }
}
