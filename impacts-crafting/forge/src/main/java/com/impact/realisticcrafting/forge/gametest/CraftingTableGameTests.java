package com.impact.realisticcrafting.forge.gametest;

import com.impact.lib.ResourceIds;
import com.impact.lib.gametest.ConfigOverrides;
import com.impact.lib.gametest.MockPlayers;
import com.impact.lib.item.UsesDurability;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.block.MakeshiftTableBlock;
import com.impact.realisticcrafting.block.MakeshiftTableBlockEntity;
import com.impact.realisticcrafting.block.TableWear;
import com.impact.realisticcrafting.config.CraftingCommonConfig;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.config.OverhaulCategory;
import com.impact.realisticcrafting.forge.condition.OverhaulEnabledCondition;
import com.impact.realisticcrafting.menu.CraftingStation;
import com.impact.realisticcrafting.menu.CraftingTier;
import com.impact.realisticcrafting.menu.TableTierCheck;
import com.impact.realisticcrafting.menu.TieredCraftingMenu;
import com.impact.realisticcrafting.recipe.TierResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/** Crafting milestone 2: the tier tags and resolver, every station's tier check, and the makeshift tables' wear. */
@GameTestHolder(CraftingMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CraftingTableGameTests {
    private static final String LIB_NAMESPACE = CraftingSkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = CraftingSkeletonGameTests.EMPTY_TEMPLATE;
    private static final int RESULT_SLOT = 0;
    private static final int TEST_CONTAINER_ID = 1;
    private static final int PLANKS_PER_BARE_LOG = 1;
    private static final int LONG_ENOUGH_FOR_THREE_CRAFTS = 100;

    private CraftingTableGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void itemsGetTheirTierFromTheTags(GameTestHelper helper) {
        assertTier(helper, Items.IRON_INGOT, CraftingTier.STONE);
        assertTier(helper, Items.IRON_NUGGET, CraftingTier.STONE);
        assertTier(helper, Items.COPPER_INGOT, CraftingTier.STONE);
        assertTier(helper, Items.RAW_IRON, CraftingTier.STONE);
        assertTier(helper, Items.REDSTONE, CraftingTier.STONE);
        assertTier(helper, Items.GOLD_INGOT, CraftingTier.ADVANCED);
        assertTier(helper, Items.DIAMOND, CraftingTier.ADVANCED);
        assertTier(helper, Items.OAK_PLANKS, CraftingTier.NON_METAL);
        assertTier(helper, Items.COAL, CraftingTier.NON_METAL);
        assertTier(helper, Items.IRON_CHESTPLATE, CraftingTier.NON_METAL);
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void configListsOverrideTheTags(GameTestHelper helper) {
        CraftingServerConfig.TierSettings tiers = CraftingServerConfig.get().tiers;
        ConfigOverrides.withValue(tiers.extraAdvanced, List.of("minecraft:iron_ingot"), () ->
                assertTier(helper, Items.IRON_INGOT, CraftingTier.ADVANCED));
        ConfigOverrides.withValue(tiers.extraStone, List.of("#minecraft:planks"), () ->
                assertTier(helper, Items.OAK_PLANKS, CraftingTier.STONE));
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void recipesTakeTheirHighestIngredient(GameTestHelper helper) {
        assertRecipeTier(helper, "impactscrafting:oak_planks_by_hand", CraftingTier.NON_METAL);
        assertRecipeTier(helper, "minecraft:iron_nugget", CraftingTier.STONE);
        assertRecipeTier(helper, "minecraft:compass", CraftingTier.STONE);
        assertRecipeTier(helper, "minecraft:clock", CraftingTier.ADVANCED);
        assertRecipeTier(helper, "minecraft:golden_apple", CraftingTier.ADVANCED);
        assertRecipeTier(helper, "minecraft:firework_rocket", CraftingTier.NON_METAL);
        assertRecipeTier(helper, "impactscrafting:makeshift_table", CraftingTier.NON_METAL);
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void stationsRefuseRecipesAboveTheirTier(GameTestHelper helper) {
        helper.assertTrue(CraftingStation.INVENTORY_GRID.refuses(CraftingTier.STONE), "The 2x2 grid refuses iron");
        helper.assertTrue(CraftingStation.MAKESHIFT_TABLE.refuses(CraftingTier.STONE), "The Makeshift Table refuses iron");
        helper.assertFalse(CraftingStation.MAKESHIFT_STONE_TABLE.refuses(CraftingTier.STONE), "The Makeshift Stone Table takes iron");
        helper.assertTrue(CraftingStation.MAKESHIFT_STONE_TABLE.refuses(CraftingTier.ADVANCED), "but not gold");
        helper.assertFalse(CraftingStation.CRAFTING_TABLE.refuses(CraftingTier.ADVANCED), "The Crafting Table takes everything");
        helper.assertTrue(CraftingStation.lowestAccepting(CraftingTier.STONE) == CraftingStation.MAKESHIFT_STONE_TABLE, "Iron needs the Stone Table");
        helper.assertTrue(CraftingStation.lowestAccepting(CraftingTier.ADVANCED) == CraftingStation.CRAFTING_TABLE, "Gold needs the Crafting Table");
        ConfigOverrides.withValue(CraftingServerConfig.get().tableTiers.isOn, false, () ->
                helper.assertFalse(CraftingStation.INVENTORY_GRID.refuses(CraftingTier.ADVANCED), "With table tiers off, nothing is refused"));
        ConfigOverrides.withValue(CraftingServerConfig.get().tableTiers.checksInventoryGrid, false, () ->
                helper.assertFalse(CraftingStation.INVENTORY_GRID.refuses(CraftingTier.STONE), "Each grid's check has its own switch"));
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void inventoryGridCraftsNonMetalOnly(GameTestHelper helper) {
        withPlayer(helper, (player, level) -> {
            player.inventoryMenu.getCraftSlots().setItem(0, new ItemStack(Items.IRON_INGOT));
            helper.assertTrue(player.inventoryMenu.getSlot(RESULT_SLOT).getItem().isEmpty(), "The 2x2 grid refuses iron nuggets");
            player.inventoryMenu.getCraftSlots().setItem(0, new ItemStack(Items.OAK_LOG));
            helper.assertTrue(player.inventoryMenu.getSlot(RESULT_SLOT).getItem().is(Items.OAK_PLANKS), "but crafts planks");
            player.inventoryMenu.getCraftSlots().clearContent();
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void eachTableCraftsUpToItsTier(GameTestHelper helper) {
        withPlayer(helper, (player, level) -> {
            BlockPos woodenPosition = placeTable(helper, MakeshiftTable.WOODEN, BlockPos.ZERO);
            BlockPos stonePosition = placeTable(helper, MakeshiftTable.STONE, BlockPos.ZERO.east());
            try {
                TieredCraftingMenu woodenTable = openTable(player, woodenPosition, MakeshiftTable.WOODEN);
                woodenTable.grid().setItem(0, new ItemStack(Items.IRON_INGOT));
                helper.assertTrue(woodenTable.getSlot(RESULT_SLOT).getItem().isEmpty(), "The Makeshift Table refuses iron");
                TieredCraftingMenu stoneTable = openTable(player, stonePosition, MakeshiftTable.STONE);
                stoneTable.grid().setItem(0, new ItemStack(Items.IRON_INGOT));
                helper.assertTrue(stoneTable.getSlot(RESULT_SLOT).getItem().is(Items.IRON_NUGGET), "The Makeshift Stone Table crafts iron");
                stoneTable.grid().setItem(0, new ItemStack(Items.GOLD_INGOT));
                helper.assertTrue(stoneTable.getSlot(RESULT_SLOT).getItem().isEmpty(), "but refuses gold");
                Recipe<?> goldenApple = recipe(helper, "minecraft:golden_apple");
                helper.assertTrue(TableTierCheck.neededStation(stoneTable, goldenApple).equals(Optional.of(CraftingStation.CRAFTING_TABLE)),
                        "A refused golden apple names the Crafting Table");
                CraftingMenu craftingTable = new CraftingMenu(TEST_CONTAINER_ID, player.getInventory(), ContainerLevelAccess.create(level, stonePosition));
                craftingTable.getSlot(1).set(new ItemStack(Items.GOLD_INGOT));
                helper.assertTrue(craftingTable.getSlot(RESULT_SLOT).getItem().is(Items.GOLD_NUGGET), "The Crafting Table crafts gold");
                woodenTable.grid().clearContent();
                stoneTable.grid().clearContent();
                craftingTable.getSlot(1).set(ItemStack.EMPTY);
            } finally {
                removeTable(level, woodenPosition);
                removeTable(level, stonePosition);
            }
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void eachCraftTakenWearsTheTable(GameTestHelper helper) {
        withPlayer(helper, (player, level) -> {
            BlockPos position = placeTable(helper, MakeshiftTable.WOODEN, BlockPos.ZERO);
            try {
                TieredCraftingMenu table = openTable(player, position, MakeshiftTable.WOODEN);
                player.containerMenu = table;
                table.grid().setItem(0, new ItemStack(Items.OAK_LOG, 4));
                CraftingTestGrids.finishCraft(table, player);
                table.clicked(RESULT_SLOT, 0, ClickType.PICKUP, player);
                helper.assertTrue(table.getCarried().getCount() == PLANKS_PER_BARE_LOG, "Taking the result gives a bare log's 1 planks");
                helper.assertTrue(tableEntity(level, position).usesSpent() == 1, "and spends one of the table's uses");
                table.setCarried(ItemStack.EMPTY);
                CraftingTestGrids.finishCraft(table, player);
                table.clicked(RESULT_SLOT, 0, ClickType.QUICK_MOVE, player);
                CraftingTestGrids.runTicks(player, LONG_ENOUGH_FOR_THREE_CRAFTS);
                helper.assertTrue(tableEntity(level, position).usesSpent() == 4, "Shift-clicking spends one use per craft (3 more logs)");
                helper.assertTrue(player.getInventory().countItem(Items.OAK_PLANKS) == 3 * PLANKS_PER_BARE_LOG, "and moves every craft to the inventory");
            } finally {
                player.containerMenu = player.inventoryMenu;
                player.getInventory().clearContent();
                removeTable(level, position);
            }
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void aWornOutTableFallsApart(GameTestHelper helper) {
        withPlayer(helper, (player, level) -> {
            BlockPos position = placeTable(helper, MakeshiftTable.WOODEN, BlockPos.ZERO);
            try {
                tableEntity(level, position).setUsesSpent(MakeshiftTable.WOODEN.maxUses() - 1);
                TieredCraftingMenu table = openTable(player, position, MakeshiftTable.WOODEN);
                table.grid().setItem(0, new ItemStack(Items.OAK_LOG, 2));
                CraftingTestGrids.finishCraft(table, player);
                table.clicked(RESULT_SLOT, 0, ClickType.PICKUP, player);
                helper.assertTrue(level.getBlockState(position).isAir(), "The last use breaks the table");
                helper.assertTrue(table.grid().isEmpty() && table.getSlot(RESULT_SLOT).getItem().isEmpty(), "Its grid is emptied");
                List<ItemEntity> dropped = level.getEntitiesOfClass(ItemEntity.class, new AABB(position).inflate(1.0));
                helper.assertTrue(dropped.size() == 1 && dropped.get(0).getItem().is(Items.OAK_LOG), "The grid's log drops on the ground, and nothing else");
                dropped.forEach(ItemEntity::discard);
                table.setCarried(ItemStack.EMPTY);
            } finally {
                removeTable(level, position);
            }
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void wearShowsInThreeStages(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos position = placeTable(helper, MakeshiftTable.WOODEN, BlockPos.ZERO);
        try {
            MakeshiftTableBlockEntity table = tableEntity(level, position);
            helper.assertTrue(wear(level, position) == TableWear.NEW, "A new table looks new");
            table.setUsesSpent(50);
            helper.assertTrue(wear(level, position) == TableWear.WORN, "Below 2/3 of its uses it looks worn");
            table.setUsesSpent(100);
            helper.assertTrue(wear(level, position) == TableWear.FALLING_APART, "Below 1/3 it looks like it's falling apart");
        } finally {
            removeTable(level, position);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void pickedUpTablesKeepTheirUses(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos position = placeTable(helper, MakeshiftTable.STONE, BlockPos.ZERO);
        try {
            ItemStack freshDrop = dropOf(level, position);
            helper.assertFalse(freshDrop.hasTag(), "An unused table's item has no tag, so fresh tables stack");
            tableEntity(level, position).setUsesSpent(10);
            ItemStack wornDrop = dropOf(level, position);
            helper.assertTrue(UsesDurability.usesSpent(wornDrop) == 10, "A used table's item keeps its uses spent");
            helper.assertTrue(UsesDurability.get(wornDrop) == MakeshiftTable.STONE.maxUses() - 10, "and shows the uses left");
            helper.assertTrue(wornDrop.getMaxStackSize() == 1, "A used table stacks to 1");
            BlockPos placedAgain = helper.absolutePos(BlockPos.ZERO.east());
            level.setBlockAndUpdate(placedAgain, MakeshiftTable.STONE.block().defaultBlockState());
            MakeshiftTable.STONE.block().setPlacedBy(level, placedAgain, level.getBlockState(placedAgain), null, wornDrop);
            helper.assertTrue(tableEntity(level, placedAgain).usesSpent() == 10, "Placing it again doesn't repair it");
            removeTable(level, placedAgain);
        } finally {
            removeTable(level, position);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void theMakeshiftTableReplacesTheCraftingTableRecipe(GameTestHelper helper) {
        helper.assertTrue(findRecipe(helper, "impactscrafting:makeshift_table").isPresent(), "4 planks make a Makeshift Table");
        helper.assertTrue(findRecipe(helper, "impactscrafting:makeshift_stone_table").isPresent(), "Flint on smooth stone makes the Stone Table");
        helper.assertTrue(findRecipe(helper, "minecraft:crafting_table").isEmpty(), "The vanilla Crafting Table recipe is switched off");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void recipeListsSwitchSingleRecipes(GameTestHelper helper) {
        ResourceLocation makeshiftTable = ResourceIds.of(CraftingMod.MOD_ID, MakeshiftTable.WOODEN.path);
        ResourceLocation craftingTable = ResourceIds.of("minecraft", "crafting_table");
        CraftingCommonConfig config = CraftingCommonConfig.get();
        helper.assertTrue(new OverhaulEnabledCondition(OverhaulCategory.WOOD, makeshiftTable).test(ICondition.IContext.EMPTY), "On by default");
        ConfigOverrides.withValue(config.disabledRecipes, List.of(makeshiftTable.toString()), () ->
                helper.assertFalse(new OverhaulEnabledCondition(OverhaulCategory.WOOD, makeshiftTable).test(ICondition.IContext.EMPTY),
                        "recipes.disabled drops a Crafting recipe"));
        ConfigOverrides.withValue(config.keptVanillaRecipes, List.of(craftingTable.toString()), () ->
                helper.assertFalse(new OverhaulEnabledCondition(OverhaulCategory.WOOD, craftingTable).test(ICondition.IContext.EMPTY),
                        "recipes.keepVanilla brings a vanilla recipe back (its override's not-condition passes)"));
        helper.succeed();
    }

    private static void withPlayer(GameTestHelper helper, BiConsumer<ServerPlayer, ServerLevel> check) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            check.accept(player, helper.getLevel());
        } finally {
            MockPlayers.leaveWorld(player);
        }
    }

    private static void assertTier(GameTestHelper helper, Item item, CraftingTier expected) {
        CraftingTier actual = TierResolver.tierOf(new ItemStack(item));
        helper.assertTrue(actual == expected, ResourceIds.itemId(item) + " should be " + expected.serializedName + ", was " + actual.serializedName);
    }

    private static void assertRecipeTier(GameTestHelper helper, String recipeId, CraftingTier expected) {
        CraftingTier actual = TierResolver.tierOf(recipe(helper, recipeId));
        helper.assertTrue(actual == expected, recipeId + " should be " + expected.serializedName + ", was " + actual.serializedName);
    }

    private static Recipe<?> recipe(GameTestHelper helper, String recipeId) {
        return findRecipe(helper, recipeId).orElseThrow(() -> new IllegalStateException("Missing recipe " + recipeId));
    }

    private static Optional<? extends Recipe<?>> findRecipe(GameTestHelper helper, String recipeId) {
        return helper.getLevel().getRecipeManager().byKey(ResourceIds.tryParse(recipeId).orElseThrow());
    }

    private static BlockPos placeTable(GameTestHelper helper, MakeshiftTable table, BlockPos relativePosition) {
        BlockPos position = helper.absolutePos(relativePosition);
        helper.getLevel().setBlockAndUpdate(position, table.block().defaultBlockState());
        return position;
    }

    private static void removeTable(ServerLevel level, BlockPos position) {
        level.setBlockAndUpdate(position, Blocks.AIR.defaultBlockState());
    }

    private static TieredCraftingMenu openTable(ServerPlayer player, BlockPos position, MakeshiftTable table) {
        return new TieredCraftingMenu(TEST_CONTAINER_ID, player.getInventory(), ContainerLevelAccess.create(player.level(), position), table);
    }

    private static MakeshiftTableBlockEntity tableEntity(ServerLevel level, BlockPos position) {
        return (MakeshiftTableBlockEntity) level.getBlockEntity(position);
    }

    private static int wear(ServerLevel level, BlockPos position) {
        return level.getBlockState(position).getValue(MakeshiftTableBlock.WEAR);
    }

    private static ItemStack dropOf(ServerLevel level, BlockPos position) {
        List<ItemStack> drops = Block.getDrops(level.getBlockState(position), level, position, level.getBlockEntity(position));
        return drops.isEmpty() ? ItemStack.EMPTY : drops.get(0);
    }
}
