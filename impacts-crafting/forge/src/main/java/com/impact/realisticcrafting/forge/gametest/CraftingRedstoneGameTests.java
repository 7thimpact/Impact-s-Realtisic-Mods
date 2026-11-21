package com.impact.realisticcrafting.forge.gametest;

import com.impact.lib.ResourceIds;
import com.impact.lib.wood.WoodData;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.CraftingTool;
import com.impact.realisticcrafting.item.LooseIron;
import com.impact.realisticcrafting.item.WoodPart;
import com.impact.realisticcrafting.menu.CraftingStation;
import com.impact.realisticcrafting.menu.TableTierCheck;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.atTable;
import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.craft;

/** Crafting milestone 11: the Redstone Mechanism and Piston Head, and the redstone machines built on housings. */
@GameTestHolder(CraftingMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CraftingRedstoneGameTests {
    private static final String LIB_NAMESPACE = CraftingSkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = CraftingSkeletonGameTests.EMPTY_TEMPLATE;
    private static final ItemStack NO_ITEM = ItemStack.EMPTY;

    private CraftingRedstoneGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void machinesAreAHousingAndAMechanism(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.STONE, (player, table) -> {
            ItemStack mechanism = craft(table, player, stack(part(CraftingPart.IRON_PLATE)), stack(Items.REDSTONE), stack(Items.REDSTONE),
                    stack(part(CraftingPart.IRON_RIVETS)), stack(CraftingItems.tool(CraftingTool.IRON_TWEEZERS)));
            helper.assertTrue(mechanism.is(part(CraftingPart.REDSTONE_MECHANISM)), "A plate, 2 redstone and rivets with the Tweezers make a Redstone Mechanism");
            table.grid().clearContent();
            ItemStack panel = WoodData.of(CraftingItems.woodPart(WoodPart.WOODEN_PANEL), ResourceIds.of("minecraft", "oak"));
            ItemStack pistonHead = craft(table, player, panel, stack(part(CraftingPart.IRON_PLATE)));
            helper.assertTrue(pistonHead.is(part(CraftingPart.PISTON_HEAD)), "A Wooden Panel and an Iron Plate make a Piston Head");
            table.grid().clearContent();
            ItemStack loosePiston = craft(table, player, pistonHead, stack(part(CraftingPart.STONE_CASING)), mechanism.copy(), stack(part(CraftingPart.IRON_ROD)));
            helper.assertTrue(loosePiston.is(CraftingItems.looseIron(LooseIron.PISTON)), "Head, casing, mechanism and an Iron Rod make an Unfitted Piston");
            table.grid().clearContent();
            ItemStack dropper = craft(table, player, stack(part(CraftingPart.STONE_CASING)), mechanism.copy());
            helper.assertTrue(dropper.is(Items.DROPPER), "A casing and a mechanism make a dropper");
            table.grid().clearContent();
            ItemStack torch = craft(table, player, stack(Items.REDSTONE), NO_ITEM, NO_ITEM, stack(part(CraftingPart.TRIMMED_STICK)));
            helper.assertTrue(torch.is(Items.REDSTONE_TORCH), "Redstone on a Trimmed Stick makes a redstone torch");
            table.grid().clearContent();
            ItemStack repeater = craft(table, player, stack(part(CraftingPart.STONE_BASE)), stack(Items.REDSTONE_TORCH), stack(Items.REDSTONE_TORCH),
                    stack(Items.REDSTONE));
            helper.assertTrue(repeater.is(Items.REPEATER), "A Stone Base, 2 torches and redstone make a repeater");
        });
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack gunpowder = stack(Items.GUNPOWDER);
            ItemStack sand = stack(Items.SAND);
            ItemStack tnt = craft(table, player, gunpowder.copy(), sand.copy(), gunpowder.copy(), sand.copy(), stack(Items.PAPER), sand.copy(),
                    gunpowder.copy(), sand.copy(), gunpowder.copy());
            helper.assertTrue(tnt.is(Items.TNT), "4 gunpowder, 4 sand and paper make TNT at the Makeshift Table");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void quartzAndDiamondNeedTheCraftingTable(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.STONE, (player, table) -> {
            for (String machine : List.of("comparator", "daylight_detector", "observer", "jukebox")) {
                Recipe<?> recipe = helper.getLevel().getRecipeManager().byKey(ResourceIds.of(CraftingMod.MOD_ID, machine)).orElseThrow();
                helper.assertTrue(TableTierCheck.neededStation(table, recipe).equals(Optional.of(CraftingStation.CRAFTING_TABLE)),
                        "The " + machine + " needs the Crafting Table");
            }
        });
        for (String vanillaRecipe : List.of("piston", "dropper", "dispenser", "repeater", "comparator", "tnt", "redstone_torch")) {
            helper.assertTrue(helper.getLevel().getRecipeManager().byKey(ResourceIds.of("minecraft", vanillaRecipe)).isEmpty(),
                    "Vanilla's " + vanillaRecipe + " recipe is switched off");
        }
        helper.assertTrue(helper.getLevel().getRecipeManager().byKey(ResourceIds.of("minecraft", "sticky_piston")).isPresent(),
                "The sticky piston keeps its vanilla recipe");
        helper.succeed();
    }

    private static Item part(CraftingPart part) {
        return CraftingItems.part(part);
    }

    private static ItemStack stack(Item item) {
        return new ItemStack(item);
    }
}
