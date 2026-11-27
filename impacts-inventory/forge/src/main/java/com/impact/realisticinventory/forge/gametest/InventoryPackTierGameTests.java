package com.impact.realisticinventory.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.pack.InventoryItems;
import com.impact.realisticinventory.pack.PackContents;
import com.impact.realisticinventory.pack.PackEffects;
import com.impact.realisticinventory.pack.PackKind;
import com.impact.realisticinventory.pack.PackMenu;
import com.impact.realisticinventory.pack.PackMenus;
import com.impact.realisticinventory.pack.PackRules;
import com.impact.realisticinventory.pack.PlacedPackBlockEntity;
import com.impact.realisticinventory.pack.PlacedPacks;
import com.impact.realisticinventory.pack.WornOn;
import com.impact.realisticinventory.pack.WornPack;
import com.impact.realisticinventory.pack.WornSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.function.Supplier;

/**
 * The packs past the Frame Pack: each holds at least as much as the last without slowing its wearer, each upgrade keeps
 * the contents and dye all the way to the Netherite Pack (which survives fire), and the Ender Pack opens its wearer's
 * Ender Chest, worn or set down. Without Crafting the upgrades use the vanilla-material recipes, with it Crafting's.
 */
@GameTestHolder(InventoryMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InventoryPackTierGameTests {
    private static final String CRAFTING_MOD_ID = InventoryCraftingGameTests.CRAFTING_MOD_ID;
    private static final List<PackKind> TIERS_PAST_THE_FRAME_PACK = List.of(PackKind.EXPLORERS_PACK, PackKind.EXPEDITION_PACK, PackKind.NETHERITE_PACK);
    private static final double SPEED_TOLERANCE = 0.0001;
    private static final int APPLE_COUNT = 5;
    private static final int BLUE = 0x3355CC;

    private InventoryPackTierGameTests() {
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void everyTierHoldsMoreWithoutSlowingYouDown(GameTestHelper helper) {
        helper.assertTrue(PackKind.EXPLORERS_PACK.slotCount() >= PackKind.FRAME_PACK.slotCount(), "The Explorer's Pack holds as much as the Frame Pack");
        helper.assertTrue(PackKind.EXPEDITION_PACK.slotCount() > PackKind.EXPLORERS_PACK.slotCount(), "The Expedition Pack holds more");
        helper.assertTrue(PackKind.NETHERITE_PACK.slotCount() > PackKind.EXPEDITION_PACK.slotCount(), "The Netherite Pack holds the most");
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            double normalSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
            for (PackKind kind : TIERS_PAST_THE_FRAME_PACK) {
                WornSlots.set(player, WornOn.BACK, new ItemStack(InventoryItems.pack(kind)));
                PackEffects.tick(player);
                helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.MOVEMENT_SPEED) - normalSpeed) < SPEED_TOLERANCE, kind + " doesn't slow walking");
            }
            WornSlots.set(player, WornOn.BACK, new ItemStack(InventoryItems.pack(PackKind.NETHERITE_PACK)));
            PackMenus.openWornPack(player, WornOn.BACK);
            helper.assertTrue(player.containerMenu instanceof PackMenu menu && menu.packSlotCount() == PackKind.NETHERITE_PACK.slotCount(),
                    "The worn Netherite Pack opens with all 36 slots");
            player.closeContainer();
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void upgradesKeepTheContentsAndDyeUpToNetherite(GameTestHelper helper) {
        ItemStack framePack = blueFramePackOfApples();
        ItemStack explorersPack = craftUpgrade(helper, "explorers_pack",
                List.of(new ItemStack(Items.PHANTOM_MEMBRANE), ItemStack.EMPTY, new ItemStack(Items.PHANTOM_MEMBRANE),
                        new ItemStack(Items.LEATHER), framePack, new ItemStack(Items.LEATHER)),
                () -> List.of(framePack, new ItemStack(Items.PHANTOM_MEMBRANE), new ItemStack(Items.PHANTOM_MEMBRANE),
                        crafting("leather_panel"), crafting("leather_panel"), crafting("twine"), crafting("bone_needle")));
        helper.assertTrue(explorersPack.is(InventoryItems.pack(PackKind.EXPLORERS_PACK)), "Phantom membranes make the Explorer's Pack");
        helper.assertTrue(InventoryPackGameTests.keepsTheSatchelsDyeAndApples(explorersPack), "It keeps the Frame Pack's dye and contents");
        ItemStack expeditionPack = craftUpgrade(helper, "expedition_pack",
                List.of(new ItemStack(Items.DIAMOND), new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.DIAMOND),
                        new ItemStack(Items.LEATHER), explorersPack, new ItemStack(Items.LEATHER)),
                () -> List.of(explorersPack, new ItemStack(Items.DIAMOND), new ItemStack(Items.DIAMOND), new ItemStack(Items.GOLD_INGOT),
                        crafting("leather_panel"), crafting("leather_panel"), crafting("twine"), crafting("bone_needle")));
        helper.assertTrue(expeditionPack.is(InventoryItems.pack(PackKind.EXPEDITION_PACK)), "Diamonds and gold make the Expedition Pack");
        helper.assertTrue(InventoryPackGameTests.keepsTheSatchelsDyeAndApples(expeditionPack), "It keeps the dye and contents");
        SimpleContainer smithingTable = new SimpleContainer(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), expeditionPack,
                new ItemStack(Items.NETHERITE_INGOT));
        SmithingRecipe smithing = recipe(helper, ResourceIds.of(InventoryMod.MOD_ID, "netherite_pack_smithing"), SmithingRecipe.class);
        helper.assertTrue(smithing.matches(smithingTable, helper.getLevel()), "The smithing table upgrades an Expedition Pack with netherite");
        ItemStack netheritePack = smithing.assemble(smithingTable, helper.getLevel().registryAccess());
        helper.assertTrue(netheritePack.is(InventoryItems.pack(PackKind.NETHERITE_PACK)), "The result is a Netherite Pack");
        helper.assertTrue(InventoryPackGameTests.keepsTheSatchelsDyeAndApples(netheritePack), "Smithing keeps the dye and contents too");
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void theNetheritePackSurvivesFireAndLava(GameTestHelper helper) {
        ItemStack netheritePack = new ItemStack(InventoryItems.pack(PackKind.NETHERITE_PACK));
        helper.assertTrue(netheritePack.getItem().isFireResistant(), "The Netherite Pack is fire resistant, like netherite gear");
        BlockPos above = helper.absolutePos(BlockPos.ZERO.above());
        ItemEntity dropped = new ItemEntity(helper.getLevel(), above.getX() + 0.5, above.getY(), above.getZ() + 0.5, netheritePack);
        try {
            helper.assertTrue(dropped.fireImmune(), "Dropped, it doesn't burn in lava");
        } finally {
            dropped.discard();
        }
        helper.assertFalse(new ItemStack(InventoryItems.pack(PackKind.EXPEDITION_PACK)).getItem().isFireResistant(), "The packs below it still burn");
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void theEnderPackOpensYourEnderChest(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        BlockPos placedAt = helper.absolutePos(BlockPos.ZERO.above());
        try {
            WornSlots.set(player, WornOn.BACK, new ItemStack(InventoryItems.pack(PackKind.ENDER_PACK)));
            PackMenus.openWornPack(player, WornOn.BACK);
            helper.assertTrue(isEnderChestOpen(player), "The worn Ender Pack opens the wearer's Ender Chest");
            player.closeContainer();
            helper.assertTrue(WornPack.placeInEmptySlots(player, new ItemStack(Items.APPLE)) == 0, "Pickups don't go into the Ender Chest");
            helper.setBlock(BlockPos.ZERO, Blocks.STONE);
            PlacedPacks.place(helper.getLevel(), placedAt, Direction.SOUTH, new ItemStack(InventoryItems.pack(PackKind.ENDER_PACK)));
            helper.assertTrue(helper.getLevel().getBlockEntity(placedAt) instanceof PlacedPackBlockEntity, "The Ender Pack sets down");
            PackMenus.openPlacedPack(player, (PlacedPackBlockEntity) helper.getLevel().getBlockEntity(placedAt));
            helper.assertTrue(isEnderChestOpen(player), "Set down, it opens the opener's Ender Chest, like an ender chest block");
            player.closeContainer();
        } finally {
            helper.getLevel().removeBlock(placedAt, false);
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    private static boolean isEnderChestOpen(ServerPlayer player) {
        return player.containerMenu instanceof ChestMenu chest && chest.getContainer() == player.getEnderChestInventory();
    }

    private static ItemStack blueFramePackOfApples() {
        ItemStack framePack = new ItemStack(InventoryItems.framePack());
        InventoryItems.framePack().setColor(framePack, BLUE);
        NonNullList<ItemStack> slots = NonNullList.withSize(PackRules.MAXIMUM_SLOTS, ItemStack.EMPTY);
        slots.set(0, new ItemStack(Items.APPLE, APPLE_COUNT));
        PackContents.write(framePack, slots);
        return framePack;
    }

    // Without Crafting the vanilla-material recipe (laid out row by row in a 3x3 grid), with it Crafting's sewn version.
    private static ItemStack craftUpgrade(GameTestHelper helper, String packName, List<ItemStack> aloneGrid,
                                          Supplier<List<ItemStack>> craftingGrid) {
        boolean hasCrafting = ImpactsLib.isLoaded(CRAFTING_MOD_ID);
        String recipeName = hasCrafting ? packName + "_with_" + CRAFTING_MOD_ID : packName;
        CraftingRecipe recipe = recipe(helper, ResourceIds.of(InventoryMod.MOD_ID, recipeName), CraftingRecipe.class);
        CraftingContainer grid = TestCraftingGrids.holding(hasCrafting ? craftingGrid.get() : aloneGrid);
        helper.assertTrue(recipe.matches(grid, helper.getLevel()), "The " + recipeName + " recipe matches its ingredients");
        return recipe.assemble(grid, helper.getLevel().registryAccess());
    }

    private static <T extends Recipe<?>> T recipe(GameTestHelper helper, ResourceLocation recipeId, Class<T> type) {
        Recipe<?> loaded = helper.getLevel().getRecipeManager().byKey(recipeId).orElseThrow(() -> new IllegalStateException("No recipe " + recipeId));
        return type.cast(loaded);
    }

    private static ItemStack crafting(String itemPath) {
        ResourceLocation itemId = ResourceIds.of(CRAFTING_MOD_ID, itemPath);
        Item item = ResourceIds.item(itemId).orElseThrow(() -> new IllegalStateException("Crafting has no item " + itemId));
        return new ItemStack(item);
    }
}
