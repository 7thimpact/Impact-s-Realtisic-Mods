package com.impact.realisticcrafting.forge.gametest;

import com.impact.lib.ResourceIds;
import com.impact.lib.gametest.ConfigOverrides;
import com.impact.lib.gametest.MockPlayers;
import com.impact.lib.hold.HoldToUse;
import com.impact.lib.item.UsesDurability;
import com.impact.lib.wood.WoodData;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.config.CraftingCommonConfig;
import com.impact.realisticcrafting.config.CraftingFeature;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.LooseItem;
import com.impact.realisticcrafting.item.ToolKind;
import com.impact.realisticcrafting.item.ToolMaterial;
import com.impact.realisticcrafting.registry.CraftingItems;
import com.impact.realisticcrafting.registry.CraftingTags;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.function.Consumer;

import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.atTable;
import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.craft;

/** Crafting milestone 4: tool heads, loose tools, and hammering them into the 10 wooden and stone tools. */
@GameTestHolder(CraftingMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CraftingHammeringGameTests {
    private static final String LIB_NAMESPACE = CraftingSkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = CraftingSkeletonGameTests.EMPTY_TEMPLATE;
    private static final int DEFAULT_HAMMER_TICKS = 40;
    private static final ItemStack NO_ITEM = ItemStack.EMPTY;

    private CraftingHammeringGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void headsAndLooseToolsFollowThePatterns(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack planks = WoodData.of(CraftingItems.trimmedPlanks(), ResourceIds.of("minecraft", "oak"));
            ItemStack pickaxeHead = craft(table, player, planks.copy(), planks.copy(), planks.copy());
            helper.assertTrue(pickaxeHead.is(part(ToolMaterial.WOODEN, ToolKind.PICKAXE)), "3 Trimmed Planks in the top row make a pickaxe head");
            table.grid().clearContent();
            ItemStack loosePickaxe = craft(table, player, NO_ITEM, pickaxeHead, NO_ITEM, NO_ITEM, stick(), NO_ITEM, NO_ITEM, stick());
            helper.assertTrue(loosePickaxe.is(CraftingItems.looseTool(ToolMaterial.WOODEN, ToolKind.PICKAXE)), "The head on 2 Trimmed Sticks makes a loose pickaxe");
            table.grid().clearContent();
            ItemStack shovelHead = craft(table, player, planks.copy());
            helper.assertTrue(shovelHead.is(part(ToolMaterial.WOODEN, ToolKind.SHOVEL)), "One Trimmed Planks alone makes a shovel head");
            table.grid().clearContent();
            ItemStack hoeHead = craft(table, player, planks.copy(), planks.copy(), new ItemStack(CraftingItems.stonePebble()));
            helper.assertTrue(hoeHead.is(part(ToolMaterial.WOODEN, ToolKind.HOE)), "2 side by side with a trimming tool make a hoe head");
            table.grid().clearContent();
            ItemStack stone = new ItemStack(CraftingItems.part(CraftingPart.SHAPED_STONE));
            ItemStack axeHead = craft(table, player, stone.copy(), stone.copy(), NO_ITEM, stone.copy());
            helper.assertTrue(axeHead.is(part(ToolMaterial.STONE, ToolKind.AXE)), "Shaped Stone in the axe pattern makes a stone axe head");
            table.grid().clearContent();
            ItemStack blade = craft(table, player, stone.copy(), NO_ITEM, NO_ITEM, stone.copy());
            helper.assertTrue(blade.is(part(ToolMaterial.STONE, ToolKind.SWORD)), "2 Shaped Stone stacked make a stone sword blade");
            table.grid().clearContent();
            ItemStack looseSword = craft(table, player, blade, NO_ITEM, NO_ITEM, stick());
            helper.assertTrue(looseSword.is(CraftingItems.looseTool(ToolMaterial.STONE, ToolKind.SWORD)), "A blade on 1 Trimmed Stick makes a loose sword");
        });
        helper.assertTrue(helper.getLevel().getRecipeManager().byKey(ResourceIds.of("minecraft", "wooden_pickaxe")).isEmpty(),
                "Vanilla's wooden pickaxe recipe is switched off");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void hammeringFinishesALooseTool(GameTestHelper helper) {
        withHands(helper, ToolMaterial.WOODEN, ToolKind.PICKAXE, new ItemStack(CraftingItems.stonePebble()), player -> {
            helper.assertTrue(startHammering(player) == InteractionResult.CONSUME, "A hammer in the off hand starts the hold");
            holdFor(player, DEFAULT_HAMMER_TICKS - 1);
            helper.assertTrue(player.getMainHandItem().getItem() instanceof LooseItem, "Not done before 40 ticks");
            holdFor(player, 1);
            ItemStack finished = player.getMainHandItem();
            helper.assertTrue(ItemStack.isSameItemSameTags(finished, new ItemStack(Items.WOODEN_PICKAXE)), "40 ticks make a clean vanilla wooden pickaxe");
            helper.assertTrue(UsesDurability.usesSpent(player.getOffhandItem()) == 1, "The pebble loses 1 use");
            helper.assertFalse(player.isUsingItem(), "and the hold ends");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void everyToolCanBeHammered(GameTestHelper helper) {
        for (ToolMaterial material : ToolMaterial.values()) {
            for (ToolKind kind : ToolKind.values()) {
                withHands(helper, material, kind, new ItemStack(CraftingItems.stonePebble()), player -> {
                    startHammering(player);
                    holdFor(player, DEFAULT_HAMMER_TICKS);
                    String expected = kind.vanillaPath(material);
                    helper.assertTrue(ResourceIds.itemId(player.getMainHandItem().getItem()).getPath().equals(expected), "A loose tool hammers into " + expected);
                });
            }
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void noHammerNoHold(GameTestHelper helper) {
        withHands(helper, ToolMaterial.STONE, ToolKind.AXE, ItemStack.EMPTY, player -> {
            helper.assertTrue(startHammering(player) == InteractionResult.PASS, "Without a hammer, right-click does nothing");
            helper.assertFalse(player.isUsingItem(), "and nothing is held");
        });
        withHands(helper, ToolMaterial.STONE, ToolKind.AXE, new ItemStack(Items.STICK), player ->
                helper.assertTrue(startHammering(player) == InteractionResult.PASS, "A stick isn't a hammer"));
        CraftingCommonConfig config = CraftingCommonConfig.get();
        ConfigOverrides.withValue(config.features.get(CraftingFeature.HAMMERING), false, () ->
                withHands(helper, ToolMaterial.STONE, ToolKind.AXE, new ItemStack(CraftingItems.stonePebble()), player ->
                        helper.assertTrue(startHammering(player) == InteractionResult.PASS, "With hammering switched off, nothing happens")));
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void lettingGoLosesAllProgress(GameTestHelper helper) {
        withHands(helper, ToolMaterial.WOODEN, ToolKind.HOE, new ItemStack(CraftingItems.stonePebble()), player -> {
            startHammering(player);
            holdFor(player, DEFAULT_HAMMER_TICKS / 2);
            player.releaseUsingItem();
            startHammering(player);
            holdFor(player, DEFAULT_HAMMER_TICKS - 1);
            helper.assertTrue(player.getMainHandItem().getItem() instanceof LooseItem, "Letting go starts over");
            holdFor(player, 1);
            helper.assertTrue(player.getMainHandItem().is(Items.WOODEN_HOE), "and a full hold finishes it");
            helper.assertTrue(UsesDurability.usesSpent(player.getOffhandItem()) == 1, "The cancelled hold cost the pebble nothing");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void stackedPebblesHammerOneAtATime(GameTestHelper helper) {
        withHands(helper, ToolMaterial.STONE, ToolKind.SHOVEL, new ItemStack(CraftingItems.stonePebble(), 3), player -> {
            startHammering(player);
            holdFor(player, DEFAULT_HAMMER_TICKS);
            ItemStack offHand = player.getOffhandItem();
            helper.assertTrue(offHand.getCount() == 2 && UsesDurability.isUnused(offHand), "2 unused pebbles stay in the off hand");
            boolean hasWornPebble = player.getInventory().items.stream()
                    .anyMatch(stack -> stack.is(CraftingItems.stonePebble()) && UsesDurability.usesSpent(stack) == 1);
            helper.assertTrue(hasWornPebble, "and the one used goes to the inventory, worn");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void hammeringTimeFollowsTheConfig(GameTestHelper helper) {
        ConfigOverrides.withValue(CraftingServerConfig.get().hammering.hammerUseTicks, 10, () ->
                withHands(helper, ToolMaterial.STONE, ToolKind.SWORD, new ItemStack(CraftingItems.stonePebble()), player -> {
                    startHammering(player);
                    holdFor(player, 10);
                    helper.assertTrue(player.getMainHandItem().is(Items.STONE_SWORD), "hammering.hammerUseTicks sets the hold's length");
                }));
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void looseToolsAreUnfinished(GameTestHelper helper) {
        ItemStack loose = new ItemStack(CraftingItems.looseTool(ToolMaterial.STONE, ToolKind.PICKAXE));
        helper.assertTrue(loose.getMaxStackSize() == 1, "Loose items stack to 1");
        helper.assertFalse(loose.isEnchantable(), "They can't be enchanted");
        helper.assertTrue(loose.getDestroySpeed(Blocks.STONE.defaultBlockState()) == 1.0F, "They mine like an empty hand");
        helper.assertTrue(loose.getAttributeModifiers(EquipmentSlot.MAINHAND).isEmpty(), "and hit like one");
        helper.assertTrue(loose.is(CraftingTags.LOOSE_TOOLS), "Every loose tool is in #impactscrafting:loose_tools");
        helper.assertTrue(HoldToUse.actionFor(loose).isPresent(), "and has the hammering action");
        helper.succeed();
    }

    private static void withHands(GameTestHelper helper, ToolMaterial material, ToolKind kind, ItemStack offHand, Consumer<ServerPlayer> check) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(CraftingItems.looseTool(material, kind)));
            player.setItemInHand(InteractionHand.OFF_HAND, offHand);
            check.accept(player);
        } finally {
            MockPlayers.leaveWorld(player);
        }
    }

    private static InteractionResult startHammering(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        return held.getItem().use(player.level(), player, InteractionHand.MAIN_HAND).getResult();
    }

    // A mock player doesn't tick, so the hold's ticks are run by hand, as vanilla's item-use loop would.
    private static void holdFor(ServerPlayer player, int ticks) {
        for (int tick = 0; tick < ticks && player.isUsingItem(); tick++) {
            ItemStack held = player.getUseItem();
            held.getItem().onUseTick(player.level(), player, held, player.getUseItemRemainingTicks());
        }
    }

    private static ItemStack stick() {
        return new ItemStack(CraftingItems.part(CraftingPart.TRIMMED_STICK));
    }

    private static Item part(ToolMaterial material, ToolKind kind) {
        return CraftingItems.toolPart(material, kind);
    }
}
