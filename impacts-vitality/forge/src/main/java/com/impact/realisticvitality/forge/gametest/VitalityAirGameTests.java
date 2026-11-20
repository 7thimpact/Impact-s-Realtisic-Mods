package com.impact.realisticvitality.forge.gametest;

import com.impact.lib.gametest.MockPlayers;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.air.AirProtection;
import com.impact.realisticvitality.air.AirQualities;
import com.impact.realisticvitality.air.AirQuality;
import com.impact.realisticvitality.air.AirTicks;
import com.impact.realisticvitality.player.VitalityState;
import com.impact.realisticvitality.registry.VitalityDamageTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.function.Consumer;

/** Vitality milestone 8: air quality by height, dimension and lava; bubbles lost in bad air; damage once they run out. */
@GameTestHolder(VitalityMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class VitalityAirGameTests {
    private static final String LIB_NAMESPACE = VitalitySkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = VitalitySkeletonGameTests.EMPTY_TEMPLATE;
    private static final int FULL_AIR = 300;

    private VitalityAirGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void heightBandsInTheOverworld(GameTestHelper helper) {
        helper.assertTrue(AirQualities.ofPlace(Level.OVERWORLD, 100) == AirQuality.GOOD, "Y 100 is good air");
        helper.assertTrue(AirQualities.ofPlace(Level.OVERWORLD, 170) == AirQuality.THIN, "Y 170 is thin");
        helper.assertTrue(AirQualities.ofPlace(Level.OVERWORLD, 230) == AirQuality.VERY_THIN, "Y 230 is very thin");
        helper.assertTrue(AirQualities.ofPlace(Level.OVERWORLD, -50) == AirQuality.THIN, "Below Y −40 the deep air is stale");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void theNetherIsSmokyAndTheEndIsThin(GameTestHelper helper) {
        helper.assertTrue(AirQualities.ofPlace(Level.NETHER, 64) == AirQuality.SMOKY, "The Nether is smoky everywhere (from its data file)");
        helper.assertTrue(AirQualities.ofPlace(Level.END, 64) == AirQuality.THIN, "The End is thin everywhere");
        helper.assertTrue(AirQualities.ofPlace(Level.NETHER, 250) == AirQuality.SMOKY, "Height bands are only the Overworld's");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void lavaFumesButFurnacesDont(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos body = helper.absolutePos(BlockPos.ZERO);
        AABB playerBox = new AABB(body).setMaxY(body.getY() + 1.8);
        helper.assertTrue(AirQualities.ofLava(level, playerBox) == AirQuality.GOOD, "No lava nearby: good air");
        placeAndCheck(helper, body.east(), Blocks.FURNACE.defaultBlockState().setValue(AbstractFurnaceBlock.LIT, true), playerBox, AirQuality.GOOD,
                "A lit furnace never affects air");
        placeAndCheck(helper, body.east(3), Blocks.LAVA.defaultBlockState(), playerBox, AirQuality.SMOKY, "Lava 3 blocks away is smoky");
        placeAndCheck(helper, body.east(), Blocks.LAVA.defaultBlockState(), playerBox, AirQuality.VERY_SMOKY, "Lava right next to you is very smoky");
        placeAndCheck(helper, body.east(), Blocks.LAVA_CAULDRON.defaultBlockState(), playerBox, AirQuality.VERY_SMOKY, "A lava cauldron fumes too");
        placeAndCheck(helper, body.east(), Blocks.MAGMA_BLOCK.defaultBlockState(), playerBox, AirQuality.GOOD, "Magma doesn't fume");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void thinAirTakesABubbleEverySixSeconds(GameTestHelper helper) {
        withPlayer(helper, player -> {
            VitalityState.of(player).air.setQuality(AirQuality.THIN);
            player.setAirSupply(FULL_AIR);
            for (int tick = 0; tick < 120; tick++) {
                drainTick(player);
            }
            helper.assertTrue(player.getAirSupply() == FULL_AIR - AirTicks.AIR_PER_BUBBLE, "6 seconds of thin air takes one bubble, got " + player.getAirSupply());
            helper.assertTrue(AirTicks.holdsBackRefill(player, AirQuality.THIN), "and vanilla's refill waits");
            helper.assertFalse(AirTicks.holdsBackRefill(player, AirQuality.GOOD), "Good air refills as usual");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void breathingEffectsAndRespirationProtect(GameTestHelper helper) {
        withPlayer(helper, player -> {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200));
            helper.assertTrue(AirProtection.bubbleTicks(player, AirQuality.VERY_SMOKY).isEmpty(), "Water Breathing protects against bad air");
            player.removeAllEffects();
            ItemStack helmet = new ItemStack(Items.IRON_HELMET);
            helmet.enchant(Enchantments.RESPIRATION, 1);
            player.setItemSlot(EquipmentSlot.HEAD, helmet);
            helper.assertTrue(AirProtection.bubbleTicks(player, AirQuality.THIN).getAsInt() == 180, "Respiration I slows thin air from 120 to 180 ticks a bubble");
            ItemStack respirationThree = new ItemStack(Items.IRON_HELMET);
            respirationThree.enchant(Enchantments.RESPIRATION, 3);
            player.setItemSlot(EquipmentSlot.HEAD, respirationThree);
            helper.assertTrue(AirProtection.bubbleTicks(player, AirQuality.THIN).isEmpty(), "Respiration III stops the loss");
        });
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void fumesHurtOnceTheBubblesRunOut(GameTestHelper helper) {
        withPlayer(helper, player -> {
            Difficulty original = helper.getLevel().getServer().getWorldData().getDifficulty();
            helper.getLevel().getServer().setDifficulty(Difficulty.NORMAL, true);
            try {
                MockPlayers.endSpawnProtection(player);
                VitalityState.of(player).air.setQuality(AirQuality.VERY_SMOKY);
                player.setAirSupply(0);
                for (int tick = 0; tick < 40; tick++) {
                    drainTick(player);
                }
                helper.assertTrue(player.getHealth() == player.getMaxHealth() - 2.0F, "Out of air, smoke deals 2 damage every 2 seconds, got " + player.getHealth());
                helper.assertTrue(player.getLastDamageSource().is(VitalityDamageTypes.FUMES), "as impactsvitality:fumes");
            } finally {
                helper.getLevel().getServer().setDifficulty(original, true);
            }
        });
    }

    // One tick of air without the once-a-second quality reading, which would read the test spot's real air.
    private static void drainTick(ServerPlayer player) {
        player.tickCount = 1 - player.getId();
        AirTicks.tick(player);
    }

    private static void placeAndCheck(GameTestHelper helper, BlockPos position, BlockState placed, AABB body,
                                      AirQuality expected, String what) {
        ServerLevel level = helper.getLevel();
        BlockState original = level.getBlockState(position);
        level.setBlockAndUpdate(position, placed);
        AirQuality actual = AirQualities.ofLava(level, body);
        level.setBlockAndUpdate(position, original);
        helper.assertTrue(actual == expected, what + ", got " + actual);
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
