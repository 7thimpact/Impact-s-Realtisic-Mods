package com.impact.realisticfood.item;

import com.impact.realisticfood.food.ConfiguredFoodValues;
import com.impact.realisticfood.food.FoodBase;
import com.impact.realisticfood.food.FoodStyle;
import com.impact.realisticfood.food.FoodValueRules;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A dried, toasted or boiled food. Its values follow its base food and the style's settings: hunger plus the style's
 * bonus, the base's saturation times the style's multiplier, the style's eating time, and (boiled) safer effects.
 * Forge's stack-aware {@code getFoodProperties} calls the vanilla one overridden here.
 */
@SuppressWarnings("deprecation")
public final class StyledFoodItem extends Item {
    private final FoodBase base;
    private final FoodStyle style;
    private final ConfiguredFoodValues<StyledFoodInputs> food;

    public StyledFoodItem(FoodBase base, FoodStyle style) {
        super(new Item.Properties().food(FoodValueRules.styled(base.baseItem().getFoodProperties(), FoodValueRules.StyleRule.defaultsOf(style))));
        this.base = base;
        this.style = style;
        this.food = new ConfiguredFoodValues<>(() -> new StyledFoodInputs(FoodValueRules.StyleRule.configuredFor(style), base.baseItem().getFoodProperties()),
                inputs -> FoodValueRules.styled(inputs.baseFood(), inputs.rule()));
    }

    public FoodBase base() {
        return base;
    }

    public FoodStyle style() {
        return style;
    }

    @Override
    public FoodProperties getFoodProperties() {
        return food.get();
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return style.settings().eatTicks.get();
    }

    // A base piece's values follow the config too, and a piece hands out the same values until they change.
    private record StyledFoodInputs(FoodValueRules.StyleRule rule, FoodProperties baseFood) {
    }
}
