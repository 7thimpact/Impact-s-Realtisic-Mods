package com.impact.realisticinventory.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.lib.backslot.BackSlot;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.forge.curios.CuriosWornSlots;
import com.impact.realisticinventory.pack.InventoryItems;
import com.impact.realisticinventory.pack.InventoryTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/**
 * Inventory's item tags: {@code #impactsinventory:packs}, Curios's {@code #curios:back}, which lets the packs and Lib's
 * {@code #impactslib:back_slot_items} (Vitality's Air Tanks) into the back slot, and {@code #curios:body}, which lets
 * the packs onto the chest.
 */
final class InventoryItemTags extends ItemTagsProvider {
    private static final TagKey<Item> CURIOS_BACK = TagKey.create(Registries.ITEM, ResourceIds.of("curios", CuriosWornSlots.BACK_SLOT_TYPE));
    private static final TagKey<Item> CURIOS_BODY = TagKey.create(Registries.ITEM, ResourceIds.of("curios", CuriosWornSlots.CHEST_SLOT_TYPE));

    InventoryItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFiles) {
        super(output, lookupProvider, CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()), InventoryMod.MOD_ID, existingFiles);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        tag(InventoryTags.PACKS).add(InventoryItems.packs().toArray(Item[]::new));
        tag(CURIOS_BACK).addTag(InventoryTags.PACKS).addOptionalTag(BackSlot.BACK_SLOT_ITEMS.location());
        tag(CURIOS_BODY).addTag(InventoryTags.PACKS);
    }
}
