package com.impact.realisticinventory.pack;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A worn pack's slots as a container, for the pack menu on the server. Every change is written straight back into
 * the pack's NBT, so the pack item always holds its contents. The menu stays open only while that same pack is worn
 * in the same place (back or chest).
 * While it's open, other changes to that pack (pickups) go through this container, so nothing is overwritten.
 */
public final class PackContainer implements Container {
    private static final Map<ItemStack, PackContainer> OPEN_CONTAINERS_BY_PACK = Collections.synchronizedMap(new IdentityHashMap<>());

    private final Player wearer;
    private final WornOn place;
    private final ItemStack pack;
    private final NonNullList<ItemStack> slots;

    public PackContainer(Player wearer, WornOn place, ItemStack pack, NonNullList<ItemStack> slots) {
        this.wearer = wearer;
        this.place = place;
        this.pack = pack;
        this.slots = slots;
    }

    public ItemStack pack() {
        return pack;
    }

    /** The open container showing this very pack stack, if its menu is open. */
    public static Optional<PackContainer> openFor(ItemStack pack) {
        return Optional.ofNullable(OPEN_CONTAINERS_BY_PACK.get(pack));
    }

    /** The slots themselves, for changes made while the menu is open; call {@link #setChanged()} afterwards. */
    public NonNullList<ItemStack> slots() {
        return slots;
    }

    @Override
    public void startOpen(Player player) {
        OPEN_CONTAINERS_BY_PACK.put(pack, this);
    }

    @Override
    public void stopOpen(Player player) {
        OPEN_CONTAINERS_BY_PACK.remove(pack, this);
    }

    @Override
    public int getContainerSize() {
        return slots.size();
    }

    @Override
    public boolean isEmpty() {
        return slots.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int index) {
        return index >= 0 && index < slots.size() ? slots.get(index) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack removed = ContainerHelper.removeItem(slots, index, count);
        setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack removed = ContainerHelper.takeItem(slots, index);
        setChanged();
        return removed;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index < 0 || index >= slots.size()) {
            return;
        }
        slots.set(index, stack);
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return PackRules.canGoInPack(stack);
    }

    @Override
    public void setChanged() {
        PackContents.write(pack, slots);
    }

    @Override
    public boolean stillValid(Player player) {
        return WornSlots.get(wearer, place) == pack;
    }

    @Override
    public void clearContent() {
        slots.clear();
        setChanged();
    }
}
