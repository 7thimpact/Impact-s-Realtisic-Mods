package com.impact.realisticinventory.pack;

import com.impact.realisticinventory.config.InventoryCommonConfig;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The packs a player wears, one on the back and one on the chest, and putting items into them (the back pack first).
 * Server side for every change.
 */
public final class WornPack {
    private WornPack() {
    }

    /** Packs can be switched off in the common config ({@code packs.enabled}): they can't be worn, opened or filled then. */
    public static boolean arePacksEnabled() {
        return InventoryCommonConfig.get().features.arePacksEnabled.get();
    }

    /** The pack worn there, when that slot holds one of Inventory's packs. */
    public static Optional<ItemStack> of(Player player, WornOn place) {
        ItemStack worn = WornSlots.get(player, place);
        return PackRules.isPack(worn) ? Optional.of(worn) : Optional.empty();
    }

    public static Optional<PackKind> kindOf(Player player, WornOn place) {
        return of(player, place).map(pack -> ((PackItem) pack.getItem()).kind());
    }

    /** Every pack worn, the back one first. */
    public static List<ItemStack> worn(Player player) {
        return Arrays.stream(WornOn.values()).flatMap(place -> of(player, place).stream()).toList();
    }

    /** The kinds of every pack worn, the back one first. */
    public static List<PackKind> wornKinds(Player player) {
        return worn(player).stream().map(pack -> ((PackItem) pack.getItem()).kind()).toList();
    }

    public static int slotCount(ItemStack pack) {
        return ((PackItem) pack.getItem()).kind().slotCount();
    }

    /** Adds the stack onto matching stacks already in the worn packs (the back one first). Returns how many items moved. */
    public static int stackOntoMatching(Player player, ItemStack stack) {
        return Arrays.stream(WornOn.values()).mapToInt(place -> changeContents(player, place, stack, true)).sum();
    }

    /** Puts the stack into the worn packs' empty slots (the back one first). Returns how many items moved. */
    public static int placeInEmptySlots(Player player, ItemStack stack) {
        return Arrays.stream(WornOn.values()).mapToInt(place -> changeContents(player, place, stack, false)).sum();
    }

    private static int changeContents(Player player, WornOn place, ItemStack stack, boolean isStackingOnly) {
        Optional<ItemStack> pack = of(player, place);
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
