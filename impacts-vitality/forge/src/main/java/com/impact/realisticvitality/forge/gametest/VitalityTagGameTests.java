package com.impact.realisticvitality.forge.gametest;

import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.air.BreathingMask;
import com.impact.realisticvitality.registry.VitalityBlocks;
import com.impact.realisticvitality.registry.VitalityItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Vitality's items and blocks in vanilla's and Forge's shared tags, and what vanilla does with them. */
@GameTestHolder(VitalityMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class VitalityTagGameTests {
    private VitalityTagGameTests() {
    }

    @GameTest(templateNamespace = VitalitySkeletonGameTests.LIB_NAMESPACE, template = VitalitySkeletonGameTests.EMPTY_TEMPLATE)
    public static void headwearIsHelmetsAndCopperNeedsStone(GameTestHelper helper) {
        for (Item headwear : VitalityItems.headwear()) {
            helper.assertTrue(new ItemStack(headwear).is(Tags.Items.ARMORS_HELMETS), headwear + " is in forge:armors/helmets");
        }
        helper.assertTrue(new ItemStack(VitalityItems.mask(BreathingMask.CLOTH_MASK)).is(ItemTags.DAMPENS_VIBRATIONS), "The wool Cloth Mask muffles vibrations");
        for (BlockState copper : new BlockState[] {VitalityBlocks.copperStill().defaultBlockState(), VitalityBlocks.airCompressor().defaultBlockState()}) {
            helper.assertFalse(new ItemStack(Items.WOODEN_PICKAXE).isCorrectToolForDrops(copper), copper.getBlock() + " drops nothing to a wooden pickaxe");
            helper.assertTrue(new ItemStack(Items.STONE_PICKAXE).isCorrectToolForDrops(copper), copper.getBlock() + " drops to a stone pickaxe");
        }
        helper.succeed();
    }
}
