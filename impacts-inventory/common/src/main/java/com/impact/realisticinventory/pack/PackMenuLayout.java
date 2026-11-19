package com.impact.realisticinventory.pack;

/**
 * Where the pack screen puts its slots. Pack slots come first, in 3 by 3 grids like the pockets (a satchel is one row of
 * 3, a frame pack two grids side by side), up to three grids per band; the player's pockets and hands sit below them in
 * the same places as in the inventory screen. Both the menu and the screen use these numbers.
 */
public final class PackMenuLayout {
    public static final int SCREEN_WIDTH = 176;
    public static final int SLOT_SPACING = 18;
    public static final int TITLE_Y = 6;
    public static final int PACK_TOP = 18;
    private static final int GRID_COLUMNS = 3;
    private static final int GRID_ROWS = 3;
    private static final int GRID_SLOTS = GRID_COLUMNS * GRID_ROWS;
    private static final int GRIDS_PER_BAND = 3;
    private static final int GRID_GAP = 4;
    private static final int BAND_GAP = 4;
    private static final int PLAYER_SECTION_GAP = 14;
    private static final int POCKETS_LEFT = 62;
    private static final int HANDS_LEFT = 71;
    private static final int HANDS_BELOW_POCKETS = 4;
    private static final int BOTTOM_MARGIN = 7;
    private static final int SLOT_FRAME_INSET = 1;

    private final int packSlotCount;

    public PackMenuLayout(int packSlotCount) {
        this.packSlotCount = packSlotCount;
    }

    public int packSlotX(int packSlot) {
        int grid = packSlot / GRID_SLOTS;
        int band = grid / GRIDS_PER_BAND;
        int gridInBand = grid % GRIDS_PER_BAND;
        int column = packSlot % GRID_SLOTS % GRID_COLUMNS;
        int gridsInBand = gridsInBand(band);
        int gap = gridsInBand < GRIDS_PER_BAND ? GRID_GAP : 0;
        int bandWidth = gridsInBand * GRID_COLUMNS * SLOT_SPACING + (gridsInBand - 1) * gap;
        int bandLeft = (SCREEN_WIDTH - bandWidth) / 2 + SLOT_FRAME_INSET;
        return bandLeft + gridInBand * (GRID_COLUMNS * SLOT_SPACING + gap) + column * SLOT_SPACING;
    }

    public int packSlotY(int packSlot) {
        int grid = packSlot / GRID_SLOTS;
        int band = grid / GRIDS_PER_BAND;
        int row = packSlot % GRID_SLOTS / GRID_COLUMNS;
        return PACK_TOP + band * (GRID_ROWS * SLOT_SPACING + BAND_GAP) + row * SLOT_SPACING;
    }

    public int pocketsTop() {
        return PACK_TOP + packHeight() + PLAYER_SECTION_GAP;
    }

    public int pocketX(int column) {
        return POCKETS_LEFT + column * SLOT_SPACING;
    }

    public int pocketY(int row) {
        return pocketsTop() + row * SLOT_SPACING;
    }

    public int handsTop() {
        return pocketsTop() + GRID_ROWS * SLOT_SPACING + HANDS_BELOW_POCKETS;
    }

    /** The left edge of hotbar slot 1 or 2. */
    public int handX(int hotbarSlot) {
        return HANDS_LEFT + (hotbarSlot - 1) * SLOT_SPACING;
    }

    public int screenHeight() {
        return handsTop() + SLOT_SPACING + BOTTOM_MARGIN;
    }

    private int packHeight() {
        int gridCount = (packSlotCount + GRID_SLOTS - 1) / GRID_SLOTS;
        int bandCount = (gridCount + GRIDS_PER_BAND - 1) / GRIDS_PER_BAND;
        int fullBandsHeight = (bandCount - 1) * (GRID_ROWS * SLOT_SPACING + BAND_GAP);
        return fullBandsHeight + lastBandRows(bandCount - 1) * SLOT_SPACING;
    }

    private int gridsInBand(int band) {
        int gridCount = (packSlotCount + GRID_SLOTS - 1) / GRID_SLOTS;
        return Math.min(GRIDS_PER_BAND, gridCount - band * GRIDS_PER_BAND);
    }

    // A band with a full grid is 3 rows tall; a band holding one partly filled grid only as tall as its rows.
    private int lastBandRows(int band) {
        int slotsInBand = packSlotCount - band * GRIDS_PER_BAND * GRID_SLOTS;
        if (slotsInBand >= GRID_SLOTS) {
            return GRID_ROWS;
        }
        return (slotsInBand + GRID_COLUMNS - 1) / GRID_COLUMNS;
    }
}
