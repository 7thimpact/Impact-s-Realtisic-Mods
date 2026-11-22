package com.impact.realisticcrafting.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.item.ToolKind;
import com.impact.realisticcrafting.item.ToolMaterial;
import com.impact.realisticcrafting.menu.CraftingTier;
import com.impact.realisticcrafting.recipe.TierResolver;
import com.impact.realisticcrafting.registry.CraftingItems;
import com.impact.realisticcrafting.registry.CraftingTags;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

/**
 * Step 6, Salty's Realistic Forging: its smithing hammers finish Crafting's loose items through the {@code hammers}
 * tag, and its metal items take Crafting's tiers through optional tag entries. SRF's items are looked up by registry
 * ID; without SRF the tests pass at once.
 */
@GameTestHolder(CraftingMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CraftingSrfGameTests {
    private static final String SRF_MOD_ID = "realisticforgingores";

    private CraftingSrfGameTests() {
    }

    @GameTest(templateNamespace = CraftingSkeletonGameTests.LIB_NAMESPACE, template = CraftingSkeletonGameTests.EMPTY_TEMPLATE)
    public static void srfsSmithingHammerFinishesLooseTools(GameTestHelper helper) {
        Optional<Item> smithingHammer = srfItem("smithing_hammer");
        if (smithingHammer.isEmpty()) {
            helper.succeed();
            return;
        }
        helper.assertTrue(new ItemStack(smithingHammer.get()).is(CraftingTags.HAMMERS), "SRF's smithing hammer is in Crafting's hammers tag");
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(CraftingItems.looseTool(ToolMaterial.STONE, ToolKind.PICKAXE)));
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(smithingHammer.get()));
            CraftingAchievementGameTests.holdUse(player);
            helper.assertTrue(player.getMainHandItem().is(Items.STONE_PICKAXE), "SRF's smithing hammer finishes a loose pickaxe");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = CraftingSkeletonGameTests.LIB_NAMESPACE, template = CraftingSkeletonGameTests.EMPTY_TEMPLATE)
    public static void srfsMetalsTakeCraftingsTiers(GameTestHelper helper) {
        if (!ImpactsLib.isLoaded(SRF_MOD_ID)) {
            helper.succeed();
            return;
        }
        srfItem("raw_iron_ore").ifPresent(ironOre ->
                helper.assertTrue(TierResolver.tierOf(new ItemStack(ironOre)) == CraftingTier.STONE, "SRF's raw iron ore is stone tier"));
        srfItem("copperdust").ifPresent(copperDust ->
                helper.assertTrue(TierResolver.tierOf(new ItemStack(copperDust)) == CraftingTier.STONE, "SRF's copper dust is stone tier"));
        srfItem("rawgoldore").ifPresent(goldOre ->
                helper.assertTrue(TierResolver.tierOf(new ItemStack(goldOre)) == CraftingTier.ADVANCED, "SRF's raw gold ore is advanced"));
        srfItem("roughdiamond").ifPresent(diamond ->
                helper.assertTrue(TierResolver.tierOf(new ItemStack(diamond)) == CraftingTier.ADVANCED, "SRF's rough diamond is advanced"));
        helper.succeed();
    }

    private static Optional<Item> srfItem(String path) {
        ResourceLocation itemId = ResourceIds.of(SRF_MOD_ID, path);
        return ResourceIds.item(itemId);
    }
}
