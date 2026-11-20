package com.impact.realisticvitality.registry;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.realisticvitality.VitalityMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/** Vitality's item tags, and the shared tags it fills. */
public final class VitalityTags {
    /** Costs thirst to eat, and can't be eaten while Hot. */
    public static final TagKey<Item> DRY_FOODS = item("dry_foods");
    /** Everything Vitality lets you drink: its water bottles, its containers and (with Crafting) the Water-filled Wooden Bucket. */
    public static final TagKey<Item> DRINKS = item("drinks");
    /** Bowl soups and stews: they hydrate and warm you. */
    public static final TagKey<Item> SOUPS = item("soups");
    /** Fruit and vegetables that give a little thirst. */
    public static final TagKey<Item> JUICY_FOODS = item("juicy_foods");
    /** Armor that keeps you warm: leather by default. */
    public static final TagKey<Item> WARM_ARMOR = item("warm_armor");
    /** Armor that breathes and cools: chainmail by default. */
    public static final TagKey<Item> COOLING_ARMOR = item("cooling_armor");
    /** Metal armor that makes both heat and cold worse: iron by default. */
    public static final TagKey<Item> CONDUCTIVE_ARMOR = item("conductive_armor");
    /** Lib's shared tag: anything that works as a water bottle in recipes. */
    public static final TagKey<Item> WATER_BOTTLES = TagKey.create(Registries.ITEM, ResourceIds.of(ImpactsLib.MOD_ID, "water_bottles"));

    private VitalityTags() {
    }

    private static TagKey<Item> item(String path) {
        return TagKey.create(Registries.ITEM, ResourceIds.of(VitalityMod.MOD_ID, path));
    }
}
