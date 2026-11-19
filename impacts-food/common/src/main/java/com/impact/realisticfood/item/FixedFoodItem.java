package com.impact.realisticfood.item;

import com.impact.realisticfood.config.FoodServerConfig;
import com.impact.realisticfood.food.ConfiguredFoodValues;
import com.impact.realisticfood.food.FixedFood;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * One of Food's own foods, with hunger, saturation, eating time and effect from {@code foods.<name>} in the server
 * config. Forge's stack-aware {@code getFoodProperties} calls the vanilla one overridden here.
 */
@SuppressWarnings("deprecation")
public final class FixedFoodItem extends Item {
    private final FixedFood fixedFood;
    private final ConfiguredFoodValues<FoodValues> food;

    public FixedFoodItem(FixedFood fixedFood) {
        super(new Item.Properties().food(FoodValues.defaultsOf(fixedFood).toFood(fixedFood)));
        this.fixedFood = fixedFood;
        this.food = new ConfiguredFoodValues<>(() -> FoodValues.configuredFor(fixedFood), values -> values.toFood(fixedFood));
    }

    public FixedFood fixedFood() {
        return fixedFood;
    }

    @Override
    public FoodProperties getFoodProperties() {
        return food.get();
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return FoodServerConfig.get().fixedFoods.get(fixedFood).eatTicks.get();
    }

    private record FoodValues(int hunger, double saturation, double effectChance, int effectTicks) {
        static FoodValues defaultsOf(FixedFood fixedFood) {
            double effectChance = fixedFood.effect.map(FixedFood.Effect::defaultChance).orElse(0.0);
            int effectTicks = fixedFood.effect.map(FixedFood.Effect::defaultTicks).orElse(0);
            return new FoodValues(fixedFood.defaultHunger, fixedFood.defaultSaturation, effectChance, effectTicks);
        }

        static FoodValues configuredFor(FixedFood fixedFood) {
            FoodServerConfig.FixedFoodSettings settings = FoodServerConfig.get().fixedFoods.get(fixedFood);
            double effectChance = settings.effectChance.map(value -> value.get()).orElse(0.0);
            int effectTicks = settings.effectTicks.map(value -> value.get()).orElse(0);
            return new FoodValues(settings.hunger.get(), settings.saturation.get(), effectChance, effectTicks);
        }

        // Forge deprecates effects added as instances (it prefers suppliers, for effects registered later); these are vanilla's.
        FoodProperties toFood(FixedFood fixedFood) {
            FoodProperties.Builder builder = new FoodProperties.Builder().nutrition(hunger).saturationMod((float) saturation);
            if (fixedFood.isMeat) {
                builder.meat();
            }
            fixedFood.effect.filter(effect -> effectChance > 0.0)
                    .ifPresent(effect -> builder.effect(new MobEffectInstance(effect.type().get(), effectTicks), (float) effectChance));
            return builder.build();
        }
    }
}
