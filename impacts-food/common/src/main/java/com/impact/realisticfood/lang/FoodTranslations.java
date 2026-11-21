package com.impact.realisticfood.lang;

import com.impact.lib.advancement.ImpactsAdvancementBuilder;
import com.impact.lib.registry.CreativeTabs;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.advancement.FoodAdvancements;
import com.impact.realisticfood.boiling.RecipeVessel;
import com.impact.realisticfood.food.BakingIngredient;
import com.impact.realisticfood.food.FixedFood;
import com.impact.realisticfood.food.FoodBase;
import com.impact.realisticfood.food.FoodPiece;
import com.impact.realisticfood.food.FoodStyle;
import com.impact.realisticfood.item.KnifeMaterial;
import com.impact.realisticfood.registry.FoodSounds;

import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Food's translation keys and their English text. Data generation writes {@link #ENGLISH} to en_us.json. */
public final class FoodTranslations {
    public static final String CREATIVE_TAB = CreativeTabs.titleKey(FoodMod.MOD_ID);
    public static final String CAMPFIRE_BUCKET = "block." + FoodMod.MOD_ID + ".campfire_bucket";
    public static final String JEI_BOILING = "jei.impactsfood.boiling";
    public static final String JEI_SOUP = "jei.impactsfood.soup";
    public static final String JEI_SPIT_ROASTING = "jei.impactsfood.spit_roasting";
    public static final String JEI_SECONDS = "jei.impactsfood.seconds";
    public static final String JEI_POT_AND_TIME = "jei.impactsfood.pot_and_time";
    public static final String JEI_SOUP_DETAILS = "jei.impactsfood.soup_details";
    public static final String JEI_SPIT_TIMES = "jei.impactsfood.spit_times";
    public static final String JEI_STYLES_INFO = "jei.impactsfood.info.styles";
    public static final String JEI_SPIT_INFO = "jei.impactsfood.info.spit";
    public static final String JEI_CAMPFIRE_BUCKET_INFO = "jei.impactsfood.info.campfire_bucket";
    public static final String KNIFE_TOOLTIP = "tooltip.impactsfood.knife";
    public static final String SPIT_TOOLTIP = "tooltip.impactsfood.spit";
    public static final String PIECE_TOOLTIP = "tooltip.impactsfood.piece";
    public static final String RAW_RISK_TOOLTIP = "tooltip.impactsfood.raw_risk";
    private static final String SUBTITLE_PREFIX = "subtitles.impactsfood.";
    private static final String JEI_VESSEL_PREFIX = "jei.impactsfood.vessel.";
    private static final String JEI_STYLE_INFO_PREFIX = "jei.impactsfood.info.style.";
    /** Minecraft names a tab after its root; this key is for tools that list tabs by mod. */
    public static final String ADVANCEMENT_TAB = "advancements." + FoodMod.MOD_ID + ".tab";
    private static final String ITEM_KEY_PREFIX = "item." + FoodMod.MOD_ID + ".";

    private static final Map<KnifeMaterial, String> KNIFE_NAMES = new EnumMap<>(Map.of(
            KnifeMaterial.FLINT, "Flint Knife",
            KnifeMaterial.IRON, "Iron Knife"));
    private static final Map<FoodPiece, String> PIECE_NAMES = new EnumMap<>(Map.ofEntries(
            Map.entry(FoodPiece.RAW_BEEF_CUT, "Raw Beef Cut"),
            Map.entry(FoodPiece.RAW_PORK_CUT, "Raw Pork Cut"),
            Map.entry(FoodPiece.RAW_MUTTON_CUT, "Raw Mutton Cut"),
            Map.entry(FoodPiece.RAW_CHICKEN_PIECE, "Raw Chicken Piece"),
            Map.entry(FoodPiece.RAW_RABBIT_PIECE, "Raw Rabbit Piece"),
            Map.entry(FoodPiece.RAW_COD_FILLET, "Raw Cod Fillet"),
            Map.entry(FoodPiece.RAW_SALMON_FILLET, "Raw Salmon Fillet"),
            Map.entry(FoodPiece.COOKED_BEEF_CUT, "Cooked Beef Cut"),
            Map.entry(FoodPiece.COOKED_PORK_CUT, "Cooked Pork Cut"),
            Map.entry(FoodPiece.COOKED_MUTTON_CUT, "Cooked Mutton Cut"),
            Map.entry(FoodPiece.COOKED_CHICKEN_PIECE, "Cooked Chicken Piece"),
            Map.entry(FoodPiece.COOKED_RABBIT_PIECE, "Cooked Rabbit Piece"),
            Map.entry(FoodPiece.COOKED_COD_FILLET, "Cooked Cod Fillet"),
            Map.entry(FoodPiece.COOKED_SALMON_FILLET, "Cooked Salmon Fillet"),
            Map.entry(FoodPiece.POTATO_SLICES, "Potato Slices"),
            Map.entry(FoodPiece.FRIED_POTATO_SLICES, "Fried Potato Slices"),
            Map.entry(FoodPiece.CARROT_SLICES, "Carrot Slices")));

    public static final Map<String, String> ENGLISH = Collections.unmodifiableMap(english());

    private FoodTranslations() {
    }

    /** The subtitle key of one of Food's sounds, by its sound event path. */
    public static String subtitle(String soundPath) {
        return SUBTITLE_PREFIX + soundPath;
    }

    public static String jeiVessel(RecipeVessel vessel) {
        return JEI_VESSEL_PREFIX + vessel.jsonName;
    }

    public static String jeiStyleInfo(FoodStyle style) {
        return JEI_STYLE_INFO_PREFIX + style.prefix;
    }

    public static String itemKey(String itemPath) {
        return ITEM_KEY_PREFIX + itemPath;
    }

    private static void addTooltipsAndSubtitles(Map<String, String> translations) {
        translations.put(KNIFE_TOOLTIP, "Cuts raw food in any crafting grid");
        translations.put(SPIT_TOOLTIP, "Use on a lit campfire");
        translations.put(PIECE_TOOLTIP, "Cooks in half the time");
        translations.put(RAW_RISK_TOOLTIP, "May cause Hunger if eaten raw");
        translations.put(subtitle(FoodSounds.POT_SIMMER), "Pot simmers");
        translations.put(subtitle(FoodSounds.POT_SPLASH), "Food splashes into a pot");
        translations.put(subtitle(FoodSounds.SOUP_LADLE), "Soup is ladled");
        translations.put(subtitle(FoodSounds.SPIT_SIZZLE), "Spit sizzles");
    }

    private static void addJei(Map<String, String> translations) {
        translations.put(JEI_BOILING, "Boiling");
        translations.put(JEI_SOUP, "Soups");
        translations.put(JEI_SPIT_ROASTING, "Spit Roasting");
        translations.put(JEI_SECONDS, "%s s");
        translations.put(JEI_POT_AND_TIME, "%s, %s");
        translations.put(JEI_SOUP_DETAILS, "%s bowls a set, %s");
        translations.put(JEI_SPIT_TIMES, "%s on a spit, %s turned");
        translations.put(jeiVessel(RecipeVessel.CAULDRON), "Cauldron");
        translations.put(jeiVessel(RecipeVessel.BUCKET), "Bucket");
        translations.put(jeiVessel(RecipeVessel.ANY), "Cauldron or bucket");
        translations.put(JEI_STYLES_INFO, "Food cooks in order: raw, then cooked or boiled, then toasted. Once food is cooked, a campfire "
                + "toasts it, a furnace dries it, and boiling softens it. Toasted and dried food burns with more dry heat; boiled food "
                + "can only be boiled again, into Mush.");
        translations.put(jeiStyleInfo(FoodStyle.DRIED), "Dried food (furnace or smoker) restores the same hunger with less saturation, and is "
                + "eaten twice as fast.");
        translations.put(jeiStyleInfo(FoodStyle.TOASTED), "Toasted food (campfire or spit) restores 1 more hunger and more saturation.");
        translations.put(jeiStyleInfo(FoodStyle.BOILED), "Boiled food (cauldron or campfire bucket) restores the same hunger with a little "
                + "less saturation, and its harmful effects are less likely.");
        translations.put(JEI_SPIT_INFO, "Use a spit on a lit campfire to mount it. It holds 6 more items and cooks them faster. Use it with an "
                + "empty hand to turn it for a burst of speed, and sneak with an empty hand to take it down.");
        translations.put(JEI_CAMPFIRE_BUCKET_INFO, "Sneak and use a water bucket on the top of a lit campfire to set it down as a small pot. "
                + "Its water lasts 8 boils. Sneak with an empty hand to take it back.");
    }

    private static void addAdvancements(Map<String, String> translations) {
        addAdvancement(translations, FoodAdvancements.SHARP_THINKING, "Sharp Thinking", "Craft a knife");
        addAdvancement(translations, FoodAdvancements.CUT_IT_OUT, "Cut It Out", "Cut raw meat into cuts or pieces");
        addAdvancement(translations, FoodAdvancements.SOMETHING_FISHY, "Something Fishy", "Fillet a fish");
        addAdvancement(translations, FoodAdvancements.BONE_APPETIT, "Bone Appetit", "Get a bone, feather or bone meal from cutting");
        addAdvancement(translations, FoodAdvancements.RAW_DEAL, "Raw Deal", "Get Hunger from eating raw food");
        addAdvancement(translations, FoodAdvancements.HOT_WATER, "Hot Water", "Bring a cauldron to a boil");
        addAdvancement(translations, FoodAdvancements.HARD_BOILED, "Hard Boiled", "Boil an egg");
        addAdvancement(translations, FoodAdvancements.WHY_WAS_THIS_NECESSARY, "Why was this necessary?", "Get Boiled Bread");
        addAdvancement(translations, FoodAdvancements.MUSH_ADO_ABOUT_NOTHING, "Mush Ado About Nothing", "Make Mush");
        addAdvancement(translations, FoodAdvancements.CAMP_COOK, "Camp Cook", "Boil something in a bucket on a campfire");
        addAdvancement(translations, FoodAdvancements.STONE_SOUP, "Stone Soup", "Boil water with a Hot Pebble");
        addAdvancement(translations, FoodAdvancements.SOUPER, "Souper", "Ladle a bowl of soup");
        addAdvancement(translations, FoodAdvancements.WATERED_DOWN, "Watered Down", "Make Watery Soup");
        addAdvancement(translations, FoodAdvancements.MASTER_CHEF, "Master Chef", "Make every soup at least once");
        addAdvancement(translations, FoodAdvancements.TOAST_OF_THE_TOWN, "Toast of the Town", "Toast a cooked food on a campfire");
        addAdvancement(translations, FoodAdvancements.JERKY_MOVE, "Jerky Move", "Dry cooked meat into jerky in a furnace");
        addAdvancement(translations, FoodAdvancements.WELL_DONE, "Well Done", "Make Burnt Food");
        addAdvancement(translations, FoodAdvancements.THREE_WAYS, "Three Ways", "Own the toasted, dried and boiled versions of the same food");
        addAdvancement(translations, FoodAdvancements.DAY_IN_AND_DAY_OUT, "Day in and Day out", "Eat the same boiled or toasted food 100 times");
        addAdvancement(translations, FoodAdvancements.TASTE_TESTER, "Taste Tester", "Eat 25 different toasted, dried or boiled foods");
        addAdvancement(translations, FoodAdvancements.SPIT_TAKE, "Spit Take", "Mount a spit on a campfire");
        addAdvancement(translations, FoodAdvancements.ROUND_AND_ROUND, "Round and Round", "Turn a spit 20 times");
        addAdvancement(translations, FoodAdvancements.USE_YOUR_LOAF, "Use Your Loaf", "Bake bread from Loaf Dough");
        addAdvancement(translations, FoodAdvancements.DOUGH_RE_MI, "Dough Re Mi", "Make a Dough Bun, a Flatbread and a Dumpling");
        addAdvancement(translations, FoodAdvancements.RISE_AND_GRIND, "Rise and Grind", "Grind wheat into Flour");
        addAdvancement(translations, FoodAdvancements.WELL_FED, "Well Fed", "Earn every other Realistic Food achievement");
    }

    private static void addAdvancement(Map<String, String> translations, ResourceLocation advancementId, String title, String description) {
        translations.put(ImpactsAdvancementBuilder.titleKey(advancementId), title);
        translations.put(ImpactsAdvancementBuilder.descriptionKey(advancementId), description);
    }

    private static Map<String, String> english() {
        Map<String, String> translations = new LinkedHashMap<>();
        translations.put(CREATIVE_TAB, "Impact's Realistic Food");
        translations.put(CAMPFIRE_BUCKET, "Campfire Bucket");
        translations.put(itemKey("spit"), "Spit");
        translations.put(ADVANCEMENT_TAB, "Realistic Food");
        addJei(translations);
        addTooltipsAndSubtitles(translations);
        addAdvancements(translations);
        KNIFE_NAMES.forEach((material, name) -> translations.put(itemKey(material.itemPath), name));
        PIECE_NAMES.forEach((piece, name) -> translations.put(itemKey(piece.path), name));
        Arrays.stream(BakingIngredient.values()).forEach(ingredient -> translations.put(itemKey(ingredient.itemPath), ingredient.displayName));
        Arrays.stream(FixedFood.values()).forEach(food -> translations.put(itemKey(food.itemPath), food.displayName));
        for (FoodBase base : FoodBase.values()) {
            for (FoodStyle style : FoodStyle.values()) {
                base.name(style).ifPresent(name -> translations.put(itemKey(style.itemPath(base)), name));
            }
        }
        return translations;
    }
}
