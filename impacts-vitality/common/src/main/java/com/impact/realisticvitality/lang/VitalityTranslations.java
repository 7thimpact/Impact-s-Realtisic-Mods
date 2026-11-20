package com.impact.realisticvitality.lang;

import com.impact.lib.registry.CreativeTabs;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.registry.VitalityDamageTypes;
import com.impact.realisticvitality.thirst.BottledWater;
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

    private static Map<String, String> buildEnglish() {
        Map<String, String> english = new LinkedHashMap<>();
        english.put(ADVANCEMENT_TAB, "Realistic Vitality");
        english.put(PARCHED, "Parched");
        english.put(CREATIVE_TAB, "Impact's Realistic Vitality");
        WATER_NAMES.forEach((water, name) -> english.put(ITEM_KEY_PREFIX + water.itemPath, name));
        for (ResourceKey<DamageType> damageType : VitalityDamageTypes.ALL) {
            String deathKey = DEATH_PREFIX + VitalityDamageTypes.messageId(damageType);
            english.put(deathKey, DEATH_MESSAGES.get(damageType));
            english.put(deathKey + WHILE_FIGHTING_SUFFIX, DEATH_WHILE_FIGHTING_MESSAGES.get(damageType));
        }
        return Collections.unmodifiableMap(english);
    }
}
