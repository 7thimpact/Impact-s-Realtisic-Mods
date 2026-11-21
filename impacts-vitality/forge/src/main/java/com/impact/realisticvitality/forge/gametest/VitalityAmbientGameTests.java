package com.impact.realisticvitality.forge.gametest;

import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.player.TemperatureState;
import com.impact.realisticvitality.temperature.AmbientTemperature;
import com.impact.realisticvitality.temperature.BlockTemperatures;
import com.impact.realisticvitality.temperature.TemperatureTicks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Vitality milestone 5: the ambient temperature from biome, dimension, height, caves, nearby blocks and water. */
@GameTestHolder(VitalityMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class VitalityAmbientGameTests {
    private static final String LIB_NAMESPACE = VitalitySkeletonGameTests.LIB_NAMESPACE;
    private static final String EMPTY_TEMPLATE = VitalitySkeletonGameTests.EMPTY_TEMPLATE;
    private static final double TOLERANCE = 0.001;

    private VitalityAmbientGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void biomesFollowTheFormula(GameTestHelper helper) {
        assertClose(helper, AmbientTemperature.biomeBase(biome(helper, Biomes.PLAINS)), 62.0, "Plains");
        assertClose(helper, AmbientTemperature.biomeBase(biome(helper, Biomes.DESERT)), 110.0, "Desert");
        assertClose(helper, AmbientTemperature.biomeBase(biome(helper, Biomes.SNOWY_PLAINS)), 30.0, "Snowy plains");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void theNetherIsHotterAndTheEndIsFlat(GameTestHelper helper) {
        assertClose(helper, AmbientTemperature.dimensionOffset(Level.NETHER), 40.0, "The Nether's offset");
        assertClose(helper, AmbientTemperature.dimensionOffset(Level.OVERWORLD), 0.0, "The Overworld's offset");
        helper.assertTrue(AmbientTemperature.dimensionFixed(Level.END).orElse(0.0) == 20.0, "The End is a flat 20°F");
        helper.assertTrue(AmbientTemperature.dimensionFixed(Level.OVERWORLD).isEmpty(), "The Overworld isn't flat");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void heightCoolsAboveOneHundred(GameTestHelper helper) {
        assertClose(helper, AmbientTemperature.heightAdjustment(140), -10.0, "Y 140");
        assertClose(helper, AmbientTemperature.heightAdjustment(90), 0.0, "Y 90");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void cavesDriftToTheirOwnTemperature(GameTestHelper helper) {
        double surface = 62.0;
        assertClose(helper, surface + AmbientTemperature.undergroundAdjustment(surface, 30), 55.0, "A cave 20 blocks under Y 50");
        assertClose(helper, surface + AmbientTemperature.undergroundAdjustment(surface, 40), 58.5, "Halfway into the cave blend");
        assertClose(helper, surface + AmbientTemperature.undergroundAdjustment(surface, -64), 75.0, "The deepest caves");
        assertClose(helper, AmbientTemperature.undergroundAdjustment(surface, 60), 0.0, "Above Y 50");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void lavaWarmsAndIceCools(GameTestHelper helper) {
        BlockPos center = helper.absolutePos(BlockPos.ZERO);
        assertClose(helper, blockDelta(helper, center, Blocks.LAVA.defaultBlockState()), 25.0, "Lava right next to you");
        assertClose(helper, blockDelta(helper, center, Blocks.ICE.defaultBlockState()), -10.0, "Ice right next to you");
        assertClose(helper, blockDelta(helper, center, Blocks.FURNACE.defaultBlockState().setValue(AbstractFurnaceBlock.LIT, true)), 10.0,
                "A lit furnace warms a sealed room");
        assertClose(helper, blockDelta(helper, center, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false)), 0.0,
                "An unlit campfire gives no heat");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void waterPullsTowardItsOwnTemperature(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos center = helper.absolutePos(BlockPos.ZERO);
        double dry = AmbientTemperature.at(level, center, false).total();
        double soaked = AmbientTemperature.at(level, center, true).total();
        double waterTemperature = level.getBiome(center).value().coldEnoughToSnow(center) ? 35.0 : 60.0;
        assertClose(helper, soaked, dry + (waterTemperature - dry) * 0.75, "Water pulls three quarters of the way to its own °F");
        double desertSwim = AmbientTemperature.waterAdjustment(level, center, level.getBiome(center), 110.0);
        helper.assertTrue(110.0 + desertSwim < 80.0, "A swim in 110°F air feels comfortable, not hot: " + (110.0 + desertSwim));
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void waterLeavesYouWetForThirtySeconds(GameTestHelper helper) {
        TemperatureState temperature = new TemperatureState();
        TemperatureTicks.updateWet(temperature, true);
        helper.assertTrue(temperature.wetTicks() == 600, "Water or rain makes you Wet for 30 seconds");
        TemperatureTicks.updateWet(temperature, false);
        helper.assertTrue(temperature.wetTicks() == 599 && temperature.isWet(), "Out of the water you dry off tick by tick");
        helper.succeed();
    }

    // The difference one block makes right next to the spot, so other blocks around the test don't matter.
    private static double blockDelta(GameTestHelper helper, BlockPos center, BlockState placed) {
        ServerLevel level = helper.getLevel();
        BlockPos neighbor = center.above();
        BlockState original = level.getBlockState(neighbor);
        double without = BlockTemperatures.near(level, center);
        level.setBlockAndUpdate(neighbor, placed);
        double with = BlockTemperatures.near(level, center);
        level.setBlockAndUpdate(neighbor, original);
        return with - without;
    }

    private static Holder<Biome> biome(GameTestHelper helper, ResourceKey<Biome> biome) {
        return helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(biome);
    }

    private static void assertClose(GameTestHelper helper, double actual, double expected, String what) {
        helper.assertTrue(Math.abs(actual - expected) < TOLERANCE, what + " should be " + expected + "°F, got " + actual);
    }
}
