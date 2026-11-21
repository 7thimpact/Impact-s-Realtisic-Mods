package com.impact.realisticfood.cutting;

import com.impact.realisticfood.food.FoodPiece;
import com.impact.realisticfood.registry.FoodItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;

/** Every cut from the spec's "Knives and prep" table, with the recipe's own yields and byproduct chances. */
public final class Cuts {
    private static final int PIECES_PER_CUT = 2;
    private static final int MELON_SEEDS_PER_SLICE = 1;
    private static final int PUMPKIN_SEEDS_PER_PUMPKIN = 4;
    private static final float BONE_CHANCE = 0.15F;
    private static final float FEATHER_CHANCE = 0.25F;
    private static final float RABBIT_HIDE_CHANCE = 0.1F;
    private static final float BONE_MEAL_CHANCE = 0.2F;

    public static final List<Cut> ALL = List.of(
            pieces(Items.BEEF, FoodPiece.RAW_BEEF_CUT, Items.BONE, BONE_CHANCE),
            pieces(Items.PORKCHOP, FoodPiece.RAW_PORK_CUT, Items.BONE, BONE_CHANCE),
            pieces(Items.MUTTON, FoodPiece.RAW_MUTTON_CUT, Items.BONE, BONE_CHANCE),
            pieces(Items.CHICKEN, FoodPiece.RAW_CHICKEN_PIECE, Items.FEATHER, FEATHER_CHANCE),
            pieces(Items.RABBIT, FoodPiece.RAW_RABBIT_PIECE, Items.RABBIT_HIDE, RABBIT_HIDE_CHANCE),
            pieces(Items.COD, FoodPiece.RAW_COD_FILLET, Items.BONE_MEAL, BONE_MEAL_CHANCE),
            pieces(Items.SALMON, FoodPiece.RAW_SALMON_FILLET, Items.BONE_MEAL, BONE_MEAL_CHANCE),
            new Cut(Items.POTATO, () -> FoodItems.piece(FoodPiece.POTATO_SLICES), PIECES_PER_CUT, Optional.empty()),
            new Cut(Items.CARROT, () -> FoodItems.piece(FoodPiece.CARROT_SLICES), PIECES_PER_CUT, Optional.empty()),
            seeds(Items.MELON_SLICE, Items.MELON_SEEDS, MELON_SEEDS_PER_SLICE),
            seeds(Items.PUMPKIN, Items.PUMPKIN_SEEDS, PUMPKIN_SEEDS_PER_PUMPKIN));

    private Cuts() {
    }

    /** The cut whose input is this item, if any. */
    public static Optional<Cut> forInput(Item input) {
        return ALL.stream().filter(cut -> cut.input() == input).findFirst();
    }

    private static Cut pieces(Item input, FoodPiece piece, Item byproduct, float chance) {
        return new Cut(input, () -> FoodItems.piece(piece), PIECES_PER_CUT, Optional.of(new Cut.CutByproduct(byproduct, chance)));
    }

    private static Cut seeds(Item input, Item seeds, int count) {
        return new Cut(input, () -> seeds, count, Optional.empty());
    }
}
