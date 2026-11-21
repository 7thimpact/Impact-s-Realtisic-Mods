package com.impact.realisticinventory.forge.gametest;

import com.impact.lib.attachment.PlayerAttachments;
import com.impact.lib.gametest.ConfigOverrides;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.config.InventoryServerConfig;
import com.impact.realisticinventory.inventory.InventoryLayout;
import com.impact.realisticinventory.inventory.LockedSlotOverflow;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

/** Inventory milestone 5: the first-join move, creative mode ending, and stray items in locked slots. */
@GameTestHolder(InventoryMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InventoryMigrationGameTests {
    private static final int LOCKED_SLOT_COUNT = 26;
    private static final int STRAY_INDEX = 25;
    private static final int STACK_COUNT = 2;
    // 35 items that never stack with each other: one per locked slot, then one per pocket.
    private static final List<Item> DISTINCT_ITEMS = List.of(Items.STONE, Items.DIRT, Items.COBBLESTONE, Items.OAK_PLANKS,
            Items.SAND, Items.GRAVEL, Items.OAK_LOG, Items.GLASS, Items.BRICK, Items.CLAY_BALL, Items.COAL, Items.IRON_INGOT,
            Items.GOLD_INGOT, Items.DIAMOND, Items.EMERALD, Items.REDSTONE, Items.LAPIS_LAZULI, Items.QUARTZ, Items.FLINT,
            Items.FEATHER, Items.STRING, Items.BONE, Items.ARROW, Items.PAPER, Items.BOOK, Items.SUGAR, Items.WHEAT,
            Items.WHEAT_SEEDS, Items.LEATHER, Items.GUNPOWDER, Items.SLIME_BALL, Items.GLOWSTONE_DUST, Items.STICK, Items.BOWL,
            Items.TORCH);

    private InventoryMigrationGameTests() {
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void firstJoinMovesLockedItemsIntoPocketsThenAChest(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        Optional<BlockPos> chestPosition = Optional.empty();
        try {
            Inventory inventory = player.getInventory();
            fillPockets(inventory);
            fillLockedSlots(inventory);
            endCreativeMode(player);
            helper.assertTrue(countLockedStacks(inventory) == 0, "Every locked slot is empty after the first-join move");
            chestPosition = findChestNear(player);
            helper.assertTrue(chestPosition.isPresent(), "Items that didn't fit go into a chest at the player's feet");
            helper.assertTrue(countStacks(chestAt(player, chestPosition.get())) == LOCKED_SLOT_COUNT, "The chest holds all 26 moved stacks");
            helper.assertTrue(PlayerAttachments.get(player, LockedSlotOverflow.MIGRATION).isFirstJoinDone(), "The first-join move is recorded");
        } finally {
            chestPosition.ifPresent(position -> helper.getLevel().setBlockAndUpdate(position, Blocks.AIR.defaultBlockState()));
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void lockedItemsGoIntoEmptyPocketsFirst(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            Inventory inventory = player.getInventory();
            inventory.items.set(STRAY_INDEX, new ItemStack(Items.APPLE, STACK_COUNT));
            endCreativeMode(player);
            helper.assertTrue(inventory.items.get(STRAY_INDEX).isEmpty(), "The locked slot is emptied");
            helper.assertTrue(inventory.getItem(InventoryLayout.FIRST_POCKET_INDEX).is(Items.APPLE), "The apples move into the first pocket");
            helper.assertTrue(findChestNear(player).isEmpty(), "No chest is needed when everything fits");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void overflowCanDropInsteadOfPlacingAChest(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            ConfigOverrides.withValue(InventoryServerConfig.get().firstJoin.overflow, InventoryServerConfig.OverflowTarget.DROP, () -> {
                Inventory inventory = player.getInventory();
                fillPockets(inventory);
                inventory.items.set(STRAY_INDEX, new ItemStack(Items.APPLE, STACK_COUNT));
                Set<UUID> itemsAlreadyNearby = DroppedItems.idsNear(player);
                endCreativeMode(player);
                helper.assertTrue(DroppedItems.countNewNear(player, Items.APPLE, itemsAlreadyNearby) == STACK_COUNT, "The apples drop at the player's feet");
                helper.assertTrue(findChestNear(player).isEmpty(), "No chest is placed when overflow is set to drop");
            });
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void strayItemsInLockedSlotsAreMovedOut(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            LockedSlotOverflow.tick(player);
            Inventory inventory = player.getInventory();
            // Written to the list directly, the way some mods do, so setItem's redirect never sees it.
            inventory.items.set(STRAY_INDEX, new ItemStack(Items.APPLE, STACK_COUNT));
            LockedSlotOverflow.tick(player);
            helper.assertTrue(inventory.items.get(STRAY_INDEX).isEmpty(), "The stray apples leave the locked slot");
            helper.assertTrue(inventory.countItem(Items.APPLE) == STACK_COUNT, "The apples land in a hand or pocket");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    // The mock player joins in creative (the GameTest world's default), where vanilla's slots are open.
    private static void endCreativeMode(ServerPlayer player) {
        LockedSlotOverflow.tick(player);
        player.setGameMode(GameType.SURVIVAL);
        LockedSlotOverflow.tick(player);
    }

    private static void fillPockets(Inventory inventory) {
        IntStream.range(0, InventoryLayout.POCKET_COUNT).forEach(pocket ->
                inventory.items.set(InventoryLayout.FIRST_POCKET_INDEX + pocket, new ItemStack(DISTINCT_ITEMS.get(LOCKED_SLOT_COUNT + pocket), STACK_COUNT)));
    }

    private static void fillLockedSlots(Inventory inventory) {
        int itemIndex = 0;
        for (int inventoryIndex = 0; inventoryIndex < inventory.items.size(); inventoryIndex++) {
            if (InventoryLayout.isLocked(inventoryIndex)) {
                inventory.items.set(inventoryIndex, new ItemStack(DISTINCT_ITEMS.get(itemIndex++), STACK_COUNT));
            }
        }
    }

    private static long countLockedStacks(Inventory inventory) {
        return IntStream.range(0, inventory.items.size())
                .filter(InventoryLayout::isLocked)
                .filter(inventoryIndex -> !inventory.items.get(inventoryIndex).isEmpty())
                .count();
    }

    private static Optional<BlockPos> findChestNear(ServerPlayer player) {
        BlockPos feet = player.blockPosition();
        return Direction.Plane.HORIZONTAL.stream()
                .map(feet::relative)
                .filter(position -> player.level().getBlockEntity(position) instanceof ChestBlockEntity)
                .findFirst()
                .or(() -> player.level().getBlockEntity(feet) instanceof ChestBlockEntity ? Optional.of(feet) : Optional.empty());
    }

    private static Container chestAt(ServerPlayer player, BlockPos position) {
        return (ChestBlockEntity) player.level().getBlockEntity(position);
    }

    private static long countStacks(Container container) {
        return IntStream.range(0, container.getContainerSize()).filter(slot -> !container.getItem(slot).isEmpty()).count();
    }
}
