package com.impact.realisticinventory.pack;

import com.impact.realisticinventory.config.InventoryCommonConfig;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** The pack a player wears in the back slot, and putting items into it. Server side for every change. */
public final class WornPack {
    private WornPack() {
    }

    /** Packs can be switched off in the common config ({@code packs.enabled}): they can't be worn, opened or filled then. */
    public static boolean arePacksEnabled() {
        return InventoryCommonConfig.get().features.arePacksEnabled.get();
    }

    /** The worn pack, when the back slot holds one of Inventory's packs. */
    public static Optional<ItemStack> of(Player player) {
        ItemStack worn = BackSlotStorage.get(player);
        return PackRules.isPack(worn) ? Optional.of(worn) : Optional.empty();
    }

    public static Optional<PackKind> kindOf(Player player) {
        return of(player).map(pack -> ((PackItem) pack.getItem()).kind());
    }

    public static int slotCount(ItemStack pack) {
        return ((PackItem) pack.getItem()).kind().slotCount();
    }

    /** Adds the stack onto matching stacks already in the worn pack. Returns how many items moved. */
    public static int stackOntoMatching(Player player, ItemStack stack) {
        return changeContents(player, stack, true);
    }

    /** Puts the stack into the worn pack's empty slots. Returns how many items moved. */
    public static int placeInEmptySlots(Player player, ItemStack stack) {
        return changeContents(player, stack, false);
    }

    private static int changeContents(Player player, ItemStack stack, boolean isStackingOnly) {
        Optional<ItemStack> pack = of(player);
        boolean canGoIn = pack.isPresent() && !stack.isEmpty() && arePacksEnabled() && PackRules.canGoInPack(stack);
        if (!canGoIn || player.level().isClientSide()) {
            return 0;
        }
        Optional<PackContainer> openContainer = PackContainer.openFor(pack.get());
        if (openContainer.isPresent()) {
            int movedCount = moveInto(openContainer.get().slots(), stack, isStackingOnly);
            openContainer.get().setChanged();
            return movedCount;
        }
        List<ItemStack> itemsBeyondSize = new ArrayList<>();
        NonNullList<ItemStack> slots = PackContents.read(pack.get(), slotCount(pack.get()), itemsBeyondSize);
        int movedCount = moveInto(slots, stack, isStackingOnly);
        if (movedCount > 0) {
            PackContents.write(pack.get(), slots);
            PackOverflow.handOut(player, itemsBeyondSize);
        }
        return movedCount;
    }

    private static int moveInto(NonNullList<ItemStack> slots, ItemStack stack, boolean isStackingOnly) {
        int startingCount = stack.getCount();
        if (isStackingOnly) {
            slots.forEach(slotStack -> stackOnto(slotStack, stack));
        } else {
            fillEmptySlots(slots, stack);
        }
        return startingCount - stack.getCount();
    }

    private static void stackOnto(ItemStack slotStack, ItemStack incoming) {
        boolean canStack = !incoming.isEmpty() && !slotStack.isEmpty() && ItemStack.isSameItemSameTags(slotStack, incoming);
        if (!canStack) {
            return;
        }
        int movedCount = Math.min(incoming.getCount(), slotStack.getMaxStackSize() - slotStack.getCount());
        if (movedCount > 0) {
            slotStack.grow(movedCount);
            incoming.shrink(movedCount);
        }
    }

    private static void fillEmptySlots(NonNullList<ItemStack> slots, ItemStack incoming) {
        for (int slotIndex = 0; slotIndex < slots.size() && !incoming.isEmpty(); slotIndex++) {
            if (slots.get(slotIndex).isEmpty()) {
                slots.set(slotIndex, incoming.split(incoming.getMaxStackSize()));
            }
        }
    }
}
