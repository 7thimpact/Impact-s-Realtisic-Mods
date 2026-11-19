package com.impact.realisticfood.food;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Every cut, piece, fillet and slice. A piece's food values come from its whole item ({@link FoodValueRules#piece}),
 * except for potato slices, which aren't eaten raw. Raw pieces cook into their {@link #cooked} piece.
 */
public enum FoodPiece {
    COOKED_BEEF_CUT("cooked_beef_cut", Items.COOKED_BEEF, true, null),
    COOKED_PORK_CUT("cooked_pork_cut", Items.COOKED_PORKCHOP, true, null),
    COOKED_MUTTON_CUT("cooked_mutton_cut", Items.COOKED_MUTTON, true, null),
    COOKED_CHICKEN_PIECE("cooked_chicken_piece", Items.COOKED_CHICKEN, true, null),
    COOKED_RABBIT_PIECE("cooked_rabbit_piece", Items.COOKED_RABBIT, true, null),
    COOKED_COD_FILLET("cooked_cod_fillet", Items.COOKED_COD, true, null),
    COOKED_SALMON_FILLET("cooked_salmon_fillet", Items.COOKED_SALMON, true, null),
    FRIED_POTATO_SLICES("fried_potato_slices", Items.BAKED_POTATO, true, null),
    RAW_BEEF_CUT("raw_beef_cut", Items.BEEF, true, COOKED_BEEF_CUT),
    RAW_PORK_CUT("raw_pork_cut", Items.PORKCHOP, true, COOKED_PORK_CUT),
    RAW_MUTTON_CUT("raw_mutton_cut", Items.MUTTON, true, COOKED_MUTTON_CUT),
    RAW_CHICKEN_PIECE("raw_chicken_piece", Items.CHICKEN, true, COOKED_CHICKEN_PIECE),
    RAW_RABBIT_PIECE("raw_rabbit_piece", Items.RABBIT, true, COOKED_RABBIT_PIECE),
    RAW_COD_FILLET("raw_cod_fillet", Items.COD, true, COOKED_COD_FILLET),
    RAW_SALMON_FILLET("raw_salmon_fillet", Items.SALMON, true, COOKED_SALMON_FILLET),
    POTATO_SLICES("potato_slices", Items.POTATO, false, FRIED_POTATO_SLICES),
    CARROT_SLICES("carrot_slices", Items.CARROT, true, null);

    public final String path;
    public final Item wholeItem;
    public final boolean isEdible;
    /** The piece this one cooks into, or null for pieces that are already cooked (or eaten raw, like carrot slices). */
    public final FoodPiece cooked;

    FoodPiece(String path, Item wholeItem, boolean isEdible, FoodPiece cooked) {
        this.path = path;
        this.wholeItem = wholeItem;
        this.isEdible = isEdible;
        this.cooked = cooked;
    }

    public boolean cooks() {
        return cooked != null;
    }

    public boolean isMeatCut() {
        return switch (this) {
            case RAW_BEEF_CUT, RAW_PORK_CUT, RAW_MUTTON_CUT, RAW_CHICKEN_PIECE, RAW_RABBIT_PIECE -> true;
            default -> false;
        };
    }

    public boolean isFishFillet() {
        return this == RAW_COD_FILLET || this == RAW_SALMON_FILLET;
    }
}
