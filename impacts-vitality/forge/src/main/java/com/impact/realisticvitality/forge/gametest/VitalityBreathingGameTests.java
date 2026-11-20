package com.impact.realisticvitality.forge.gametest;

import com.impact.lib.gametest.CraftingGrids;
import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.air.AirProtection;
import com.impact.realisticvitality.air.AirQuality;
import com.impact.realisticvitality.air.AirTank;
import com.impact.realisticvitality.air.BreathingGear;
import com.impact.realisticvitality.air.BreathingMask;
import com.impact.realisticvitality.air.MaskFilters;
import com.impact.realisticvitality.air.block.AirCompressorBlockEntity;
import com.impact.realisticvitality.item.AirBladderItem;
import com.impact.realisticvitality.item.BreathingMaskItem;
import com.impact.realisticvitality.player.VitalityState;
import com.impact.realisticvitality.registry.VitalityBlocks;
import com.impact.realisticvitality.registry.VitalityItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.function.Consumer;

/** Vitality milestone 9: masks and filters against smoke, tanks against thin air, the bladder, bottled air and the compressor. */
@GameTestHolder(VitalityMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class VitalityBreathingGameTests {
    private static final String LIB_NAMESPACE = VitalitySkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = VitalitySkeletonGameTests.EMPTY_TEMPLATE;

    private VitalityBreathingGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void filteredRespiratorStopsSmoke(GameTestHelper helper) {
        withPlayer(helper, player -> {
            ItemStack respirator = new ItemStack(VitalityItems.mask(BreathingMask.RESPIRATOR));
            player.setItemSlot(EquipmentSlot.HEAD, respirator);
            helper.assertTrue(AirProtection.bubbleTicks(player, AirQuality.VERY_SMOKY).isPresent(), "A Respirator without a filter doesn't help");
            MaskFilters.setFilter(respirator, new ItemStack(VitalityItems.charcoalFilter()));
            helper.assertTrue(AirProtection.bubbleTicks(player, AirQuality.VERY_SMOKY).isEmpty(), "With a filter, lava fumes count as good air");
            helper.assertTrue(AirProtection.bubbleTicks(player, AirQuality.THIN).isPresent(), "A filter doesn't help in thin air");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void netheriteRebreatherNeverNeedsAFilter(GameTestHelper helper) {
        withPlayer(helper, player -> {
            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(VitalityItems.mask(BreathingMask.NETHERITE_REBREATHER)));
            helper.assertTrue(AirProtection.bubbleTicks(player, AirQuality.SMOKY).isEmpty(), "Smoke never affects you");
            helper.assertTrue(VitalityItems.mask(BreathingMask.NETHERITE_REBREATHER).isFireResistant(), "and it survives fire");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void dampClothMaskHalvesSmoke(GameTestHelper helper) {
        withPlayer(helper, player -> {
            ItemStack mask = new ItemStack(VitalityItems.mask(BreathingMask.CLOTH_MASK));
            player.setItemSlot(EquipmentSlot.HEAD, mask);
            helper.assertTrue(AirProtection.bubbleTicks(player, AirQuality.SMOKY).getAsInt() == 80, "A dry mask doesn't help");
            BreathingMaskItem.setDampTicks(mask, 2400);
            helper.assertTrue(AirProtection.bubbleTicks(player, AirQuality.SMOKY).getAsInt() == 160, "A damp mask halves the bubble loss");
            BreathingGear.tickSecond(player, AirQuality.GOOD, false);
            helper.assertTrue(BreathingMaskItem.dampTicks(mask) == 2380, "It dries while worn");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void airTankSuppliesThinAirWithARespirator(GameTestHelper helper) {
        withPlayer(helper, player -> {
            ItemStack tank = new ItemStack(VitalityItems.tank(AirTank.AIR_TANK));
            player.getInventory().add(tank);
            helper.assertTrue(AirProtection.bubbleTicks(player, AirQuality.THIN).isPresent(), "A tank needs a Respirator to breathe through");
            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(VitalityItems.mask(BreathingMask.RESPIRATOR)));
            helper.assertTrue(AirProtection.bubbleTicks(player, AirQuality.THIN).isEmpty(), "With a Respirator on, the tank supplies thin air");
            ItemStack carried = player.getInventory().items.stream().filter(stack -> stack.is(VitalityItems.tank(AirTank.AIR_TANK))).findFirst().orElseThrow();
            BreathingGear.tickSecond(player, AirQuality.THIN, true);
            helper.assertTrue(AirTank.AIR_TANK.airIn(carried) == 3580, "A second of thin air uses 20 ticks of the tank's 3600");
            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(VitalityItems.mask(BreathingMask.REBREATHER)));
            BreathingGear.tickSecond(player, AirQuality.THIN, true);
            helper.assertTrue(AirTank.AIR_TANK.airIn(carried) == 3570, "A Rebreather makes the tank last twice as long");
            AirTank.AIR_TANK.setAir(carried, 0);
            BreathingGear.tickSecond(player, AirQuality.GOOD, false);
            helper.assertTrue(AirTank.AIR_TANK.airIn(carried) == 120, "In good air it refills, full in 30 seconds");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void filtersWearInSmoke(GameTestHelper helper) {
        withPlayer(helper, player -> {
            ItemStack respirator = new ItemStack(VitalityItems.mask(BreathingMask.RESPIRATOR));
            MaskFilters.setFilter(respirator, new ItemStack(VitalityItems.charcoalFilter()));
            player.setItemSlot(EquipmentSlot.HEAD, respirator);
            BreathingGear.tickSecond(player, AirQuality.SMOKY, true);
            helper.assertTrue(MaskFilters.filterIn(player.getItemBySlot(EquipmentSlot.HEAD)).getDamageValue() == 1, "A second of smoke wears the filter by 1");
            ItemStack worn = new ItemStack(VitalityItems.charcoalFilter());
            worn.setDamageValue(worn.getMaxDamage() - 1);
            MaskFilters.setFilter(respirator, worn);
            BreathingGear.tickSecond(player, AirQuality.SMOKY, true);
            helper.assertTrue(MaskFilters.filterIn(player.getItemBySlot(EquipmentSlot.HEAD)).isEmpty(), "A worn-out filter crumbles");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void filtersFitInAGrid(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack respirator = new ItemStack(VitalityItems.mask(BreathingMask.RESPIRATOR));
        ItemStack oldFilter = new ItemStack(VitalityItems.charcoalFilter());
        oldFilter.setDamageValue(500);
        MaskFilters.setFilter(respirator, oldFilter);
        CraftingContainer grid = CraftingGrids.of(respirator, new ItemStack(VitalityItems.charcoalFilter()));
        CraftingRecipe recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level).orElseThrow();
        ItemStack fitted = recipe.assemble(grid, level.registryAccess());
        helper.assertTrue(MaskFilters.filterIn(fitted).getDamageValue() == 0, "The new filter goes in");
        helper.assertTrue(recipe.getRemainingItems(grid).get(1).getDamageValue() == 500, "The old filter comes back");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void airBladderFillsInGoodAirAndHelpsInThinAir(GameTestHelper helper) {
        withPlayer(helper, player -> {
            ItemStack bladder = new ItemStack(VitalityItems.airBladder());
            VitalityState.of(player).air.setQuality(AirQuality.GOOD);
            BreathingGear.useAirBladder(helper.getLevel(), player, bladder);
            helper.assertTrue(AirBladderItem.uses(bladder) == 3, "Filling in good air gives 3 uses");
            VitalityState.of(player).air.setQuality(AirQuality.THIN);
            player.setAirSupply(0);
            BreathingGear.useAirBladder(helper.getLevel(), player, bladder);
            helper.assertTrue(player.getAirSupply() == 120 && AirBladderItem.uses(bladder) == 2, "A use in thin air gives back 4 bubbles");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void glassBottlesBottleGoodAir(GameTestHelper helper) {
        withPlayer(helper, player -> {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
            VitalityState.of(player).air.setQuality(AirQuality.THIN);
            BreathingGear.tryBottleAir(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            helper.assertTrue(player.getMainHandItem().is(Items.GLASS_BOTTLE), "Thin air can't be bottled");
            VitalityState.of(player).air.setQuality(AirQuality.GOOD);
            BreathingGear.tryBottleAir(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            helper.assertTrue(player.getMainHandItem().is(VitalityItems.bottledAir()), "Good air can");
            player.setAirSupply(0);
            player.getMainHandItem().finishUsingItem(helper.getLevel(), player);
            helper.assertTrue(player.getAirSupply() == 150, "Breathing it gives back 5 bubbles");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void compressorFillsATankWithFuel(GameTestHelper helper) {
        BlockPos position = helper.absolutePos(BlockPos.ZERO);
        helper.getLevel().setBlockAndUpdate(position, VitalityBlocks.airCompressor().defaultBlockState());
        AirCompressorBlockEntity compressor = (AirCompressorBlockEntity) helper.getLevel().getBlockEntity(position);
        withPlayer(helper, player -> {
            ItemStack emptyTank = new ItemStack(VitalityItems.tank(AirTank.AIR_TANK));
            AirTank.AIR_TANK.setAir(emptyTank, 0);
            player.setItemInHand(InteractionHand.MAIN_HAND, emptyTank);
            compressor.use(player, InteractionHand.MAIN_HAND);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COAL));
            compressor.use(player, InteractionHand.MAIN_HAND);
            for (int tick = 0; tick < 100; tick++) {
                compressor.tickCompressor();
            }
            helper.assertTrue(AirTank.AIR_TANK.airIn(compressor.tank()) == AirTank.AIR_TANK.capacity(), "5 seconds of fuel fill the tank, even in thin air");
            helper.assertTrue(compressor.burnTicks() == 1500, "Only the time spent filling burns fuel");
        });
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
