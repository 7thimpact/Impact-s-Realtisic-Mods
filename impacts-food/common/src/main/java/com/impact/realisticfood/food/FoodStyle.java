package com.impact.realisticfood.food;

import com.impact.realisticfood.config.FoodFeature;
import com.impact.realisticfood.config.FoodServerConfig;

/** The three ways to cook food that is already cooked: dried (furnace, smoker), toasted (campfire, spit) and boiled. */
public enum FoodStyle {
    DRIED("dried", FoodFeature.DRIED),
    TOASTED("toasted", FoodFeature.TOASTED),
    BOILED("boiled", FoodFeature.BOILED);

    /** The ID prefix ({@code impactsfood:dried_bread}) and the tag name ({@code impactsfood:dried_foods}). */
    public final String prefix;
    public final FoodFeature feature;

    FoodStyle(String prefix, FoodFeature feature) {
        this.prefix = prefix;
        this.feature = feature;
    }

    public FoodServerConfig.StyleValues settings() {
        FoodServerConfig.StyleSettings styles = FoodServerConfig.get().styles;
        return switch (this) {
            case DRIED -> styles.dried;
            case TOASTED -> styles.toasted;
            case BOILED -> styles.boiled;
        };
    }

    /** The factor on the chance of the base food's harmful effects: boiling makes food safer. */
    public double harmfulEffectChanceFactor() {
        return this == BOILED ? FoodServerConfig.get().styles.boiledNegativeEffectChance.get() : 1.0;
    }

    public String itemPath(FoodBase base) {
        return prefix + "_" + base.path;
    }
}
