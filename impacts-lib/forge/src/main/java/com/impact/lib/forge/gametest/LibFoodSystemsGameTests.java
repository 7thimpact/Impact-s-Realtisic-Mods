package com.impact.lib.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.crafting.Byproduct;
import com.impact.lib.crafting.CraftRemainder;
import com.impact.lib.crafting.ToolCraftingRecipe;
import com.impact.lib.event.ImpactsEvents;
import com.impact.lib.event.WaterFilled;
import com.impact.lib.forge.crafting.WaterBottleIngredient;
import com.impact.lib.fuel.FuelRegistry;
import com.impact.lib.gametest.MockPlayers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Lib's food slice: tool crafting, the water bottle ingredient, heat sources, fuel, boiling and the water-filled event. */
@GameTestHolder(ImpactsLib.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibFoodSystemsGameTests {
    private static final String EMPTY_TEMPLATE = "empty";
    private static final int GRID_SIZE = 3;
    private static final int TOOL_SLOT = 4;
    private static final int FOOD_SLOT = 0;
    private static final int TEST_BURN_TICKS = 50;
    private static final BlockPos HEAT_TEST_POSITION = BlockPos.ZERO;

    private LibFoodSystemsGameTests() {
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void toolsStayInTheGridAndTakeDamage(GameTestHelper helper) {
        ToolCraftingRecipe cutting = cuttingRecipe(List.of());
        CraftingContainer grid = gridWith(new ItemStack(Items.BEEF), new ItemStack(Items.IRON_SWORD));
        helper.assertTrue(cutting.matches(grid, helper.getLevel()), "Beef and a tool in any slot match");
        helper.assertTrue(cutting.assemble(grid, helper.getLevel().registryAccess()).is(Items.COOKED_BEEF), "The result comes out");
        NonNullList<ItemStack> remaining = cutting.getRemainingItems(grid);
        helper.assertTrue(remaining.get(TOOL_SLOT).is(Items.IRON_SWORD) && remaining.get(TOOL_SLOT).getDamageValue() == 1,
                "The tool comes back with 1 damage");
        helper.assertTrue(remaining.get(FOOD_SLOT).isEmpty(), "The beef is used up");
        helper.assertFalse(cutting.matches(gridWith(new ItemStack(Items.BEEF), ItemStack.EMPTY), helper.getLevel()), "No tool, no match");
        helper.assertFalse(cutting.matches(gridWith(new ItemStack(Items.BEEF), new ItemStack(Items.DIRT)), helper.getLevel()),
                "An item without durability is never a tool");
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void byproductsGoToTheCraftingPlayer(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            ToolCraftingRecipe cutting = cuttingRecipe(List.of(new Byproduct(new ItemStack(Items.BONE), 1.0F)));
            ForgeHooks.setCraftingPlayer(player);
            cutting.getRemainingItems(gridWith(new ItemStack(Items.BEEF), new ItemStack(Items.IRON_SWORD)));
            helper.assertTrue(player.getInventory().countItem(Items.BONE) == 1, "The crafting player gets the bone");
        } finally {
            ForgeHooks.setCraftingPlayer(null);
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void remaindersAndNbtCopyFollowTheRecipe(GameTestHelper helper) {
        ItemStack waterBottle = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        ItemStack namedPaper = new ItemStack(Items.PAPER);
        namedPaper.getOrCreateTag().putString("impactslibtest:mark", "kept");
        ShapelessRecipe shape = new ShapelessRecipe(ResourceIds.of(ImpactsLib.MOD_ID, "test_dough"), "", CraftingBookCategory.MISC,
                new ItemStack(Items.BOOK), NonNullList.of(Ingredient.EMPTY, WaterBottleIngredient.INSTANCE, Ingredient.of(Items.PAPER)));
        ToolCraftingRecipe recipe = new ToolCraftingRecipe(shape, List.of(), 1, List.of(),
                List.of(new CraftRemainder(WaterBottleIngredient.INSTANCE, new ItemStack(Items.GLASS_BOTTLE))), ToolCraftingRecipe.NO_CRAFT_TICKS,
                Optional.of(Ingredient.of(Items.PAPER)));
        CraftingContainer grid = gridWith(waterBottle, namedPaper);
        helper.assertTrue(recipe.matches(grid, helper.getLevel()), "A water bottle and paper match");
        CompoundTag resultTag = recipe.assemble(grid, helper.getLevel().registryAccess()).getTag();
        helper.assertTrue(resultTag != null && "kept".equals(resultTag.getString("impactslibtest:mark")), "The result takes the paper's NBT");
        helper.assertTrue(recipe.getRemainingItems(grid).get(FOOD_SLOT).is(Items.GLASS_BOTTLE), "The water bottle leaves a glass bottle");
        ItemStack awkwardPotion = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD);
        helper.assertFalse(WaterBottleIngredient.INSTANCE.test(awkwardPotion), "Other potions aren't water");
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void heatSourcesCountOnlyWhileLit(GameTestHelper helper) {
        BlockPos position = helper.absolutePos(HEAT_TEST_POSITION);
        helper.setBlock(HEAT_TEST_POSITION, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        helper.assertTrue(ImpactsLib.isHeated(helper.getLevel(), position), "A lit campfire heats");
        helper.setBlock(HEAT_TEST_POSITION, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
        helper.assertFalse(ImpactsLib.isHeated(helper.getLevel(), position), "An unlit campfire doesn't");
        helper.setBlock(HEAT_TEST_POSITION, Blocks.MAGMA_BLOCK);
        helper.assertTrue(ImpactsLib.isHeated(helper.getLevel(), position), "Magma heats");
        helper.setBlock(HEAT_TEST_POSITION, Blocks.STONE);
        helper.assertFalse(ImpactsLib.isHeated(helper.getLevel(), position), "Stone doesn't");
        helper.setBlock(HEAT_TEST_POSITION, Blocks.AIR);
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void fuelBoilingAndWaterFilledReachTheirListeners(GameTestHelper helper) {
        FuelRegistry.register(Items.STRUCTURE_VOID, TEST_BURN_TICKS);
        helper.assertTrue(ForgeHooks.getBurnTime(new ItemStack(Items.STRUCTURE_VOID), null) == TEST_BURN_TICKS, "Furnaces see Lib's burn time");
        BlockPos boilingPosition = helper.absolutePos(HEAT_TEST_POSITION);
        helper.assertFalse(ImpactsLib.isBoiling(helper.getLevel(), boilingPosition), "Nothing boils without a provider saying so");
        ImpactsLib.registerBoilingProvider("impactslibtest", (level, position) -> position.equals(boilingPosition));
        helper.assertTrue(ImpactsLib.isBoiling(helper.getLevel(), boilingPosition), "A provider's answer is used");
        List<WaterFilled> received = new ArrayList<>();
        ImpactsEvents.onWaterFilled(received::add);
        ImpactsEvents.postWaterFilled(new WaterFilled(null, new ItemStack(Items.WATER_BUCKET), helper.getLevel(), boilingPosition, true));
        helper.assertTrue(received.size() == 1 && received.get(0).boiled(), "Listeners get the water-filled event");
        helper.succeed();
    }

    private static ToolCraftingRecipe cuttingRecipe(List<Byproduct> byproducts) {
        ShapelessRecipe shape = new ShapelessRecipe(ResourceIds.of(ImpactsLib.MOD_ID, "test_cutting"), "", CraftingBookCategory.MISC,
                new ItemStack(Items.COOKED_BEEF), NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.BEEF)));
        return new ToolCraftingRecipe(shape, List.of(Ingredient.of(Items.IRON_SWORD)), 1, byproducts, List.of(),
                ToolCraftingRecipe.NO_CRAFT_TICKS, Optional.empty());
    }

    private static CraftingContainer gridWith(ItemStack first, ItemStack second) {
        NonNullList<ItemStack> items = NonNullList.withSize(GRID_SIZE * GRID_SIZE, ItemStack.EMPTY);
        items.set(FOOD_SLOT, first);
        items.set(TOOL_SLOT, second);
        return new TransientCraftingContainer(new UnshownMenu(), GRID_SIZE, GRID_SIZE, items);
    }

    private static final class UnshownMenu extends AbstractContainerMenu {
        UnshownMenu() {
            super(null, -1);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slotIndex) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return false;
        }
    }
}
