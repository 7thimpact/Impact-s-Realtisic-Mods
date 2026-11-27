package com.impact.realisticvitality.forge.compat.curios;

import com.impact.realisticvitality.client.BackGearModels;
import com.impact.realisticvitality.registry.VitalityItems;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

/**
 * Client side: Curios draws worn headwear through {@link HeadwearCurioRenderer}, and the Air Tanks and Hydration Pack
 * in the back slot (Inventory's) through {@link BackGearCurioRenderer}.
 */
final class VitalityCurioRendering {
    private VitalityCurioRendering() {
    }

    static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BackGearModels.AIR_TANK, BackGearModels::airTank);
        event.registerLayerDefinition(BackGearModels.HYDRATION_PACK, BackGearModels::hydrationPack);
    }

    static void onClientSetup(FMLClientSetupEvent event) {
        HeadwearCurioRenderer headwearRenderer = new HeadwearCurioRenderer();
        VitalityItems.headwear().forEach(item -> CuriosRendererRegistry.register(item, () -> headwearRenderer));
        BackGearCurioRenderer backGearRenderer = new BackGearCurioRenderer();
        VitalityCurios.backGear().forEach(item -> CuriosRendererRegistry.register(item, () -> backGearRenderer));
    }
}
