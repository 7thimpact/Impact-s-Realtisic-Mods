package com.impact.realisticvitality.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.lib.backslot.BackSlot;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.air.AirTank;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.registry.VitalityTags;
import com.impact.realisticvitality.thirst.WaterContainer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/**
 * Vitality's item tags, and its entries in Lib's {@code #impactslib:water_bottles}. Food's items are optional entries,
 * so the tags load without Food.
 */
final class VitalityItemTags extends ItemTagsProvider {
    private static final String FOOD_MOD_ID = VitalityMod.FOOD_MOD_ID;
    private static final String INVENTORY_MOD_ID = VitalityMod.INVENTORY_MOD_ID;

    VitalityItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFiles) {
        super(output, lookupProvider, CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()), VitalityMod.MOD_ID, existingFiles);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        Item[] waterBottles = VitalityItems.waterBottles().toArray(Item[]::new);
        tag(VitalityTags.WATER_BOTTLES).add(waterBottles);
        tag(VitalityTags.DRINKS).add(waterBottles).add(VitalityItems.containers().toArray(Item[]::new));
        tag(BackSlot.BACK_SLOT_ITEMS).add(VitalityItems.container(WaterContainer.HYDRATION_PACK), VitalityItems.tank(AirTank.AIR_TANK),
                VitalityItems.tank(AirTank.SHULKER_AIR_TANK));
        tag(VitalityTags.DRY_FOODS).add(Items.BREAD, Items.COOKIE, Items.DRIED_KELP)
                .addOptionalTag(ResourceIds.of(FOOD_MOD_ID, "dried_foods"))
                .addOptional(ResourceIds.of(FOOD_MOD_ID, "dough_bun"))
                .addOptional(ResourceIds.of(FOOD_MOD_ID, "flatbread"));
        tag(VitalityTags.SOUPS).add(Items.MUSHROOM_STEW, Items.RABBIT_STEW, Items.BEETROOT_SOUP, Items.SUSPICIOUS_STEW)
                .addOptionalTag(ResourceIds.of(FOOD_MOD_ID, "soups"));
        tag(VitalityTags.HEAVY_PACKS).addOptional(ResourceIds.of(INVENTORY_MOD_ID, "frame_pack"));
        tag(VitalityTags.JUICY_FOODS).add(Items.SWEET_BERRIES, Items.GLOW_BERRIES, Items.APPLE, Items.CARROT, Items.BEETROOT);
        tag(VitalityTags.WARM_ARMOR).add(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
        tag(VitalityTags.COOLING_ARMOR).add(Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS);
        tag(VitalityTags.CONDUCTIVE_ARMOR).add(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
    }
}
