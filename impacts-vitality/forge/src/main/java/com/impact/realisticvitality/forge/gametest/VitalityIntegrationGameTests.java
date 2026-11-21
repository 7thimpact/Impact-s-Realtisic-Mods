package com.impact.realisticvitality.forge.gametest;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.backslot.BackSlot;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.air.AirTank;
import com.impact.realisticvitality.air.BreathingGear;
import com.impact.realisticvitality.air.BreathingMask;
import com.impact.realisticvitality.player.VitalityState;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.registry.VitalityTags;
import com.impact.realisticvitality.thirst.BottledWater;
import com.impact.realisticvitality.thirst.Drinking;
import com.impact.realisticvitality.thirst.ThirstDrain;
import com.impact.realisticvitality.thirst.VesselFilling;
import com.impact.realisticvitality.thirst.WaterContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Vitality milestone 10, the Food and Inventory parts. Each test checks its mod is installed and passes on its own
 * otherwise, so they run in every family setup that includes Vitality.
 */
@GameTestHolder(VitalityMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class VitalityIntegrationGameTests {
    private static final String LIB_NAMESPACE = VitalitySkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = VitalitySkeletonGameTests.EMPTY_TEMPLATE;
    private static final int HALF_THIRST = 10;
    private static final float TOLERANCE = 0.001F;
    private static final String SRF_MOD_ID = "realisticforgingores";

    private VitalityIntegrationGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void foodSoupsHydrateAndWarm(GameTestHelper helper) {
        Optional<Item> heartyStew = ResourceIds.item(ResourceIds.of(VitalityMod.FOOD_MOD_ID, "hearty_stew"));
        if (heartyStew.isEmpty()) {
            helper.succeed();
            return;
        }
        withPlayer(helper, player -> {
            VitalityState.of(player).thirst.setLevel(HALF_THIRST);
            Drinking.onUseFinished(player, new ItemStack(heartyStew.get()));
            helper.assertTrue(VitalityState.of(player).thirst.level() == HALF_THIRST + 6, "Food's soups give 6 thirst");
            helper.assertTrue(Math.abs(VitalityState.of(player).temperature.body() - 15.0F) < TOLERANCE, "and warm you by 15");
            helper.assertTrue(new ItemStack(heartyStew.get()).is(VitalityTags.SOUPS), "They fit the Insulated Flask too");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void foodStylesAffectThirstAndWarmth(GameTestHelper helper) {
        Optional<Item> boiled = firstInTag(VitalityMod.FOOD_MOD_ID, "boiled_foods");
        Optional<Item> toasted = firstInTag(VitalityMod.FOOD_MOD_ID, "toasted_foods");
        Optional<Item> dried = firstInTag(VitalityMod.FOOD_MOD_ID, "dried_foods");
        if (boiled.isEmpty() || toasted.isEmpty() || dried.isEmpty()) {
            helper.succeed();
            return;
        }
        withPlayer(helper, player -> {
            VitalityState.of(player).thirst.setLevel(HALF_THIRST);
            Drinking.onUseFinished(player, new ItemStack(boiled.get()));
            helper.assertTrue(VitalityState.of(player).thirst.level() == HALF_THIRST + 2, "Boiled food gives 2 thirst");
            Drinking.onUseFinished(player, new ItemStack(toasted.get()));
            helper.assertTrue(Math.abs(VitalityState.of(player).temperature.body() - 5.0F) < TOLERANCE, "Toasted food warms you by 5");
            helper.assertTrue(new ItemStack(dried.get()).is(VitalityTags.DRY_FOODS), "Dried food is a dry food");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void bottlesFillFromFoodsCampfireBucket(GameTestHelper helper) {
        Optional<Block> bucket = Optional.ofNullable(ForgeRegistries.BLOCKS.getValue(ResourceIds.of(VitalityMod.FOOD_MOD_ID, "campfire_bucket")))
                .filter(block -> block != Blocks.AIR);
        if (bucket.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        BlockPos campfire = helper.absolutePos(BlockPos.ZERO);
        BlockPos bucketPosition = campfire.above();
        level.setBlockAndUpdate(campfire, Blocks.CAMPFIRE.defaultBlockState());
        level.setBlockAndUpdate(bucketPosition, bucket.get().defaultBlockState());
        try {
            withPlayer(helper, player -> {
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
                VesselFilling.tryFill(level, bucketPosition, player, InteractionHand.MAIN_HAND);
                boolean isBoiling = ImpactsLib.isBoiling(level, bucketPosition);
                BottledWater expected = isBoiling ? BottledWater.PURIFIED : BottledWater.UNCLEAN;
                helper.assertTrue(player.getMainHandItem().is(VitalityItems.waterBottle(expected)),
                        "A bottle from the campfire bucket is " + expected.itemPath + ", got " + player.getMainHandItem());
            });
        } finally {
            level.setBlockAndUpdate(bucketPosition, Blocks.AIR.defaultBlockState());
        }
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void framePackMakesExertionThirstier(GameTestHelper helper) {
        Optional<Item> framePack = ResourceIds.item(ResourceIds.of(VitalityMod.INVENTORY_MOD_ID, "frame_pack"));
        if (framePack.isEmpty() || !BackSlot.isAvailable()) {
            helper.succeed();
            return;
        }
        withPlayer(helper, player -> {
            BackSlot.set(player, new ItemStack(framePack.get()));
            ThirstDrain.onFoodExhaustion(player, 1.0F);
            helper.assertTrue(Math.abs(VitalityState.of(player).thirst.exhaustion() - 1.1F) < TOLERANCE, "A Frame Pack adds 10% to thirst from exertion");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void tanksAndHydrationPackFitTheBackSlot(GameTestHelper helper) {
        if (!BackSlot.isAvailable()) {
            helper.succeed();
            return;
        }
        helper.assertTrue(BackSlot.accepts(new ItemStack(VitalityItems.tank(AirTank.AIR_TANK))), "An Air Tank fits the back slot");
        helper.assertTrue(BackSlot.accepts(new ItemStack(VitalityItems.tank(AirTank.SHULKER_AIR_TANK))), "So does a Shulker Air Tank");
        helper.assertTrue(BackSlot.accepts(new ItemStack(VitalityItems.container(WaterContainer.HYDRATION_PACK))), "And the Hydration Pack");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void cauldronsLeaveSrfOreToSrf(GameTestHelper helper) {
        Optional<Item> srfOre = ResourceIds.item(ResourceIds.of(SRF_MOD_ID, "rawgoldore"));
        if (srfOre.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        BlockPos cauldron = helper.absolutePos(BlockPos.ZERO);
        BlockState fullCauldron = Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
        level.setBlockAndUpdate(cauldron, fullCauldron);
        withPlayer(helper, player -> {
            ItemStack ore = new ItemStack(srfOre.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, ore);
            InteractionResult result = CauldronInteraction.WATER.get(ore.getItem()).interact(fullCauldron, level, cauldron, player, InteractionHand.MAIN_HAND, ore);
            helper.assertTrue(result == InteractionResult.PASS, "Vitality's cauldron entries leave SRF's ore alone");
            helper.assertTrue(level.getBlockState(cauldron).getValue(LayeredCauldronBlock.LEVEL) == 3, "and the water stays");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
            CauldronInteraction.WATER.get(Items.GLASS_BOTTLE).interact(fullCauldron, level, cauldron, player, InteractionHand.MAIN_HAND,
                    player.getMainHandItem());
            helper.assertTrue(player.getMainHandItem().is(VitalityItems.waterBottle(BottledWater.UNCLEAN)), "Bottles still fill with SRF installed");
        });
    }

    private static Optional<Item> firstInTag(String namespace, String path) {
        TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceIds.of(namespace, path));
        return ForgeRegistries.ITEMS.tags().getTag(tag).stream().findFirst();
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

    @GameTest(templateNamespace = VitalitySkeletonGameTests.LIB_NAMESPACE, template = VitalitySkeletonGameTests.EMPTY_TEMPLATE)
    public static void headwearWorksFromCuriosHeadSlot(GameTestHelper helper) {
        if (!ImpactsLib.isLoaded("curios")) {
            helper.succeed();
            return;
        }
        ServerPlayer player = MockPlayers.joinWorld(helper.getLevel());
        try {
            boolean hasHeadSlot = CuriosHeadSlots.putOnHead(player, new ItemStack(VitalityItems.mask(BreathingMask.RESPIRATOR)));
            helper.assertTrue(hasHeadSlot, "With Curios, Vitality gives players a head slot");
            helper.assertTrue(BreathingGear.wornMask(player).equals(Optional.of(BreathingMask.RESPIRATOR)), "A Respirator in the head slot is worn");
            helper.assertTrue(player.getItemBySlot(EquipmentSlot.HEAD).isEmpty(), "while the helmet slot stays free");
        } finally {
            MockPlayers.leaveWorld(player);
        }
        helper.succeed();
    }
}
