package com.impact.realisticfood.food;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;

/** Builds food values from a base food's: its flags and effects carry over, and the caller picks hunger and saturation. */
public final class FoodValueRules {
    private static final int MINIMUM_PIECE_HUNGER = 1;

    private FoodValueRules() {
    }

    /**
     * A piece of the whole food: its share of the hunger, rounded down so cutting never gains food, but at least 1.
     * The saturation modifier stays, so the saturation a piece gives shrinks with its hunger.
     */
    public static FoodProperties piece(FoodProperties whole, double share) {
        int hunger = Math.max(MINIMUM_PIECE_HUNGER, (int) Math.floor(whole.getNutrition() * share));
        return withValues(whole, hunger, whole.getSaturationModifier(), 1.0F);
    }

    /**
     * The base food with new hunger and saturation, and each effect's chance times {@code effectChanceFactor}. Forge
     * deprecates adding effects as instances (it wants suppliers, for effects registered after the food); these come
     * from a food that already exists, so their effects are registered.
     */
    @SuppressWarnings("deprecation")
    public static FoodProperties withValues(FoodProperties base, int hunger, float saturationModifier, float effectChanceFactor) {
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
            builder.effect(new MobEffectInstance(effect.getFirst()), effect.getSecond() * effectChanceFactor);
        }
        return builder.build();
    }
}
