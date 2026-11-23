package com.impact.realisticinventory.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.config.InventoryServerConfig;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Inventory milestone 1: the mod registers with Lib and its config files load with the spec's defaults. */
@GameTestHolder(InventoryMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InventorySkeletonGameTests {
    static final String LIB_NAMESPACE = ImpactsLib.MOD_ID;
    static final String EMPTY_TEMPLATE = "empty";
    private static final int SPEC_SATCHEL_SLOTS = 3;
    private static final int SPEC_BACKPACK_SLOTS = 9;
    private static final int SPEC_FRAME_PACK_SLOTS = 18;
    private static final int SPEC_EXPEDITION_PACK_SLOTS = 27;
    private static final int SPEC_NETHERITE_PACK_SLOTS = 36;
    private static final double SPEC_FRAME_PACK_SLOWNESS = 0.1;

    private InventorySkeletonGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void registersWithLib(GameTestHelper helper) {
        helper.assertTrue(ImpactsLib.registeredImpactMods().contains(InventoryMod.MOD_ID), "Inventory should register itself with Lib");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void serverConfigUsesSpecDefaults(GameTestHelper helper) {
        InventoryServerConfig config = InventoryServerConfig.get();
        helper.assertTrue(config.hotbar.swapCooldownTicks.defaultValue() == 0, "hotbar.swapCooldownTicks should default to 0");
        helper.assertTrue(config.isCreativeVanilla.defaultValue(), "creativeVanilla should default to true");
        helper.assertTrue(config.pickup.isIntoPack.defaultValue(), "pickup.intoPack should default to true");
        helper.assertFalse(config.packs.isShulkerAllowed.defaultValue(), "packs.allowShulkers should default to false");
        helper.assertTrue(config.packs.framePackSlowness.defaultValue() == SPEC_FRAME_PACK_SLOWNESS, "packs.framePackSlowness should default to 0.1");
        helper.assertTrue(config.packs.satchelSlots.defaultValue() == SPEC_SATCHEL_SLOTS, "Satchels hold 3");
        helper.assertTrue(config.packs.backpackSlots.defaultValue() == SPEC_BACKPACK_SLOTS, "Backpacks hold 9");
        helper.assertTrue(config.packs.framePackSlots.defaultValue() == SPEC_FRAME_PACK_SLOTS, "Frame Packs hold 18");
        helper.assertTrue(config.packs.explorersPackSlots.defaultValue() == SPEC_FRAME_PACK_SLOTS, "Explorer's Packs hold 18, like the Frame Pack");
        helper.assertTrue(config.packs.expeditionPackSlots.defaultValue() == SPEC_EXPEDITION_PACK_SLOTS, "Expedition Packs hold 27");
        helper.assertTrue(config.packs.netheritePackSlots.defaultValue() == SPEC_NETHERITE_PACK_SLOTS, "Netherite Packs hold 36");
        helper.assertTrue(config.firstJoin.overflow.defaultValue() == InventoryServerConfig.OverflowTarget.CHEST, "firstJoin.overflow should default to chest");
        helper.succeed();
    }
}
