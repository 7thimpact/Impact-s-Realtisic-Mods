package com.impact.realisticinventory.client;

import com.impact.realisticinventory.hotbar.HotbarState;
import com.impact.realisticinventory.inventory.InventoryLayout;
import com.impact.realisticinventory.mixin.SlotPositionAccessor;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

/**
 * Where Inventory's screen puts the player's slots, in vanilla's own inventory menu: the pockets in a 3 by 3 grid like
 * a crafting table's, centered, and the two hands below them in hotbar order. Every moved slot goes back to where it
 * was when the screen closes. The armor, the 2 by 2 grid and its output stay in vanilla's places.
 */
public final class InventoryScreenLayout {
    public static final int SLOT_SPACING = 18;
    public static final int POCKET_COLUMNS = 3;
    public static final int POCKETS_LEFT = 62;
    public static final int POCKETS_TOP = 84;
    public static final int HANDS_LEFT = 71;
    public static final int HANDS_TOP = 142;
    // In vanilla's inventory menu, slots 9 to 35 show inventory indexes 9 to 35, so the pockets are menu slots 9 to 17.
    private static final int MENU_FIRST_POCKET_SLOT = InventoryMenu.INV_SLOT_START;
    private static final int MENU_SLOTS_NEEDED = InventoryLayout.MENU_BACK_SLOT + 1;

    private final List<SavedPosition> savedPositions = new ArrayList<>();

    public void moveIntoLayout(InventoryMenu menu, int activeSlot) {
        restore();
        if (menu.slots.size() < MENU_SLOTS_NEEDED) {
            return;
        }
        for (int pocket = 0; pocket < InventoryLayout.POCKET_COUNT; pocket++) {
            int column = pocket % POCKET_COLUMNS;
            int row = pocket / POCKET_COLUMNS;
            move(menu.slots.get(MENU_FIRST_POCKET_SLOT + pocket), pocketLeft(column), pocketTop(row));
        }
        placeHands(menu, activeSlot);
    }

    /** The hand shown as hotbar slot 1 sits left, slot 2 right, so items stay put when the active slot changes. */
    public void placeHands(InventoryMenu menu, int activeSlot) {
        if (menu.slots.size() < MENU_SLOTS_NEEDED) {
            return;
        }
        move(menu.slots.get(InventoryLayout.MENU_MAIN_HAND_SLOT), handLeft(activeSlot), HANDS_TOP);
        move(menu.slots.get(InventoryLayout.MENU_OFF_HAND_SLOT), handLeft(HotbarState.otherSlot(activeSlot)), HANDS_TOP);
    }

    public void restore() {
        savedPositions.forEach(SavedPosition::putBack);
        savedPositions.clear();
    }

    public static int pocketLeft(int column) {
        return POCKETS_LEFT + column * SLOT_SPACING;
    }

    public static int pocketTop(int row) {
        return POCKETS_TOP + row * SLOT_SPACING;
    }

    /** The left edge of hotbar slot 1 or 2 in the screen. */
    public static int handLeft(int hotbarSlot) {
        return HANDS_LEFT + (hotbarSlot - HotbarState.FIRST_SLOT) * SLOT_SPACING;
    }

    private void move(Slot slot, int x, int y) {
        boolean isAlreadySaved = savedPositions.stream().anyMatch(saved -> saved.slot() == slot);
        if (!isAlreadySaved) {
            savedPositions.add(new SavedPosition(slot, slot.x, slot.y));
        }
        setPosition(slot, x, y);
    }

    private static void setPosition(Slot slot, int x, int y) {
        SlotPositionAccessor position = (SlotPositionAccessor) slot;
        position.impactsinventory$setX(x);
        position.impactsinventory$setY(y);
    }

    private record SavedPosition(Slot slot, int x, int y) {
        void putBack() {
            setPosition(slot, x, y);
        }
    }
}
