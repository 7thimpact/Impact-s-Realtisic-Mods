package com.impact.realisticinventory.pack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

/**
 * A pack set down as a block: it remembers the pack item (its type, color and name) and holds the contents as a small
 * chest would, hoppers included. Picking it up or breaking it gives back the pack with everything inside.
 */
public final class PlacedPackBlockEntity extends BlockEntity implements Container {
    private static final String PACK_KEY = "Pack";
    private static final String SIZE_KEY = "Size";

    private ItemStack pack = ItemStack.EMPTY;
    private NonNullList<ItemStack> contents = NonNullList.create();

    public PlacedPackBlockEntity(BlockPos position, BlockState state) {
        super(PlacedPacks.blockEntityType(), position, state);
    }

    /** The pack item without its contents: what clients need to draw it. */
    public ItemStack pack() {
        return pack;
    }

    /** Takes in a pack: its contents move into this block, the rest of the item is kept to give back later. */
    public void holdPack(ItemStack packWithContents) {
        int slotCount = WornPack.slotCount(packWithContents);
        contents = PackContents.read(packWithContents, Math.max(slotCount, highestStoredSlot(packWithContents)), new ArrayList<>());
        pack = packWithContents.copyWithCount(1);
        pack.removeTagKey(PackContents.ITEMS_KEY);
        setChanged();
    }

    /** Hands the pack back with its contents, leaving this block empty. */
    public ItemStack takePack() {
        ItemStack packWithContents = pack.copy();
        PackContents.write(packWithContents, contents);
        pack = ItemStack.EMPTY;
        contents = NonNullList.create();
        setChanged();
        return packWithContents;
    }

    @Override
    public int getContainerSize() {
        return contents.size();
    }

    @Override
    public boolean isEmpty() {
        return contents.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int index) {
        return index >= 0 && index < contents.size() ? contents.get(index) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack removed = ContainerHelper.removeItem(contents, index, count);
        setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(contents, index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index >= 0 && index < contents.size()) {
            contents.set(index, stack);
            setChanged();
        }
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return PackRules.canGoInPack(stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return !pack.isEmpty() && Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        contents.replaceAll(stack -> ItemStack.EMPTY);
        setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        pack = ItemStack.of(tag.getCompound(PACK_KEY));
        contents = NonNullList.withSize(tag.getInt(SIZE_KEY), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, contents);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(PACK_KEY, pack.save(new CompoundTag()));
        tag.putInt(SIZE_KEY, contents.size());
        ContainerHelper.saveAllItems(tag, contents);
    }

    // Clients get the pack item only (to draw its kind and color), never the contents.
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.put(PACK_KEY, pack.save(new CompoundTag()));
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        pack = ItemStack.of(tag.getCompound(PACK_KEY));
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // A pack that shrank in the config still keeps every stored item while it's placed.
    private static int highestStoredSlot(ItemStack packWithContents) {
        return PackContents.highestUsedSlot(packWithContents) + 1;
    }
}
