package com.impact.realisticinventory.forge.curios;

import com.impact.realisticinventory.pack.InventoryItems;
import com.impact.realisticinventory.pack.PackWearing;
import com.impact.realisticinventory.pack.WornOn;
import com.impact.realisticinventory.pack.WornSlots;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.util.Optional;

/** Connects Inventory's worn slots (back and chest) to Curios: the storage, the packs' curio behavior, and Curios's change events. */
public final class InventoryCurios {
    private static final PackCurio PACK_CURIO = new PackCurio();

    private InventoryCurios() {
    }

    /** Called during mod construction. */
    public static void register(IEventBus modEventBus) {
        WornSlots.useBackend(new CuriosWornSlots());
        modEventBus.addListener(InventoryCurios::onCommonSetup);
        MinecraftForge.EVENT_BUS.addListener(InventoryCurios::onCurioChange);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> InventoryItems.packs().forEach(pack -> CuriosApi.registerCurio(pack, PACK_CURIO)));
    }

    /** Curios reports every change of a worn curio, whichever screen or mod made it. */
    private static void onCurioChange(CurioChangeEvent event) {
        Optional<WornOn> place = CuriosWornSlots.placeOf(event.getIdentifier());
        if (place.isPresent() && event.getEntity() instanceof ServerPlayer player) {
            PackWearing.onWornItemChanged(player, place.get(), event.getFrom(), event.getTo());
        }
    }
}
