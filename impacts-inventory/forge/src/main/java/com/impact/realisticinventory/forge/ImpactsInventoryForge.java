package com.impact.realisticinventory.forge;

import com.impact.lib.forge.ForgeModConstruction;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.InventoryStartup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/** Impact's Realistic Inventory on Forge: connects the loader-independent parts to Forge's events. */
@Mod(InventoryMod.MOD_ID)
public final class ImpactsInventoryForge {
    public ImpactsInventoryForge(FMLJavaModLoadingContext context) {
        ForgeModConstruction.run(context, InventoryStartup::onModConstruction);
    }
}
