package com.impact.realisticfood.registry;

import com.impact.lib.ResourceIds;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.food.FoodStyle;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/** Food's item tags (spec, "Tags"). */
public final class FoodTags {
    public static final TagKey<Item> KNIVES = itemTag("knives");
    /** Raw meat cuts and pieces, so a soup can ask for "any cut". */
    public static final TagKey<Item> MEAT_CUTS = itemTag("meat_cuts");
    /** Raw fish fillets, so a soup can ask for "any fillet". */
    public static final TagKey<Item> FISH_FILLETS = itemTag("fish_fillets");

    public static final TagKey<Item> DRIED_FOODS = itemTag("dried_foods");
    public static final TagKey<Item> TOASTED_FOODS = itemTag("toasted_foods");
    public static final TagKey<Item> BOILED_FOODS = itemTag("boiled_foods");
    /** Every dried, toasted and boiled food. */
    public static final TagKey<Item> STYLED_FOODS = itemTag("styled_foods");
    /** Styled foods plus Food's other finished foods (eggs, doughs' results, Burnt Food, Mush), for the overcooking rules and achievements. */
    public static final TagKey<Item> FINISHED_FOODS = itemTag("finished_foods");

    /** Every bowl soup and stew Food adds (not vanilla's, which Vitality gives its own values). Vitality reads it. */
    public static final TagKey<Item> SOUPS = itemTag("soups");
    /** Bowls a ready soup can be ladled into: vanilla's bowl, and any a pack adds. */
    public static final TagKey<Item> BOWLS = itemTag("bowls");

    private FoodTags() {
    }

    public static TagKey<Item> styledFoods(FoodStyle style) {
        return switch (style) {
            case DRIED -> DRIED_FOODS;
            case TOASTED -> TOASTED_FOODS;
            case BOILED -> BOILED_FOODS;
        };
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceIds.of(FoodMod.MOD_ID, path));
    }
}
