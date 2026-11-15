package com.impact.realisticinventory.hotbar;

import com.impact.lib.ResourceIds;
import com.impact.lib.attachment.DeathCopyRule;
import com.impact.lib.attachment.PlayerAttachment;
import com.impact.lib.attachment.PlayerAttachments;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import com.impact.realisticinventory.inventory.InventoryLayout;
import com.impact.realisticinventory.inventory.InventoryLocks;
import com.impact.realisticinventory.network.InventoryNetwork;
import com.impact.realisticinventory.network.SelectHotbarSlotMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Switching hotbar slots. The main hand is always vanilla inventory index 0 and the off hand is vanilla's off-hand
 * slot, so every mod that reads the hands keeps working. Switching swaps the two stacks and flips the active slot.
 * Runs on the server; clients ask for a switch with {@link SelectHotbarSlotMessage}.
 */
public final class HandSwitch {
    /** Kept like the inventory: lost on death unless keepInventory is on. */
    public static final PlayerAttachment<HotbarState> HOTBAR =
            new PlayerAttachment<>(ResourceIds.of(InventoryMod.MOD_ID, "hotbar"), HotbarState::new, DeathCopyRule.LIKE_INVENTORY);
    /** The "Juggler" achievement counts switches within this many ticks (5 seconds). */
    public static final long RECENT_SWITCH_WINDOW_TICKS = 100;
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);

    private HandSwitch() {
    }

    public static HotbarState stateOf(Player player) {
        return PlayerAttachments.get(player, HOTBAR);
    }

    /** Makes the slot active. Nothing happens when it already is. */
    public static void selectSlot(ServerPlayer player, int slot, SwitchSource source) {
        if (!InventoryLocks.isActiveFor(player)) {
            return;
        }
        HotbarState state = stateOf(player);
        if (state.activeSlot() == slot) {
            return;
        }
        switchHands(player, state, source);
    }

    /** Makes the other slot active: what the scroll wheel and the swap-hands key do. */
    public static void switchToOtherSlot(ServerPlayer player, SwitchSource source) {
        if (!InventoryLocks.isActiveFor(player)) {
            return;
        }
        switchHands(player, stateOf(player), source);
    }

    /** Handles a client's request: slot 1, slot 2, or {@link SelectHotbarSlotMessage#OTHER_SLOT}. */
    public static void handleRequest(SelectHotbarSlotMessage message, ServerPlayer sender) {
        int requestedSlot = message.requestedSlot();
        if (requestedSlot == SelectHotbarSlotMessage.OTHER_SLOT) {
            switchToOtherSlot(sender, message.source());
            return;
        }
        boolean isValidSlot = requestedSlot == HotbarState.FIRST_SLOT || requestedSlot == HotbarState.SECOND_SLOT;
        if (!isValidSlot) {
            LOG.warn(InventoryLogCategories.HOTBAR, "invalid slot request refused", "player", sender.getGameProfile().getName(), "slot", requestedSlot);
            return;
        }
        selectSlot(sender, requestedSlot, message.source());
    }

    private static void switchHands(ServerPlayer player, HotbarState state, SwitchSource source) {
        Inventory inventory = player.getInventory();
        ItemStack previousMainHand = inventory.getItem(InventoryLayout.MAIN_HAND_INDEX);
        ItemStack previousOffHand = inventory.offhand.get(0);
        boolean wasUsingItem = player.isUsingItem();
        // Written straight to the lists: both are hand slots, and neither is locked.
        inventory.items.set(InventoryLayout.MAIN_HAND_INDEX, previousOffHand);
        inventory.offhand.set(0, previousMainHand);
        inventory.setChanged();
        player.stopUsingItem();
        int previousSlot = state.activeSlot();
        state.setActiveSlot(HotbarState.otherSlot(previousSlot));
        state.recordSwitch(player.level().getGameTime(), RECENT_SWITCH_WINDOW_TICKS);
        InventoryNetwork.sendHotbarState(player);
        logSwitch(player, previousSlot, state.activeSlot(), source, wasUsingItem);
    }

    private static void logSwitch(ServerPlayer player, int fromSlot, int toSlot, SwitchSource source, boolean wasUsingItem) {
        if (!LOG.isEnabled(InventoryLogCategories.HOTBAR)) {
            return;
        }
        String playerName = player.getGameProfile().getName();
        LOG.debug(InventoryLogCategories.HOTBAR, "switched", "player", playerName, "from", fromSlot, "to", toSlot,
                "mainHand", ResourceIds.itemId(player.getMainHandItem().getItem()),
                "offHand", ResourceIds.itemId(player.getOffhandItem().getItem()), "source", source.logName());
        if (wasUsingItem) {
            LOG.debug(InventoryLogCategories.HOTBAR, "hold action cancelled by switch", "player", playerName);
        }
    }
}
