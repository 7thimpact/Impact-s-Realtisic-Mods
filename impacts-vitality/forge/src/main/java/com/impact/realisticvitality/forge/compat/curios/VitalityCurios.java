package com.impact.realisticvitality.forge.compat.curios;

import com.impact.lib.log.ImpactsLog;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.air.AirTank;
import com.impact.realisticvitality.debug.VitalityLogCategories;
import com.impact.realisticvitality.player.Headwear;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.thirst.WaterContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Optional Curios support: Vitality's masks and the Straw Hat also fit Curios's {@code head} slot, which Vitality's
 * data gives every player; the Air Tanks and the Hydration Pack show on the player's back in the back slot; and all of
 * them sound like themselves when equipped. Loaded only when Curios is installed; Vitality runs the same without it.
 */
public final class VitalityCurios {
    public static final String MOD_ID = "curios";
    private static final String HEAD_SLOT_TYPE = "head";
    private static final ImpactsLog LOG = ImpactsLog.of(VitalityMod.MOD_ID);
    private static final VitalityCurio CURIO = new VitalityCurio();

    private VitalityCurios() {
    }

    public static void register(IEventBus modEventBus) {
        Headwear.useExtraSlots(VitalityCurios::wornOnHead);
        modEventBus.addListener(VitalityCurios::onCommonSetup);
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(VitalityCurioRendering::onRegisterLayers);
            modEventBus.addListener(VitalityCurioRendering::onClientSetup);
        }
        LOG.debug(VitalityLogCategories.STARTUP, "curios support on", "headSlot", true, "backGearShown", true, "equipSounds", true);
    }

    /** Curios's behavior for the headwear and the back gear (Air Tanks and the Hydration Pack, worn in the back slot). */
    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> curioItems().forEach(item -> CuriosApi.registerCurio(item, CURIO)));
    }

    static List<Item> curioItems() {
        List<Item> items = new ArrayList<>(VitalityItems.headwear());
        items.addAll(backGear());
        return items;
    }

    static List<Item> backGear() {
        return List.of(VitalityItems.tank(AirTank.AIR_TANK), VitalityItems.tank(AirTank.SHULKER_AIR_TANK),
                VitalityItems.container(WaterContainer.HYDRATION_PACK));
    }

    private static List<ItemStack> wornOnHead(Player player) {
        return CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(curios -> curios.getStacksHandler(HEAD_SLOT_TYPE))
                .map(ICurioStacksHandler::getStacks)
                .map(VitalityCurios::stacksOf)
                .orElse(List.of());
    }

    private static List<ItemStack> stacksOf(IDynamicStackHandler stacks) {
        List<ItemStack> worn = new ArrayList<>();
        for (int slot = 0; slot < stacks.getSlots(); slot++) {
            worn.add(stacks.getStackInSlot(slot));
        }
        return worn;
    }
}
