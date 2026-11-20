package com.impact.realisticcrafting.forge;

import com.impact.lib.forge.ForgeModConstruction;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.CraftingStartup;
import com.impact.realisticcrafting.forge.client.CraftingForgeClient;
import com.impact.realisticcrafting.forge.condition.OverhaulEnabledCondition;
import com.impact.realisticcrafting.forge.datagen.CraftingDataGenerators;
import com.impact.realisticcrafting.forge.loot.PebbleLootModifier;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/** Impact's Realistic Crafting on Forge: connects the loader-independent parts to Forge's events. */
@Mod(CraftingMod.MOD_ID)
public final class ImpactsCraftingForge {
    public ImpactsCraftingForge(FMLJavaModLoadingContext context) {
        ForgeModConstruction.run(context, () -> {
            CraftingStartup.onModConstruction();
            LoaderPlatform.current().registrar(CraftingMod.MOD_ID)
                    .register(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, PebbleLootModifier.PATH, PebbleLootModifier.CODEC);
        });
        context.getModEventBus().addListener(CraftingDataGenerators::onGatherData);
        context.getModEventBus().addListener(ImpactsCraftingForge::onRegister);
        CraftingForgeEvents.register();
        if (FMLEnvironment.dist.isClient()) {
            CraftingForgeClient.register(context.getModEventBus());
        }
    }

    private static void onRegister(RegisterEvent event) {
        // Forge asks for condition serializers to be registered while recipe serializers register.
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS)) {
            CraftingHelper.register(OverhaulEnabledCondition.Serializer.INSTANCE);
        }
    }
}
