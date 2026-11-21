package com.impact.realisticinventory.forge.gametest;

import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.pack.InventoryItems;
import com.impact.realisticinventory.pack.PackContents;
import com.impact.realisticinventory.pack.PackMenu;
import com.impact.realisticinventory.pack.PlacedPackBlock;
import com.impact.realisticinventory.pack.PlacedPackBlockEntity;
import com.impact.realisticinventory.pack.PlacedPacks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** Inventory milestone 7: packs set down as blocks. */
@GameTestHolder(InventoryMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InventoryPlacedPackGameTests {
    private static final int APPLE_COUNT = 5;
    private static final int RED_DYE_COLOR = 0xB02E26;
    private static final BlockPos FLOOR = BlockPos.ZERO;
    private static final double DROP_SEARCH_BLOCKS = 2.0;

    private InventoryPlacedPackGameTests() {
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void placingAndPickingUpKeepsContentsAndColor(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        BlockPos floor = helper.absolutePos(FLOOR);
        BlockPos packPosition = floor.above();
        try {
            helper.getLevel().setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
            player.setItemInHand(InteractionHand.MAIN_HAND, filledRedBackpack());
            player.setShiftKeyDown(true);
            BlockHitResult topOfFloor = new BlockHitResult(Vec3.atCenterOf(floor), Direction.UP, floor, false);
            player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, topOfFloor));
            helper.assertTrue(helper.getLevel().getBlockState(packPosition).is(PlacedPacks.block()), "Sneak-right-click sets the backpack down");
            helper.assertTrue(player.getMainHandItem().isEmpty(), "The backpack leaves the hand");
            PlacedPackBlockEntity placedPack = (PlacedPackBlockEntity) helper.getLevel().getBlockEntity(packPosition);
            helper.assertTrue(placedPack.getItem(0).is(Items.APPLE), "The placed pack holds the apples like a chest");
            helper.assertTrue(PlacedPacks.pickUp(player, packPosition), "Sneak-right-click with an empty hand picks it up");
            ItemStack pickedUp = player.getMainHandItem();
            helper.assertTrue(pickedUp.is(InventoryItems.backpack()), "The backpack is back in the hand");
            helper.assertTrue(InventoryItems.backpack().getColor(pickedUp) == RED_DYE_COLOR, "It keeps its color");
            helper.assertTrue(PackContents.stacksIn(pickedUp).stream().anyMatch(stack -> stack.is(Items.APPLE) && stack.getCount() == APPLE_COUNT),
                    "It keeps its contents");
            helper.assertTrue(helper.getLevel().getBlockState(packPosition).isAir(), "The block is gone");
        } finally {
            player.setShiftKeyDown(false);
            helper.getLevel().setBlockAndUpdate(packPosition, Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(floor, Blocks.AIR.defaultBlockState());
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void breakingAPlacedPackDropsItWhole(GameTestHelper helper) {
        BlockPos packPosition = helper.absolutePos(FLOOR);
        PlacedPacks.place(helper.getLevel(), packPosition, Direction.NORTH, filledRedBackpack());
        helper.getLevel().destroyBlock(packPosition, false);
        AABB searchArea = new AABB(packPosition).inflate(DROP_SEARCH_BLOCKS);
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, searchArea, drop -> drop.getItem().is(InventoryItems.backpack()));
        helper.assertTrue(drops.size() == 1, "Breaking it drops the backpack");
        ItemStack dropped = drops.get(0).getItem();
        helper.assertTrue(PackContents.stacksIn(dropped).stream().anyMatch(stack -> stack.is(Items.APPLE)), "The dropped backpack keeps its contents");
        drops.forEach(ItemEntity::discard);
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void aPlacedPackOpensLikeASmallChest(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        BlockPos packPosition = helper.absolutePos(FLOOR);
        try {
            player.teleportTo(packPosition.getX() + 0.5, packPosition.getY(), packPosition.getZ() + 1.5);
            PlacedPacks.place(helper.getLevel(), packPosition, Direction.SOUTH, filledRedBackpack());
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(packPosition), Direction.SOUTH, packPosition, false);
            helper.getLevel().getBlockState(packPosition).use(helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
            helper.assertTrue(player.containerMenu instanceof PackMenu, "Right-click opens the placed pack");
            helper.assertTrue(player.containerMenu.slots.get(0).getItem().is(Items.APPLE), "Its contents show in the menu");
            PlacedPackBlockEntity placedPack = (PlacedPackBlockEntity) helper.getLevel().getBlockEntity(packPosition);
            helper.assertFalse(placedPack.canPlaceItem(1, new ItemStack(InventoryItems.satchel())), "Packs can't go inside a placed pack either");
            helper.assertTrue(helper.getLevel().getBlockState(packPosition).getValue(PlacedPackBlock.FACING) == Direction.SOUTH, "It faces the way it was set down");
            player.closeContainer();
        } finally {
            helper.getLevel().getBlockEntity(packPosition, PlacedPacks.blockEntityType()).ifPresent(PlacedPackBlockEntity::takePack);
            helper.getLevel().setBlockAndUpdate(packPosition, Blocks.AIR.defaultBlockState());
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    private static ItemStack filledRedBackpack() {
        ItemStack backpack = new ItemStack(InventoryItems.backpack());
        InventoryItems.backpack().setColor(backpack, RED_DYE_COLOR);
        NonNullList<ItemStack> slots = NonNullList.withSize(InventoryItems.backpack().kind().slotCount(), ItemStack.EMPTY);
        slots.set(0, new ItemStack(Items.APPLE, APPLE_COUNT));
        PackContents.write(backpack, slots);
        return backpack;
    }
}
