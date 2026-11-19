package com.impact.realisticfood.forge;

import com.impact.lib.forge.ForgeModConstruction;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.FoodStartup;
import com.impact.realisticfood.forge.datagen.FoodDataGenerators;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/** Impact's Realistic Food on Forge: connects the loader-independent parts to Forge's events. */
@Mod(FoodMod.MOD_ID)
public final class ImpactsFoodForge {
    public ImpactsFoodForge(FMLJavaModLoadingContext context) {
        ForgeModConstruction.run(context, FoodStartup::onModConstruction);
        context.getModEventBus().addListener(FoodDataGenerators::onGatherData);
    }
}
