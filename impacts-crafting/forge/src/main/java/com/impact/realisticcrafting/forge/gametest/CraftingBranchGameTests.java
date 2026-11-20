package com.impact.realisticcrafting.forge.gametest;

import com.impact.lib.ResourceIds;
import com.impact.lib.event.ImpactsEvents;
import com.impact.lib.gametest.MockPlayers;
import com.impact.lib.item.UsesDurability;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.wood.WoodData;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.CraftingTool;
import com.impact.realisticcrafting.item.WoodPart;
import com.impact.realisticcrafting.registry.CraftingItems;
import com.impact.realisticcrafting.timing.CraftDepthTable;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.atTable;
import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.craft;

/** Crafting milestone 6: the wood and stone branches, both Wooden Buckets, and the non-iron catalog rows. */
@GameTestHolder(CraftingMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CraftingBranchGameTests {
    private static final String LIB_NAMESPACE = CraftingSkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = CraftingSkeletonGameTests.EMPTY_TEMPLATE;
    private static final ResourceLocation OAK = ResourceIds.of("minecraft", "oak");
    private static final ResourceLocation BIRCH = ResourceIds.of("minecraft", "birch");
    private static final ResourceLocation BAMBOO = ResourceIds.of("minecraft", "bamboo");
    private static final ItemStack NO_ITEM = ItemStack.EMPTY;
    private static final AtomicInteger WATER_FILLED_EVENTS = new AtomicInteger();
    private static final String OTHER_MODS_KEY = "impactsvitality:purity";

    static {
        ImpactsEvents.onWaterFilled(event -> {
            if (event.filledStack().is(CraftingItems.woodenWaterBucket())) {
                WATER_FILLED_EVENTS.incrementAndGet();
            }
        });
    }

    private CraftingBranchGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void woodPartsKeepTheirWood(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack panel = craft(table, player, planks(OAK), planks(OAK));
            helper.assertTrue(isPart(panel, WoodPart.WOODEN_PANEL, OAK), "2 oak Trimmed Planks side by side make an oak Wooden Panel");
            table.grid().clearContent();
            ItemStack frames = craft(table, player, planks(OAK), stick(), planks(OAK), stick(), NO_ITEM, stick(), planks(OAK), stick(), planks(OAK));
            helper.assertTrue(isPart(frames, WoodPart.WOODEN_FRAME, OAK) && frames.getCount() == 2, "The frame pattern makes 2 oak Wooden Frames");
            table.grid().clearContent();
            ItemStack posts = craft(table, player, planks(OAK), pebble(), NO_ITEM, planks(OAK));
            helper.assertTrue(isPart(posts, WoodPart.FENCE_POST, OAK) && posts.getCount() == 4, "2 stacked with a trimming tool make 4 oak Fence Posts");
            table.grid().clearContent();
            ItemStack doors = craft(table, player, part(WoodPart.WOODEN_PANEL, BIRCH), part(WoodPart.WOODEN_PANEL, BIRCH), part(WoodPart.WOODEN_PANEL, BIRCH),
                    hinge(), hinge());
            helper.assertTrue(doors.is(Items.BIRCH_DOOR) && doors.getCount() == 3, "Birch panels make birch doors");
            helper.assertFalse(doors.hasTag(), "and the door comes out clean, with no wood data");
            table.grid().clearContent();
            ItemStack mixed = craft(table, player, part(WoodPart.WOODEN_PANEL, BIRCH), part(WoodPart.WOODEN_PANEL, OAK), part(WoodPart.WOODEN_PANEL, BIRCH),
                    hinge(), hinge());
            helper.assertTrue(mixed.isEmpty(), "Mixed woods make nothing");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void aChestNeedsAnUnusedBucketATopAndHinges(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack bucket = craft(table, player, planks(OAK), NO_ITEM, planks(OAK), planks(OAK), planks(OAK), planks(OAK));
            helper.assertTrue(bucket.is(CraftingItems.woodenBucket()) && !bucket.hasTag(), "5 Trimmed Planks make an unused Wooden Bucket");
            table.grid().clearContent();
            ItemStack chestTop = craft(table, player, planks(OAK), planks(OAK), NO_ITEM, planks(OAK), planks(OAK));
            helper.assertTrue(chestTop.is(CraftingItems.part(CraftingPart.CHEST_TOP)), "4 Trimmed Planks make a Chest Top");
            table.grid().clearContent();
            ItemStack chest = craft(table, player, hinge(), chestTop.copy(), hinge(), NO_ITEM, bucket.copy());
            helper.assertTrue(ItemStack.isSameItemSameTags(chest, new ItemStack(Items.CHEST)), "Hinge, top, hinge over a bucket make a clean chest");
            table.grid().clearContent();
            ItemStack usedBucket = bucket.copy();
            usedBucket.getOrCreateTag().putInt(UsesDurability.USES_KEY, 1);
            ItemStack refused = craft(table, player, hinge(), chestTop.copy(), hinge(), NO_ITEM, usedBucket);
            helper.assertTrue(refused.isEmpty(), "A used bucket isn't a crafting part");
        });
        helper.assertTrue(CraftDepthTable.depthOf(Items.CHEST).orElse(-1) == 4, "A chest is depth 4 (40 ticks)");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void theBucketKeepsItsWearThroughAScoopAndAPour(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos water = helper.absolutePos(BlockPos.ZERO);
        ServerPlayer player = MockPlayers.joinWorld(level);
        int eventsBefore = WATER_FILLED_EVENTS.get();
        try {
            level.setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
            lookDownAt(player, water);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(CraftingItems.woodenBucket()));
            useHeld(player);
            ItemStack filled = player.getMainHandItem();
            helper.assertTrue(filled.is(CraftingItems.woodenWaterBucket()), "Scooping water fills the bucket");
            helper.assertTrue(UsesDurability.usesSpent(filled) == 1, "and costs 1 use");
            helper.assertTrue(level.getBlockState(water).isAir(), "taking the water source");
            helper.assertTrue(WATER_FILLED_EVENTS.get() == eventsBefore + 1, "Lib's water-filled event is posted");

            filled.getOrCreateTag().putString(OTHER_MODS_KEY, "purified");
            level.setBlockAndUpdate(water, Blocks.STONE.defaultBlockState());
            useHeld(player);
            ItemStack emptied = player.getMainHandItem();
            helper.assertTrue(emptied.is(CraftingItems.woodenBucket()) && UsesDurability.usesSpent(emptied) == 2, "Pouring costs another use");
            helper.assertFalse(emptied.getTag().contains(OTHER_MODS_KEY), "and other mods' water data leaves with the water");
            helper.assertTrue(level.getBlockState(water.above()).is(Blocks.WATER), "The water is poured on the block");

            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE));
            level.setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
            level.setBlockAndUpdate(water.above(), Blocks.AIR.defaultBlockState());
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(CraftingItems.woodenBucket(), 3));
            useHeld(player);
            helper.assertTrue(player.getMainHandItem().getCount() == 2 && player.getInventory().countItem(CraftingItems.woodenWaterBucket()) == 1,
                    "Scooping from a stack fills one bucket and keeps the rest");
        } finally {
            level.setBlockAndUpdate(water.above(), Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(water, Blocks.AIR.defaultBlockState());
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void boatsAreBuiltAndHammered(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack panel = part(WoodPart.WOODEN_PANEL, BAMBOO);
            ItemStack hull = craft(table, player, panel.copy(), NO_ITEM, panel.copy(), NO_ITEM, panel.copy());
            helper.assertTrue(isPart(hull, WoodPart.BOAT_HULL, BAMBOO), "3 bamboo panels make a bamboo Boat Hull");
            table.grid().clearContent();
            ItemStack oar = craft(table, player, planks(OAK), NO_ITEM, NO_ITEM, stick(), NO_ITEM, NO_ITEM, stick());
            helper.assertTrue(oar.is(CraftingItems.part(CraftingPart.OAR)), "Trimmed Planks on 2 Trimmed Sticks make an Oar");
            table.grid().clearContent();
            ItemStack looseBoat = craft(table, player, hull, oar.copy(), oar.copy());
            helper.assertTrue(looseBoat.is(CraftingItems.looseBoat()) && WoodData.get(looseBoat).equals(Optional.of(BAMBOO)),
                    "A hull and 2 oars make a loose bamboo boat");
            player.setItemInHand(InteractionHand.MAIN_HAND, looseBoat);
            player.setItemInHand(InteractionHand.OFF_HAND, pebble());
            player.getMainHandItem().getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
            for (int tick = 0; tick < 40 && player.isUsingItem(); tick++) {
                ItemStack held = player.getUseItem();
                held.getItem().onUseTick(player.level(), player, held, player.getUseItemRemainingTicks());
            }
            helper.assertTrue(player.getMainHandItem().is(Items.BAMBOO_RAFT), "Hammering it makes a bamboo raft");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void stoneIsCutWithAChisel(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack slabs = craft(table, player, stack(Items.STONE), stack(Items.STONE), stack(Items.STONE), chisel());
            helper.assertTrue(slabs.is(Items.STONE_SLAB) && slabs.getCount() == 6, "3 stone and a chisel make 6 slabs");
            table.grid().clearContent();
            assertSingleCut(helper, table, player, Items.GRANITE, Items.POLISHED_GRANITE);
            assertSingleCut(helper, table, player, Items.STONE, Items.STONE_BRICKS);
            assertSingleCut(helper, table, player, Items.SANDSTONE, Items.CUT_SANDSTONE);
            ItemStack chiseled = craft(table, player, stack(Items.SANDSTONE_SLAB), chisel(), NO_ITEM, stack(Items.SANDSTONE_SLAB));
            helper.assertTrue(chiseled.is(Items.CHISELED_SANDSTONE), "2 slabs stacked and a chisel make the chiseled block");
            table.grid().clearContent();
            ItemStack buttons = craft(table, player, stack(Items.STONE), NO_ITEM, NO_ITEM, NO_ITEM, stack(Items.STONE), chisel());
            helper.assertTrue(buttons.is(Items.STONE_BUTTON) && buttons.getCount() == 4, "2 diagonal stone and a chisel make 4 buttons");
            table.grid().clearContent();
            ItemStack plate = craft(table, player, stack(Items.STONE), stack(Items.STONE), chisel());
            helper.assertTrue(plate.is(Items.STONE_PRESSURE_PLATE), "2 side by side make a pressure plate");
            table.grid().clearContent();
            ItemStack walls = craft(table, player, stack(Items.COBBLESTONE), stack(Items.COBBLESTONE), stack(Items.COBBLESTONE),
                    stack(Items.COBBLESTONE), stack(Items.COBBLESTONE), stack(Items.COBBLESTONE), chisel());
            helper.assertTrue(walls.is(Items.COBBLESTONE_WALL) && walls.getCount() == 6, "6 cobblestone make 6 walls");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void mortarBricksAndStoneMachines(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack mortar = craft(table, player, stack(Items.CLAY_BALL), stack(Items.SAND), stack(Items.GRAVEL));
            helper.assertTrue(mortar.is(CraftingItems.part(CraftingPart.MORTAR)) && mortar.getCount() == 4, "Clay, sand and gravel make 4 Mortar");
            table.grid().clearContent();
            ItemStack bricks = craft(table, player, stack(Items.BRICK), stack(Items.BRICK), stack(Items.BRICK), stack(Items.BRICK), mortar.copyWithCount(1));
            helper.assertTrue(bricks.is(Items.BRICKS), "4 bricks and Mortar make a brick block");
            table.grid().clearContent();
            ItemStack shaped = new ItemStack(CraftingItems.part(CraftingPart.SHAPED_STONE));
            ItemStack casing = craft(table, player, shaped.copy(), shaped.copy(), shaped.copy(), shaped.copy(), NO_ITEM, shaped.copy(), shaped.copy(),
                    shaped.copy(), shaped.copy());
            helper.assertTrue(casing.is(CraftingItems.part(CraftingPart.STONE_CASING)), "8 Shaped Stone in a ring make a Stone Casing");
            table.grid().clearContent();
            ItemStack furnace = craft(table, player, casing, hinge());
            helper.assertTrue(furnace.is(Items.FURNACE), "A casing and a hinge make a furnace");
            table.grid().clearContent();
            ItemStack wornPebble = pebble();
            wornPebble.getOrCreateTag().putInt(UsesDurability.USES_KEY, 5);
            ItemStack cobblestone = craft(table, player, wornPebble, pebble(), pebble(), pebble(), pebble(), pebble(), pebble(), pebble(), pebble());
            helper.assertTrue(cobblestone.is(Items.COBBLESTONE), "9 pebbles, worn ones too, make cobblestone");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void catalogRowsUseTheSharedParts(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack arrowheads = craft(table, player, stack(Items.FLINT), pebble());
            helper.assertTrue(arrowheads.is(CraftingItems.part(CraftingPart.FLINT_ARROWHEAD)) && arrowheads.getCount() == 2,
                    "Flint and a pebble make 2 Flint Arrowheads (the pebble is the tool, not the flint)");
            table.grid().clearContent();
            ItemStack strips = craft(table, player, stack(Items.LEATHER), pebble());
            helper.assertTrue(strips.is(CraftingItems.part(CraftingPart.LEATHER_STRIP)) && strips.getCount() == 3, "Leather and a pebble make 3 strips");
            table.grid().clearContent();
            ItemStack frame = part(WoodPart.WOODEN_FRAME, OAK);
            ItemStack itemFrames = craft(table, player, frame.copy(), strips.copyWithCount(1));
            helper.assertTrue(itemFrames.is(Items.ITEM_FRAME) && itemFrames.getCount() == 2, "A Wooden Frame and a Leather Strip make 2 item frames");
            table.grid().clearContent();
            ItemStack fletching = craft(table, player, frame.copy(), arrowheads.copyWithCount(1), arrowheads.copyWithCount(1));
            helper.assertTrue(fletching.is(Items.FLETCHING_TABLE), "A frame and 2 arrowheads make a fletching table");
            table.grid().clearContent();
            ItemStack ladders = craft(table, player, stick(), NO_ITEM, stick(), stick(), stack(CraftingItems.part(CraftingPart.TWINE)), stick(), stick(), NO_ITEM,
                    stick());
            helper.assertTrue(ladders.is(Items.LADDER) && ladders.getCount() == 3, "Trimmed Sticks and Twine make 3 ladders");
            table.grid().clearContent();
            ItemStack lever = craft(table, player, hinge(), stick());
            helper.assertTrue(lever.is(Items.LEVER), "A Stone Hinge and a Trimmed Stick make a lever");
        });
        for (String vanillaRecipe : List.of("chest", "oak_door", "stone_slab", "furnace", "bricks", "ladder", "oak_boat")) {
            helper.assertTrue(helper.getLevel().getRecipeManager().byKey(ResourceIds.of("minecraft", vanillaRecipe)).isEmpty(),
                    "Vanilla's " + vanillaRecipe + " recipe is switched off");
        }
        helper.assertTrue(helper.getLevel().getRecipeManager().byKey(ResourceIds.of("minecraft", "stone_slab_from_stone_stonecutting")).isPresent(),
                "The stonecutter still works");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void woodenBranchPartsBurn(GameTestHelper helper) {
        assertBurns(helper, part(WoodPart.WOODEN_PANEL, OAK), 300);
        assertBurns(helper, part(WoodPart.WOODEN_FRAME, OAK), 400);
        assertBurns(helper, part(WoodPart.FENCE_POST, OAK), 150);
        assertBurns(helper, part(WoodPart.BOAT_HULL, OAK), 900);
        assertBurns(helper, part(WoodPart.WOODEN_PANEL, ResourceIds.of("minecraft", "crimson")), 0);
        assertBurns(helper, stack(CraftingItems.part(CraftingPart.CHEST_TOP)), 600);
        assertBurns(helper, stack(CraftingItems.woodenBucket()), 600);
        assertBurns(helper, stack(CraftingItems.woodenWaterBucket()), 0);
        assertBurns(helper, stack(CraftingItems.part(CraftingPart.MORTAR)), 0);
        helper.succeed();
    }

    private static void assertSingleCut(GameTestHelper helper, AbstractContainerMenu table, ServerPlayer player, Item block,
                                        Item expected) {
        ItemStack cut = craft(table, player, stack(block), chisel());
        helper.assertTrue(cut.is(expected), ResourceIds.itemId(block) + " and a chisel should make " + ResourceIds.itemId(expected));
        for (int slot = 1; slot <= 9; slot++) {
            table.getSlot(slot).set(ItemStack.EMPTY);
        }
    }

    private static void assertBurns(GameTestHelper helper, ItemStack stack, int expected) {
        int ticks = LoaderPlatform.current().burnTicks(stack);
        helper.assertTrue(ticks == expected, stack.getHoverName().getString() + " should burn " + expected + ", burns " + ticks);
    }

    private static void lookDownAt(ServerPlayer player, BlockPos target) {
        player.moveTo(target.getX() + 0.5, target.getY() + 2.0, target.getZ() + 0.5, 0.0F, 90.0F);
    }

    private static void useHeld(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        player.setItemInHand(InteractionHand.MAIN_HAND, held.getItem().use(player.level(), player, InteractionHand.MAIN_HAND).getObject());
    }

    private static boolean isPart(ItemStack stack, WoodPart part, ResourceLocation wood) {
        return stack.is(CraftingItems.woodPart(part)) && WoodData.get(stack).equals(Optional.of(wood));
    }

    private static ItemStack part(WoodPart part, ResourceLocation wood) {
        return WoodData.of(CraftingItems.woodPart(part), wood);
    }

    private static ItemStack planks(ResourceLocation wood) {
        return part(WoodPart.TRIMMED_PLANKS, wood);
    }

    private static ItemStack stick() {
        return stack(CraftingItems.part(CraftingPart.TRIMMED_STICK));
    }

    private static ItemStack hinge() {
        return stack(CraftingItems.part(CraftingPart.STONE_HINGE));
    }

    private static ItemStack pebble() {
        return stack(CraftingItems.stonePebble());
    }

    private static ItemStack chisel() {
        return stack(CraftingItems.tool(CraftingTool.FLINT_CHISEL));
    }

    private static ItemStack stack(Item item) {
        return new ItemStack(item);
    }
}
