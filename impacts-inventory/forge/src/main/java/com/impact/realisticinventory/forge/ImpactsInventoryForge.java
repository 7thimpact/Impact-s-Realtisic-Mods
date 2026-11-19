package com.impact.realisticinventory.forge;

import com.impact.lib.forge.ForgeModConstruction;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.InventoryStartup;
import com.impact.realisticinventory.forge.client.InventoryForgeClient;
import com.impact.realisticinventory.forge.condition.BackSlotEnabledCondition;
import com.impact.realisticinventory.forge.condition.PacksEnabledCondition;
import com.impact.realisticinventory.forge.curios.InventoryCurios;
import com.impact.realisticinventory.forge.datagen.InventoryDataGenerators;
import com.impact.realisticinventory.inventory.SlotLocks;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/** Impact's Realistic Inventory on Forge: connects the loader-independent parts to Forge's events. */
@Mod(InventoryMod.MOD_ID)
public final class ImpactsInventoryForge {
    public ImpactsInventoryForge(FMLJavaModLoadingContext context) {
        ForgeModConstruction.run(context, InventoryStartup::onModConstruction);
        InventoryCurios.register(context.getModEventBus());
        InventoryForgeEvents.register();
        SlotLocks.registerLockedSlotRule(ForgeSlotLocks::isLockedItemHandlerSlot);
        context.getModEventBus().addListener(InventoryDataGenerators::onGatherData);
        context.getModEventBus().addListener(ImpactsInventoryForge::onRegister);
        if (FMLEnvironment.dist.isClient()) {
            InventoryForgeClient.register(context.getModEventBus());
        }
    }

    private static void onRegister(RegisterEvent event) {
        // Forge asks for condition serializers to be registered while recipe serializers register.
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS)) {
            CraftingHelper.register(PacksEnabledCondition.Serializer.INSTANCE);
            CraftingHelper.register(BackSlotEnabledCondition.Serializer.INSTANCE);
        }
    }
}
