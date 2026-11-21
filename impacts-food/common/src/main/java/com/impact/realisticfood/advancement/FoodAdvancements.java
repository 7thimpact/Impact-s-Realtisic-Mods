package com.impact.realisticfood.advancement;

import com.impact.lib.ResourceIds;
import com.impact.lib.advancement.ImpactsAdvancementBuilder;
import com.impact.lib.advancement.ToolCraftedTrigger;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.food.BakingIngredient;
import com.impact.realisticfood.food.FixedFood;
import com.impact.realisticfood.food.FoodBase;
import com.impact.realisticfood.food.FoodPiece;
import com.impact.realisticfood.food.FoodStyle;
import com.impact.realisticfood.item.KnifeMaterial;
import com.impact.realisticfood.registry.FoodItems;
import com.impact.realisticfood.registry.FoodTags;
import com.impact.realisticfood.spit.Spits;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The "Realistic Food" advancement tab: 26 achievements in five branches (prep, boiling, cooking styles, spit and
 * baking). Goals mark big steps; challenges are long grinds and give 50 XP. Stone Soup needs Crafting. The counting
 * achievements use Food's own triggers, fired once Lib's counters reach the numbers in Food's server config.
 */
public final class FoodAdvancements {
    public static final ResourceLocation SHARP_THINKING = id("sharp_thinking");
    public static final ResourceLocation CUT_IT_OUT = id("cut_it_out");
    public static final ResourceLocation SOMETHING_FISHY = id("something_fishy");
    public static final ResourceLocation BONE_APPETIT = id("bone_appetit");
    public static final ResourceLocation RAW_DEAL = id("raw_deal");
    public static final ResourceLocation HOT_WATER = id("hot_water");
    public static final ResourceLocation HARD_BOILED = id("hard_boiled");
    public static final ResourceLocation WHY_WAS_THIS_NECESSARY = id("why_was_this_necessary");
    public static final ResourceLocation MUSH_ADO_ABOUT_NOTHING = id("mush_ado_about_nothing");
    public static final ResourceLocation CAMP_COOK = id("camp_cook");
    public static final ResourceLocation STONE_SOUP = id("stone_soup");
    public static final ResourceLocation SOUPER = id("souper");
    public static final ResourceLocation WATERED_DOWN = id("watered_down");
    public static final ResourceLocation MASTER_CHEF = id("master_chef");
    public static final ResourceLocation TOAST_OF_THE_TOWN = id("toast_of_the_town");
    public static final ResourceLocation JERKY_MOVE = id("jerky_move");
    public static final ResourceLocation WELL_DONE = id("well_done");
    public static final ResourceLocation THREE_WAYS = id("three_ways");
    public static final ResourceLocation DAY_IN_AND_DAY_OUT = id("day_in_and_day_out");
    public static final ResourceLocation TASTE_TESTER = id("taste_tester");
    public static final ResourceLocation SPIT_TAKE = id("spit_take");
    public static final ResourceLocation ROUND_AND_ROUND = id("round_and_round");
    public static final ResourceLocation USE_YOUR_LOAF = id("use_your_loaf");
    public static final ResourceLocation DOUGH_RE_MI = id("dough_re_mi");
    public static final ResourceLocation RISE_AND_GRIND = id("rise_and_grind");
    public static final ResourceLocation WELL_FED = id("well_fed");
    /** Only exists with Impact's Realistic Crafting installed. */
    public static final List<ResourceLocation> NEEDS_CRAFTING = List.of(STONE_SOUP);
    /** The soups "Master Chef" asks for: every soup with a recipe (Watery Soup has none). */
    public static final List<Item> MASTER_CHEF_SOUPS = List.of(Items.MUSHROOM_STEW, Items.BEETROOT_SOUP, Items.RABBIT_STEW, Items.SUSPICIOUS_STEW);
    private static final ResourceLocation TAB_BACKGROUND = ResourceIds.of("minecraft", "textures/block/stripped_spruce_log.png");
    private static final List<FoodBase> JERKY_BASES = List.of(FoodBase.COOKED_BEEF, FoodBase.COOKED_PORKCHOP, FoodBase.COOKED_MUTTON,
            FoodBase.COOKED_CHICKEN, FoodBase.COOKED_RABBIT, FoodBase.COOKED_ROTTEN_FLESH, FoodBase.COOKED_BEEF_CUT, FoodBase.COOKED_PORK_CUT,
            FoodBase.COOKED_MUTTON_CUT, FoodBase.COOKED_CHICKEN_PIECE, FoodBase.COOKED_RABBIT_PIECE);

    private FoodAdvancements() {
    }

    /** Every achievement in the tab. Call once items are registered (during data generation). */
    public static List<ImpactsAdvancementBuilder> all() {
        List<ImpactsAdvancementBuilder> advancements = new ArrayList<>(prep());
        advancements.addAll(boiling());
        advancements.addAll(styles());
        advancements.addAll(spitAndBaking());
        return advancements;
    }

    private static List<ImpactsAdvancementBuilder> prep() {
        return List.of(
                ImpactsAdvancementBuilder.of(SHARP_THINKING, FoodItems.knife(KnifeMaterial.FLINT)).root(TAB_BACKGROUND)
                        .criterion("has_knife", hasAny(FoodTags.KNIVES)),
                ImpactsAdvancementBuilder.of(CUT_IT_OUT, FoodItems.piece(FoodPiece.RAW_BEEF_CUT)).parent(SHARP_THINKING)
                        .criterion("cut_meat", ToolCraftedTrigger.TriggerInstance.resultMatching(tagPredicate(FoodTags.MEAT_CUTS))),
                ImpactsAdvancementBuilder.of(SOMETHING_FISHY, FoodItems.piece(FoodPiece.RAW_COD_FILLET)).parent(SHARP_THINKING)
                        .criterion("filleted_fish", ToolCraftedTrigger.TriggerInstance.resultMatching(tagPredicate(FoodTags.FISH_FILLETS))),
                ImpactsAdvancementBuilder.of(BONE_APPETIT, Items.BONE).parent(CUT_IT_OUT)
                        .criterion("bone", ToolCraftedTrigger.TriggerInstance.byproductMatching(itemPredicate(Items.BONE)))
                        .criterion("feather", ToolCraftedTrigger.TriggerInstance.byproductMatching(itemPredicate(Items.FEATHER)))
                        .criterion("bone_meal", ToolCraftedTrigger.TriggerInstance.byproductMatching(itemPredicate(Items.BONE_MEAL)))
                        .anyCriterion(),
                ImpactsAdvancementBuilder.of(RAW_DEAL, Items.BEEF).parent(SHARP_THINKING)
                        .criterion("got_sick", FoodTriggers.RAW_FOOD_SICKNESS.anyPlayer()));
    }

    private static List<ImpactsAdvancementBuilder> boiling() {
        List<ImpactsAdvancementBuilder> boiling = new ArrayList<>(List.of(
                ImpactsAdvancementBuilder.of(HOT_WATER, Items.CAULDRON).parent(SHARP_THINKING)
                        .criterion("cauldron_boiled", FoodTriggers.CAULDRON_BOILED.anyPlayer()),
                ImpactsAdvancementBuilder.of(HARD_BOILED, fixed(FixedFood.BOILED_EGG)).parent(HOT_WATER)
                        .criterion("has_boiled_egg", hasItems(fixed(FixedFood.BOILED_EGG))),
                ImpactsAdvancementBuilder.of(WHY_WAS_THIS_NECESSARY, styled(FoodBase.BREAD, FoodStyle.BOILED)).parent(HOT_WATER).hidden()
                        .criterion("has_boiled_bread", hasItems(styled(FoodBase.BREAD, FoodStyle.BOILED))),
                ImpactsAdvancementBuilder.of(MUSH_ADO_ABOUT_NOTHING, fixed(FixedFood.MUSH)).parent(HOT_WATER).hidden()
                        .criterion("has_mush", hasItems(fixed(FixedFood.MUSH))),
                ImpactsAdvancementBuilder.of(CAMP_COOK, Items.CAMPFIRE).parent(HOT_WATER)
                        .criterion("boiled_in_bucket", FoodTriggers.CAMPFIRE_BUCKET_BOILED.anyPlayer()),
                ImpactsAdvancementBuilder.of(STONE_SOUP, Items.COBBLESTONE).parent(CAMP_COOK)
                        .criterion("stone_boiled", FoodTriggers.STONE_BOILED.anyPlayer()),
                ImpactsAdvancementBuilder.of(SOUPER, Items.BOWL).parent(HOT_WATER)
                        .criterion("ladled", SoupLadledTrigger.TriggerInstance.anySoup()),
                ImpactsAdvancementBuilder.of(WATERED_DOWN, fixed(FixedFood.WATERY_SOUP)).parent(SOUPER).hidden()
                        .criterion("watery_soup", SoupLadledTrigger.TriggerInstance.soup(itemId(fixed(FixedFood.WATERY_SOUP))))));
        ImpactsAdvancementBuilder masterChef = ImpactsAdvancementBuilder.of(MASTER_CHEF, fixed(FixedFood.HEARTY_STEW)).parent(SOUPER).challenge();
        for (Item soup : masterChefSoups()) {
            masterChef.criterion(itemId(soup).getPath(), SoupLadledTrigger.TriggerInstance.soup(itemId(soup)));
        }
        boiling.add(masterChef);
        return boiling;
    }

    private static List<ImpactsAdvancementBuilder> styles() {
        ImpactsAdvancementBuilder jerkyMove = ImpactsAdvancementBuilder.of(JERKY_MOVE, styled(FoodBase.COOKED_BEEF, FoodStyle.DRIED)).parent(TOAST_OF_THE_TOWN);
        JERKY_BASES.forEach(base -> jerkyMove.criterion(base.path, hasItems(styled(base, FoodStyle.DRIED))));
        jerkyMove.anyCriterion();
        ImpactsAdvancementBuilder threeWays = ImpactsAdvancementBuilder.of(THREE_WAYS, styled(FoodBase.COOKED_BEEF, FoodStyle.BOILED)).parent(JERKY_MOVE).goal();
        Arrays.stream(FoodBase.values()).filter(base -> Arrays.stream(FoodStyle.values()).allMatch(base::has)).forEach(base -> threeWays.criterion(base.path,
                hasItems(styled(base, FoodStyle.DRIED), styled(base, FoodStyle.TOASTED), styled(base, FoodStyle.BOILED))));
        threeWays.anyCriterion();
        return List.of(
                ImpactsAdvancementBuilder.of(TOAST_OF_THE_TOWN, styled(FoodBase.BREAD, FoodStyle.TOASTED)).parent(SHARP_THINKING)
                        .criterion("has_toasted_food", hasAny(FoodTags.TOASTED_FOODS)),
                jerkyMove,
                ImpactsAdvancementBuilder.of(WELL_DONE, fixed(FixedFood.BURNT_FOOD)).parent(TOAST_OF_THE_TOWN).hidden()
                        .criterion("has_burnt_food", hasItems(fixed(FixedFood.BURNT_FOOD))),
                threeWays,
                ImpactsAdvancementBuilder.of(DAY_IN_AND_DAY_OUT, styled(FoodBase.COOKED_BEEF, FoodStyle.TOASTED)).parent(THREE_WAYS).challenge()
                        .criterion("ate_one_food_often", FoodTriggers.DAY_IN_AND_DAY_OUT.anyPlayer()),
                ImpactsAdvancementBuilder.of(TASTE_TESTER, styled(FoodBase.APPLE, FoodStyle.DRIED)).parent(THREE_WAYS).challenge()
                        .criterion("tasted_many_foods", FoodTriggers.TASTE_TESTER.anyPlayer()));
    }

    private static List<ImpactsAdvancementBuilder> spitAndBaking() {
        return List.of(
                ImpactsAdvancementBuilder.of(SPIT_TAKE, Spits.spitItem()).parent(SHARP_THINKING)
                        .criterion("mounted_spit", FoodTriggers.SPIT_MOUNTED.anyPlayer()),
                ImpactsAdvancementBuilder.of(ROUND_AND_ROUND, Items.CLOCK).parent(SPIT_TAKE)
                        .criterion("turned_often", FoodTriggers.SPIT_TURNED_OFTEN.anyPlayer()),
                ImpactsAdvancementBuilder.of(USE_YOUR_LOAF, Items.BREAD).parent(SHARP_THINKING)
                        .criterion("baked_bread", FoodTriggers.BREAD_BAKED.anyPlayer()),
                ImpactsAdvancementBuilder.of(DOUGH_RE_MI, FoodItems.bakingIngredient(BakingIngredient.DOUGH)).parent(USE_YOUR_LOAF).goal()
                        .criterion("dough_bun", hasItems(fixed(FixedFood.DOUGH_BUN)))
                        .criterion("flatbread", hasItems(fixed(FixedFood.FLATBREAD)))
                        .criterion("dumpling", hasItems(fixed(FixedFood.DUMPLING))),
                ImpactsAdvancementBuilder.of(RISE_AND_GRIND, FoodItems.bakingIngredient(BakingIngredient.FLOUR)).parent(USE_YOUR_LOAF)
                        .criterion("has_flour", hasItems(FoodItems.bakingIngredient(BakingIngredient.FLOUR))),
                ImpactsAdvancementBuilder.of(WELL_FED, Items.CAKE).parent(DAY_IN_AND_DAY_OUT).challenge()
                        .criterion("earned_everything", FoodTriggers.WELL_FED.anyPlayer()));
    }

    /** Every Realistic Food achievement but "Well Fed" itself, for its check. */
    public static List<ResourceLocation> wellFedRequirements() {
        return all().stream().map(ImpactsAdvancementBuilder::id).filter(advancementId -> !advancementId.equals(WELL_FED)).toList();
    }

    private static List<Item> masterChefSoups() {
        List<Item> soups = new ArrayList<>(MASTER_CHEF_SOUPS);
        soups.addAll(List.of(fixed(FixedFood.BONE_BROTH), fixed(FixedFood.HEARTY_STEW), fixed(FixedFood.FISH_SOUP), fixed(FixedFood.PORRIDGE)));
        return soups;
    }

    private static InventoryChangeTrigger.TriggerInstance hasItems(ItemLike... items) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(items);
    }

    private static InventoryChangeTrigger.TriggerInstance hasAny(TagKey<Item> tag) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(tagPredicate(tag));
    }

    private static ItemPredicate tagPredicate(TagKey<Item> tag) {
        return ItemPredicate.Builder.item().of(tag).build();
    }

    private static ItemPredicate itemPredicate(Item item) {
        return ItemPredicate.Builder.item().of(item).build();
    }

    private static Item fixed(FixedFood food) {
        return FoodItems.fixedFood(food);
    }

    private static Item styled(FoodBase base, FoodStyle style) {
        return FoodItems.styled(base, style).orElseThrow();
    }

    private static ResourceLocation itemId(Item item) {
        return ResourceIds.itemId(item);
    }

    private static ResourceLocation id(String path) {
        return ResourceIds.of(FoodMod.MOD_ID, path);
    }
}
