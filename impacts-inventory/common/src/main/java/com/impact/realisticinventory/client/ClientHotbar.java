package com.impact.realisticinventory.client;

import com.impact.realisticinventory.hotbar.HandSwitch;
import com.impact.realisticinventory.hotbar.HotbarState;
import com.impact.realisticinventory.hotbar.SwitchSource;
import com.impact.realisticinventory.network.HotbarStateMessage;
import com.impact.realisticinventory.network.InventoryNetwork;
import com.impact.realisticinventory.network.SelectHotbarSlotMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/** The local player's hotbar: turns keys and the scroll wheel into switch requests, and applies the server's answer. */
public final class ClientHotbar {
    private static final int FIRST_SLOT_KEY_INDEX = 0;
    private static final int SECOND_SLOT_KEY_INDEX = 1;

    private ClientHotbar() {
    }

    public static void applyState(HotbarStateMessage message) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        HandSwitch.stateOf(player).setActiveSlot(message.activeSlot());
    }

    public static int activeSlot() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? HotbarState.FIRST_SLOT : HandSwitch.stateOf(player).activeSlot();
    }

    /** A hotbar number key was pressed (index 0 for key 1). Keys 3 to 9 do nothing, freeing them for other uses. */
    public static void onNumberKey(int keyIndex) {
        if (keyIndex == FIRST_SLOT_KEY_INDEX) {
            InventoryNetwork.requestSlot(HotbarState.FIRST_SLOT, SwitchSource.NUMBER_KEY);
            return;
        }
        if (keyIndex == SECOND_SLOT_KEY_INDEX) {
            InventoryNetwork.requestSlot(HotbarState.SECOND_SLOT, SwitchSource.NUMBER_KEY);
        }
    }

    /** The scroll wheel moved, either way: switch to the other slot. */
    public static void onScroll() {
        InventoryNetwork.requestSlot(SelectHotbarSlotMessage.OTHER_SLOT, SwitchSource.SCROLL_WHEEL);
    }
}
