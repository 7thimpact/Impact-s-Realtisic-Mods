package com.impact.realisticcrafting.forge.gametest;

import com.impact.lib.ResourceIds;
import com.impact.lib.dump.DumpSection;
import com.impact.lib.dump.ImpactsDump;
import com.impact.lib.gametest.ConfigOverrides;
import com.impact.lib.item.UsesDurability;
import com.impact.lib.platform.LoaderPlatform;
import com.impact.lib.wood.WoodData;
import com.impact.lib.wood.WoodKind;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.CraftingTool;
import com.impact.realisticcrafting.registry.CraftingItems;
import com.impact.realisticcrafting.registry.CraftingTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.common.Tags;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.atTable;
import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.craft;

/** Crafting milestone 3: Lib's wood data, the trimming recipes and their Wood Scraps, the starting parts and tools, fuel. */
@GameTestHolder(CraftingMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CraftingPartGameTests {
    private static final String LIB_NAMESPACE = CraftingSkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = CraftingSkeletonGameTests.EMPTY_TEMPLATE;
    private static final ResourceLocation OAK = wood("oak");
    private static final ResourceLocation CRIMSON = wood("crimson");
    private static final ResourceLocation WARPED = wood("warped");
    private static final ItemStack NO_ITEM = ItemStack.EMPTY;

    private CraftingPartGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void craftingsPartsAreInVanillasAndSharedTags(GameTestHelper helper) {
        ItemStack ironPlate = new ItemStack(CraftingItems.part(CraftingPart.IRON_PLATE));
        helper.assertTrue(ironPlate.is(forgeTag("plates/iron")) && ironPlate.is(CraftingTags.IRON_PLATES), "The Iron Plate is in forge:plates/iron");
        ItemStack ironRod = new ItemStack(CraftingItems.part(CraftingPart.IRON_ROD));
        helper.assertTrue(ironRod.is(forgeTag("rods/iron")) && ironRod.is(Tags.Items.RODS), "The Iron Rod is in forge:rods/iron");
        helper.assertTrue(new ItemStack(CraftingItems.part(CraftingPart.TRIMMED_STICK)).is(Tags.Items.RODS_WOODEN), "A Trimmed Stick is a wooden rod");
        for (CraftingTool tool : CraftingTool.values()) {
            helper.assertTrue(new ItemStack(CraftingItems.tool(tool)).is(ItemTags.TOOLS), tool + " is a tool to vanilla");
        }
        helper.assertTrue(new ItemStack(CraftingItems.comforter()).is(ItemTags.DAMPENS_VIBRATIONS), "Cloth muffles vibrations like wool");
        helper.succeed();
    }

    private static TagKey<Item> forgeTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceIds.of("forge", path));
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void woodDataMapsWoodsToVanillaItems(GameTestHelper helper) {
        helper.assertTrue(WoodData.vanillaItem(OAK, WoodKind.DOOR).equals(Optional.of(Items.OAK_DOOR)), "Oak maps to the oak door");
        helper.assertTrue(WoodData.vanillaItem(wood("bamboo"), WoodKind.BOAT).equals(Optional.of(Items.BAMBOO_RAFT)), "Bamboo boats are rafts");
        helper.assertTrue(WoodData.vanillaItem(wood("bamboo"), WoodKind.CHEST_BOAT).equals(Optional.of(Items.BAMBOO_CHEST_RAFT)), "and chest rafts");
        helper.assertTrue(WoodData.vanillaItem(CRIMSON, WoodKind.BOAT).isEmpty(), "Nether woods have no boats");
        helper.assertTrue(WoodData.logTag(CRIMSON).location().getPath().equals("crimson_stems"), "Crimson logs are stems");
        helper.assertTrue(WoodData.logTag(wood("bamboo")).location().getPath().equals("bamboo_blocks"), "Bamboo logs are blocks");
        helper.assertTrue(WoodData.allWoods().containsAll(WoodData.VANILLA_WOODS), "Every vanilla wood is registered");
        helper.assertTrue(WoodData.isFlammable(OAK) && !WoodData.isFlammable(WARPED), "Oak burns, warped doesn't");
        helper.assertTrue(WoodData.get(WoodData.of(CraftingItems.trimmedPlanks(), OAK)).equals(Optional.of(OAK)), "The wood NBT reads back");
        helper.assertTrue(WoodData.displayName(wood("dark_oak")).getString().equals("Dark Oak"), "Woods have display names");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void trimmedPlanksKeepTheirWood(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack trimmed = craft(table, player, new ItemStack(Items.OAK_PLANKS), pebble());
            helper.assertTrue(trimmed.is(CraftingItems.trimmedPlanks()) && WoodData.get(trimmed).equals(Optional.of(OAK)), "Oak planks trim into oak Trimmed Planks");
            helper.assertTrue(trimmed.getHoverName().getString().equals("Oak Trimmed Planks"), "named for their wood");
            ItemStack pebbleLeft = table.grid().getItem(1);
            helper.assertTrue(pebbleLeft.is(CraftingItems.stonePebble()) && UsesDurability.usesSpent(pebbleLeft) == 1, "The pebble stays in the grid, worn by 1");
            helper.assertTrue(player.getInventory().countItem(woodScraps()) == 1, "Trimming wood gives a Wood Scrap");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void stackedPebblesWearOneAtATime(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            craft(table, player, new ItemStack(Items.OAK_PLANKS), new ItemStack(CraftingItems.stonePebble(), 5));
            ItemStack pebblesLeft = table.grid().getItem(1);
            helper.assertTrue(pebblesLeft.getCount() == 4 && UsesDurability.isUnused(pebblesLeft), "4 unused pebbles stay in the grid");
            boolean hasWornPebble = player.getInventory().items.stream()
                    .anyMatch(stack -> stack.is(CraftingItems.stonePebble()) && stack.getCount() == 1 && UsesDurability.usesSpent(stack) == 1);
            helper.assertTrue(hasWornPebble, "and only the one used goes to the inventory, worn");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void flintIsUsedUpAsATrimmingTool(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack trimmed = craft(table, player, new ItemStack(Items.SPRUCE_PLANKS), new ItemStack(Items.FLINT));
            helper.assertTrue(trimmed.is(CraftingItems.trimmedPlanks()), "Flint trims too");
            helper.assertTrue(table.grid().getItem(1).isEmpty(), "and is used up, since it has no durability");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void netherWoodGivesNoScraps(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack trimmed = craft(table, player, new ItemStack(Items.CRIMSON_PLANKS), pebble());
            helper.assertTrue(WoodData.get(trimmed).equals(Optional.of(CRIMSON)), "Crimson planks trim into crimson Trimmed Planks");
            ItemStack sticks = craft(table, player, WoodData.of(CraftingItems.trimmedPlanks(), WARPED), pebble());
            helper.assertTrue(sticks.is(CraftingItems.part(CraftingPart.TRIMMED_STICK)) && sticks.getCount() == 2, "Warped Trimmed Planks give 2 Trimmed Sticks");
            helper.assertTrue(player.getInventory().countItem(woodScraps()) == 0, "Nether wood gives no Wood Scraps");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void planksAndSticksNeedATrimmingTool(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack byHand = craft(player.inventoryMenu, player, new ItemStack(Items.OAK_LOG));
            helper.assertTrue(byHand.is(Items.OAK_PLANKS) && byHand.getCount() == 1, "A bare log gives 1 planks in the 2x2 grid");
            ItemStack trimmed = craft(player.inventoryMenu, player, new ItemStack(Items.OAK_LOG), pebble());
            helper.assertTrue(trimmed.is(Items.OAK_PLANKS) && trimmed.getCount() == 2, "A log and a pebble give 2");
            ItemStack bamboo = craft(player.inventoryMenu, player, new ItemStack(Items.BAMBOO_BLOCK), pebble());
            helper.assertTrue(bamboo.is(Items.BAMBOO_PLANKS) && bamboo.getCount() == 2, "Bamboo blocks count as logs");
            ItemStack sticks = craft(player.inventoryMenu, player, new ItemStack(Items.BIRCH_PLANKS), pebble(), new ItemStack(Items.BIRCH_PLANKS));
            helper.assertTrue(sticks.is(Items.STICK) && sticks.getCount() == 4, "2 planks stacked and a pebble give 4 sticks");
            player.inventoryMenu.getCraftSlots().clearContent();
            helper.assertTrue(recipe(helper, "minecraft:oak_planks").isEmpty() && recipe(helper, "minecraft:stick").isEmpty(),
                    "Vanilla's planks and stick recipes are switched off");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void stonePartsComeFromCobblestone(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack shapedStone = craft(table, player, new ItemStack(Items.COBBLESTONE), pebble());
            helper.assertTrue(shapedStone.is(CraftingItems.part(CraftingPart.SHAPED_STONE)), "Cobblestone trims into Shaped Stone");
            table.grid().clearContent();
            ItemStack hinges = craft(table, player, shapedStone, pebble());
            helper.assertTrue(hinges.is(CraftingItems.part(CraftingPart.STONE_HINGE)) && hinges.getCount() == 2, "Shaped Stone trims into 2 Stone Hinges");
            helper.assertTrue(player.getInventory().countItem(woodScraps()) == 0, "Stone gives no Wood Scraps");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void stringMakesTwineAndClumps(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack twine = craft(table, player, new ItemStack(Items.STRING), NO_ITEM, NO_ITEM, new ItemStack(Items.STRING));
            helper.assertTrue(twine.is(CraftingItems.part(CraftingPart.TWINE)), "2 string stacked make Twine");
            ItemStack clump = craft(table, player, new ItemStack(Items.STRING), new ItemStack(Items.STRING));
            helper.assertTrue(clump.is(CraftingItems.part(CraftingPart.STRING_CLUMP)), "2 string side by side make a String Clump");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void theFirstGridToolsAreCrafted(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack needle = craft(table, player, new ItemStack(Items.BONE), pebble());
            helper.assertTrue(needle.is(CraftingItems.tool(CraftingTool.BONE_NEEDLE)) && needle.getMaxDamage() == 64, "Bone and a pebble make a 64-use Bone Needle");
            table.grid().clearContent();
            ItemStack chisel = craft(table, player, new ItemStack(Items.FLINT), NO_ITEM, NO_ITEM, new ItemStack(CraftingItems.part(CraftingPart.TRIMMED_STICK)));
            helper.assertTrue(chisel.is(CraftingItems.tool(CraftingTool.FLINT_CHISEL)) && chisel.getMaxDamage() == 48, "Flint on a Trimmed Stick makes a 48-use Flint Chisel");
            table.grid().clearContent();
            ItemStack sticks = craft(table, player, WoodData.of(CraftingItems.trimmedPlanks(), OAK), pebble());
            helper.assertTrue(sticks.is(CraftingItems.part(CraftingPart.TRIMMED_STICK)) && sticks.getCount() == 2, "Trimmed Planks trim into 2 Trimmed Sticks");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void woodScrapsFollowTheConfig(GameTestHelper helper) {
        ConfigOverrides.withValue(CraftingServerConfig.get().woodScraps.perCraft, 3, () -> atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            craft(table, player, new ItemStack(Items.OAK_PLANKS), pebble());
            helper.assertTrue(player.getInventory().countItem(woodScraps()) == 3, "woodScraps.perCraft sets how many a craft gives");
        }));
        helper.assertTrue(ComposterBlock.COMPOSTABLES.getFloat(woodScraps()) == 0.3F, "Wood Scraps compost at 30%");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void woodenPartsBurnAndStoneOnesDont(GameTestHelper helper) {
        assertBurns(helper, new ItemStack(woodScraps()), 100);
        assertBurns(helper, new ItemStack(CraftingItems.part(CraftingPart.TRIMMED_STICK)), 100);
        assertBurns(helper, WoodData.of(CraftingItems.trimmedPlanks(), OAK), 300);
        assertBurns(helper, WoodData.of(CraftingItems.trimmedPlanks(), CRIMSON), 0);
        assertBurns(helper, new ItemStack(CraftingItems.table(MakeshiftTable.WOODEN)), 300);
        assertBurns(helper, new ItemStack(CraftingItems.table(MakeshiftTable.STONE)), 0);
        assertBurns(helper, new ItemStack(CraftingItems.part(CraftingPart.TWINE)), 0);
        ConfigOverrides.withValue(CraftingServerConfig.get().isFuelOn, false, () -> assertBurns(helper, new ItemStack(woodScraps()), 0));
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void dumpsShowWoodTierAndWear(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            List<DumpSection> itemSections = ImpactsDump.describeItem(WoodData.of(CraftingItems.trimmedPlanks(), OAK), player);
            helper.assertTrue(entry(itemSections, "lib", "wood").equals("minecraft:oak"), "dump held shows the wood");
            helper.assertTrue(entry(itemSections, "crafting", "tier").equals("non_metal"), "and Crafting's tier");
            List<DumpSection> blockSections = ImpactsDump.describeBlock(helper.getLevel(), helper.absolutePos(BlockPos.ZERO));
            helper.assertTrue(entry(blockSections, "crafting", "usesLeft").equals("128/128"), "dump block shows a table's uses left");
        });
        helper.succeed();
    }

    private static String entry(List<DumpSection> sections, String sectionName, String key) {
        return sections.stream().filter(section -> section.name().equals(sectionName)).findFirst()
                .map(section -> String.valueOf(section.entries().get(key))).orElse("");
    }

    private static void assertBurns(GameTestHelper helper, ItemStack stack, int expectedTicks) {
        int burnTicks = LoaderPlatform.current().burnTicks(stack);
        helper.assertTrue(burnTicks == expectedTicks, stack.getHoverName().getString() + " should burn " + expectedTicks + " ticks, burns " + burnTicks);
    }

    private static Optional<?> recipe(GameTestHelper helper, String recipeId) {
        return helper.getLevel().getRecipeManager().byKey(ResourceIds.tryParse(recipeId).orElseThrow());
    }

    private static ItemStack pebble() {
        return new ItemStack(CraftingItems.stonePebble());
    }

    private static Item woodScraps() {
        return CraftingItems.part(CraftingPart.WOOD_SCRAPS);
    }

    private static ResourceLocation wood(String name) {
        return ResourceIds.of("minecraft", name);
    }
}
