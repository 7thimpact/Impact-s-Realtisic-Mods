package com.impact.realisticcrafting.forge.gametest;

import com.impact.lib.ResourceIds;
import com.impact.lib.gametest.ConfigOverrides;
import com.impact.lib.gametest.MockPlayers;
import com.impact.lib.wood.WoodData;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.bed.BedFrameBlock;
import com.impact.realisticcrafting.bed.BedLayering;
import com.impact.realisticcrafting.block.MakeshiftTable;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.CraftingTool;
import com.impact.realisticcrafting.item.WoodPart;
import com.impact.realisticcrafting.recipe.CraftingRecipeTypes;
import com.impact.realisticcrafting.recipe.VatDyeingRecipe;
import com.impact.realisticcrafting.registry.CraftingBlocks;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.function.BiConsumer;

import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.atTable;
import static com.impact.realisticcrafting.forge.gametest.CraftingTestGrids.craft;

/** Crafting milestone 10: bed parts, building a bed in three layers, recoloring, and beds dropping their parts. */
@GameTestHolder(CraftingMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CraftingBedGameTests {
    private static final String LIB_NAMESPACE = CraftingSkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = CraftingSkeletonGameTests.EMPTY_TEMPLATE;
    private static final ItemStack NO_ITEM = ItemStack.EMPTY;
    private static final int QUIET = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    private CraftingBedGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void bedPartsAreSewn(GameTestHelper helper) {
        atTable(helper, MakeshiftTable.WOODEN, (player, table) -> {
            ItemStack planks = WoodData.of(CraftingItems.trimmedPlanks(), ResourceIds.of("minecraft", "oak"));
            ItemStack frame = craft(table, player, WoodData.of(CraftingItems.woodPart(WoodPart.WOODEN_FRAME), ResourceIds.of("minecraft", "oak")),
                    planks.copy(), planks.copy(), planks.copy());
            helper.assertTrue(frame.is(CraftingItems.bedFrame()), "A Wooden Frame and 3 Trimmed Planks make a Bed Frame");
            table.grid().clearContent();
            ItemStack comforter = craft(table, player, stack(Items.WHITE_WOOL), twine(), stack(Items.WHITE_WOOL), NO_ITEM, stack(Items.WHITE_WOOL), needle());
            helper.assertTrue(comforter.is(CraftingItems.comforter()), "White wool around Twine, sewn with the Bone Needle, makes a Comforter");
            table.grid().clearContent();
            ItemStack refused = craft(table, player, stack(Items.RED_WOOL), twine(), stack(Items.RED_WOOL), NO_ITEM, stack(Items.RED_WOOL), needle());
            helper.assertTrue(refused.isEmpty(), "Only white wool makes a comforter");
            table.grid().clearContent();
            ItemStack sheet = craft(table, player, stack(Items.RED_WOOL), stack(Items.RED_WOOL), stack(Items.RED_WOOL), NO_ITEM, twine(), needle());
            helper.assertTrue(sheet.is(CraftingItems.sheet(DyeColor.RED)), "3 red wool over Twine make a red Sheet");
        });
        helper.assertTrue(helper.getLevel().getRecipeManager().byKey(ResourceIds.of("minecraft", "red_bed")).isEmpty(), "Vanilla's bed recipe is off");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void aBedIsBuiltInThreeLayers(GameTestHelper helper) {
        atFrame(helper, (player, foot) -> {
            ServerLevel level = helper.getLevel();
            BlockPos head = foot.north();
            helper.assertTrue(use(player, foot, stack(CraftingItems.sheet(DyeColor.RED))) == InteractionResult.FAIL, "A sheet on a bare frame is refused");
            helper.assertTrue(player.getMainHandItem().getCount() == 1 && level.getBlockState(foot).is(CraftingBlocks.bedFrame()), "and nothing changes");
            use(player, head, stack(CraftingItems.comforter()));
            helper.assertTrue(level.getBlockState(foot).getValue(BedFrameBlock.COMFORTER) && level.getBlockState(head).getValue(BedFrameBlock.COMFORTER),
                    "The comforter goes on, and either half works");
            helper.assertTrue(player.getMainHandItem().isEmpty(), "using up the comforter");
            use(player, foot, stack(CraftingItems.sheet(DyeColor.RED)));
            BlockState finishedFoot = level.getBlockState(foot);
            BlockState finishedHead = level.getBlockState(head);
            helper.assertTrue(finishedFoot.is(Blocks.RED_BED) && finishedHead.is(Blocks.RED_BED), "The sheet makes both halves a red bed");
            helper.assertTrue(finishedFoot.getValue(BedBlock.PART) == BedPart.FOOT && finishedHead.getValue(BedBlock.PART) == BedPart.HEAD
                    && finishedFoot.getValue(BedBlock.FACING) == Direction.NORTH, "facing the same way");

            player.setItemInHand(InteractionHand.MAIN_HAND, stack(CraftingItems.sheet(DyeColor.BLUE)));
            BedLayering.onBedUsed(level, head, finishedHead, player, InteractionHand.MAIN_HAND);
            helper.assertTrue(level.getBlockState(foot).is(Blocks.BLUE_BED) && level.getBlockState(head).is(Blocks.BLUE_BED),
                    "A blue sheet on a red bed makes it blue");
            List<ItemEntity> dropped = level.getEntitiesOfClass(ItemEntity.class, new AABB(head).inflate(1.5));
            helper.assertTrue(dropped.stream().anyMatch(item -> item.getItem().is(CraftingItems.sheet(DyeColor.RED))), "and the red sheet drops");
            dropped.forEach(ItemEntity::discard);
        });
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void bedsDropTheirParts(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos head = helper.absolutePos(BlockPos.ZERO);
        try {
            level.setBlock(head, Blocks.LIME_BED.defaultBlockState().setValue(BedBlock.PART, BedPart.HEAD), QUIET);
            List<ItemStack> parts = Block.getDrops(level.getBlockState(head), level, head, level.getBlockEntity(head));
            helper.assertTrue(contains(parts, CraftingItems.bedFrame()) && contains(parts, CraftingItems.comforter())
                    && contains(parts, CraftingItems.sheet(DyeColor.LIME)) && parts.size() == 3, "A broken lime bed drops a frame, a comforter and a lime sheet");
            ConfigOverrides.withValue(CraftingServerConfig.get().bedsDropParts, false, () -> {
                List<ItemStack> bed = Block.getDrops(level.getBlockState(head), level, head, level.getBlockEntity(head));
                helper.assertTrue(contains(bed, Items.LIME_BED), "With bedsDropParts off, it drops the bed");
            });
            level.setBlock(head, CraftingBlocks.bedFrame().defaultBlockState().setValue(BedFrameBlock.PART, BedPart.HEAD)
                    .setValue(BedFrameBlock.COMFORTER, true), QUIET);
            List<ItemStack> unfinished = Block.getDrops(level.getBlockState(head), level, head, null);
            helper.assertTrue(contains(unfinished, CraftingItems.bedFrame()) && contains(unfinished, CraftingItems.comforter()) && unfinished.size() == 2,
                    "An unfinished bed drops the frame and what's on it");
            level.setBlock(head, CraftingBlocks.bedFrame().defaultBlockState(), QUIET);
            helper.assertTrue(Block.getDrops(level.getBlockState(head), level, head, null).isEmpty(), "Only the head half drops");
        } finally {
            level.setBlock(head, Blocks.AIR.defaultBlockState(), QUIET);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void aWhiteSheetDipsInTheVat(GameTestHelper helper) {
        boolean dyes = helper.getLevel().getRecipeManager().getAllRecipesFor(CraftingRecipeTypes.vatDyeing()).stream()
                .map(recipe -> recipe.resultFor(stack(CraftingItems.sheet(DyeColor.WHITE)), DyeColor.PURPLE.getName()))
                .anyMatch(result -> result.isPresent() && result.get().is(CraftingItems.sheet(DyeColor.PURPLE)));
        helper.assertTrue(dyes, "A white sheet dipped in a purple vat becomes a purple sheet");
        helper.assertTrue(VatDyeingRecipe.PLAIN_WATER_KEY.equals("none"), "Plain water recipes use the color key none");
        helper.succeed();
    }

    static void atFrame(GameTestHelper helper, BiConsumer<ServerPlayer, BlockPos> check) {
        ServerLevel level = helper.getLevel();
        BlockPos foot = helper.absolutePos(new BlockPos(0, 0, 1));
        ServerPlayer player = MockPlayers.joinWorld(level);
        BlockState footState = CraftingBlocks.bedFrame().defaultBlockState().setValue(BedFrameBlock.FACING, Direction.NORTH);
        level.setBlock(foot, footState, QUIET);
        level.setBlock(foot.north(), footState.setValue(BedFrameBlock.PART, BedPart.HEAD), QUIET);
        try {
            check.accept(player, foot);
        } finally {
            level.setBlock(foot.north(), Blocks.AIR.defaultBlockState(), QUIET);
            level.setBlock(foot, Blocks.AIR.defaultBlockState(), QUIET);
            MockPlayers.leaveWorld(player);
        }
    }

    static InteractionResult use(ServerPlayer player, BlockPos position, ItemStack held) {
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        return BedLayering.onFrameUsed(player.level(), position, player.level().getBlockState(position), player, InteractionHand.MAIN_HAND);
    }

    private static boolean contains(List<ItemStack> drops, Item item) {
        return drops.stream().anyMatch(stack -> stack.is(item));
    }

    private static ItemStack twine() {
        return stack(CraftingItems.part(CraftingPart.TWINE));
    }

    private static ItemStack needle() {
        return stack(CraftingItems.tool(CraftingTool.BONE_NEEDLE));
    }

    private static ItemStack stack(Item item) {
        return new ItemStack(item);
    }
}
