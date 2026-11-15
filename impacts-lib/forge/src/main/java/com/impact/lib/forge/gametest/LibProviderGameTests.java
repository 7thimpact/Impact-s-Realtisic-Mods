package com.impact.lib.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.backslot.BackSlot;
import com.impact.lib.crafting.CraftingProgress;
import com.impact.lib.forge.config.ServerConfigResync;
import com.impact.lib.gametest.MockPlayers;
import com.impact.lib.hud.HudBounds;
import com.impact.lib.hud.HudLayout;
import com.impact.lib.item.ImpactsItems;
import com.impact.lib.network.LibNetwork;
import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Lib milestone 3: Give or drop, the back slot, crafting progress, the HUD layout and Lib's network channel. */
@GameTestHolder(ImpactsLib.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibProviderGameTests {
    private static final String EMPTY_TEMPLATE = "empty";
    private static final int GIVEN_STONE_COUNT = 5;
    private static final double DROP_SEARCH_RADIUS_BLOCKS = 2.0;
    private static final int SAMPLE_SCREEN_WIDTH = 400;
    private static final int SAMPLE_SCREEN_HEIGHT = 300;
    private static final HudBounds VANILLA_HOTBAR_ON_SAMPLE_SCREEN = new HudBounds(109, 278, 182, 22);
    private static final int MAXIMUM_FILL_ATTEMPTS = 100;

    private LibProviderGameTests() {
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void givesItemsThatFit(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            ImpactsItems.giveOrDrop(player, new ItemStack(Items.STONE, GIVEN_STONE_COUNT));
            int stoneInInventory = player.getInventory().countItem(Items.STONE);
            helper.assertTrue(stoneInInventory == GIVEN_STONE_COUNT, "Give or drop should put the stone in the inventory, found " + stoneInInventory);
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void dropsWhatDoesNotFitAtPlayersFeet(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            fillInventory(player);
            Set<UUID> itemsAlreadyNearby = droppedItemIdsNear(player);
            ImpactsItems.giveOrDrop(player, new ItemStack(Items.STONE, GIVEN_STONE_COUNT));
            helper.assertTrue(player.getInventory().countItem(Items.STONE) == 0, "A full inventory should take no stone");
            int droppedStone = countNewlyDroppedNear(player, Items.STONE, itemsAlreadyNearby);
            helper.assertTrue(droppedStone == GIVEN_STONE_COUNT, "Every stone should drop at the player's feet, found " + droppedStone);
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void backSlotIsSafeWithoutProvider(GameTestHelper helper) {
        if (BackSlot.isAvailable()) {
            helper.succeed();
            return;
        }
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            helper.assertTrue(BackSlot.get(player).isEmpty(), "Without a provider the back slot should be empty");
            helper.assertFalse(BackSlot.set(player, new ItemStack(Items.LEATHER_CHESTPLATE)), "Without a provider setting the back slot should do nothing");
            helper.assertFalse(BackSlot.accepts(new ItemStack(Items.LEATHER_CHESTPLATE)), "Without a provider nothing fits the back slot");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void craftingProgressIsEmptyWithoutCraft(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            helper.assertTrue(CraftingProgress.get(player.inventoryMenu).isEmpty(), "No craft is running, so there is no progress");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void reportsVanillaHotbarBounds(GameTestHelper helper) {
        HudBounds bounds = HudLayout.vanillaHotbarBounds(SAMPLE_SCREEN_WIDTH, SAMPLE_SCREEN_HEIGHT);
        helper.assertTrue(bounds.equals(VANILLA_HOTBAR_ON_SAMPLE_SCREEN), "Unexpected vanilla hotbar bounds: " + bounds);
        helper.succeed();
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public static void libChannelUsesMinorVersionAndRoundTripsConfig(GameTestHelper helper) {
        helper.assertTrue(LibNetwork.protocolVersion().equals("1.0"), "Lib's channel version should be its minor version, got " + LibNetwork.protocolVersion());
        byte[] contents = "[grinder]\nwoodenDurability = 80\n".getBytes(StandardCharsets.UTF_8);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        ServerConfigResync.CODEC.encode(new ServerConfigResync.ConfigFileContents("impactslib/impactslib-server.toml", contents), buffer);
        ServerConfigResync.ConfigFileContents decoded = ServerConfigResync.CODEC.decode(buffer);
        helper.assertTrue(decoded.fileName().equals("impactslib/impactslib-server.toml"), "The file name should survive the round trip");
        helper.assertTrue(Arrays.equals(decoded.contents(), contents), "The file contents should survive the round trip");
        helper.succeed();
    }

    private static void fillInventory(ServerPlayer player) {
        for (int attempt = 0; attempt < MAXIMUM_FILL_ATTEMPTS; attempt++) {
            ItemStack filler = new ItemStack(Items.DIRT);
            filler.setCount(filler.getMaxStackSize());
            player.getInventory().add(filler);
            if (!filler.isEmpty()) {
                return;
            }
        }
    }

    // The test world keeps items dropped by earlier runs, and mock players spawn near the same point, so only
    // items that appear during the test count.
    private static Set<UUID> droppedItemIdsNear(ServerPlayer player) {
        return itemsNear(player).stream().map(ItemEntity::getUUID).collect(Collectors.toSet());
    }

    private static int countNewlyDroppedNear(ServerPlayer player, Item expectedItem, Set<UUID> itemsAlreadyNearby) {
        return itemsNear(player).stream()
                .filter(itemEntity -> !itemsAlreadyNearby.contains(itemEntity.getUUID()))
                .map(ItemEntity::getItem)
                .filter(dropped -> dropped.is(expectedItem))
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    private static List<ItemEntity> itemsNear(ServerPlayer player) {
        return player.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(DROP_SEARCH_RADIUS_BLOCKS));
    }
}
