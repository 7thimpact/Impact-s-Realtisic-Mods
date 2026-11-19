package com.impact.realisticfood.forge;

import com.impact.lib.forge.ForgeModConstruction;
import com.impact.realisticfood.FoodMod;
import com.impact.realisticfood.FoodStartup;
import com.impact.realisticfood.forge.client.FoodForgeClient;
import com.impact.realisticfood.forge.condition.FeatureEnabledCondition;
import com.impact.realisticfood.forge.datagen.FoodDataGenerators;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/** Impact's Realistic Food on Forge: connects the loader-independent parts to Forge's events. */
@Mod(FoodMod.MOD_ID)
public final class ImpactsFoodForge {
    public ImpactsFoodForge(FMLJavaModLoadingContext context) {
        ForgeModConstruction.run(context, FoodStartup::onModConstruction);
        context.getModEventBus().addListener(FoodDataGenerators::onGatherData);
        context.getModEventBus().addListener(ImpactsFoodForge::onRegister);
        FoodForgeEvents.register();
        if (FMLEnvironment.dist.isClient()) {
            FoodForgeClient.register(context.getModEventBus());
        }
    }

    private static void onRegister(RegisterEvent event) {
        // Forge asks for condition serializers to be registered while recipe serializers register.
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS)) {
            CraftingHelper.register(FeatureEnabledCondition.Serializer.INSTANCE);
        }
    }
}
