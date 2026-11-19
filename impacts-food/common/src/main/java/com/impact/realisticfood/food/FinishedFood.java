package com.impact.realisticfood.food;

/**
 * Food's own finished foods with fixed food values (each one a config value, with these as defaults). Milestone 4 adds
 * the rest of the spec's finished foods.
 */
public enum FinishedFood {
    BOILED_EGG("boiled_egg", "Boiled Egg", 4, 0.6);

    public final String itemPath;
    public final String displayName;
    public final int defaultHunger;
    public final double defaultSaturation;

    FinishedFood(String itemPath, String displayName, int defaultHunger, double defaultSaturation) {
        this.itemPath = itemPath;
        this.displayName = displayName;
        this.defaultHunger = defaultHunger;
        this.defaultSaturation = defaultSaturation;
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
}
