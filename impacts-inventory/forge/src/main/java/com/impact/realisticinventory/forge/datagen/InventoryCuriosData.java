package com.impact.realisticinventory.forge.datagen;

import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.forge.condition.BackSlotEnabledCondition;
import com.impact.realisticinventory.forge.condition.ChestSlotEnabledCondition;
import com.impact.realisticinventory.forge.curios.CuriosWornSlots;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosDataProvider;

import java.util.concurrent.CompletableFuture;

/**
 * Gives every player Curios's {@code back} slot while the back slot is on, and its {@code body} slot (a pack worn on
 * the chest) while the chest slot is on. Curios defines the slot types; mods choose who gets them.
 */
final class InventoryCuriosData extends CuriosDataProvider {
    InventoryCuriosData(PackOutput output, ExistingFileHelper existingFiles, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(InventoryMod.MOD_ID, output, existingFiles, lookupProvider);
    }

    @Override
    public void generate(HolderLookup.Provider registries, ExistingFileHelper existingFiles) {
        createEntities("player_back")
                .addPlayer()
                .addSlots(CuriosWornSlots.BACK_SLOT_TYPE)
                .addCondition(BackSlotEnabledCondition.INSTANCE);
        createEntities("player_body")
                .addPlayer()
                .addSlots(CuriosWornSlots.CHEST_SLOT_TYPE)
                .addCondition(ChestSlotEnabledCondition.INSTANCE);
    }
}
