package com.impact.realisticinventory.pack;

import com.impact.realisticinventory.inventory.InventoryLayout;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The open pack: the pack's slots above the player's pockets and hands. On the server the pack slots read and write
 * the worn pack's NBT; on the client they hold the synced copy. Shift-click moves between the pack and the pockets.
 */
public final class PackMenu extends AbstractContainerMenu {
    private static final int POCKET_COLUMNS = 3;

    private final Container packContainer;
    private final int packSlotCount;
    private final PackMenuLayout layout;

    public PackMenu(int containerId, Inventory playerInventory, Container packContainer) {
        super(PackMenus.packMenuType(), containerId);
        this.packContainer = packContainer;
        this.packSlotCount = packContainer.getContainerSize();
        this.layout = new PackMenuLayout(packSlotCount);
        addPackSlots();
        addPlayerSlots(playerInventory);
        packContainer.startOpen(playerInventory.player);
    }

    public int packSlotCount() {
        return packSlotCount;
    }

    public PackMenuLayout layout() {
        return layout;
    }

    @Override
    public boolean stillValid(Player player) {
        return packContainer.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        packContainer.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int clickedSlotIndex) {
        Slot clickedSlot = slots.get(clickedSlotIndex);
        if (!clickedSlot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack clickedStack = clickedSlot.getItem();
        ItemStack originalStack = clickedStack.copy();
        boolean isPackSlot = clickedSlotIndex < packSlotCount;
        boolean hasMoved = isPackSlot
                ? moveItemStackTo(clickedStack, packSlotCount, slots.size(), false)
                : moveItemStackTo(clickedStack, 0, packSlotCount, false);
        if (!hasMoved) {
            return ItemStack.EMPTY;
        }
        if (clickedStack.isEmpty()) {
            clickedSlot.setByPlayer(ItemStack.EMPTY);
        } else {
            clickedSlot.setChanged();
        }
        return originalStack;
    }

    private void addPackSlots() {
        for (int packSlot = 0; packSlot < packSlotCount; packSlot++) {
            addSlot(new PackMenuSlot(packContainer, packSlot, layout.packSlotX(packSlot), layout.packSlotY(packSlot)));
        }
    }

    // The hands start in hotbar order for slot 1 active; the screen moves them to match the real active slot.
    private void addPlayerSlots(Inventory playerInventory) {
        for (int pocket = 0; pocket < InventoryLayout.POCKET_COUNT; pocket++) {
            int column = pocket % POCKET_COLUMNS;
            int row = pocket / POCKET_COLUMNS;
            addSlot(new Slot(playerInventory, InventoryLayout.FIRST_POCKET_INDEX + pocket, layout.pocketX(column), layout.pocketY(row)));
        }
        addSlot(new Slot(playerInventory, InventoryLayout.MAIN_HAND_INDEX, layout.mainHandX(), layout.handsTop()));
        addSlot(new OffHandSlot(playerInventory, layout.offHandX(), layout.handsTop()));
    }

    /** The off hand shows vanilla's empty-shield outline, as in the inventory screen. */
    private static final class OffHandSlot extends Slot {
        OffHandSlot(Inventory playerInventory, int x, int y) {
            super(playerInventory, InventoryLayout.OFF_HAND_INDEX, x, y);
        }

        @Override
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
        }
    }
}
