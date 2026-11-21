package com.impact.realisticvitality.forge.compat.curios;

import com.impact.realisticvitality.registry.VitalityItems;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

/** Client side: Curios draws worn headwear through {@link HeadwearCurioRenderer}. */
final class HeadwearCurioRendering {
    private HeadwearCurioRendering() {
    }

    static void onClientSetup(FMLClientSetupEvent event) {
        HeadwearCurioRenderer renderer = new HeadwearCurioRenderer();
        VitalityItems.headwear().forEach(item -> CuriosRendererRegistry.register(item, () -> renderer));
    }
}
