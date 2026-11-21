package com.impact.realisticinventory.inventory;

import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.config.InventoryServerConfig;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import com.impact.realisticinventory.pack.WornPack;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The places picked-up items can go, in the order {@code pickup.order} lists them (hands, pockets, pack by default).
 * The hands only take items that stack onto what's there, or go into an empty main hand (the active slot).
 */
public enum PickupDestination {
    HANDS("hands") {
        @Override
        void stackOnto(Inventory inventory, ItemStack stack) {
            growInventoryStack(inventory, InventoryLayout.MAIN_HAND_INDEX, stack);
            growInventoryStack(inventory, InventoryLayout.OFF_HAND_INDEX, stack);
        }

        @Override
        void placeInEmpty(Inventory inventory, ItemStack stack) {
            placeInEmptyInventorySlot(inventory, InventoryLayout.MAIN_HAND_INDEX, stack);
        }
    },
    POCKETS("pockets") {
        @Override
        void stackOnto(Inventory inventory, ItemStack stack) {
            for (int pocketIndex = InventoryLayout.FIRST_POCKET_INDEX; pocketIndex <= InventoryLayout.LAST_POCKET_INDEX; pocketIndex++) {
                growInventoryStack(inventory, pocketIndex, stack);
            }
        }

        @Override
        void placeInEmpty(Inventory inventory, ItemStack stack) {
            for (int pocketIndex = InventoryLayout.FIRST_POCKET_INDEX; pocketIndex <= InventoryLayout.LAST_POCKET_INDEX; pocketIndex++) {
                placeInEmptyInventorySlot(inventory, pocketIndex, stack);
            }
        }
    },
    /** The worn pack, when {@code pickup.intoPack} is on. Server side only. */
    PACK("pack") {
        @Override
        void stackOnto(Inventory inventory, ItemStack stack) {
            Item item = stack.getItem();
            logMovedIntoPack(inventory, item, isIntoPackOn() ? WornPack.stackOntoMatching(inventory.player, stack) : 0);
        }

        @Override
        void placeInEmpty(Inventory inventory, ItemStack stack) {
            Item item = stack.getItem();
            logMovedIntoPack(inventory, item, isIntoPackOn() ? WornPack.placeInEmptySlots(inventory.player, stack) : 0);
        }
    };

    private static final int POP_ANIMATION_TICKS = 5;
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);

    private final String configName;

    PickupDestination(String configName) {
        this.configName = configName;
    }

    /** Adds onto matching stacks already in this place. */
    abstract void stackOnto(Inventory inventory, ItemStack stack);

    /** Puts what's left into this place's empty slots. */
    abstract void placeInEmpty(Inventory inventory, ItemStack stack);

    /** The configured order; names it doesn't know are skipped. */
    static List<PickupDestination> configuredOrder() {
        return InventoryServerConfig.get().pickup.order.get().stream()
                .map(PickupDestination::fromConfigName)
                .flatMap(Optional::stream)
                .distinct()
                .toList();
    }

    private static Optional<PickupDestination> fromConfigName(String name) {
        return Arrays.stream(values()).filter(destination -> destination.configName.equals(name)).findFirst();
    }

    private static boolean isIntoPackOn() {
        return InventoryServerConfig.get().pickup.isIntoPack.get();
    }

    private static void growInventoryStack(Inventory inventory, int inventoryIndex, ItemStack incoming) {
        ItemStack existing = inventory.getItem(inventoryIndex);
        boolean canStack = !incoming.isEmpty() && !existing.isEmpty() && ItemStack.isSameItemSameTags(existing, incoming) && existing.isStackable();
        if (!canStack) {
            return;
        }
        int room = Math.min(existing.getMaxStackSize(), inventory.getMaxStackSize()) - existing.getCount();
        int movedCount = Math.min(room, incoming.getCount());
        if (movedCount <= 0) {
            return;
        }
        existing.grow(movedCount);
        existing.setPopTime(POP_ANIMATION_TICKS);
        incoming.shrink(movedCount);
        inventory.setChanged();
    }

    private static void placeInEmptyInventorySlot(Inventory inventory, int inventoryIndex, ItemStack incoming) {
        if (incoming.isEmpty() || !inventory.getItem(inventoryIndex).isEmpty()) {
            return;
        }
        int movedCount = Math.min(incoming.getCount(), Math.min(incoming.getMaxStackSize(), inventory.getMaxStackSize()));
        ItemStack placed = incoming.split(movedCount);
        placed.setPopTime(POP_ANIMATION_TICKS);
        inventory.setItem(inventoryIndex, placed);
    }

    // The item is read before the move, because a stack that moved completely is empty afterwards.
    private static void logMovedIntoPack(Inventory inventory, Item item, int movedCount) {
        if (movedCount <= 0) {
            return;
        }
        LOG.debug(InventoryLogCategories.PICKUP, "item sent into worn pack", "player", inventory.player.getGameProfile().getName(),
                "item", ResourceIds.itemId(item), "count", movedCount);
    }
}
