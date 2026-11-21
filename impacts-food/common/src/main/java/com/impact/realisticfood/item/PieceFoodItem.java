package com.impact.realisticfood.item;

import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.food.ConfiguredFoodValues;
import com.impact.realisticfood.food.FoodPiece;
import com.impact.realisticfood.food.FoodValueRules;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

/**
 * An edible cut, piece, fillet or slice. Its food values follow its whole item and {@code pieces.foodShare}. Forge
 * deprecates vanilla's {@code getFoodProperties()} in favor of a stack-aware version, but that version calls this one,
 * so overriding it covers vanilla's callers and Forge's.
 */
@SuppressWarnings("deprecation")
public final class PieceFoodItem extends Item {
    private final FoodPiece piece;
    private final ConfiguredFoodValues<Double> food;

    public PieceFoodItem(FoodPiece piece) {
        super(new Item.Properties().food(FoodValueRules.piece(piece.wholeItem.getFoodProperties(), defaultShare())));
        this.piece = piece;
        this.food = new ConfiguredFoodValues<>(() -> FoodServerConfig.get().pieces.foodShare.get(),
                share -> FoodValueRules.piece(piece.wholeItem.getFoodProperties(), share));
    }

    public FoodPiece piece() {
        return piece;
    }

    @Override
    public FoodProperties getFoodProperties() {
        return food.get();
    }

    private static double defaultShare() {
        return FoodServerConfig.get().pieces.foodShare.defaultValue();
    }
}
