package com.impact.realisticfood.item;

import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.food.ConfiguredFoodValues;
import com.impact.realisticfood.food.FinishedFood;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

/**
 * One of Food's finished foods, with hunger and saturation from {@code foods.<name>} in the server config. Forge's
 * stack-aware {@code getFoodProperties} calls the vanilla one overridden here.
 */
@SuppressWarnings("deprecation")
public final class FinishedFoodItem extends Item {
    private final ConfiguredFoodValues<FoodValues> food;

    public FinishedFoodItem(FinishedFood finishedFood) {
        super(new Item.Properties().food(FoodValues.defaultsOf(finishedFood).toFood()));
        this.food = new ConfiguredFoodValues<>(() -> FoodValues.configuredFor(finishedFood), FoodValues::toFood);
    }

    @Override
    public FoodProperties getFoodProperties() {
        return food.get();
    }

    private record FoodValues(int hunger, double saturation) {
        static FoodValues defaultsOf(FinishedFood finishedFood) {
            return new FoodValues(finishedFood.defaultHunger, finishedFood.defaultSaturation);
        }

        static FoodValues configuredFor(FinishedFood finishedFood) {
            FoodServerConfig.FoodValueSettings settings = FoodServerConfig.get().finishedFoods.get(finishedFood);
            return new FoodValues(settings.hunger.get(), settings.saturation.get());
        }

        FoodProperties toFood() {
            return new FoodProperties.Builder().nutrition(hunger).saturationMod((float) saturation).build();
        }
    }
}
