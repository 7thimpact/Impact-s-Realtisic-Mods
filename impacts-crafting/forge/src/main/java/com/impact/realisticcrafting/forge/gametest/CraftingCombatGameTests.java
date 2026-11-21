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
import com.impact.realisticcrafting.menu.CraftingStation;
import com.impact.realisticcrafting.menu.TableTierCheck;
import com.impact.realisticcrafting.menu.TieredCraftingMenu;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.atTable;
import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.craft;

/** Crafting milestone 12: the Bow Stave and ranged weapons, the shield, leather goods sewn from panels, and textiles. */
@GameTestHolder(CraftingMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CraftingCombatGameTests {
    private static final String LIB_NAMESPACE = CraftingSkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = CraftingSkeletonGameTests.EMPTY_TEMPLATE;
    private static final ItemStack NO_ITEM = ItemStack.EMPTY;
    private static final int IRON_HAMMER_TICKS = 20;

    private CraftingCombatGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void rangedWeaponsBranchFromTheBowStave(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack stave = craft(table, player, stick(), stick(), stick(), stack(Items.FLINT));
            helper.assertTrue(stave.is(part(CraftingPart.BOW_STAVE)), "3 Trimmed Sticks and a trimming tool make a Bow Stave");
            helper.assertTrue(player.getInventory().countItem(part(CraftingPart.WOOD_SCRAPS)) == 1, "Trimming the stave gives a Wood Scrap");
            table.grid().clearContent();
            ItemStack bow = craft(table, player, stave, twine(), twine());
            helper.assertTrue(bow.is(Items.BOW), "A Bow Stave and 2 Twine make a bow");
            table.grid().clearContent();
            ItemStack arrows = craft(table, player, stack(part(CraftingPart.FLINT_ARROWHEAD)), NO_ITEM, NO_ITEM, stick(), NO_ITEM, NO_ITEM,
                    stack(Items.FEATHER));
            helper.assertTrue(arrows.is(Items.ARROW) && arrows.getCount() == 2, "An arrowhead over a Trimmed Stick and a feather make 2 arrows");
            table.grid().clearContent();
            ItemStack rod = craft(table, player, NO_ITEM, NO_ITEM, stick(), NO_ITEM, stick(), twine(), stick());
            helper.assertTrue(rod.is(Items.FISHING_ROD), "3 Trimmed Sticks and Twine make a fishing rod");
            table.grid().clearContent();
            ItemStack leads = craft(table, player, twine(), twine(), twine(), stack(Items.SLIME_BALL));
            helper.assertTrue(leads.is(Items.LEAD) && leads.getCount() == 2, "3 Twine and a slimeball make 2 leads");
        });
        atTable(helper, MakeshiftTable.STONE, (player, table) -> {
            ItemStack crossbow = craft(table, player, stack(part(CraftingPart.BOW_STAVE)), oakPanel(), stack(part(CraftingPart.IRON_PLATE)),
                    stack(Items.TRIPWIRE_HOOK), twine());
            helper.assertTrue(crossbow.is(Items.CROSSBOW), "A Bow Stave, a panel stock, an Iron Plate, a tripwire hook and Twine make a crossbow");
            table.grid().clearContent();
            ItemStack looseShield = craft(table, player, oakPanel(), oakPanel(), oakPanel(), stack(part(CraftingPart.IRON_PLATE)),
                    stack(part(CraftingPart.IRON_RIVETS)));
            helper.assertTrue(looseShield.is(CraftingItems.looseIron(LooseIron.SHIELD)), "3 Wooden Panels, an Iron Plate and Rivets make a loose shield");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void theLooseShieldIsHammered(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, stack(CraftingItems.looseIron(LooseIron.SHIELD)));
            player.setItemInHand(InteractionHand.OFF_HAND, stack(CraftingItems.tool(CraftingTool.IRON_HAMMER)));
            player.getMainHandItem().getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
            for (int tick = 0; tick < IRON_HAMMER_TICKS && player.isUsingItem(); tick++) {
                ItemStack held = player.getUseItem();
                held.getItem().onUseTick(player.level(), player, held, player.getUseItemRemainingTicks());
            }
            helper.assertTrue(player.getMainHandItem().is(Items.SHIELD), "Hammering a loose shield finishes the shield");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void leatherGoodsAreSewnFromPanels(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack panel = craft(table, player, stack(Items.LEATHER), stack(Items.LEATHER), needle());
            helper.assertTrue(panel.is(part(CraftingPart.LEATHER_PANEL)), "2 leather and the Bone Needle make a Leather Panel");
            helper.assertTrue(table.grid().getItem(2).getDamageValue() == 1, "and the needle wears by 1");
            table.grid().clearContent();
            assertSewn(helper, player, table, Items.LEATHER_HELMET, 3, 1);
            assertSewn(helper, player, table, Items.LEATHER_CHESTPLATE, 5, 1);
            assertSewn(helper, player, table, Items.LEATHER_LEGGINGS, 4, 1);
            assertSewn(helper, player, table, Items.LEATHER_BOOTS, 2, 1);
            assertSewn(helper, player, table, Items.LEATHER_HORSE_ARMOR, 4, 2);
            ItemStack book = craft(table, player, stack(Items.PAPER), stack(Items.PAPER), stack(Items.PAPER), stack(part(CraftingPart.LEATHER_STRIP)),
                    twine(), needle());
            helper.assertTrue(book.is(Items.BOOK), "3 paper, a Leather Strip and Twine sewn with the needle make a book");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void textilesAreCutAndSewn(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            Recipe<?> carpet = helper.getLevel().getRecipeManager().byKey(ResourceIds.of(CraftingMod.MOD_ID, "red_carpet")).orElseThrow();
            helper.assertTrue(TableTierCheck.neededStation(table, carpet).equals(Optional.of(CraftingStation.MAKESHIFT_STONE_TABLE)),
                    "Cutting carpet with shears needs the Stone Table");
            ItemStack wool = stack(Items.RED_WOOL);
            ItemStack cloth = craft(table, player, wool.copy(), wool.copy(), wool.copy(), wool.copy(), wool.copy(), wool.copy(), needle(), twine());
            helper.assertTrue(cloth.is(CraftingItems.bannerCloth(DyeColor.RED)), "6 wool over Twine, sewn with the needle, make Banner Cloth");
            table.grid().clearContent();
            ItemStack banner = craft(table, player, cloth, NO_ITEM, NO_ITEM, stick());
            helper.assertTrue(banner.is(Items.RED_BANNER), "Banner Cloth over a Trimmed Stick makes a banner in the cloth's color");
            table.grid().clearContent();
            ItemStack candle = craft(table, player, twine(), NO_ITEM, NO_ITEM, stack(Items.HONEYCOMB));
            helper.assertTrue(candle.is(Items.CANDLE), "Twine over honeycomb makes a candle");
            table.grid().clearContent();
            ItemStack clumpWool = craft(table, player, stack(part(CraftingPart.STRING_CLUMP)), stack(part(CraftingPart.STRING_CLUMP)));
            helper.assertTrue(clumpWool.is(Items.WHITE_WOOL), "2 String Clumps make wool");
        });
        atTable(helper, MakeshiftTable.STONE, (player, table) -> {
            ItemStack carpets = craft(table, player, stack(Items.RED_WOOL), stack(Items.RED_WOOL), NO_ITEM, NO_ITEM, NO_ITEM, NO_ITEM, NO_ITEM, NO_ITEM,
                    stack(Items.SHEARS));
            helper.assertTrue(carpets.is(Items.RED_CARPET) && carpets.getCount() == 3, "2 wool side by side, cut with shears, make 3 carpets");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void bannerClothTakesTheVatsColor(GameTestHelper helper) {
        CraftingDyeVatGameTests.atVat(helper, (player, vat) -> {
            vat.fillWithWater();
            vat.addDye(DyeColor.BLUE, Optional.empty());
            CraftingDyeVatGameTests.use(player, vat, stack(CraftingItems.bannerCloth(DyeColor.WHITE)));
            helper.assertTrue(player.getMainHandItem().is(CraftingItems.bannerCloth(DyeColor.BLUE)), "White Banner Cloth dips into blue Banner Cloth");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void vanillaCombatAndTextileRecipesAreOff(GameTestHelper helper) {
        List<String> switchedOff = new ArrayList<>(List.of("bow", "arrow", "crossbow", "shield", "fishing_rod", "lead", "leather_helmet",
                "leather_chestplate", "leather_leggings", "leather_boots", "leather_horse_armor", "book", "candle", "white_wool_from_string"));
        for (DyeColor color : DyeColor.values()) {
            switchedOff.add(color.getName() + "_carpet");
            switchedOff.add(color.getName() + "_banner");
        }
        for (String vanillaRecipe : switchedOff) {
            helper.assertTrue(helper.getLevel().getRecipeManager().byKey(ResourceIds.of("minecraft", vanillaRecipe)).isEmpty(),
                    "Vanilla's " + vanillaRecipe + " recipe is switched off");
        }
        for (String keptRecipe : List.of("carrot_on_a_stick", "warped_fungus_on_a_stick", "brush", "spyglass")) {
            helper.assertTrue(helper.getLevel().getRecipeManager().byKey(ResourceIds.of("minecraft", keptRecipe)).isPresent(),
                    "Vanilla's " + keptRecipe + " recipe stays");
        }
        helper.succeed();
    }

    private static void assertSewn(GameTestHelper helper, ServerPlayer player, TieredCraftingMenu table, Item expected, int panels, int twines) {
        List<ItemStack> grid = new ArrayList<>();
        for (int count = 0; count < panels; count++) {
            grid.add(stack(part(CraftingPart.LEATHER_PANEL)));
        }
        for (int count = 0; count < twines; count++) {
            grid.add(twine());
        }
        grid.add(needle());
        ItemStack sewn = craft(table, player, grid.toArray(ItemStack[]::new));
        helper.assertTrue(sewn.is(expected), panels + " Leather Panels and " + twines + " Twine sew into " + ResourceIds.itemId(expected).getPath());
        table.grid().clearContent();
    }

    private static ItemStack oakPanel() {
        return WoodData.of(CraftingItems.woodPart(WoodPart.WOODEN_PANEL), ResourceIds.of("minecraft", "oak"));
    }

    private static ItemStack needle() {
        return stack(CraftingItems.tool(CraftingTool.BONE_NEEDLE));
    }

    private static ItemStack twine() {
        return stack(part(CraftingPart.TWINE));
    }

    private static ItemStack stick() {
        return stack(part(CraftingPart.TRIMMED_STICK));
    }

    private static Item part(CraftingPart part) {
        return CraftingItems.part(part);
    }

    private static ItemStack stack(Item item) {
        return new ItemStack(item);
    }
}
