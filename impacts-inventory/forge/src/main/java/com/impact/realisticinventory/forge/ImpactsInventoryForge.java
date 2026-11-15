package com.impact.realisticinventory.forge;

import com.impact.lib.forge.ForgeModConstruction;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.InventoryStartup;
import com.impact.realisticinventory.forge.client.InventoryForgeClient;
import com.impact.realisticinventory.forge.datagen.InventoryDataGenerators;
import com.impact.realisticinventory.inventory.SlotLocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** Impact's Realistic Inventory on Forge: connects the loader-independent parts to Forge's events. */
@Mod(InventoryMod.MOD_ID)
public final class ImpactsInventoryForge {
    public ImpactsInventoryForge(FMLJavaModLoadingContext context) {
        ForgeModConstruction.run(context, InventoryStartup::onModConstruction);
        InventoryForgeEvents.register();
        SlotLocks.registerLockedSlotRule(ForgeSlotLocks::isLockedItemHandlerSlot);
        context.getModEventBus().addListener(InventoryDataGenerators::onGatherData);
        if (FMLEnvironment.dist.isClient()) {
            InventoryForgeClient.register(context.getModEventBus());
        }
    }
}
