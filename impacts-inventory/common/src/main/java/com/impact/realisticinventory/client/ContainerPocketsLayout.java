package com.impact.realisticinventory.client;

import com.impact.lib.log.ImpactsLog;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.debug.InventoryLogCategories;
import com.impact.realisticinventory.inventory.InventoryLayout;
import com.impact.realisticinventory.inventory.InventoryLocks;
import com.impact.realisticinventory.mixin.SlotPositionAccessor;
import com.impact.realisticinventory.pack.PackMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * Other containers' screens (chests, furnaces, crafting tables, modded machines): the player's part shows the 9 pockets
 * as a 3 by 3 grid with the main hand beside it, drawn over a plain panel that covers vanilla's 36-slot background.
 * Only the client's slot positions move; clicks still go by slot index.
 */
public final class ContainerPocketsLayout {
    private static final int SLOT_SPACING = 18;
    private static final int SLOT_SIZE = 16;
    private static final int POCKET_COLUMNS = 3;
    private static final int GRID_SIZE = SLOT_SPACING * (POCKET_COLUMNS - 1) + SLOT_SIZE;
    private static final int MAIN_HAND_GAP = 8;
    private static final int PANEL_COLOR = 0xFFC6C6C6;
    private static final int PANEL_MARGIN = 1;
    private static final ImpactsLog LOG = ImpactsLog.of(InventoryMod.MOD_ID);
    // Kept per menu, so a screen resize (which runs init again) reuses the first layout instead of measuring moved slots.
    private static final Map<AbstractContainerMenu, ContainerPocketsLayout> LAYOUTS = new WeakHashMap<>();

    private final PlayerArea playerArea;
    private final List<Slot> movedSlots;
    private final Slot mainHand;

    private ContainerPocketsLayout(PlayerArea playerArea, List<Slot> movedSlots, Slot mainHand) {
        this.playerArea = playerArea;
        this.movedSlots = movedSlots;
        this.mainHand = mainHand;
    }

    /** Lays the player's slots out in the menu, once per menu. Nothing for Inventory's own screens or while the locks are off. */
    public static Optional<ContainerPocketsLayout> applyTo(AbstractContainerMenu menu, Player player) {
        boolean hasOwnLayout = menu.getClass() == InventoryMenu.class || menu instanceof PackMenu;
        if (hasOwnLayout || player == null || !InventoryLocks.isActiveFor(player)) {
            return Optional.empty();
        }
        ContainerPocketsLayout known = LAYOUTS.get(menu);
        if (known != null) {
            return Optional.of(known);
        }
        Optional<ContainerPocketsLayout> created = create(menu, player.getInventory());
        created.ifPresent(layout -> LAYOUTS.put(menu, layout));
        return created;
    }

    /** Covers the player's part of the background and draws a frame for each pocket and the highlighted main hand. */
    public void drawBackground(GuiGraphics graphics, int screenLeft, int screenTop) {
        graphics.fill(screenLeft + playerArea.left() - PANEL_MARGIN, screenTop + playerArea.top() - PANEL_MARGIN,
                screenLeft + playerArea.right() + PANEL_MARGIN, screenTop + playerArea.bottom() + PANEL_MARGIN, PANEL_COLOR);
        movedSlots.forEach(slot -> GuiTexturePieces.drawSlotFrame(graphics, screenLeft + slot.x, screenTop + slot.y));
        GuiTexturePieces.drawActiveHandFrame(graphics, screenLeft + mainHand.x, screenTop + mainHand.y);
    }

    private static Optional<ContainerPocketsLayout> create(AbstractContainerMenu menu, Inventory playerInventory) {
        List<Slot> playerSlots = menu.slots.stream()
                .filter(slot -> slot.container == playerInventory && slot.getContainerSlot() < Inventory.INVENTORY_SIZE)
                .toList();
        List<Slot> pockets = playerSlots.stream().filter(slot -> InventoryLayout.isPocket(slot.getContainerSlot()))
                .sorted(Comparator.comparingInt(Slot::getContainerSlot)).toList();
        Optional<Slot> mainHand = playerSlots.stream().filter(slot -> slot.getContainerSlot() == InventoryLayout.MAIN_HAND_INDEX).findFirst();
        if (pockets.size() != InventoryLayout.POCKET_COUNT || mainHand.isEmpty()) {
            return Optional.empty();
        }
        PlayerArea area = PlayerArea.around(playerSlots);
        int contentWidth = GRID_SIZE + MAIN_HAND_GAP + SLOT_SIZE;
        int gridLeft = area.left() + (area.width() - contentWidth) / 2;
        int gridTop = area.top() + (area.height() - GRID_SIZE) / 2;
        List<Slot> moved = new ArrayList<>();
        for (int pocket = 0; pocket < pockets.size(); pocket++) {
            int column = pocket % POCKET_COLUMNS;
            int row = pocket / POCKET_COLUMNS;
            moveTo(pockets.get(pocket), gridLeft + column * SLOT_SPACING, gridTop + row * SLOT_SPACING);
            moved.add(pockets.get(pocket));
        }
        moveTo(mainHand.get(), gridLeft + GRID_SIZE + MAIN_HAND_GAP, gridTop + (POCKET_COLUMNS - 1) * SLOT_SPACING);
        moved.add(mainHand.get());
        LOG.debug(InventoryLogCategories.SCREEN, "container pockets laid out", "menu", menu.getClass().getSimpleName(),
                "gridLeft", gridLeft, "gridTop", gridTop);
        return Optional.of(new ContainerPocketsLayout(area, moved, mainHand.get()));
    }

    private static void moveTo(Slot slot, int x, int y) {
        SlotPositionAccessor position = (SlotPositionAccessor) slot;
        position.impactsinventory$setX(x);
        position.impactsinventory$setY(y);
    }

    /** The rectangle vanilla's player slots covered, in screen-relative pixels. */
    private record PlayerArea(int left, int top, int right, int bottom) {
        static PlayerArea around(List<Slot> slots) {
            int left = slots.stream().mapToInt(slot -> slot.x).min().orElse(0);
            int top = slots.stream().mapToInt(slot -> slot.y).min().orElse(0);
            int right = slots.stream().mapToInt(slot -> slot.x + SLOT_SIZE).max().orElse(0);
            int bottom = slots.stream().mapToInt(slot -> slot.y + SLOT_SIZE).max().orElse(0);
            return new PlayerArea(left, top, right, bottom);
        }

        int width() {
            return right - left;
        }

        int height() {
            return bottom - top;
        }
    }
}
