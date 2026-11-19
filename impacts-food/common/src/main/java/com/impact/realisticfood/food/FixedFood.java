package com.impact.realisticfood.food;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Food's own foods with values of their own (each one a config value under {@code foods.<name>}, with these as
 * defaults), as opposed to pieces and styled foods, whose values follow another food.
 */
public enum FixedFood {
    COOKED_TROPICAL_FISH("cooked_tropical_fish", "Cooked Tropical Fish", 4, 0.6, true),
    COOKED_ROTTEN_FLESH("cooked_rotten_flesh", "Cooked Rotten Flesh", 3, 0.3, true,
            new Effect(() -> MobEffects.HUNGER, 0.1, 600)),
    FRIED_EGG("fried_egg", "Fried Egg", 3, 0.6, false),
    BOILED_EGG("boiled_egg", "Boiled Egg", 4, 0.6, false),
    BURNT_FOOD("burnt_food", "Burnt Food", 1, 0.1, false,
            new Effect(() -> MobEffects.CONFUSION, 0.5, 100)),
    MUSH("mush", "Mush", 2, 0.2, false, 16);

    private static final int VANILLA_EAT_TICKS = 32;

    public final String itemPath;
    public final String displayName;
    public final int defaultHunger;
    public final double defaultSaturation;
    public final boolean isMeat;
    public final int defaultEatTicks;
    public final Optional<Effect> effect;

    FixedFood(String itemPath, String displayName, int defaultHunger, double defaultSaturation, boolean isMeat) {
        this(itemPath, displayName, defaultHunger, defaultSaturation, isMeat, VANILLA_EAT_TICKS, Optional.empty());
    }

    FixedFood(String itemPath, String displayName, int defaultHunger, double defaultSaturation, boolean isMeat, int defaultEatTicks) {
        this(itemPath, displayName, defaultHunger, defaultSaturation, isMeat, defaultEatTicks, Optional.empty());
    }

    FixedFood(String itemPath, String displayName, int defaultHunger, double defaultSaturation, boolean isMeat, Effect effect) {
        this(itemPath, displayName, defaultHunger, defaultSaturation, isMeat, VANILLA_EAT_TICKS, Optional.of(effect));
    }

    FixedFood(String itemPath, String displayName, int defaultHunger, double defaultSaturation, boolean isMeat, int defaultEatTicks,
              Optional<Effect> effect) {
        this.itemPath = itemPath;
        this.displayName = displayName;
        this.defaultHunger = defaultHunger;
        this.defaultSaturation = defaultSaturation;
        this.isMeat = isMeat;
        this.defaultEatTicks = defaultEatTicks;
        this.effect = effect;
    }

    /** The config section name: the item path in camel case, such as {@code boiledEgg}. */
    public String configKey() {
        StringBuilder key = new StringBuilder();
        boolean isWordStart = false;
        for (char character : itemPath.toCharArray()) {
            if (character == '_') {
                isWordStart = true;
                continue;
            }
            key.append(isWordStart ? Character.toUpperCase(character) : character);
            isWordStart = false;
        }
        return key.toString();
    }

    /** An effect eating the food can give: Burnt Food's Nausea, Cooked Rotten Flesh's Hunger. */
    public record Effect(Supplier<MobEffect> type, double defaultChance, int defaultTicks) {
    }
}
