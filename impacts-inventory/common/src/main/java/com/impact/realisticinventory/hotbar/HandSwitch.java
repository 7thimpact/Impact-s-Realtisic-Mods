package com.impact.realisticinventory.hotbar;

import com.impact.lib.ResourceIds;
import com.impact.lib.attachment.DeathCopyRule;
import com.impact.lib.attachment.PlayerAttachment;
import com.impact.lib.attachment.PlayerAttachments;
import com.impact.lib.counter.ImpactsCounters;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.advancement.InventoryTriggers;
import com.impact.realisticinventory.config.InventoryServerConfig;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import com.impact.realisticinventory.inventory.InventoryLocks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * The two hands: the main hand is vanilla inventory index 0 (the one hotbar slot) and the off hand is vanilla's own
 * off-hand slot beside it. Swapping them is vanilla's swap-hands key (F, or the scroll wheel), so vanilla moves and
 * syncs the items; Inventory only counts the swap for its achievements and adds the optional swap cooldown.
 */
public final class HandSwitch {
    /** Kept like the inventory: lost on death unless keepInventory is on. */
    public static final PlayerAttachment<HotbarState> HOTBAR =
            new PlayerAttachment<>(ResourceIds.of(InventoryMod.MOD_ID, "hotbar"), HotbarState::new, DeathCopyRule.LIKE_INVENTORY);
    /** The "Juggler" achievement counts swaps within this many ticks (5 seconds). */
    public static final long RECENT_SWITCH_WINDOW_TICKS = 100;
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);

    private HandSwitch() {
    }

    public static HotbarState stateOf(Player player) {
        return PlayerAttachments.get(player, HOTBAR);
    }

    /** Server, as vanilla swaps the hands: the achievements, the swap counter, the optional cooldown and the log line. */
    public static void onHandsSwapped(ServerPlayer player) {
        if (!InventoryLocks.isActiveFor(player)) {
            return;
        }
        // Vanilla swaps right after this, so the off hand's item is about to be the main hand's.
        ItemStack comingToMainHand = player.getOffhandItem();
        int recentSwitches = stateOf(player).recordSwitch(player.level().getGameTime(), RECENT_SWITCH_WINDOW_TICKS);
        int cooldownTicks = InventoryServerConfig.get().hotbar.swapCooldownTicks.get();
        // An item already cooling down (a thrown ender pearl) keeps its own, possibly longer, cooldown.
        boolean isAlreadyCooling = player.getCooldowns().isOnCooldown(comingToMainHand.getItem());
        if (cooldownTicks > 0 && !comingToMainHand.isEmpty() && !isAlreadyCooling) {
            player.getCooldowns().addCooldown(comingToMainHand.getItem(), cooldownTicks);
        }
        InventoryTriggers.SLOT_SWITCHED.trigger(player, recentSwitches);
        ImpactsCounters.add(player, InventoryTriggers.SLOT_SWITCHES_COUNTER, null, 1);
        LOG.debug(InventoryLogCategories.HOTBAR, "hands swapped", "player", player.getGameProfile().getName(),
                "toMainHand", ResourceIds.itemId(comingToMainHand.getItem()), "toOffHand", ResourceIds.itemId(player.getMainHandItem().getItem()),
                "wasUsingItem", player.isUsingItem(), "recentSwaps", recentSwitches);
    }

    /** Swaps the hands the way vanilla's swap-hands key does, for callers without a key press (tests, other mods). */
    public static void swapHands(ServerPlayer player) {
        onHandsSwapped(player);
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        player.setItemInHand(InteractionHand.OFF_HAND, player.getItemInHand(InteractionHand.MAIN_HAND));
        player.setItemInHand(InteractionHand.MAIN_HAND, offHand);
        player.stopUsingItem();
    }
}
