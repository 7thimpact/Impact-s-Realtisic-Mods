package com.impact.realisticcrafting.forge.gametest;

import com.impact.lib.ResourceIds;
import com.impact.lib.grinding.BowlContents;
import com.impact.lib.grinding.GrinderTier;
import com.impact.lib.grinding.Grinding;
import com.impact.lib.grinding.GrindingRecipe;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.CraftingTool;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.atTable;
import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.craft;

/** Crafting milestone 8: Lib's grinders from Crafting's parts, and every dye, powder and mix on Lib's grinding type. */
@GameTestHolder(CraftingMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CraftingGrindingGameTests {
    private static final String LIB_NAMESPACE = CraftingSkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = CraftingSkeletonGameTests.EMPTY_TEMPLATE;
    private static final ItemStack NO_ITEM = ItemStack.EMPTY;

    private CraftingGrindingGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void grindersComeFromCraftingsParts(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack bowl = craft(table, player, stack(Items.BOWL), stack(CraftingItems.trimmedPlanks()));
            helper.assertTrue(bowl.is(Grinding.woodenBowl()), "A bowl on Trimmed Planks makes a Wooden Grinder Bowl");
            table.grid().clearContent();
            ItemStack shapedStone = stack(CraftingItems.part(CraftingPart.SHAPED_STONE));
            ItemStack stoneBowl = craft(table, player, shapedStone.copy(), NO_ITEM, shapedStone.copy(), NO_ITEM, shapedStone.copy());
            helper.assertTrue(stoneBowl.is(Grinding.stoneBowl()), "Shaped Stone in a bowl shape makes a Stone Grinder Bowl");
            table.grid().clearContent();
            ItemStack stick = craft(table, player, stack(CraftingItems.part(CraftingPart.TRIMMED_STICK)), stack(CraftingItems.stonePebble()));
            helper.assertTrue(stick.is(Grinding.woodenStick()), "A Trimmed Stick rounded off with a pebble makes a Wooden Grinding Stick");
            table.grid().clearContent();
            ItemStack chisel = craft(table, player, stack(Items.FLINT), NO_ITEM, NO_ITEM, stack(CraftingItems.part(CraftingPart.TRIMMED_STICK)));
            helper.assertTrue(chisel.is(CraftingItems.tool(CraftingTool.FLINT_CHISEL)), "Flint over a Trimmed Stick stays the Flint Chisel");
            table.grid().clearContent();
            ItemStack stoneStick = craft(table, player, shapedStone.copy(), NO_ITEM, NO_ITEM, stack(CraftingItems.part(CraftingPart.TRIMMED_STICK)));
            helper.assertTrue(stoneStick.is(Grinding.stoneStick()), "Shaped Stone on a Trimmed Stick makes a Stone Grinding Stick");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void dyesAndPowdersAreGround(GameTestHelper helper) {
        assertGrinds(helper, List.of(new ItemStack(Items.POPPY, 4)), Items.RED_DYE, 1, GrinderTier.WOOD);
        assertGrinds(helper, List.of(new ItemStack(Items.SUNFLOWER)), Items.YELLOW_DYE, 2, GrinderTier.WOOD);
        assertGrinds(helper, List.of(new ItemStack(Items.BONE)), Items.BONE_MEAL, 3, GrinderTier.STONE);
        assertGrinds(helper, List.of(new ItemStack(Items.LAPIS_LAZULI)), Items.BLUE_DYE, 1, GrinderTier.STONE);
        assertGrinds(helper, List.of(new ItemStack(Items.BLAZE_ROD)), Items.BLAZE_POWDER, 2, GrinderTier.STONE);
        assertGrinds(helper, List.of(new ItemStack(Items.RED_DYE), new ItemStack(Items.YELLOW_DYE)), Items.ORANGE_DYE, 2, GrinderTier.WOOD);
        assertGrinds(helper, List.of(new ItemStack(Items.GRAY_DYE), new ItemStack(Items.WHITE_DYE)), Items.LIGHT_GRAY_DYE, 2, GrinderTier.WOOD);
        for (String vanillaRecipe : List.of("red_dye_from_poppy", "bone_meal", "orange_dye_from_red_yellow", "sugar_from_sugar_cane", "blaze_powder")) {
            helper.assertTrue(helper.getLevel().getRecipeManager().byKey(ResourceIds.of("minecraft", vanillaRecipe)).isEmpty(),
                    "No dye or powder is crafted in a grid: " + vanillaRecipe + " is off");
        }
        helper.assertTrue(helper.getLevel().getRecipeManager().byKey(ResourceIds.of("minecraft", "green_dye")).isPresent(), "Green dye stays smelted");
        helper.succeed();
    }

    private static void assertGrinds(GameTestHelper helper, List<ItemStack> loaded, Item expected, int count, GrinderTier tier) {
        ItemStack bowl = new ItemStack(Grinding.stoneBowl());
        loaded.forEach(stack -> BowlContents.load(bowl, stack.copy()));
        Optional<GrindingRecipe> recipe = helper.getLevel().getRecipeManager()
                .getRecipeFor(Grinding.recipeType(), BowlContents.asContainer(bowl), helper.getLevel());
        String what = loaded.stream().map(stack -> ResourceIds.itemId(stack.getItem()).getPath()).toList().toString();
        helper.assertTrue(recipe.isPresent(), what + " should grind");
        ItemStack result = recipe.get().getResultItem(helper.getLevel().registryAccess());
        helper.assertTrue(result.is(expected) && result.getCount() == count, what + " should grind into " + count + " " + ResourceIds.itemId(expected));
        helper.assertTrue(recipe.get().minimumTier() == tier, what + " should need a " + tier.getSerializedName() + " bowl");
    }

    private static ItemStack stack(Item item) {
        return new ItemStack(item);
    }
}
