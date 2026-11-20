package com.impact.realisticvitality.lang;

import com.impact.lib.registry.CreativeTabs;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.registry.VitalityDamageTypes;
import com.impact.realisticvitality.temperature.ArmorLining;
import com.impact.realisticvitality.thirst.BottledWater;
import com.impact.realisticvitality.thirst.WaterContainer;
import net.minecraft.resources.ResourceKey;
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
        english.put(PARCHED, "Parched");
        english.put(CREATIVE_TAB, "Impact's Realistic Vitality");
        WATER_NAMES.forEach((water, name) -> english.put(itemName(water), name));
        CONTAINER_NAMES.forEach((container, name) -> english.put(ITEM_KEY_PREFIX + container.itemPath, name));
        english.put(ITEM_KEY_PREFIX + "charcoal_filter", "Charcoal Filter");
        english.put(BLOCK_KEY_PREFIX + "rain_barrel", "Rain Barrel");
        english.put(BLOCK_KEY_PREFIX + "water_filter", "Water Filter");
        english.put(BLOCK_KEY_PREFIX + "copper_still", "Copper Still");
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
        english.put(ITEM_KEY_PREFIX + "thermometer", "Thermometer");
        for (ResourceKey<DamageType> damageType : VitalityDamageTypes.ALL) {
            String deathKey = DEATH_PREFIX + VitalityDamageTypes.messageId(damageType);
            english.put(deathKey, DEATH_MESSAGES.get(damageType));
            english.put(deathKey + WHILE_FIGHTING_SUFFIX, DEATH_WHILE_FIGHTING_MESSAGES.get(damageType));
        }
        return Collections.unmodifiableMap(english);
    }
}
