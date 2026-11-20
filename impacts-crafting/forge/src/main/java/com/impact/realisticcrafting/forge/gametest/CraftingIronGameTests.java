package com.impact.realisticcrafting.forge.gametest;

import com.impact.lib.ResourceIds;
import com.impact.lib.gametest.MockPlayers;
import com.impact.lib.wood.WoodData;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.CraftingTool;
import com.impact.realisticcrafting.item.LooseIron;
import com.impact.realisticcrafting.item.WoodPart;
import com.impact.realisticcrafting.registry.CraftingItems;
import com.impact.realisticcrafting.timing.CraftDepthTable;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.atTable;
import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.craft;

/** Crafting milestone 7: iron tools and parts at the Makeshift Stone Table, the Crafting Table recipe, and the iron goods. */
@GameTestHolder(CraftingMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CraftingIronGameTests {
    private static final String LIB_NAMESPACE = CraftingSkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = CraftingSkeletonGameTests.EMPTY_TEMPLATE;
    private static final ItemStack NO_ITEM = ItemStack.EMPTY;
    private static final int IRON_HAMMER_TICKS = 20;

    private CraftingIronGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void ironToolsAreMadeAtTheStoneTable(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.STONE, (player, table) -> {
            ItemStack saw = craft(table, player, stick(), stack(Items.IRON_INGOT), stack(Items.IRON_INGOT));
            helper.assertTrue(saw.is(tool(CraftingTool.IRON_SAW)) && saw.getMaxDamage() == 128, "A stick and 2 ingots in a row make a 128-use Iron Saw");
            table.grid().clearContent();
            ItemStack hammer = craft(table, player, stack(Items.IRON_INGOT), stack(Items.IRON_INGOT), NO_ITEM, NO_ITEM, stick());
            helper.assertTrue(hammer.is(tool(CraftingTool.IRON_HAMMER)), "2 ingots over a stick make an Iron Hammer");
            table.grid().clearContent();
            ItemStack tweezers = craft(table, player, NO_ITEM, stack(Items.IRON_INGOT), NO_ITEM, stack(Items.IRON_NUGGET), NO_ITEM, stack(Items.IRON_NUGGET));
            helper.assertTrue(tweezers.is(tool(CraftingTool.IRON_TWEEZERS)), "An ingot over 2 nuggets makes Iron Tweezers (not SRF's pan pattern)");
            table.grid().clearContent();
            ItemStack chisel = craft(table, player, stack(Items.IRON_INGOT), NO_ITEM, NO_ITEM, stick());
            helper.assertTrue(chisel.is(tool(CraftingTool.IRON_CHISEL)) && chisel.getMaxDamage() == 192, "An ingot on a stick makes a 192-use Iron Chisel");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void ironPartsUseTheIronTools(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.STONE, (player, table) -> {
            ItemStack plate = craft(table, player, stack(Items.IRON_INGOT), stack(tool(CraftingTool.IRON_HAMMER)));
            helper.assertTrue(plate.is(part(CraftingPart.IRON_PLATE)), "An ingot and the Iron Hammer make an Iron Plate");
            table.grid().clearContent();
            ItemStack rods = craft(table, player, stack(Items.IRON_INGOT), stack(tool(CraftingTool.IRON_SAW)));
            helper.assertTrue(rods.is(part(CraftingPart.IRON_ROD)) && rods.getCount() == 2, "An ingot and the Iron Saw make 2 Iron Rods");
            table.grid().clearContent();
            ItemStack rivets = craft(table, player, stack(Items.IRON_NUGGET), stack(tool(CraftingTool.IRON_HAMMER)));
            helper.assertTrue(rivets.is(part(CraftingPart.IRON_RIVETS)) && rivets.getCount() == 2, "A nugget and the Iron Hammer make 2 Iron Rivets");
            table.grid().clearContent();
            ItemStack hinges = craft(table, player, plate.copy(), rivets.copyWithCount(1));
            helper.assertTrue(hinges.is(part(CraftingPart.IRON_HINGE)) && hinges.getCount() == 2, "A plate and rivets make 2 Iron Hinges");
            table.grid().clearContent();
            ItemStack rod = stack(part(CraftingPart.IRON_ROD));
            ItemStack wheel = craft(table, player, NO_ITEM, rod.copy(), NO_ITEM, rod.copy(), plate.copy(), rod.copy(), NO_ITEM, rod.copy());
            helper.assertTrue(wheel.is(part(CraftingPart.IRON_WHEEL)), "4 rods around a plate make an Iron Wheel");
        });
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack refused = craft(table, player, stack(Items.IRON_INGOT), stack(tool(CraftingTool.IRON_HAMMER)));
            helper.assertTrue(refused.isEmpty(), "The Makeshift Table refuses iron parts");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void theCraftingTableNeedsTheThreeIronTools(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.STONE, (player, table) -> {
            ItemStack craftingTable = craft(table, player, stack(Items.OAK_PLANKS), stack(Items.OAK_PLANKS), stack(Items.BIRCH_PLANKS),
                    stack(Items.OAK_PLANKS), stack(tool(CraftingTool.IRON_SAW)), stack(tool(CraftingTool.IRON_HAMMER)), stack(tool(CraftingTool.IRON_TWEEZERS)));
            helper.assertTrue(craftingTable.is(Items.CRAFTING_TABLE), "4 planks and the 3 iron tools make a Crafting Table");
            long wornTools = List.of(4, 5, 6).stream().map(slot -> table.grid().getItem(slot)).filter(stack -> stack.getDamageValue() == 1).count();
            helper.assertTrue(wornTools == 3, "The tools stay in the grid, each worn by 1");
        });
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack refused = craft(table, player, stack(Items.OAK_PLANKS), stack(Items.OAK_PLANKS), stack(Items.OAK_PLANKS), stack(Items.OAK_PLANKS),
                    stack(tool(CraftingTool.IRON_SAW)), stack(tool(CraftingTool.IRON_HAMMER)), stack(tool(CraftingTool.IRON_TWEEZERS)));
            helper.assertTrue(refused.isEmpty(), "Its iron tools make it a Stone Table recipe");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void theIronSawDoublesWoodYields(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack planks = craft(table, player, stack(Items.OAK_LOG), stack(tool(CraftingTool.IRON_SAW)));
            helper.assertTrue(planks.is(Items.OAK_PLANKS) && planks.getCount() == 4, "A log and the Iron Saw make 4 planks");
            table.grid().clearContent();
            ItemStack trimmed = craft(table, player, stack(Items.OAK_PLANKS), stack(tool(CraftingTool.IRON_SAW)));
            helper.assertTrue(trimmed.is(CraftingItems.trimmedPlanks()) && trimmed.getCount() == 2, "and planks 2 Trimmed Planks");
            table.grid().clearContent();
            ItemStack sticks = craft(table, player, stack(Items.OAK_PLANKS), stack(tool(CraftingTool.IRON_SAW)), NO_ITEM, stack(Items.OAK_PLANKS));
            helper.assertTrue(sticks.is(Items.STICK) && sticks.getCount() == 8, "and 2 stacked planks 8 sticks");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void ironGoodsComeFromIronParts(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.STONE, (player, table) -> {
            ItemStack plate = stack(part(CraftingPart.IRON_PLATE));
            ItemStack looseBucket = craft(table, player, plate.copy(), NO_ITEM, plate.copy(), NO_ITEM, plate.copy());
            helper.assertTrue(looseBucket.is(CraftingItems.looseIron(LooseIron.BUCKET)), "3 Iron Plates make a loose bucket");
            table.grid().clearContent();
            ItemStack rod = stack(part(CraftingPart.IRON_ROD));
            ItemStack bars = craft(table, player, rod.copy(), rod.copy(), rod.copy());
            helper.assertTrue(bars.is(Items.IRON_BARS) && bars.getCount() == 8, "3 Iron Rods make 8 iron bars");
            table.grid().clearContent();
            ItemStack rivet = stack(part(CraftingPart.IRON_RIVETS));
            ItemStack chain = craft(table, player, rivet.copy(), rivet.copy(), rivet.copy());
            helper.assertTrue(chain.is(Items.CHAIN), "3 Iron Rivets make a chain");
            table.grid().clearContent();
            ItemStack rails = craft(table, player, rod.copy(), rod.copy(), rod.copy(), rod.copy(), stick(), stick());
            helper.assertTrue(rails.is(Items.RAIL) && rails.getCount() == 8, "4 rods and 2 Trimmed Sticks make 8 rails");
            table.grid().clearContent();
            ItemStack panes = craft(table, player, stack(Items.GLASS), stack(tool(CraftingTool.IRON_SAW)));
            helper.assertTrue(panes.is(Items.GLASS_PANE) && panes.getCount() == 3, "Glass and the Iron Saw make 3 panes");
            table.grid().clearContent();
            ItemStack bottle = craft(table, player, stack(Items.GLASS), stack(tool(CraftingTool.IRON_TWEEZERS)));
            helper.assertTrue(bottle.is(Items.GLASS_BOTTLE), "Glass and the Iron Tweezers make a bottle");
            table.grid().clearContent();
            ItemStack panel = WoodData.of(CraftingItems.woodPart(WoodPart.WOODEN_PANEL), ResourceIds.of("minecraft", "spruce"));
            ItemStack signs = craft(table, player, panel, stack(Items.CHAIN));
            helper.assertTrue(signs.is(Items.SPRUCE_HANGING_SIGN) && signs.getCount() == 6, "A spruce panel and a chain make 6 spruce hanging signs");
        });
        for (String vanillaRecipe : List.of("bucket", "rail", "minecart", "glass_pane", "iron_bars", "spruce_hanging_sign")) {
            helper.assertTrue(helper.getLevel().getRecipeManager().byKey(ResourceIds.of("minecraft", vanillaRecipe)).isEmpty(),
                    "Vanilla's " + vanillaRecipe + " recipe is switched off");
        }
        helper.assertTrue(CraftDepthTable.depthOf(Items.BUCKET).orElse(-1) == CraftDepthTable.depthOf(CraftingItems.looseIron(LooseIron.BUCKET)).orElse(-2),
                "A bucket is as deep as its loose bucket");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void theIronHammerIsQuicker(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, stack(CraftingItems.looseIron(LooseIron.MINECART)));
            player.setItemInHand(InteractionHand.OFF_HAND, stack(tool(CraftingTool.IRON_HAMMER)));
            player.getMainHandItem().getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
            for (int tick = 0; tick < IRON_HAMMER_TICKS && player.isUsingItem(); tick++) {
                ItemStack held = player.getUseItem();
                held.getItem().onUseTick(player.level(), player, held, player.getUseItemRemainingTicks());
            }
            helper.assertTrue(player.getMainHandItem().is(Items.MINECART), "The Iron Hammer finishes a loose minecart in 20 ticks");
            helper.assertTrue(player.getOffhandItem().getDamageValue() == 1, "and loses 1 durability");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    private static ItemStack stick() {
        return stack(part(CraftingPart.TRIMMED_STICK));
    }

    private static Item part(CraftingPart part) {
        return CraftingItems.part(part);
    }

    private static Item tool(CraftingTool tool) {
        return CraftingItems.tool(tool);
    }

    private static ItemStack stack(Item item) {
        return new ItemStack(item);
    }
}
