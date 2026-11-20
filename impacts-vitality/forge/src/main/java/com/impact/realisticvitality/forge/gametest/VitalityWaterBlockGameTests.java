package com.impact.realisticvitality.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.registry.VitalityBlocks;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.thirst.BottledWater;
import com.impact.realisticvitality.thirst.ContainerContents;
import com.impact.realisticvitality.thirst.WaterContainer;
import com.impact.realisticvitality.thirst.WaterPurity;
import com.impact.realisticvitality.thirst.block.CopperStillBlock;
import com.impact.realisticvitality.thirst.block.CopperStillBlockEntity;
import com.impact.realisticvitality.thirst.block.RainBarrelBlock;
import com.impact.realisticvitality.thirst.block.RainBarrelBlockEntity;
import com.impact.realisticvitality.thirst.block.WaterFilterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.function.Consumer;

/** Vitality milestone 3, part 3: the Rain Barrel collects rain, the Water Filter and Copper Still purify water. */
@GameTestHolder(VitalityMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class VitalityWaterBlockGameTests {
    private static final String LIB_NAMESPACE = VitalitySkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = VitalitySkeletonGameTests.EMPTY_TEMPLATE;

    private VitalityWaterBlockGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void rainBarrelCollectsADrinkPerMinuteOfRain(GameTestHelper helper) {
        BlockPos position = helper.absolutePos(BlockPos.ZERO);
        helper.getLevel().setBlockAndUpdate(position, VitalityBlocks.rainBarrel().defaultBlockState());
        RainBarrelBlockEntity barrel = (RainBarrelBlockEntity) helper.getLevel().getBlockEntity(position);
        int ticksPerDrink = VitalityServerConfig.get().waterBlocks.rainBarrelTicksPerDrink.get();
        for (int tick = 0; tick < ticksPerDrink; tick++) {
            barrel.collectRain(true);
        }
        helper.assertTrue(barrel.tank().drinks() == 1 && barrel.tank().purity() == WaterPurity.PURIFIED, "A minute of rain gives 1 drink of clean water");
        helper.assertTrue(helper.getLevel().getBlockState(position).getValue(RainBarrelBlock.FILL) == 1, "The barrel shows some water");
        for (int tick = 0; tick < ticksPerDrink; tick++) {
            barrel.collectRain(false);
        }
        helper.assertTrue(barrel.tank().drinks() == 1, "Without rain it collects nothing");
        withPlayer(helper, player -> {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
            useBlock(helper, player, position);
            helper.assertTrue(player.getMainHandItem().is(VitalityItems.waterBottle(BottledWater.PURIFIED)), "A bottle fills with purified rainwater");
            helper.assertTrue(barrel.tank().isEmpty(), "and empties the barrel");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void waterFilterDripsPurifiedWaterThroughAFilter(GameTestHelper helper) {
        BlockPos position = helper.absolutePos(BlockPos.ZERO);
        helper.getLevel().setBlockAndUpdate(position, VitalityBlocks.waterFilter().defaultBlockState());
        WaterFilterBlockEntity filter = (WaterFilterBlockEntity) helper.getLevel().getBlockEntity(position);
        withPlayer(helper, player -> {
            player.setItemInHand(InteractionHand.MAIN_HAND, VitalityItems.bottle(BottledWater.SALT));
            useBlock(helper, player, position);
            helper.assertTrue(filter.input().drinks() == 1 && player.getMainHandItem().is(Items.GLASS_BOTTLE), "Salt water pours in, leaving the bottle");
            int ticksPerDrink = VitalityServerConfig.get().waterBlocks.waterFilterTicksPerDrink.get();
            tickFilter(filter, ticksPerDrink);
            helper.assertTrue(filter.output().isEmpty(), "Without a Charcoal Filter nothing drips");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(VitalityItems.charcoalFilter()));
            useBlock(helper, player, position);
            helper.assertFalse(filter.charcoalFilter().isEmpty(), "A Charcoal Filter goes in");
            tickFilter(filter, ticksPerDrink);
            helper.assertTrue(filter.output().drinks() == 1 && filter.output().purity() == WaterPurity.PURIFIED, "It drips 1 drink of purified water");
            int wear = VitalityServerConfig.get().waterBlocks.waterFilterWearPerDrink.get();
            helper.assertTrue(filter.charcoalFilter().getDamageValue() == wear, "Each drink wears the filter");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(VitalityItems.container(WaterContainer.CANTEEN)));
            useBlock(helper, player, position);
            helper.assertTrue(ContainerContents.read(player.getMainHandItem()).purity() == WaterPurity.PURIFIED, "A canteen takes the purified water");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void copperStillPurifiesOnlyWhenHeated(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos campfire = helper.absolutePos(BlockPos.ZERO);
        BlockPos position = campfire.above();
        level.setBlockAndUpdate(campfire, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
        level.setBlockAndUpdate(position, VitalityBlocks.copperStill().defaultBlockState());
        CopperStillBlockEntity still = (CopperStillBlockEntity) level.getBlockEntity(position);
        try {
            still.setItem(CopperStillBlockEntity.WATER_SLOT, new ItemStack(Items.WATER_BUCKET));
            still.setItem(CopperStillBlockEntity.GLASS_BOTTLE_SLOT, new ItemStack(Items.GLASS_BOTTLE, 2));
            int ticksPerDrink = VitalityServerConfig.get().waterBlocks.copperStillTicksPerBucket.get() / VitalityServerConfig.get().waterBlocks.bucketDrinks.get();
            tickStill(helper, still, position, ticksPerDrink);
            helper.assertTrue(still.input().drinks() == 4, "A water bucket is 4 drinks");
            helper.assertTrue(still.getItem(CopperStillBlockEntity.EMPTIED_SLOT).is(Items.BUCKET), "The empty bucket waits for a hopper");
            helper.assertTrue(still.getItem(CopperStillBlockEntity.PURIFIED_SLOT).isEmpty(), "An unheated still purifies nothing");
            level.setBlockAndUpdate(campfire, Blocks.CAMPFIRE.defaultBlockState());
            tickStill(helper, still, position, ticksPerDrink);
            helper.assertTrue(still.getItem(CopperStillBlockEntity.PURIFIED_SLOT).is(VitalityItems.waterBottle(BottledWater.PURIFIED)),
                    "Over a lit campfire it bottles purified water, got " + still.getItem(CopperStillBlockEntity.PURIFIED_SLOT));
            helper.assertTrue(level.getBlockState(position).getValue(CopperStillBlock.LIT), "A working still shows it");
        } finally {
            level.setBlockAndUpdate(position, Blocks.AIR.defaultBlockState());
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void hoppersFeedTheStillAndEmptyIt(GameTestHelper helper) {
        BlockPos position = helper.absolutePos(BlockPos.ZERO);
        helper.getLevel().setBlockAndUpdate(position, VitalityBlocks.copperStill().defaultBlockState());
        CopperStillBlockEntity still = (CopperStillBlockEntity) helper.getLevel().getBlockEntity(position);
        int[] fedSlots = still.getSlotsForFace(Direction.UP);
        helper.assertTrue(still.canPlaceItemThroughFace(fedSlots[0], new ItemStack(Items.WATER_BUCKET), Direction.UP), "Hoppers feed water buckets");
        helper.assertTrue(still.canPlaceItemThroughFace(CopperStillBlockEntity.WATER_SLOT, VitalityItems.bottle(BottledWater.UNCLEAN), Direction.NORTH),
                "and water bottles");
        helper.assertFalse(still.canPlaceItemThroughFace(CopperStillBlockEntity.WATER_SLOT, new ItemStack(Items.DIRT), Direction.UP), "but not dirt");
        helper.assertTrue(still.canPlaceItemThroughFace(CopperStillBlockEntity.GLASS_BOTTLE_SLOT, new ItemStack(Items.GLASS_BOTTLE), Direction.EAST),
                "Hoppers feed glass bottles");
        int[] takenSlots = still.getSlotsForFace(Direction.DOWN);
        helper.assertTrue(takenSlots.length == 2 && still.canTakeItemThroughFace(CopperStillBlockEntity.PURIFIED_SLOT, ItemStack.EMPTY, Direction.DOWN),
                "Hoppers below take purified water and empties");
        helper.succeed();
    }

    private static void tickFilter(WaterFilterBlockEntity filter, int ticks) {
        for (int tick = 0; tick < ticks; tick++) {
            filter.tickFiltering();
        }
    }

    private static void tickStill(GameTestHelper helper, CopperStillBlockEntity still, BlockPos position, int ticks) {
        boolean isHeated = ImpactsLib.isHeated(helper.getLevel(), position.below());
        for (int tick = 0; tick <= ticks; tick++) {
            still.tickStill(isHeated);
        }
    }

    private static void useBlock(GameTestHelper helper, ServerPlayer player, BlockPos position) {
        helper.getLevel().getBlockState(position).use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(position), Direction.UP, position, false));
    }

    private static void withPlayer(GameTestHelper helper, Consumer<ServerPlayer> test) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            test.accept(player);
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }
}
