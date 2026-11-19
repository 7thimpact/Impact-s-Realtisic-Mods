package com.impact.realisticinventory.forge.datagen;

import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.forge.condition.BackSlotEnabledCondition;
import com.impact.realisticinventory.forge.curios.CuriosBackSlot;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosDataProvider;

import java.util.concurrent.CompletableFuture;

/** Gives every player Curios's {@code back} slot (Curios defines the slot type; mods choose who gets it), while the back slot is on. */
final class InventoryCuriosData extends CuriosDataProvider {
    InventoryCuriosData(PackOutput output, ExistingFileHelper existingFiles, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(InventoryMod.MOD_ID, output, existingFiles, lookupProvider);
    }

    @Override
    public void generate(HolderLookup.Provider registries, ExistingFileHelper existingFiles) {
        createEntities("player_back")
                .addPlayer()
                .addSlots(CuriosBackSlot.SLOT_TYPE)
                .addCondition(BackSlotEnabledCondition.INSTANCE);
    }
}
