package com.impact.realisticinventory.inventory;

import com.impact.lib.ResourceIds;
import com.impact.lib.attachment.DeathCopyRule;
import com.impact.lib.attachment.PlayerAttachment;
import com.impact.lib.attachment.PlayerAttachments;
import com.impact.lib.item.ImpactsItems;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.config.InventoryServerConfig;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import com.impact.realisticinventory.lang.InventoryTranslations;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Moves items out of locked slots, so nothing is ever stuck or lost there.
 * <ul>
 *   <li>When the locks start to apply (the first join with Inventory, every login, creative mode ending): into the
 *   pockets, then a chest at the player's feet or a pile on the ground ({@code firstJoin.overflow}), with a chat
 *   message. The first time, the message explains the new inventory.</li>
 *   <li>When an item turns up in a locked slot anyway (a mod writing the inventory's lists directly): Lib's Give or
 *   drop.</li>
 * </ul>
 */
public final class LockedSlotOverflow {
    /** Kept through death: the first-join move happens once per player. */
    public static final PlayerAttachment<MigrationState> MIGRATION =
            new PlayerAttachment<>(ResourceIds.of(InventoryMod.MOD_ID, "migration"), MigrationState::new, DeathCopyRule.ALWAYS);
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);

    private LockedSlotOverflow() {
    }

    /** Server, once a tick per player. */
    public static void tick(ServerPlayer player) {
        MigrationState state = PlayerAttachments.get(player, MIGRATION);
        boolean areLocksActive = InventoryLocks.isActiveFor(player);
        boolean haveLocksJustStarted = state.recordLocksActive(areLocksActive);
        if (!areLocksActive) {
            return;
        }
        if (haveLocksJustStarted) {
            moveOutAfterLocking(player, state);
            return;
        }
        moveOutStrayItems(player);
    }

    private static void moveOutAfterLocking(ServerPlayer player, MigrationState state) {
        boolean isFirstJoin = !state.isFirstJoinDone();
        state.markFirstJoinDone();
        Inventory inventory = player.getInventory();
        List<ItemStack> lockedStacks = takeLockedStacks(inventory);
        if (lockedStacks.isEmpty()) {
            return;
        }
        List<ItemStack> leftovers = new ArrayList<>();
        for (ItemStack stack : lockedStacks) {
            PocketPlacement.addToPockets(inventory, stack);
            if (!stack.isEmpty()) {
                leftovers.add(stack);
            }
        }
        Outcome outcome = placeLeftovers(player, lockedStacks.size(), leftovers);
        logOutcome(player, isFirstJoin, outcome);
        tellPlayer(player, isFirstJoin, outcome);
    }

    private static List<ItemStack> takeLockedStacks(Inventory inventory) {
        List<ItemStack> lockedStacks = new ArrayList<>();
        for (int inventoryIndex = 0; inventoryIndex < inventory.items.size(); inventoryIndex++) {
            boolean holdsLockedItem = InventoryLayout.isLocked(inventoryIndex) && !inventory.items.get(inventoryIndex).isEmpty();
            if (holdsLockedItem) {
                lockedStacks.add(inventory.items.set(inventoryIndex, ItemStack.EMPTY));
            }
        }
        inventory.setChanged();
        return lockedStacks;
    }

    private static Outcome placeLeftovers(ServerPlayer player, int movedStackCount, List<ItemStack> leftovers) {
        int leftoverStackCount = leftovers.size();
        boolean isChestWanted = InventoryServerConfig.get().firstJoin.overflow.get() == InventoryServerConfig.OverflowTarget.CHEST;
        Optional<BlockPos> chestPosition = leftovers.isEmpty() || !isChestWanted ? Optional.empty() : OverflowChest.placeWith(player, leftovers);
        int droppedStackCount = leftovers.size();
        leftovers.forEach(stack -> dropInPile(player, stack));
        return new Outcome(movedStackCount, leftoverStackCount, chestPosition, droppedStackCount);
    }

    private static void dropInPile(ServerPlayer player, ItemStack stack) {
        ItemEntity droppedItem = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack, 0, 0, 0);
        droppedItem.setDefaultPickUpDelay();
        player.level().addFreshEntity(droppedItem);
    }

    private static void moveOutStrayItems(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int inventoryIndex = 0; inventoryIndex < inventory.items.size(); inventoryIndex++) {
            boolean holdsLockedItem = InventoryLayout.isLocked(inventoryIndex) && !inventory.items.get(inventoryIndex).isEmpty();
            if (holdsLockedItem) {
                moveOutStrayItem(player, inventory, inventoryIndex);
            }
        }
    }

    private static void moveOutStrayItem(ServerPlayer player, Inventory inventory, int inventoryIndex) {
        ItemStack stray = inventory.items.set(inventoryIndex, ItemStack.EMPTY);
        inventory.setChanged();
        LOG.debug(InventoryLogCategories.LOCKS, "stray item moved out of locked slot", "player", player.getGameProfile().getName(),
                "index", inventoryIndex, "item", ResourceIds.itemId(stray.getItem()), "count", stray.getCount(), "to", "give or drop");
        ImpactsItems.giveOrDrop(player, stray);
    }

    private static void logOutcome(ServerPlayer player, boolean isFirstJoin, Outcome outcome) {
        int stacksInChest = outcome.leftoverStacks() - outcome.droppedStacks();
        LOG.debug(InventoryLogCategories.MIGRATION, isFirstJoin ? "first join moved items" : "locked slots emptied",
                "player", player.getGameProfile().getName(), "stacks", outcome.movedStacks(),
                "toPockets", outcome.movedStacks() - outcome.leftoverStacks(), "toChest", stacksInChest,
                "chest", outcome.chestPosition().map(Object.class::cast).orElse("none"), "dropped", outcome.droppedStacks());
    }

    private static void tellPlayer(ServerPlayer player, boolean isFirstJoin, Outcome outcome) {
        player.sendSystemMessage(Component.translatable(isFirstJoin ? InventoryTranslations.FIRST_JOIN : InventoryTranslations.SLOTS_LOCKED));
        player.sendSystemMessage(detailMessage(outcome));
    }

    private static Component detailMessage(Outcome outcome) {
        if (outcome.droppedStacks() > 0) {
            return Component.translatable(InventoryTranslations.MOVED_TO_GROUND);
        }
        if (outcome.chestPosition().isPresent()) {
            return Component.translatable(InventoryTranslations.MOVED_TO_CHEST, outcome.chestPosition().get().toShortString());
        }
        return Component.translatable(InventoryTranslations.MOVED_TO_POCKETS);
    }

    /** Where the stacks from locked slots went: how many moved, how many didn't fit the pockets, and where those went. */
    private record Outcome(int movedStacks, int leftoverStacks, Optional<BlockPos> chestPosition, int droppedStacks) {
    }
}
