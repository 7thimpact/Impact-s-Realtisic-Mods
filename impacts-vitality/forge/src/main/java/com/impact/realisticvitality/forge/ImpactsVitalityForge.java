package com.impact.realisticvitality.forge;

import com.impact.lib.forge.ForgeModConstruction;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.VitalityStartup;
import com.impact.realisticvitality.forge.brewing.PurifiedWaterBrewing;
import com.impact.realisticvitality.forge.client.VitalityForgeClient;
import com.impact.realisticvitality.forge.condition.FeatureEnabledCondition;
import com.impact.realisticvitality.forge.datagen.VitalityDataGenerators;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/** Impact's Realistic Vitality on Forge: connects the loader-independent parts to Forge's events. */
@Mod(VitalityMod.MOD_ID)
public final class ImpactsVitalityForge {
    public ImpactsVitalityForge(FMLJavaModLoadingContext context) {
        ForgeModConstruction.run(context, VitalityStartup::onModConstruction);
        context.getModEventBus().addListener(VitalityDataGenerators::onGatherData);
        context.getModEventBus().addListener(ImpactsVitalityForge::onRegister);
        context.getModEventBus().addListener(ImpactsVitalityForge::onCommonSetup);
        VitalityForgeEvents.register();
        if (FMLEnvironment.dist.isClient()) {
            VitalityForgeClient.register(context.getModEventBus());
        }
    }

    // Forge's brewing registry is a plain list, so recipes go in on the main thread.
    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(PurifiedWaterBrewing::register);
    }

    private static void onRegister(RegisterEvent event) {
        // Forge asks for condition serializers to be registered while recipe serializers register.
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS)) {
            CraftingHelper.register(FeatureEnabledCondition.Serializer.INSTANCE);
        }
    }
}
