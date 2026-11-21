package com.impact.realisticcrafting.forge.gametest;

import com.impact.lib.ResourceIds;
import com.impact.lib.gametest.MockPlayers;
import com.impact.lib.item.UsesDurability;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.block.DyeVatBlock;
import com.impact.realisticcrafting.block.DyeVatBlockEntity;
import com.impact.realisticcrafting.block.DyeVatInteractions;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.registry.CraftingBlocks;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.atTable;
import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.craft;

/** Crafting milestone 9: the Dye Vat (water, dye and mixing, dipping, leather-style items, washing, Paper Pulp). */
@GameTestHolder(CraftingMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CraftingDyeVatGameTests {
    private static final String LIB_NAMESPACE = CraftingSkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = CraftingSkeletonGameTests.EMPTY_TEMPLATE;
    private static final String CONTENTS_KEY = "BlockEntityTag";

    private CraftingDyeVatGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void theVatIsABucketLashedWithTwine(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack twine = new ItemStack(CraftingItems.part(CraftingPart.TWINE));
            ItemStack vat = craft(table, player, new ItemStack(CraftingItems.woodenBucket()), twine.copy(), twine.copy());
            helper.assertTrue(vat.is(CraftingItems.dyeVat()), "An unused Wooden Bucket and 2 Twine make a Dye Vat");
        });
        for (String vanillaRecipe : List.of("dye_red_wool", "red_stained_glass", "red_terracotta", "armor_dye", "paper")) {
            helper.assertTrue(helper.getLevel().getRecipeManager().byKey(ResourceIds.of("minecraft", vanillaRecipe)).isEmpty(),
                    "Grid dyeing is off: " + vanillaRecipe);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void waterThenDyeThenMixing(GameTestHelper helper) {
        atVat(helper, (player, vat) -> {
            ItemStack filledBucket = new ItemStack(CraftingItems.woodenWaterBucket());
            filledBucket.getOrCreateTag().putInt(UsesDurability.USES_KEY, 1);
            helper.assertTrue(use(player, vat, filledBucket) == InteractionResult.CONSUME && vat.hasWater(), "A Water-filled Wooden Bucket fills the vat");
            ItemStack backInHand = player.getMainHandItem();
            helper.assertTrue(backInHand.is(CraftingItems.woodenBucket()) && UsesDurability.usesSpent(backInHand) == 2,
                    "and comes back empty, one more use spent");
            use(player, vat, new ItemStack(Items.RED_DYE));
            helper.assertTrue(vat.color().equals(Optional.of(DyeColor.RED)) && vat.charges() == 8, "Red dye adds 8 red charges");
            use(player, vat, new ItemStack(Items.YELLOW_DYE));
            helper.assertTrue(vat.color().equals(Optional.of(DyeColor.ORANGE)) && vat.charges() == 16, "Yellow mixes with red into orange");
            helper.assertTrue(use(player, vat, new ItemStack(Items.BLUE_DYE)) == InteractionResult.FAIL, "Blue doesn't mix with orange: refused");
            helper.assertTrue(vat.color().equals(Optional.of(DyeColor.ORANGE)) && vat.charges() == 16, "and the vat is unchanged");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void dippingTakesOneItemAndOneCharge(GameTestHelper helper) {
        atVat(helper, (player, vat) -> {
            vat.fillWithWater();
            vat.addDye(DyeColor.BLUE, Optional.empty());
            use(player, vat, new ItemStack(Items.WHITE_WOOL, 3));
            helper.assertTrue(player.getMainHandItem().getCount() == 2, "One wool is dipped at a time");
            helper.assertTrue(player.getInventory().countItem(Items.BLUE_WOOL) == 1 && vat.charges() == 7, "into blue wool, for 1 charge");
            use(player, vat, player.getMainHandItem());
            helper.assertTrue(player.getMainHandItem().getCount() == 2, "The next dip waits for the dip time");
            DyeVatInteractions.forgetLastDip(player);
            use(player, vat, player.getMainHandItem());
            helper.assertTrue(player.getInventory().countItem(Items.BLUE_WOOL) == 2 && vat.charges() == 6, "then dips the next one");
            ItemStack shulkerBox = new ItemStack(Items.SHULKER_BOX);
            CompoundTag contents = new CompoundTag();
            contents.putString("Lock", "kept");
            shulkerBox.getOrCreateTag().put(CONTENTS_KEY, contents);
            DyeVatInteractions.forgetLastDip(player);
            use(player, vat, shulkerBox);
            ItemStack blueBox = player.getMainHandItem();
            helper.assertTrue(blueBox.is(Items.BLUE_SHULKER_BOX) && blueBox.getTag().getCompound(CONTENTS_KEY).equals(contents),
                    "A dipped shulker box keeps its contents");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void leatherIsDyedAndWashed(GameTestHelper helper) {
        atVat(helper, (player, vat) -> {
            vat.fillWithWater();
            vat.addDye(DyeColor.RED, Optional.empty());
            ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
            ItemStack expected = DyeableLeatherItem.dyeArmor(helmet.copy(), List.of(DyeItem.byColor(DyeColor.RED)));
            use(player, vat, helmet);
            ItemStack dyed = player.getMainHandItem();
            helper.assertTrue(((DyeableLeatherItem) Items.LEATHER_HELMET).getColor(dyed) == ((DyeableLeatherItem) Items.LEATHER_HELMET).getColor(expected),
                    "Leather takes the vat's color the way vanilla dyes leather");
            vat.spendCharges(vat.charges());
            helper.assertTrue(vat.color().isEmpty() && vat.hasWater(), "A vat out of dye is plain water");
            DyeVatInteractions.forgetLastDip(player);
            use(player, vat, dyed);
            helper.assertFalse(((DyeableLeatherItem) Items.LEATHER_HELMET).hasCustomColor(player.getMainHandItem()), "Plain water washes the dye off");
            DyeVatInteractions.forgetLastDip(player);
            use(player, vat, new ItemStack(Items.SUGAR_CANE));
            helper.assertTrue(player.getMainHandItem().is(CraftingItems.part(CraftingPart.PAPER_PULP)), "and soaks sugar cane into Paper Pulp");
        });
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack pulp = new ItemStack(CraftingItems.part(CraftingPart.PAPER_PULP));
            ItemStack paper = craft(table, player, pulp.copy(), pulp.copy(), pulp.copy());
            helper.assertTrue(paper.is(Items.PAPER) && paper.getCount() == 3, "3 Paper Pulp make 3 paper");
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void rainFillsAndAnEmptyHandEmpties(GameTestHelper helper) {
        atVat(helper, (player, vat) -> {
            int rainTicks = CraftingServerConfig.get().dyeVat.rainFillTicks.get();
            for (int tick = 0; tick < rainTicks - 1; tick++) {
                vat.collectRain(true);
            }
            helper.assertFalse(vat.hasWater(), "Not full before a minute of rain");
            vat.collectRain(true);
            helper.assertTrue(vat.hasWater(), "Rain fills an empty vat");
            helper.assertTrue(helper.getLevel().getBlockState(vat.getBlockPos()).getValue(DyeVatBlock.FILLED),
                    "and shows water");
            player.setShiftKeyDown(true);
            use(player, vat, ItemStack.EMPTY);
            helper.assertFalse(vat.hasWater(), "Shift-right-click with an empty hand empties it");
        });
        helper.succeed();
    }

    private static void atVat(GameTestHelper helper, BiConsumer<ServerPlayer, DyeVatBlockEntity> check) {
        ServerLevel level = helper.getLevel();
        BlockPos position = helper.absolutePos(BlockPos.ZERO);
        ServerPlayer player = MockPlayers.joinWorld(level);
        level.setBlockAndUpdate(position, CraftingBlocks.dyeVat().defaultBlockState());
        try {
            check.accept(player, (DyeVatBlockEntity) level.getBlockEntity(position));
        } finally {
            DyeVatInteractions.forgetLastDip(player);
            level.setBlockAndUpdate(position, Blocks.AIR.defaultBlockState());
            MockPlayers.leaveWorld(player);
        }
    }

    private static InteractionResult use(ServerPlayer player, DyeVatBlockEntity vat, ItemStack held) {
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        return DyeVatInteractions.use(vat, player.level(), vat.getBlockPos(), player, InteractionHand.MAIN_HAND);
    }
}
