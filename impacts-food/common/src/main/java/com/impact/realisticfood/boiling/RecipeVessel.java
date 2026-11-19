package com.impact.realisticfood.boiling;

import java.util.Arrays;
import java.util.Optional;

/** The {@code vessel} field of Food's boiling and soup recipes: which pots the recipe works in. */
public enum RecipeVessel {
    CAULDRON("cauldron"),
    BUCKET("bucket"),
    ANY("any");

    public final String jsonName;

    RecipeVessel(String jsonName) {
        this.jsonName = jsonName;
    }

    public boolean accepts(PotKind pot) {
        return switch (this) {
            case CAULDRON -> pot == PotKind.CAULDRON;
            case BUCKET -> pot == PotKind.BUCKET;
            case ANY -> true;
        };
    }

    public static Optional<RecipeVessel> byJsonName(String jsonName) {
        return Arrays.stream(values()).filter(vessel -> vessel.jsonName.equals(jsonName)).findFirst();
    }
}
