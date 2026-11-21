package com.impact.realisticinventory.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.backslot.BackSlot;
import com.impact.lib.config.ConfigValue;
import com.impact.lib.gametest.ConfigOverrides;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticinventory.InventoryMod;
import com.impact.realisticinventory.config.InventoryServerConfig;
import com.impact.realisticinventory.inventory.InventoryLayout;
import com.impact.realisticinventory.pack.BackSlotStorage;
import com.impact.realisticinventory.pack.InventoryItems;
import com.impact.realisticinventory.pack.PackContents;
import com.impact.realisticinventory.pack.PackEffects;
import com.impact.realisticinventory.pack.PackMenu;
import com.impact.realisticinventory.pack.PackMenus;
import com.impact.realisticinventory.pack.PackRules;
import com.impact.realisticinventory.pack.WornPack;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.Optional;

/** Inventory milestone 6: packs. */
@GameTestHolder(InventoryMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InventoryPackGameTests {
    private static final int LEFT_MOUSE_BUTTON = 0;
    private static final int STONE_COUNT = 5;
    private static final int BLUE_DYE_COLOR = 0x3355CC;
    private static final double FRAME_PACK_SPEED_FACTOR = 0.9;
    private static final double SPEED_TOLERANCE = 0.0001;
    private static final ResourceLocation BACKPACK_RECIPE = ResourceIds.of(InventoryMod.MOD_ID, "backpack");

    private InventoryPackGameTests() {
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void packsStackToOneAndFitTheBackSlot(GameTestHelper helper) {
        for (ItemStack pack : List.of(new ItemStack(InventoryItems.satchel()), new ItemStack(InventoryItems.backpack()), new ItemStack(InventoryItems.framePack()))) {
            helper.assertTrue(pack.getMaxStackSize() == 1, "Packs stack to 1");
            helper.assertTrue(BackSlot.accepts(pack), "Every pack fits the back slot");
            helper.assertFalse(PackRules.canGoInPack(pack), "Packs can't go inside packs");
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void rightClickWearsThePackAndSwapsWithTheWornOne(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            useInMainHand(player, new ItemStack(InventoryItems.satchel()));
            helper.assertTrue(BackSlotStorage.get(player).is(InventoryItems.satchel()), "Right-click puts the satchel on");
            helper.assertTrue(player.getMainHandItem().isEmpty(), "The hand is empty after putting the satchel on");
            useInMainHand(player, new ItemStack(InventoryItems.backpack()));
            helper.assertTrue(BackSlotStorage.get(player).is(InventoryItems.backpack()), "The backpack goes on");
            helper.assertTrue(player.getMainHandItem().is(InventoryItems.satchel()), "The satchel comes back to the hand");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void pickupsGoIntoTheWornPackWhenPocketsAreFull(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            BackSlotStorage.set(player, new ItemStack(InventoryItems.backpack()));
            fillHandsAndPocketsWithSwords(player.getInventory());
            ItemStack stone = new ItemStack(Items.STONE, STONE_COUNT);
            helper.assertTrue(player.getInventory().add(stone), "The stone is picked up");
            List<ItemStack> packed = PackContents.stacksIn(BackSlotStorage.get(player));
            helper.assertTrue(packed.size() == 1 && packed.get(0).is(Items.STONE) && packed.get(0).getCount() == STONE_COUNT,
                    "The stone lands in the worn backpack");
            helper.assertFalse(player.getInventory().add(new ItemStack(InventoryItems.satchel())), "A pack can't be picked up into a pack");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void giveItemToPlayerFillsTheWornPack(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            BackSlotStorage.set(player, new ItemStack(InventoryItems.satchel()));
            fillHandsAndPocketsWithSwords(player.getInventory());
            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(Items.APPLE, STONE_COUNT));
            List<ItemStack> packed = PackContents.stacksIn(BackSlotStorage.get(player));
            helper.assertTrue(packed.stream().anyMatch(stack -> stack.is(Items.APPLE) && stack.getCount() == STONE_COUNT),
                    "Forge's giveItemToPlayer puts the apples in the worn satchel");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void shulkerBoxesFollowTheAllowShulkersSetting(GameTestHelper helper) {
        ConfigValue<Boolean> allowShulkers = InventoryServerConfig.get().packs.isShulkerAllowed;
        ItemStack shulkerBox = new ItemStack(Items.SHULKER_BOX);
        ConfigOverrides.withValue(allowShulkers, false, () ->
                helper.assertFalse(PackRules.canGoInPack(shulkerBox), "Shulker boxes stay out of packs by default"));
        ConfigOverrides.withValue(allowShulkers, true, () ->
                helper.assertTrue(PackRules.canGoInPack(shulkerBox), "allowShulkers lets them in"));
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void theOpenPackReadsAndWritesItsContents(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            ItemStack backpack = new ItemStack(InventoryItems.backpack());
            PackContents.write(backpack, packSlotsWith(new ItemStack(Items.APPLE, STONE_COUNT)));
            BackSlotStorage.set(player, backpack);
            PackMenus.openWornPack(player);
            helper.assertTrue(player.containerMenu instanceof PackMenu, "B opens the pack menu");
            PackMenu menu = (PackMenu) player.containerMenu;
            helper.assertTrue(menu.packSlotCount() == WornPack.slotCount(backpack), "The menu has the backpack's 9 slots");
            helper.assertTrue(menu.slots.get(0).getItem().is(Items.APPLE), "The first pack slot shows the stored apples");
            menu.clicked(0, LEFT_MOUSE_BUTTON, ClickType.QUICK_MOVE, player);
            helper.assertTrue(PackContents.stacksIn(backpack).isEmpty(), "Moving the apples out empties the pack's NBT");
            helper.assertTrue(player.getInventory().countItem(Items.APPLE) == STONE_COUNT, "The apples land in the player's slots");
            player.closeContainer();
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void pickupsWhileThePackIsOpenAreKept(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            ItemStack backpack = new ItemStack(InventoryItems.backpack());
            BackSlotStorage.set(player, backpack);
            fillHandsAndPocketsWithSwords(player.getInventory());
            PackMenus.openWornPack(player);
            player.getInventory().add(new ItemStack(Items.STONE, STONE_COUNT));
            PackMenu menu = (PackMenu) player.containerMenu;
            menu.slots.get(1).setByPlayer(new ItemStack(Items.APPLE));
            List<ItemStack> packed = PackContents.stacksIn(backpack);
            helper.assertTrue(packed.stream().anyMatch(stack -> stack.is(Items.STONE)), "The stone picked up while the pack was open stays in it");
            helper.assertTrue(packed.stream().anyMatch(stack -> stack.is(Items.APPLE)), "The apple put in through the menu is saved too");
            player.closeContainer();
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void upgradingAPackKeepsItsContentsAndColor(GameTestHelper helper) {
        if (ImpactsLib.isLoaded(InventoryCraftingGameTests.CRAFTING_MOD_ID)) {
            helper.succeed();
            return;
        }
        Optional<? extends Recipe<?>> recipe = helper.getLevel().getRecipeManager().byKey(BACKPACK_RECIPE);
        helper.assertTrue(recipe.isPresent() && recipe.get() instanceof CraftingRecipe, "The backpack recipe loads without Crafting installed");
        ItemStack satchel = blueSatchelOfApples();
        CraftingContainer grid = TestCraftingGrids.holding(List.of(new ItemStack(Items.LEATHER), new ItemStack(Items.CHEST), new ItemStack(Items.LEATHER),
                new ItemStack(Items.LEATHER), satchel, new ItemStack(Items.LEATHER)));
        CraftingRecipe backpackRecipe = (CraftingRecipe) recipe.get();
        helper.assertTrue(backpackRecipe.matches(grid, helper.getLevel()), "Leather, a chest and a satchel make a backpack");
        ItemStack backpack = backpackRecipe.assemble(grid, helper.getLevel().registryAccess());
        helper.assertTrue(backpack.is(InventoryItems.backpack()), "The result is a backpack");
        helper.assertTrue(InventoryItems.backpack().getColor(backpack) == BLUE_DYE_COLOR, "The backpack keeps the satchel's dye");
        helper.assertTrue(PackContents.stacksIn(backpack).stream().anyMatch(stack -> stack.is(Items.APPLE)), "The backpack keeps the satchel's contents");
        helper.succeed();
    }

    @GameTest(templateNamespace = InventorySkeletonGameTests.LIB_NAMESPACE, template = InventorySkeletonGameTests.EMPTY_TEMPLATE)
    public static void aWornFramePackSlowsWalking(GameTestHelper helper) {
        ServerPlayer player = InventoryTestPlayers.joinInSurvival(helper);
        try {
            double normalSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
            BackSlotStorage.set(player, new ItemStack(InventoryItems.framePack()));
            PackEffects.tick(player);
            double slowedSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
            helper.assertTrue(Math.abs(slowedSpeed - normalSpeed * FRAME_PACK_SPEED_FACTOR) < SPEED_TOLERANCE, "A frame pack slows walking by 10%");
            BackSlotStorage.set(player, new ItemStack(InventoryItems.backpack()));
            PackEffects.tick(player);
            helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.MOVEMENT_SPEED) - normalSpeed) < SPEED_TOLERANCE, "Other packs don't slow you");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }

    private static void useInMainHand(ServerPlayer player, ItemStack held) {
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        player.gameMode.useItem(player, player.level(), held, InteractionHand.MAIN_HAND);
    }

    private static void fillHandsAndPocketsWithSwords(Inventory inventory) {
        inventory.setItem(InventoryLayout.MAIN_HAND_INDEX, new ItemStack(Items.IRON_SWORD));
        inventory.offhand.set(0, new ItemStack(Items.IRON_SWORD));
        for (int pocketIndex = InventoryLayout.FIRST_POCKET_INDEX; pocketIndex <= InventoryLayout.LAST_POCKET_INDEX; pocketIndex++) {
            inventory.setItem(pocketIndex, new ItemStack(Items.IRON_SWORD));
        }
    }

    /** A satchel dyed blue with apples inside, to check that upgrades keep both. */
    static ItemStack blueSatchelOfApples() {
        ItemStack satchel = new ItemStack(InventoryItems.satchel());
        InventoryItems.satchel().setColor(satchel, BLUE_DYE_COLOR);
        PackContents.write(satchel, packSlotsWith(new ItemStack(Items.APPLE, STONE_COUNT)));
        return satchel;
    }

    static boolean keepsTheSatchelsDyeAndApples(ItemStack upgradedPack) {
        boolean keepsDye = upgradedPack.getItem() instanceof DyeableLeatherItem dyeable && dyeable.getColor(upgradedPack) == BLUE_DYE_COLOR;
        return keepsDye && PackContents.stacksIn(upgradedPack).stream().anyMatch(stack -> stack.is(Items.APPLE));
    }

    private static NonNullList<ItemStack> packSlotsWith(ItemStack firstSlot) {
        NonNullList<ItemStack> slots = NonNullList.withSize(PackRules.MAXIMUM_SLOTS, ItemStack.EMPTY);
        slots.set(0, firstSlot);
        return slots;
    }
}
