package com.impact.realisticinventory.pack;

import com.impact.lib.ResourceIds;
import com.impact.lib.attachment.DeathCopyRule;
import com.impact.lib.attachment.PlayerAttachment;
import com.impact.lib.attachment.PlayerAttachments;
import com.impact.lib.backslot.BackSlot;
import com.impact.lib.item.ImpactsItems;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.config.InventoryCommonConfig;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * The back slot: one item worn on the player's back, saved with the player. Inventory is Lib's back-slot provider, so
 * other mods reach it through Lib's {@code BackSlot}. It holds Inventory's packs and anything in
 * {@code #impactslib:back_slot_items}.
 */
public final class BackSlotStorage {
    /** Like the inventory: dropped on death unless keepInventory is on. */
    public static final PlayerAttachment<BackSlotContents> BACK_SLOT =
            new PlayerAttachment<>(ResourceIds.of(InventoryMod.MOD_ID, "back_slot"), BackSlotContents::new, DeathCopyRule.LIKE_INVENTORY);
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);

    private BackSlotStorage() {
    }

    public static BackSlotContents contentsOf(Player player) {
        return PlayerAttachments.get(player, BACK_SLOT);
    }

    public static ItemStack get(Player player) {
        return contentsOf(player).stack();
    }

    public static void set(Player player, ItemStack stack) {
        contentsOf(player).setStack(stack);
    }

    /** The back slot can be switched off in the common config ({@code packs.backSlot}); packs then can't be worn. */
    public static boolean isEnabled() {
        return InventoryCommonConfig.get().features.isBackSlotEnabled.get();
    }

    /** Whether the item fits the back slot: one of Inventory's packs, or another mod's back-slot item. */
    public static boolean accepts(ItemStack stack) {
        return isEnabled() && (stack.is(InventoryTags.PACKS) || stack.is(BackSlot.BACK_SLOT_ITEMS));
    }

    /** On death without keepInventory: the worn item drops with everything else (a pack keeps its contents). */
    public static void dropOnDeath(Player player) {
        ItemStack worn = get(player);
        if (worn.isEmpty()) {
            return;
        }
        set(player, ItemStack.EMPTY);
        String playerName = player.getGameProfile().getName();
        String itemId = ResourceIds.itemId(worn.getItem()).toString();
        if (EnchantmentHelper.hasVanishingCurse(worn)) {
            LOG.debug(InventoryLogCategories.PACKS, "back slot item vanished on death", "player", playerName, "item", itemId);
            return;
        }
        player.drop(worn, true, false);
        LOG.debug(InventoryLogCategories.PACKS, "back slot dropped on death", "player", playerName, "item", itemId);
    }

    /** With the back slot switched off, whatever is still worn goes to the hands and pockets, or the ground. */
    public static void emptyIfDisabled(ServerPlayer player) {
        ItemStack worn = get(player);
        if (isEnabled() || worn.isEmpty()) {
            return;
        }
        set(player, ItemStack.EMPTY);
        LOG.debug(InventoryLogCategories.PACKS, "back slot is off, worn item moved out", "player", player.getGameProfile().getName(),
                "item", ResourceIds.itemId(worn.getItem()));
        ImpactsItems.giveOrDrop(player, worn);
    }
}
