package com.impact.realisticinventory.forge.curios;

import com.impact.realisticinventory.pack.BackSlotStorage;
import com.impact.realisticinventory.pack.InventoryItems;
import com.impact.realisticinventory.pack.PackWearing;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

/** Connects Inventory's back slot to Curios: the storage, the packs' curio behavior, and Curios's change events. */
public final class InventoryCurios {
    private static final PackCurio PACK_CURIO = new PackCurio();

    private InventoryCurios() {
    }

    /** Called during mod construction. */
    public static void register(IEventBus modEventBus) {
        BackSlotStorage.useBackend(new CuriosBackSlot());
        modEventBus.addListener(InventoryCurios::onCommonSetup);
        MinecraftForge.EVENT_BUS.addListener(InventoryCurios::onCurioChange);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> InventoryItems.packs().forEach(pack -> CuriosApi.registerCurio(pack, PACK_CURIO)));
    }

    /** Curios reports every change of a worn curio, whichever screen or mod made it. */
    private static void onCurioChange(CurioChangeEvent event) {
        if (CuriosBackSlot.SLOT_TYPE.equals(event.getIdentifier()) && event.getEntity() instanceof ServerPlayer player) {
            PackWearing.onWornItemChanged(player, event.getFrom(), event.getTo());
        }
    }
}
