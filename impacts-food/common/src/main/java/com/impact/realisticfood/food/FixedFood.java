package com.impact.realisticfood.food;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Food's own foods with values of their own (each one a config value under {@code foods.<name>}, with these as
 * defaults), as opposed to pieces and styled foods, whose values follow another food. Soups are bowl foods: they stack
 * to 1 and give the bowl back.
 */
public enum FixedFood {
    COOKED_TROPICAL_FISH("cooked_tropical_fish", "Cooked Tropical Fish", Defaults.of(4, 0.6).meat()),
    COOKED_ROTTEN_FLESH("cooked_rotten_flesh", "Cooked Rotten Flesh", Defaults.of(3, 0.3).meat().effect(() -> MobEffects.HUNGER, 0.1, 600)),
    FRIED_EGG("fried_egg", "Fried Egg", Defaults.of(3, 0.6)),
    BOILED_EGG("boiled_egg", "Boiled Egg", Defaults.of(4, 0.6)),
    BURNT_FOOD("burnt_food", "Burnt Food", Defaults.of(1, 0.1).effect(() -> MobEffects.CONFUSION, 0.5, 100)),
    MUSH("mush", "Mush", Defaults.of(2, 0.2).eatTicks(16)),
    BONE_BROTH("bone_broth", "Bone Broth", Defaults.of(3, 0.6).bowl().effect(() -> MobEffects.REGENERATION, 1.0, 100)),
    HEARTY_STEW("hearty_stew", "Hearty Stew", Defaults.of(8, 0.6).bowl()),
    FISH_SOUP("fish_soup", "Fish Soup", Defaults.of(6, 0.6).bowl()),
    PORRIDGE("porridge", "Porridge", Defaults.of(5, 0.6).bowl()),
    WATERY_SOUP("watery_soup", "Watery Soup", Defaults.of(2, 0.1).bowl());

    public final String itemPath;
    public final String displayName;
    public final Defaults defaults;

    FixedFood(String itemPath, String displayName, Defaults defaults) {
        this.itemPath = itemPath;
        this.displayName = displayName;
        this.defaults = defaults;
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

    /** A food's default values. Each one is a config value; these are the spec's numbers. */
    public record Defaults(int hunger, double saturation, boolean isMeat, int eatTicks, boolean isBowlFood, Optional<Effect> effect) {
        private static final int VANILLA_EAT_TICKS = 32;

        static Defaults of(int hunger, double saturation) {
            return new Defaults(hunger, saturation, false, VANILLA_EAT_TICKS, false, Optional.empty());
        }

        Defaults meat() {
            return new Defaults(hunger, saturation, true, eatTicks, isBowlFood, effect);
        }

        Defaults eatTicks(int ticks) {
            return new Defaults(hunger, saturation, isMeat, ticks, isBowlFood, effect);
        }

        Defaults bowl() {
            return new Defaults(hunger, saturation, isMeat, eatTicks, true, effect);
        }

        Defaults effect(Supplier<MobEffect> type, double chance, int ticks) {
            return new Defaults(hunger, saturation, isMeat, eatTicks, isBowlFood, Optional.of(new Effect(type, chance, ticks)));
        }
    }

    /** An effect eating the food can give: Burnt Food's Nausea, Bone Broth's Regeneration. */
    public record Effect(Supplier<MobEffect> type, double defaultChance, int defaultTicks) {
    }
}
