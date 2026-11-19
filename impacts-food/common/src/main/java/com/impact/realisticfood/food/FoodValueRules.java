package com.impact.realisticfood.food;

import com.impact.realisticfood.config.FoodServerConfig;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;

/** Builds food values from a base food's: its flags and effects carry over, and the caller picks hunger and saturation. */
public final class FoodValueRules {
    private static final int MINIMUM_PIECE_HUNGER = 1;
    /** Vanilla's rule: eating gives hunger times saturation modifier times 2 saturation. */
    private static final float SATURATION_PER_HUNGER_AND_MODIFIER = 2.0F;
    private static final float UNCHANGED_CHANCE = 1.0F;

    private FoodValueRules() {
    }

    /**
     * A piece of the whole food: its share of the hunger, rounded down so cutting never gains food, but at least 1.
     * The saturation modifier stays, so the saturation a piece gives shrinks with its hunger.
     */
    public static FoodProperties piece(FoodProperties whole, double share) {
        int hunger = Math.max(MINIMUM_PIECE_HUNGER, (int) Math.floor(whole.getNutrition() * share));
        return withValues(whole, hunger, whole.getSaturationModifier(), UNCHANGED_CHANCE);
    }

    /**
     * A styled version of the base food: the style's hunger bonus, the base's total saturation times the style's
     * multiplier, and the base's effects, with harmful ones made less likely by the style's factor.
     */
    public static FoodProperties styled(FoodProperties base, StyleRule rule) {
        int hunger = Math.max(0, base.getNutrition() + rule.hungerBonus());
        float baseSaturation = base.getNutrition() * base.getSaturationModifier() * SATURATION_PER_HUNGER_AND_MODIFIER;
        float saturation = (float) (baseSaturation * rule.saturationMultiplier());
        float saturationModifier = hunger == 0 ? 0.0F : saturation / (hunger * SATURATION_PER_HUNGER_AND_MODIFIER);
        return withValues(base, hunger, saturationModifier, (float) rule.harmfulEffectChanceFactor());
    }

    /**
     * The base food with new hunger and saturation, and each harmful effect's chance times
     * {@code harmfulEffectChanceFactor}. Forge deprecates adding effects as instances (it wants suppliers, for effects
     * registered after the food); these come from a food that already exists, so their effects are registered.
     */
    @SuppressWarnings("deprecation")
    private static FoodProperties withValues(FoodProperties base, int hunger, float saturationModifier, float harmfulEffectChanceFactor) {
        FoodProperties.Builder builder = new FoodProperties.Builder().nutrition(hunger).saturationMod(saturationModifier);
        if (base.isMeat()) {
            builder.meat();
        }
        if (base.canAlwaysEat()) {
            builder.alwaysEat();
        }
        if (base.isFastFood()) {
            builder.fast();
        }
        for (Pair<MobEffectInstance, Float> effect : base.getEffects()) {
            boolean isHarmful = effect.getFirst().getEffect().getCategory() == MobEffectCategory.HARMFUL;
            float chance = isHarmful ? effect.getSecond() * harmfulEffectChanceFactor : effect.getSecond();
            builder.effect(new MobEffectInstance(effect.getFirst()), chance);
        }
        return builder.build();
    }

    /** One style's settings, as a value: food values are worked out again only when these change. */
    public record StyleRule(int hungerBonus, double saturationMultiplier, double harmfulEffectChanceFactor) {
        public static StyleRule defaultsOf(FoodStyle style) {
            FoodServerConfig.StyleValues settings = style.settings();
            double harmfulFactor = style == FoodStyle.BOILED ? FoodServerConfig.get().styles.boiledNegativeEffectChance.defaultValue() : 1.0;
            return new StyleRule(settings.hungerBonus.defaultValue(), settings.saturationMultiplier.defaultValue(), harmfulFactor);
        }

        public static StyleRule configuredFor(FoodStyle style) {
            FoodServerConfig.StyleValues settings = style.settings();
            return new StyleRule(settings.hungerBonus.get(), settings.saturationMultiplier.get(), style.harmfulEffectChanceFactor());
        }
    }
}
