package com.impact.realisticvitality.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.lib.backslot.BackSlot;
import com.impact.realisticvitality.VitalityMod;
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

    VitalityItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFiles) {
        super(output, lookupProvider, CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()), VitalityMod.MOD_ID, existingFiles);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        Item[] waterBottles = VitalityItems.waterBottles().toArray(Item[]::new);
        tag(VitalityTags.WATER_BOTTLES).add(waterBottles);
        tag(VitalityTags.DRINKS).add(waterBottles).add(VitalityItems.containers().toArray(Item[]::new));
        tag(BackSlot.BACK_SLOT_ITEMS).add(VitalityItems.container(WaterContainer.HYDRATION_PACK));
        tag(VitalityTags.DRY_FOODS).add(Items.BREAD, Items.COOKIE, Items.DRIED_KELP)
                .addOptionalTag(ResourceIds.of(FOOD_MOD_ID, "dried_foods"))
                .addOptional(ResourceIds.of(FOOD_MOD_ID, "dough_bun"))
                .addOptional(ResourceIds.of(FOOD_MOD_ID, "flatbread"));
        tag(VitalityTags.SOUPS).add(Items.MUSHROOM_STEW, Items.RABBIT_STEW, Items.BEETROOT_SOUP, Items.SUSPICIOUS_STEW);
        tag(VitalityTags.JUICY_FOODS).add(Items.SWEET_BERRIES, Items.GLOW_BERRIES, Items.APPLE, Items.CARROT, Items.BEETROOT);
    }
}
