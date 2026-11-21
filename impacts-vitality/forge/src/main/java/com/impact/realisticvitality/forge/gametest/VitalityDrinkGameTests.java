package com.impact.realisticvitality.forge.gametest;

import com.impact.lib.forge.crafting.WaterBottleIngredient;
import com.impact.lib.gametest.ConfigOverrides;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.player.ThirstState;
import com.impact.realisticvitality.player.VitalityState;
import com.impact.realisticvitality.registry.VitalityEffects;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.registry.VitalityTags;
import com.impact.realisticvitality.thirst.BottleFilling;
import com.impact.realisticvitality.thirst.BottledWater;
import com.impact.realisticvitality.thirst.Drinking;
import com.impact.realisticvitality.thirst.Sipping;
import com.impact.realisticvitality.thirst.WaterPurity;
import com.impact.realisticvitality.thirst.WaterSources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/** Vitality milestone 3, part 1: water picks up its purity, drinks and foods change thirst, and IRV water works like a water bottle. */
@GameTestHolder(VitalityMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class VitalityDrinkGameTests {
    private static final String LIB_NAMESPACE = VitalitySkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = VitalitySkeletonGameTests.EMPTY_TEMPLATE;
    private static final int HALF_THIRST = 10;
    private static final float TOLERANCE = 0.0001F;
    private static final float LOOKING_STRAIGHT_DOWN = 90.0F;

    private VitalityDrinkGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void oceansAndBeachesAreSalty(GameTestHelper helper) {
        helper.assertTrue(WaterSources.isSalty(biome(helper, Biomes.OCEAN)), "Ocean water is salty");
        helper.assertTrue(WaterSources.isSalty(biome(helper, Biomes.WARM_OCEAN)), "Every ocean is salty");
        helper.assertTrue(WaterSources.isSalty(biome(helper, Biomes.BEACH)), "Beach water is salty");
        helper.assertFalse(WaterSources.isSalty(biome(helper, Biomes.RIVER)), "River water isn't salty");
        helper.assertFalse(WaterSources.isSalty(biome(helper, Biomes.PLAINS)), "Lake water isn't salty");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void purityMixesDownToTheWorst(GameTestHelper helper) {
        helper.assertTrue(WaterPurity.PURIFIED.mixedWith(WaterPurity.SALT) == WaterPurity.SALT, "One sip of salt water spoils purified water");
        helper.assertTrue(WaterPurity.UNCLEAN.mixedWith(WaterPurity.PURIFIED) == WaterPurity.UNCLEAN, "Unclean water spoils purified water");
        helper.assertTrue(WaterPurity.PURIFIED.mixedWith(WaterPurity.PURIFIED) == WaterPurity.PURIFIED, "Purified stays purified");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void bottleFilledFromWaterPicksUpPurity(GameTestHelper helper) {
        BlockPos water = helper.absolutePos(BlockPos.ZERO);
        helper.getLevel().setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
        withPlayer(helper, player -> {
            lookDownAt(player, water);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
            BottleFilling.fillFromWorld(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            BottledWater expected = BottledWater.filledWith(WaterSources.purityAt(helper.getLevel(), water));
            helper.assertTrue(player.getMainHandItem().is(VitalityItems.waterBottle(expected)),
                    "A bottle filled from the world should be " + expected.itemPath + ", got " + player.getMainHandItem());
        });
        helper.getLevel().setBlockAndUpdate(water, Blocks.AIR.defaultBlockState());
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void coldCauldronGivesUncleanWater(GameTestHelper helper) {
        BlockPos cauldron = helper.absolutePos(BlockPos.ZERO);
        BlockState fullCauldron = Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
        helper.getLevel().setBlockAndUpdate(cauldron, fullCauldron);
        withPlayer(helper, player -> {
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            player.setItemInHand(InteractionHand.MAIN_HAND, bottle);
            CauldronInteraction.WATER.get(Items.GLASS_BOTTLE).interact(fullCauldron, helper.getLevel(), cauldron, player, InteractionHand.MAIN_HAND, bottle);
            helper.assertTrue(player.getMainHandItem().is(VitalityItems.waterBottle(BottledWater.UNCLEAN)), "An unheated cauldron gives Unclean Water");
            helper.assertTrue(helper.getLevel().getBlockState(cauldron).getValue(LayeredCauldronBlock.LEVEL) == 2, "Filling a bottle takes one level");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void vitalityWaterPoursIntoCauldrons(GameTestHelper helper) {
        BlockPos cauldron = helper.absolutePos(BlockPos.ZERO);
        helper.getLevel().setBlockAndUpdate(cauldron, Blocks.CAULDRON.defaultBlockState());
        withPlayer(helper, player -> {
            ItemStack purified = VitalityItems.bottle(BottledWater.PURIFIED);
            player.setItemInHand(InteractionHand.MAIN_HAND, purified);
            CauldronInteraction.EMPTY.get(purified.getItem()).interact(Blocks.CAULDRON.defaultBlockState(), helper.getLevel(), cauldron,
                    player, InteractionHand.MAIN_HAND, purified);
            BlockState filled = helper.getLevel().getBlockState(cauldron);
            helper.assertTrue(filled.is(Blocks.WATER_CAULDRON) && filled.getValue(LayeredCauldronBlock.LEVEL) == 1, "Pouring fills an empty cauldron one level");
            helper.assertTrue(player.getMainHandItem().is(Items.GLASS_BOTTLE), "Pouring leaves the glass bottle");
            ItemStack salt = VitalityItems.bottle(BottledWater.SALT);
            player.setItemInHand(InteractionHand.MAIN_HAND, salt);
            CauldronInteraction.WATER.get(salt.getItem()).interact(filled, helper.getLevel(), cauldron, player, InteractionHand.MAIN_HAND, salt);
            helper.assertTrue(helper.getLevel().getBlockState(cauldron).getValue(LayeredCauldronBlock.LEVEL) == 2, "Any IRV water raises the level");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void vitalityWaterTurnsDirtIntoMud(GameTestHelper helper) {
        BlockPos dirt = helper.absolutePos(BlockPos.ZERO);
        helper.getLevel().setBlockAndUpdate(dirt, Blocks.DIRT.defaultBlockState());
        withPlayer(helper, player -> {
            ItemStack unclean = VitalityItems.bottle(BottledWater.UNCLEAN);
            player.setItemInHand(InteractionHand.MAIN_HAND, unclean);
            BlockHitResult topFace = new BlockHitResult(Vec3.atCenterOf(dirt), Direction.UP, dirt, false);
            unclean.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, topFace));
            helper.assertTrue(helper.getLevel().getBlockState(dirt).is(Blocks.MUD), "IRV water should turn dirt into mud");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void dispensersFillBottlesWithPurity(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos dispenser = helper.absolutePos(BlockPos.ZERO);
        BlockPos water = dispenser.above();
        level.setBlockAndUpdate(dispenser, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.UP));
        level.setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
        try {
            DispenserBlockEntity dispenserEntity = (DispenserBlockEntity) level.getBlockEntity(dispenser);
            dispenserEntity.setItem(0, new ItemStack(Items.GLASS_BOTTLE));
            level.getBlockState(dispenser).tick(level, dispenser, level.getRandom());
            BottledWater expected = BottledWater.filledWith(WaterSources.purityAt(level, water));
            helper.assertTrue(dispenserEntity.getItem(0).is(VitalityItems.waterBottle(expected)), "A dispenser should fill IRV water, got " + dispenserEntity.getItem(0));
        } finally {
            level.setBlockAndUpdate(water, Blocks.AIR.defaultBlockState());
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void purifiedWaterBrewsLikeAWaterBottle(GameTestHelper helper) {
        ItemStack awkward = BrewingRecipeRegistry.getOutput(VitalityItems.bottle(BottledWater.PURIFIED), new ItemStack(Items.NETHER_WART));
        helper.assertTrue(PotionUtils.getPotion(awkward) == Potions.AWKWARD, "Purified Water + Nether Wart should brew an Awkward Potion");
        ItemStack thick = BrewingRecipeRegistry.getOutput(VitalityItems.bottle(BottledWater.COLD), new ItemStack(Items.GLOWSTONE_DUST));
        helper.assertTrue(PotionUtils.getPotion(thick) == Potions.THICK, "Cold Water is purified too, so it brews a Thick Potion");
        ItemStack splash = BrewingRecipeRegistry.getOutput(VitalityItems.bottle(BottledWater.PURIFIED), new ItemStack(Items.GUNPOWDER));
        helper.assertTrue(splash.is(Items.SPLASH_POTION) && PotionUtils.getPotion(splash) == Potions.WATER, "Gunpowder makes a splash water bottle");
        ItemStack fromUnclean = BrewingRecipeRegistry.getOutput(VitalityItems.bottle(BottledWater.UNCLEAN), new ItemStack(Items.NETHER_WART));
        helper.assertTrue(fromUnclean.isEmpty(), "Unclean Water doesn't brew");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void drinksAndFoodsChangeThirst(GameTestHelper helper) {
        Map<ItemStack, int[]> expectedThirstAndTenthsOfHydration = Map.of(
                VitalityItems.bottle(BottledWater.PURIFIED), new int[] {6, 40},
                VitalityItems.bottle(BottledWater.COLD), new int[] {6, 40},
                VitalityItems.bottle(BottledWater.SALT), new int[] {-2, 0},
                vanillaWaterBottle(), new int[] {4, 10},
                new ItemStack(Items.MILK_BUCKET), new int[] {6, 30},
                new ItemStack(Items.HONEY_BOTTLE), new int[] {2, 0},
                new ItemStack(Items.MUSHROOM_STEW), new int[] {4, 20},
                new ItemStack(Items.MELON_SLICE), new int[] {2, 10},
                new ItemStack(Items.APPLE), new int[] {1, 0},
                new ItemStack(Items.BREAD), new int[] {-1, 0});
        withPlayer(helper, player -> ConfigOverrides.withValue(VitalityServerConfig.get().thirst.parchedChance, 0.0, () ->
                expectedThirstAndTenthsOfHydration.forEach((stack, expected) -> {
                    ThirstState thirst = VitalityState.of(player).thirst;
                    thirst.setLevel(HALF_THIRST);
                    thirst.setHydration(0.0F);
                    Drinking.onUseFinished(player, stack);
                    helper.assertTrue(thirst.level() == HALF_THIRST + expected[0], stack + " should change thirst by " + expected[0] + ", got " + (thirst.level() - HALF_THIRST));
                    helper.assertTrue(Math.abs(thirst.hydration() * 10 - expected[1]) < TOLERANCE, stack + " should give " + expected[1] / 10.0F + " Hydration");
                })));
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void overridesWinOverTheTable(GameTestHelper helper) {
        withPlayer(helper, player -> ConfigOverrides.withValue(VitalityServerConfig.get().drinks.overrides, List.of("minecraft:bread=5,2"), () -> {
            ThirstState thirst = VitalityState.of(player).thirst;
            thirst.setLevel(HALF_THIRST);
            Drinking.onUseFinished(player, new ItemStack(Items.BREAD));
            helper.assertTrue(thirst.level() == HALF_THIRST + 5, "drinks.overrides should win over drinks.values");
        }));
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void uncleanWaterCanMakeYouParched(GameTestHelper helper) {
        withPlayer(helper, player -> {
            ConfigOverrides.withValue(VitalityServerConfig.get().thirst.parchedChance, 1.0, () ->
                    Drinking.onUseFinished(player, VitalityItems.bottle(BottledWater.UNCLEAN)));
            helper.assertTrue(player.hasEffect(VitalityEffects.parched()), "Unclean Water should make you Parched when the roll hits");
            player.removeAllEffects();
            ConfigOverrides.withValue(VitalityServerConfig.get().thirst.parchedChance, 1.0, () ->
                    Drinking.onUseFinished(player, VitalityItems.bottle(BottledWater.PURIFIED)));
            helper.assertFalse(player.hasEffect(VitalityEffects.parched()), "Purified Water never makes you Parched");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void sippingTakesOneUncleanSip(GameTestHelper helper) {
        BlockPos water = helper.absolutePos(BlockPos.ZERO);
        helper.getLevel().setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
        withPlayer(helper, player -> ConfigOverrides.withValue(VitalityServerConfig.get().thirst.parchedChance, 0.0, () -> {
            lookDownAt(player, water);
            player.setShiftKeyDown(true);
            ThirstState thirst = VitalityState.of(player).thirst;
            thirst.setLevel(HALF_THIRST);
            helper.assertTrue(Sipping.trySip(player), "Sneaking with an empty hand over water should sip");
            helper.assertTrue(thirst.level() == HALF_THIRST + 1, "A sip restores 1 thirst");
            Sipping.trySip(player);
            helper.assertTrue(thirst.level() == HALF_THIRST + 1, "A second sip in the same second waits for the cooldown");
            player.setShiftKeyDown(false);
            helper.assertFalse(Sipping.canSip(player), "Without sneaking there's no sip");
        }));
        helper.getLevel().setBlockAndUpdate(water, Blocks.AIR.defaultBlockState());
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void smeltingPurifiesAnyWaterBottle(GameTestHelper helper) {
        assertPurifies(helper, RecipeType.SMELTING, VitalityItems.bottle(BottledWater.UNCLEAN));
        assertPurifies(helper, RecipeType.SMOKING, VitalityItems.bottle(BottledWater.SALT));
        assertPurifies(helper, RecipeType.CAMPFIRE_COOKING, vanillaWaterBottle());
        assertPurifies(helper, RecipeType.SMELTING, vanillaWaterBottle());
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void vitalityWaterCountsAsAWaterBottle(GameTestHelper helper) {
        for (BottledWater water : BottledWater.values()) {
            ItemStack stack = VitalityItems.bottle(water);
            helper.assertTrue(stack.is(VitalityTags.WATER_BOTTLES), water.itemPath + " should be in #impactslib:water_bottles");
            helper.assertTrue(WaterBottleIngredient.INSTANCE.test(stack), water.itemPath + " should match Lib's water_bottle ingredient");
            helper.assertTrue(stack.getItem().getCraftingRemainingItem(stack).is(Items.GLASS_BOTTLE), water.itemPath + " should leave a glass bottle in a grid");
        }
        helper.succeed();
    }

    private static void assertPurifies(GameTestHelper helper, RecipeType<? extends AbstractCookingRecipe> type, ItemStack water) {
        ServerLevel level = helper.getLevel();
        Optional<? extends AbstractCookingRecipe> recipe = level.getRecipeManager().getRecipeFor(type, new SimpleContainer(water), level);
        helper.assertTrue(recipe.isPresent(), "There should be a " + type + " recipe for " + water);
        ItemStack result = recipe.orElseThrow().getResultItem(level.registryAccess());
        helper.assertTrue(result.is(VitalityItems.waterBottle(BottledWater.PURIFIED)), type + " should purify " + water);
    }

    private static Holder<Biome> biome(GameTestHelper helper, ResourceKey<Biome> biome) {
        return helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(biome);
    }

    private static ItemStack vanillaWaterBottle() {
        return PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
    }

    private static void lookDownAt(ServerPlayer player, BlockPos water) {
        player.moveTo(water.getX() + 0.5, water.getY() + 1.0, water.getZ() + 0.5, 0.0F, LOOKING_STRAIGHT_DOWN);
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
