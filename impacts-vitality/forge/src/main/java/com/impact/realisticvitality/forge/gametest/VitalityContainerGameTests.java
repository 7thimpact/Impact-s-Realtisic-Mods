package com.impact.realisticvitality.forge.gametest;

import com.impact.lib.ResourceIds;
import com.impact.lib.gametest.ConfigOverrides;
import com.impact.lib.gametest.CraftingGrids;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.player.ThirstState;
import com.impact.realisticvitality.player.VitalityState;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.thirst.BottledWater;
import com.impact.realisticvitality.thirst.ContainerContents;
import com.impact.realisticvitality.thirst.Drinking;
import com.impact.realisticvitality.thirst.HydrationPacks;
import com.impact.realisticvitality.thirst.WaterContainer;
import com.impact.realisticvitality.thirst.WaterContainers;
import com.impact.realisticvitality.thirst.WaterPurity;
import com.impact.realisticvitality.thirst.WaterSources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;
import java.util.function.Consumer;

/** Vitality milestone 3, part 2: drink containers fill, drink, pour, mix purity down, and fill from bottles in a grid. */
@GameTestHolder(VitalityMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class VitalityContainerGameTests {
    private static final String LIB_NAMESPACE = VitalitySkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = VitalitySkeletonGameTests.EMPTY_TEMPLATE;
    private static final int HALF_THIRST = 10;
    private static final float LOOKING_STRAIGHT_DOWN = 90.0F;

    private VitalityContainerGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void containersHoldTheSpecsDrinks(GameTestHelper helper) {
        int[] expected = {1, 3, 5, 8, 8, 24};
        WaterContainer[] containers = WaterContainer.values();
        for (int index = 0; index < containers.length; index++) {
            helper.assertTrue(WaterContainers.capacity(containers[index]) == expected[index], containers[index].itemPath + " should hold " + expected[index]);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void canteenFillsFromWaterWithItsPurity(GameTestHelper helper) {
        BlockPos water = helper.absolutePos(BlockPos.ZERO);
        helper.getLevel().setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
        withPlayer(helper, player -> {
            player.moveTo(water.getX() + 0.5, water.getY() + 1.0, water.getZ() + 0.5, 0.0F, LOOKING_STRAIGHT_DOWN);
            ItemStack canteen = new ItemStack(VitalityItems.container(WaterContainer.CANTEEN));
            player.setItemInHand(InteractionHand.MAIN_HAND, canteen);
            canteen.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            ContainerContents contents = ContainerContents.read(player.getMainHandItem());
            helper.assertTrue(contents.drinks() == 5, "Using a canteen on water fills it to 5 drinks, got " + contents.drinks());
            helper.assertTrue(contents.purity() == WaterSources.purityAt(helper.getLevel(), water), "The canteen takes the water's purity");
        });
        helper.getLevel().setBlockAndUpdate(water, Blocks.AIR.defaultBlockState());
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void oneSaltySipSpoilsTheCanteen(GameTestHelper helper) {
        ContainerContents purified = ContainerContents.water(4, WaterPurity.PURIFIED);
        ContainerContents mixed = purified.withWater(1, WaterPurity.SALT, false, false).orElseThrow();
        helper.assertTrue(mixed.drinks() == 5 && mixed.purity() == WaterPurity.SALT, "Adding salt water makes the whole canteen salty");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void drinkingTakesOneDrinkAtATime(GameTestHelper helper) {
        withPlayer(helper, player -> ConfigOverrides.withValue(VitalityServerConfig.get().thirst.parchedChance, 0.0, () -> {
            ItemStack waterskin = new ItemStack(VitalityItems.container(WaterContainer.WATERSKIN));
            ContainerContents.water(3, WaterPurity.PURIFIED).writeTo(waterskin);
            ThirstState thirst = VitalityState.of(player).thirst;
            thirst.setLevel(HALF_THIRST);
            Drinking.onUseFinished(player, waterskin.copy());
            WaterContainers.finishDrink(player, waterskin);
            helper.assertTrue(thirst.level() == HALF_THIRST + 6, "A drink of purified water from a waterskin gives 6 thirst");
            helper.assertTrue(ContainerContents.read(waterskin).drinks() == 2, "One drink is gone");
        }));
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void pouringOverYourselfMakesYouWet(GameTestHelper helper) {
        withPlayer(helper, player -> {
            ItemStack canteen = new ItemStack(VitalityItems.container(WaterContainer.CANTEEN));
            ContainerContents.water(2, WaterPurity.UNCLEAN).writeTo(canteen);
            WaterContainers.pourOverSelf(helper.getLevel(), player, canteen);
            helper.assertTrue(VitalityState.of(player).temperature.isWet(), "Pouring water over yourself makes you Wet");
            helper.assertTrue(ContainerContents.read(canteen).drinks() == 1, "Pouring uses one drink");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void cauldronFillsOneDrinkPerLevel(GameTestHelper helper) {
        BlockPos cauldron = helper.absolutePos(BlockPos.ZERO);
        BlockState fullCauldron = Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
        helper.getLevel().setBlockAndUpdate(cauldron, fullCauldron);
        withPlayer(helper, player -> {
            ItemStack cup = new ItemStack(VitalityItems.container(WaterContainer.WOODEN_CUP));
            player.setItemInHand(InteractionHand.MAIN_HAND, cup);
            CauldronInteraction.WATER.get(cup.getItem()).interact(fullCauldron, helper.getLevel(), cauldron, player, InteractionHand.MAIN_HAND, cup);
            ContainerContents contents = ContainerContents.read(cup);
            helper.assertTrue(contents.drinks() == 1 && contents.purity() == WaterPurity.UNCLEAN, "An unheated cauldron gives a drink of unclean water");
            helper.assertTrue(helper.getLevel().getBlockState(cauldron).getValue(LayeredCauldronBlock.LEVEL) == 2, "One level per drink");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void bottlesPourIntoContainersInAGrid(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CraftingContainer grid = CraftingGrids.of(new ItemStack(VitalityItems.container(WaterContainer.CANTEEN)),
                VitalityItems.bottle(BottledWater.PURIFIED), vanillaWaterBottle());
        Optional<CraftingRecipe> recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level);
        helper.assertTrue(recipe.isPresent(), "A canteen and two bottles should make a filling recipe");
        ContainerContents filled = ContainerContents.read(recipe.get().assemble(grid, level.registryAccess()));
        helper.assertTrue(filled.drinks() == 2 && filled.purity() == WaterPurity.UNCLEAN, "Two drinks, mixed down to unclean by the vanilla bottle");
        NonNullList<ItemStack> remaining = recipe.get().getRemainingItems(grid);
        helper.assertTrue(remaining.get(1).is(Items.GLASS_BOTTLE) && remaining.get(2).is(Items.GLASS_BOTTLE), "Both bottles come back empty");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void onlyTheFlaskKeepsColdWaterCold(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ContainerContents inFlask = filledInGrid(helper, WaterContainer.INSULATED_FLASK, VitalityItems.bottle(BottledWater.COLD));
        helper.assertTrue(inFlask.kind() == ContainerContents.Kind.COLD_WATER, "The Insulated Flask keeps Cold Water cold");
        ContainerContents inCanteen = filledInGrid(helper, WaterContainer.CANTEEN, VitalityItems.bottle(BottledWater.COLD));
        helper.assertTrue(inCanteen.kind() == ContainerContents.Kind.WATER && inCanteen.purity() == WaterPurity.PURIFIED,
                "A canteen warms Cold Water up to plain Purified Water");
        ContainerContents soup = filledInGrid(helper, WaterContainer.INSULATED_FLASK, new ItemStack(Items.MUSHROOM_STEW));
        helper.assertTrue(soup.kind() == ContainerContents.Kind.SOUP, "The flask holds soup");
        CraftingContainer soupInCanteen = CraftingGrids.of(new ItemStack(VitalityItems.container(WaterContainer.CANTEEN)), new ItemStack(Items.MUSHROOM_STEW));
        helper.assertTrue(level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, soupInCanteen, level).isEmpty(), "Only the flask takes soup");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void soupFromTheFlaskFeedsYou(GameTestHelper helper) {
        withPlayer(helper, player -> {
            ItemStack flask = new ItemStack(VitalityItems.container(WaterContainer.INSULATED_FLASK));
            ContainerContents.EMPTY.withSoup(ResourceIds.itemId(Items.MUSHROOM_STEW)).orElseThrow().writeTo(flask);
            player.getFoodData().setFoodLevel(HALF_THIRST);
            ThirstState thirst = VitalityState.of(player).thirst;
            thirst.setLevel(HALF_THIRST);
            Drinking.onUseFinished(player, flask.copy());
            WaterContainers.finishDrink(player, flask);
            helper.assertTrue(player.getFoodData().getFoodLevel() == HALF_THIRST + 6, "Soup from the flask feeds you like the bowl");
            helper.assertTrue(thirst.level() == HALF_THIRST + 4, "and hydrates you like the bowl");
            helper.assertTrue(ContainerContents.read(flask).isEmpty(), "The flask is empty again");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void fillingNeverOverflows(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack fullCup = new ItemStack(VitalityItems.container(WaterContainer.WOODEN_CUP));
        ContainerContents.water(1, WaterPurity.PURIFIED).writeTo(fullCup);
        CraftingContainer grid = CraftingGrids.of(fullCup, VitalityItems.bottle(BottledWater.PURIFIED));
        helper.assertTrue(level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level).isEmpty(), "A full cup takes no more water");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void woodenCupPurifiesOnACampfire(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack uncleanCup = new ItemStack(VitalityItems.container(WaterContainer.WOODEN_CUP));
        ContainerContents.water(1, WaterPurity.UNCLEAN).writeTo(uncleanCup);
        Optional<CampfireCookingRecipe> recipe = level.getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, new SimpleContainer(uncleanCup), level);
        helper.assertTrue(recipe.isPresent(), "A cup of unclean water should cook on a campfire");
        ContainerContents result = ContainerContents.read(recipe.get().getResultItem(level.registryAccess()));
        helper.assertTrue(result.drinks() == 1 && result.purity() == WaterPurity.PURIFIED, "It comes out purified");
        helper.assertTrue(recipe.get().getCookingTime() == 300, "It takes 300 ticks, got " + recipe.get().getCookingTime());
        ItemStack emptyCup = new ItemStack(VitalityItems.container(WaterContainer.WOODEN_CUP));
        helper.assertTrue(level.getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, new SimpleContainer(emptyCup), level).isEmpty(),
                "An empty cup doesn't cook");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void tideboundCanteenRefillsItself(GameTestHelper helper) {
        withPlayer(helper, player -> {
            ItemStack tidebound = new ItemStack(VitalityItems.container(WaterContainer.TIDEBOUND_CANTEEN));
            int interval = VitalityServerConfig.get().containers.tideboundRefillTicks.get();
            WaterContainers.refillTick(tidebound, player, interval);
            helper.assertTrue(ContainerContents.read(tidebound).drinks() == 1, "It gains a drink every 20 seconds");
            WaterContainers.refillTick(tidebound, player, interval + 1);
            helper.assertTrue(ContainerContents.read(tidebound).drinks() == 1, "but not in between");
            helper.assertTrue(ContainerContents.read(tidebound).purity() == WaterPurity.PURIFIED, "Its water is purified");
            ItemStack canteen = new ItemStack(VitalityItems.container(WaterContainer.CANTEEN));
            WaterContainers.refillTick(canteen, player, interval);
            helper.assertTrue(ContainerContents.read(canteen).isEmpty(), "A plain canteen never refills");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void wornHydrationPackSipsWithItsKey(GameTestHelper helper) {
        withPlayer(helper, player -> ConfigOverrides.withValue(VitalityServerConfig.get().thirst.parchedChance, 0.0, () -> {
            ItemStack pack = new ItemStack(VitalityItems.container(WaterContainer.HYDRATION_PACK));
            ContainerContents.water(24, WaterPurity.PURIFIED).writeTo(pack);
            player.setItemSlot(EquipmentSlot.CHEST, pack);
            ThirstState thirst = VitalityState.of(player).thirst;
            thirst.setLevel(HALF_THIRST);
            helper.assertTrue(HydrationPacks.sipWorn(player), "A worn pack can be sipped without holding it");
            helper.assertTrue(thirst.level() == HALF_THIRST + 6, "The sip gives purified water's thirst");
            helper.assertTrue(ContainerContents.read(player.getItemBySlot(EquipmentSlot.CHEST)).drinks() == 23, "One drink is gone");
            helper.assertFalse(HydrationPacks.sipWorn(player), "The next sip waits for the cooldown");
        }));
    }

    private static ContainerContents filledInGrid(GameTestHelper helper, WaterContainer container, ItemStack poured) {
        ServerLevel level = helper.getLevel();
        CraftingContainer grid = CraftingGrids.of(new ItemStack(VitalityItems.container(container)), poured);
        CraftingRecipe recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level).orElseThrow();
        return ContainerContents.read(recipe.assemble(grid, level.registryAccess()));
    }

    private static ItemStack vanillaWaterBottle() {
        return PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
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
