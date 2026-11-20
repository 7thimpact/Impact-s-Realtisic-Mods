package com.impact.realisticvitality.advancement;

import com.impact.lib.ResourceIds;
import com.impact.lib.advancement.ImpactsAdvancementBuilder;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.air.AirTank;
import com.impact.realisticvitality.air.BreathingMask;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.thirst.BottledWater;
import com.impact.realisticvitality.thirst.WaterContainer;
import net.minecraft.advancements.critereon.ChangeDimensionTrigger;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;

/**
 * The "Realistic Vitality" advancement tab: 22 achievements in three branches, one per need. Goals mark big steps;
 * challenges give 50 XP. Heights in the criteria (Y 160 on a mountain, Y −58 near bedrock, Y 300 for "Peak Condition")
 * are datapack-editable like any advancement.
 */
public final class VitalityAdvancements {
    public static final ResourceLocation STAY_HYDRATED = id("stay_hydrated");
    public static final ResourceLocation SOMETHING_IN_THE_WATER = id("something_in_the_water");
    public static final ResourceLocation WORTH_ITS_SALT = id("worth_its_salt");
    public static final ResourceLocation CLEAN_LIVING = id("clean_living");
    public static final ResourceLocation SKIN_IN_THE_GAME = id("skin_in_the_game");
    public static final ResourceLocation HALF_MEASURES = id("half_measures");
    public static final ResourceLocation WELL_WATERED = id("well_watered");
    public static final ResourceLocation COLD_FEET = id("cold_feet");
    public static final ResourceLocation ALBERT_AND_EINSTEIN = id("albert_and_einstein");
    public static final ResourceLocation BUNDLE_UP = id("bundle_up");
    public static final ResourceLocation TAKING_THE_TEMPERATURE = id("taking_the_temperature");
    public static final ResourceLocation DRY_SPELL = id("dry_spell");
    public static final ResourceLocation SO_HOT = id("so_hot");
    public static final ResourceLocation HAT_TRICK = id("hat_trick");
    public static final ResourceLocation BREATHTAKING = id("breathtaking");
    public static final ResourceLocation IS_IT_WORTH_IT = id("is_it_worth_it");
    public static final ResourceLocation UP_IN_SMOKE = id("up_in_smoke");
    public static final ResourceLocation MASKED_UP = id("masked_up");
    public static final ResourceLocation IT_FEELS_LIKE_NOTHING = id("it_feels_like_nothing");
    public static final ResourceLocation INTO_THIN_AIR = id("into_thin_air");
    public static final ResourceLocation PEAK_CONDITION = id("peak_condition");
    public static final ResourceLocation IN_BALANCE = id("in_balance");
    private static final ResourceLocation TAB_BACKGROUND = ResourceIds.of("minecraft", "textures/block/packed_ice.png");
    private static final double MOUNTAIN_Y = 160.0;
    private static final double NEAR_BEDROCK_Y = -58.0;
    private static final double PEAK_Y = 300.0;

    private VitalityAdvancements() {
    }

    /** Every achievement in the tab. Call once items are registered (during data generation). */
    public static List<ImpactsAdvancementBuilder> all() {
        return List.of(
                ImpactsAdvancementBuilder.of(STAY_HYDRATED, container(WaterContainer.WATERSKIN)).root(TAB_BACKGROUND)
                        .criterion("drank_water", drank(Map.of("water", VitalityTriggers.TRUE))),
                ImpactsAdvancementBuilder.of(SOMETHING_IN_THE_WATER, VitalityItems.waterBottle(BottledWater.UNCLEAN)).parent(STAY_HYDRATED).hidden()
                        .criterion("got_parched", drank(Map.of("parched", VitalityTriggers.TRUE))),
                ImpactsAdvancementBuilder.of(WORTH_ITS_SALT, VitalityItems.waterBottle(BottledWater.SALT)).parent(STAY_HYDRATED).hidden()
                        .criterion("drank_salt_water", drank(Map.of("purity", "salt"))),
                ImpactsAdvancementBuilder.of(CLEAN_LIVING, VitalityItems.waterBottle(BottledWater.PURIFIED)).parent(STAY_HYDRATED)
                        .criterion("drank_purified_water", drank(Map.of("purity", "purified"))),
                ImpactsAdvancementBuilder.of(SKIN_IN_THE_GAME, container(WaterContainer.WATERSKIN)).parent(CLEAN_LIVING)
                        .criterion("has_waterskin", InventoryChangeTrigger.TriggerInstance.hasItems(container(WaterContainer.WATERSKIN))),
                ImpactsAdvancementBuilder.of(HALF_MEASURES, Items.GLISTERING_MELON_SLICE).parent(STAY_HYDRATED)
                        .criterion("health_capped", VitalityTriggers.HEALTH_CAPPED.anyPlayer()),
                ImpactsAdvancementBuilder.of(WELL_WATERED, container(WaterContainer.HYDRATION_PACK)).parent(SKIN_IN_THE_GAME).challenge()
                        .criterion("drank_often", VitalityTriggers.WELL_WATERED.anyPlayer()),
                ImpactsAdvancementBuilder.of(COLD_FEET, Items.SNOWBALL).parent(STAY_HYDRATED)
                        .criterion("became_cold", temperature(Map.of("cold_or_worse", VitalityTriggers.TRUE))),
                ImpactsAdvancementBuilder.of(ALBERT_AND_EINSTEIN, Items.FLINT_AND_STEEL).parent(COLD_FEET).hidden()
                        .criterion("on_fire_while_cold", temperature(Map.of("cold_or_worse", VitalityTriggers.TRUE, "on_fire", VitalityTriggers.TRUE))),
                ImpactsAdvancementBuilder.of(BUNDLE_UP, Items.WHITE_WOOL).parent(COLD_FEET)
                        .criterion("wore_warm_linings", gearUsed("warm_linings", ContextAwarePredicate.ANY)),
                ImpactsAdvancementBuilder.of(TAKING_THE_TEMPERATURE, VitalityItems.thermometer()).parent(COLD_FEET)
                        .criterion("has_thermometer", InventoryChangeTrigger.TriggerInstance.hasItems(VitalityItems.thermometer())),
                ImpactsAdvancementBuilder.of(DRY_SPELL, Items.BREAD).parent(STAY_HYDRATED).hidden()
                        .criterion("dry_food_refused", VitalityTriggers.DRY_FOOD_REFUSED.anyPlayer()),
                ImpactsAdvancementBuilder.of(SO_HOT, Items.MAGMA_BLOCK).parent(DRY_SPELL)
                        .criterion("overheated", temperature(Map.of("state", "overheating"))),
                ImpactsAdvancementBuilder.of(HAT_TRICK, VitalityItems.strawHat()).parent(DRY_SPELL)
                        .criterion("straw_hat_at_noon", gearUsed("straw_hat_in_desert_noon", ContextAwarePredicate.ANY)),
                ImpactsAdvancementBuilder.of(BREATHTAKING, VitalityItems.airBladder()).parent(STAY_HYDRATED)
                        .criterion("lost_air_on_a_mountain", airLost("thin", located(LocationPredicate.Builder.location()
                                .setDimension(Level.OVERWORLD).setY(MinMaxBounds.Doubles.atLeast(MOUNTAIN_Y))))),
                ImpactsAdvancementBuilder.of(IS_IT_WORTH_IT, Items.BEDROCK).parent(BREATHTAKING)
                        .criterion("deep_without_air_support", VitalityTriggers.AIR_LOST.needing(located(LocationPredicate.Builder.location()
                                .setY(MinMaxBounds.Doubles.atMost(NEAR_BEDROCK_Y))), Map.of("without_gear", VitalityTriggers.TRUE))),
                ImpactsAdvancementBuilder.of(UP_IN_SMOKE, Items.LAVA_BUCKET).parent(BREATHTAKING)
                        .criterion("lost_air_to_fumes", airLost("fumes", ContextAwarePredicate.ANY)),
                ImpactsAdvancementBuilder.of(MASKED_UP, mask(BreathingMask.RESPIRATOR)).parent(UP_IN_SMOKE).goal()
                        .criterion("filtered_in_the_nether", gearUsed("filtered_mask", inDimension(Level.NETHER))),
                ImpactsAdvancementBuilder.of(IT_FEELS_LIKE_NOTHING, Items.END_STONE).parent(BREATHTAKING)
                        .criterion("entered_the_end", ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(Level.END)),
                ImpactsAdvancementBuilder.of(INTO_THIN_AIR, VitalityItems.tank(AirTank.AIR_TANK)).parent(IT_FEELS_LIKE_NOTHING).goal()
                        .criterion("tank_in_the_end", gearUsed("air_tank", inDimension(Level.END))),
                ImpactsAdvancementBuilder.of(PEAK_CONDITION, Items.ELYTRA).parent(INTO_THIN_AIR).challenge()
                        .criterion("at_the_peak", VitalityTriggers.TEMPERATURE_STATE.needing(
                                located(LocationPredicate.Builder.location().setY(MinMaxBounds.Doubles.atLeast(PEAK_Y))),
                                Map.of("state", "comfortable", "thirst_full", VitalityTriggers.TRUE, "air_full", VitalityTriggers.TRUE))),
                ImpactsAdvancementBuilder.of(IN_BALANCE, Items.CLOCK).parent(BUNDLE_UP).challenge()
                        .criterion("a_day_in_balance", VitalityTriggers.IN_BALANCE.anyPlayer()));
    }

    private static FactTrigger.TriggerInstance drank(Map<String, String> facts) {
        return VitalityTriggers.DRANK.needing(facts);
    }

    private static FactTrigger.TriggerInstance temperature(Map<String, String> facts) {
        return VitalityTriggers.TEMPERATURE_STATE.needing(facts);
    }

    private static FactTrigger.TriggerInstance airLost(String cause, ContextAwarePredicate player) {
        return VitalityTriggers.AIR_LOST.needing(player, Map.of("cause", cause));
    }

    private static FactTrigger.TriggerInstance gearUsed(String gear, ContextAwarePredicate player) {
        return VitalityTriggers.GEAR_USED.needing(player, Map.of("gear", gear));
    }

    private static ContextAwarePredicate inDimension(ResourceKey<Level> dimension) {
        return located(LocationPredicate.Builder.location().setDimension(dimension));
    }

    private static ContextAwarePredicate located(LocationPredicate.Builder location) {
        return EntityPredicate.wrap(EntityPredicate.Builder.entity().located(location.build()).build());
    }

    private static Item container(WaterContainer container) {
        return VitalityItems.container(container);
    }

    private static Item mask(BreathingMask mask) {
        return VitalityItems.mask(mask);
    }

    private static ResourceLocation id(String path) {
        return ResourceIds.of(VitalityMod.MOD_ID, path);
    }
}
