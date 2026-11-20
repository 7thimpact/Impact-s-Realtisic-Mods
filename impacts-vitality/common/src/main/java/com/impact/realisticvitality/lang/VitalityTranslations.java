package com.impact.realisticvitality.lang;

import com.impact.lib.advancement.ImpactsAdvancementBuilder;
import com.impact.lib.registry.CreativeTabs;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.advancement.VitalityAdvancements;
import com.impact.realisticvitality.registry.VitalityDamageTypes;
import com.impact.realisticvitality.temperature.ArmorLining;
import com.impact.realisticvitality.thirst.BottledWater;
import com.impact.realisticvitality.thirst.WaterContainer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Vitality's translation keys and their English text. Data generation writes {@link #ENGLISH} to en_us.json. */
public final class VitalityTranslations {
    /** Minecraft names a tab after its root; this key is for tools that list tabs by mod. */
    public static final String ADVANCEMENT_TAB = "advancements." + VitalityMod.MOD_ID + ".tab";

    public static final String PARCHED = "effect." + VitalityMod.MOD_ID + ".parched";
    public static final String CREATIVE_TAB = CreativeTabs.titleKey(VitalityMod.MOD_ID);
    private static final String ITEM_KEY_PREFIX = "item." + VitalityMod.MOD_ID + ".";
    private static final String BLOCK_KEY_PREFIX = "block." + VitalityMod.MOD_ID + ".";
    public static final String CONTAINER_EMPTY = "tooltip.impactsvitality.container.empty";
    public static final String CONTAINER_DRINKS = "tooltip.impactsvitality.container.drinks";
    public static final String CONTAINER_SOUP = "tooltip.impactsvitality.container.soup";
    public static final String CONTAINER_KEEPS_TEMPERATURE = "tooltip.impactsvitality.container.keeps_temperature";
    public static final String CONTAINER_REFILLS = "tooltip.impactsvitality.container.refills";
    public static final String PACK_WORN_ON_BACK = "tooltip.impactsvitality.hydration_pack.back";
    public static final String PACK_WORN_ON_CHEST = "tooltip.impactsvitality.hydration_pack.chest";
    public static final String SIP_PACK_KEY = "key.impactsvitality.sip_pack";
    public static final String KEY_CATEGORY = "key.categories.impactsvitality";
    public static final String TOO_HOT_TO_EAT = "message.impactsvitality.too_hot_to_eat";
    private static final String LINING_TOOLTIP_PREFIX = "tooltip.impactsvitality.lining.";
    public static final String MASK_DAMP = "tooltip.impactsvitality.mask.damp";
    public static final String MASK_DRY = "tooltip.impactsvitality.mask.dry";
    public static final String MASK_FILTER = "tooltip.impactsvitality.mask.filter";
    public static final String MASK_NO_FILTER = "tooltip.impactsvitality.mask.no_filter";
    public static final String MASK_NEVER_SMOKE = "tooltip.impactsvitality.mask.never_smoke";
    public static final String TANK_AIR = "tooltip.impactsvitality.tank.air";
    public static final String TANK_NEEDS_RESPIRATOR = "tooltip.impactsvitality.tank.needs_respirator";
    public static final String BLADDER_USES = "tooltip.impactsvitality.air_bladder.uses";
    private static final Map<String, String> BREATHING_ITEM_NAMES = Map.of(
            "cloth_mask", "Cloth Mask",
            "respirator", "Respirator",
            "rebreather", "Rebreather",
            "netherite_rebreather", "Netherite Rebreather",
            "air_tank", "Air Tank",
            "shulker_air_tank", "Shulker Air Tank",
            "air_bladder", "Air Bladder",
            "bottled_air", "Bottled Air");
    private static final Map<ArmorLining, String> LINING_NAMES = new EnumMap<>(Map.of(
            ArmorLining.WOOL, "Wool Lining: warmer",
            ArmorLining.FUR, "Fur Lining: much warmer",
            ArmorLining.LEATHER, "Leather Lining: cooler"));
    private static final Map<WaterContainer, String> CONTAINER_NAMES = new EnumMap<>(Map.of(
            WaterContainer.WOODEN_CUP, "Wooden Cup",
            WaterContainer.WATERSKIN, "Waterskin",
            WaterContainer.CANTEEN, "Canteen",
            WaterContainer.INSULATED_FLASK, "Insulated Flask",
            WaterContainer.TIDEBOUND_CANTEEN, "Tidebound Canteen",
            WaterContainer.HYDRATION_PACK, "Hydration Pack"));
    private static final Map<BottledWater, String> WATER_NAMES = new EnumMap<>(Map.of(
            BottledWater.UNCLEAN, "Unclean Water",
            BottledWater.SALT, "Salt Water",
            BottledWater.PURIFIED, "Purified Water",
            BottledWater.COLD, "Cold Water"));
    private static final String DEATH_PREFIX = "death.attack.";
    private static final String WHILE_FIGHTING_SUFFIX = ".player";
    private static final Map<ResourceKey<DamageType>, String> DEATH_MESSAGES = Map.of(
            VitalityDamageTypes.DEHYDRATION, "%1$s died of thirst",
            VitalityDamageTypes.HEATSTROKE, "%1$s died of heatstroke",
            VitalityDamageTypes.THIN_AIR, "%1$s ran out of air on the mountaintop",
            VitalityDamageTypes.FUMES, "%1$s choked on lava fumes");
    private static final Map<ResourceKey<DamageType>, String> DEATH_WHILE_FIGHTING_MESSAGES = Map.of(
            VitalityDamageTypes.DEHYDRATION, "%1$s died of thirst while fighting %2$s",
            VitalityDamageTypes.HEATSTROKE, "%1$s died of heatstroke while fighting %2$s",
            VitalityDamageTypes.THIN_AIR, "%1$s ran out of air while fighting %2$s",
            VitalityDamageTypes.FUMES, "%1$s choked on lava fumes while fighting %2$s");

    private static final Map<ResourceLocation, String[]> ADVANCEMENT_TEXTS = Map.ofEntries(
            Map.entry(VitalityAdvancements.STAY_HYDRATED, new String[] {"Stay Hydrated", "Drink any water"}),
            Map.entry(VitalityAdvancements.SOMETHING_IN_THE_WATER, new String[] {"Something in the Water", "Get Parched from unclean water"}),
            Map.entry(VitalityAdvancements.WORTH_ITS_SALT, new String[] {"Worth Its Salt", "Drink salt water"}),
            Map.entry(VitalityAdvancements.CLEAN_LIVING, new String[] {"Clean Living", "Drink Purified Water"}),
            Map.entry(VitalityAdvancements.SKIN_IN_THE_GAME, new String[] {"Skin in the Game", "Craft a Waterskin"}),
            Map.entry(VitalityAdvancements.HALF_MEASURES, new String[] {"Half Measures", "Stop healing at the limit because hunger or thirst is low"}),
            Map.entry(VitalityAdvancements.WELL_WATERED, new String[] {"Well Watered", "Drink 500 times"}),
            Map.entry(VitalityAdvancements.COLD_FEET, new String[] {"Cold Feet", "Become Cold"}),
            Map.entry(VitalityAdvancements.ALBERT_AND_EINSTEIN, new String[] {"Albert and Einstein", "Catch on fire while Cold"}),
            Map.entry(VitalityAdvancements.BUNDLE_UP, new String[] {"Bundle Up", "Wear 4 armor pieces with Wool or Fur Linings"}),
            Map.entry(VitalityAdvancements.TAKING_THE_TEMPERATURE, new String[] {"Taking the Temperature", "Craft a Thermometer"}),
            Map.entry(VitalityAdvancements.DRY_SPELL, new String[] {"Dry Spell", "Try to eat something dry while Hot"}),
            Map.entry(VitalityAdvancements.SO_HOT, new String[] {"It's getting hot in here! (so hot!)", "Reach Overheating, the dangerous end of hot"}),
            Map.entry(VitalityAdvancements.HAT_TRICK, new String[] {"Hat Trick", "Wear a Straw Hat in a desert at noon"}),
            Map.entry(VitalityAdvancements.BREATHTAKING, new String[] {"Breathtaking", "Lose air to thin air on a mountain"}),
            Map.entry(VitalityAdvancements.IS_IT_WORTH_IT, new String[] {"Is it worth it?",
                    "Go down near bedrock with no air support: no mask, Air Tank, Bottled Air or breathing effect"}),
            Map.entry(VitalityAdvancements.UP_IN_SMOKE, new String[] {"Up in Smoke", "Lose air to lava fumes"}),
            Map.entry(VitalityAdvancements.MASKED_UP, new String[] {"Masked Up", "Wear a Respirator with a filter in the Nether"}),
            Map.entry(VitalityAdvancements.IT_FEELS_LIKE_NOTHING, new String[] {"It feels like… nothing", "Enter the End"}),
            Map.entry(VitalityAdvancements.INTO_THIN_AIR, new String[] {"Into Thin Air", "Use an Air Tank in the End"}),
            Map.entry(VitalityAdvancements.PEAK_CONDITION, new String[] {"Peak Condition",
                    "Stand above Y 300 with full thirst, full air and a Comfortable body temperature"}),
            Map.entry(VitalityAdvancements.IN_BALANCE, new String[] {"In Balance",
                    "Spend a whole day with thirst above 3 droplets, air never empty, and never Cold or Hot"}));

    public static final Map<String, String> ENGLISH = buildEnglish();

    private VitalityTranslations() {
    }

    public static String liningTooltip(ArmorLining lining) {
        return LINING_TOOLTIP_PREFIX + lining.serializedName;
    }

    public static String itemName(BottledWater water) {
        return ITEM_KEY_PREFIX + water.itemPath;
    }

    private static Map<String, String> buildEnglish() {
        Map<String, String> english = new LinkedHashMap<>();
        english.put(ADVANCEMENT_TAB, "Realistic Vitality");
        ADVANCEMENT_TEXTS.forEach((advancementId, titleAndDescription) -> {
            english.put(ImpactsAdvancementBuilder.titleKey(advancementId), titleAndDescription[0]);
            english.put(ImpactsAdvancementBuilder.descriptionKey(advancementId), titleAndDescription[1]);
        });
        english.put(PARCHED, "Parched");
        english.put(CREATIVE_TAB, "Impact's Realistic Vitality");
        WATER_NAMES.forEach((water, name) -> english.put(itemName(water), name));
        CONTAINER_NAMES.forEach((container, name) -> english.put(ITEM_KEY_PREFIX + container.itemPath, name));
        english.put(ITEM_KEY_PREFIX + "charcoal_filter", "Charcoal Filter");
        english.put(BLOCK_KEY_PREFIX + "rain_barrel", "Rain Barrel");
        english.put(BLOCK_KEY_PREFIX + "water_filter", "Water Filter");
        english.put(BLOCK_KEY_PREFIX + "copper_still", "Copper Still");
        english.put(BLOCK_KEY_PREFIX + "air_compressor", "Air Compressor");
        english.put(CONTAINER_EMPTY, "Empty (holds %s drinks)");
        english.put(CONTAINER_DRINKS, "%s/%s drinks of %s");
        english.put(CONTAINER_SOUP, "Soup");
        english.put(CONTAINER_KEEPS_TEMPERATURE, "Keeps Cold Water cold and soups warm");
        english.put(CONTAINER_REFILLS, "Refills itself with purified water, faster in rain or water");
        english.put(PACK_WORN_ON_BACK, "Wear it on your back and sip with %s");
        english.put(PACK_WORN_ON_CHEST, "Wear it in the chest slot and sip with %s");
        english.put(SIP_PACK_KEY, "Sip from Hydration Pack");
        english.put(KEY_CATEGORY, "Impact's Realistic Vitality");
        english.put(TOO_HOT_TO_EAT, "Too hot to eat anything dry.");
        LINING_NAMES.forEach((lining, text) -> english.put(liningTooltip(lining), text));
        english.put(ITEM_KEY_PREFIX + "straw_hat", "Straw Hat");
        BREATHING_ITEM_NAMES.forEach((path, name) -> english.put(ITEM_KEY_PREFIX + path, name));
        english.put(MASK_DAMP, "Damp: halves smoke for %s more seconds");
        english.put(MASK_DRY, "Dry: use it on water to dampen it");
        english.put(MASK_FILTER, "Charcoal Filter: %s seconds of smoke left");
        english.put(MASK_NO_FILTER, "No filter: put a Charcoal Filter in it in a crafting grid");
        english.put(MASK_NEVER_SMOKE, "Smoke never affects you");
        english.put(TANK_AIR, "Air: %s of %s seconds");
        english.put(TANK_NEEDS_RESPIRATOR, "Supplies thin air while you wear a Respirator or Rebreather");
        english.put(BLADDER_USES, "%s of %s breaths: fill it in good air, use it in thin air");
        english.put(ITEM_KEY_PREFIX + "thermometer", "Thermometer");
        for (ResourceKey<DamageType> damageType : VitalityDamageTypes.ALL) {
            String deathKey = DEATH_PREFIX + VitalityDamageTypes.messageId(damageType);
            english.put(deathKey, DEATH_MESSAGES.get(damageType));
            english.put(deathKey + WHILE_FIGHTING_SUFFIX, DEATH_WHILE_FIGHTING_MESSAGES.get(damageType));
        }
        return Collections.unmodifiableMap(english);
    }
}
