package com.impact.realisticcrafting.forge.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.impact.lib.ImpactsLib;
import com.impact.lib.item.UsesDurability;
import com.impact.lib.log.ImpactsLog;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.CraftingClientConfig;
import com.impact.realisticcrafting.config.CraftingCommonConfig;
import com.impact.realisticcrafting.config.CraftingFeature;
import com.impact.realisticcrafting.config.CraftingServerConfig;
import com.impact.realisticcrafting.config.OverhaulCategory;
import com.impact.realisticcrafting.debug.CraftingLogCategories;
import com.impact.realisticcrafting.forge.condition.OverhaulEnabledCondition;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** Crafting milestone 1: the mod registers with Lib, its config files load with the spec's defaults, and the Stone Pebble stacks. */
@GameTestHolder(CraftingMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CraftingSkeletonGameTests {
    static final String LIB_NAMESPACE = ImpactsLib.MOD_ID;
    static final String EMPTY_TEMPLATE = "empty";
    private static final int UNUSED_STACK_SIZE = 64;
    private static final String CATEGORY_FIELD = "category";

    private CraftingSkeletonGameTests() {
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void registersWithLib(GameTestHelper helper) {
        helper.assertTrue(ImpactsLib.registeredImpactMods().contains(CraftingMod.MOD_ID), "Crafting should register itself with Lib");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void registersEveryLogCategory(GameTestHelper helper) {
        ImpactsLog log = ImpactsLog.of(CraftingMod.MOD_ID);
        CraftingLogCategories.ALL.forEach(category ->
                helper.assertTrue(log.registeredCategories().contains(category), "Crafting should register the log category " + category));
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void commonConfigSwitchesDefaultOn(GameTestHelper helper) {
        CraftingCommonConfig config = CraftingCommonConfig.get();
        for (OverhaulCategory category : OverhaulCategory.values()) {
            helper.assertTrue(config.overhaul.get(category).defaultValue(), "overhaul." + category.configKey + " should default to true");
        }
        for (CraftingFeature feature : CraftingFeature.values()) {
            helper.assertTrue(config.features.get(feature).defaultValue(), "features." + feature.configKey + " should default to true");
        }
        helper.assertTrue(config.disabledRecipes.defaultValue().isEmpty(), "recipes.disabled should start empty");
        helper.assertTrue(config.keptVanillaRecipes.defaultValue().isEmpty(), "recipes.keepVanilla should start empty");
        helper.assertTrue(config.debug.isDebugEnabled(), "debug.enabled should default to true");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void serverConfigUsesSpecDefaults(GameTestHelper helper) {
        CraftingServerConfig.PebbleSettings pebbles = CraftingServerConfig.get().pebbles;
        helper.assertTrue(pebbles.gravelChance.defaultValue() == 0.25, "Gravel drops a pebble 25% of the time");
        helper.assertTrue(pebbles.stonePickaxeChance.defaultValue() == 0.15, "Stone broken with a pickaxe drops a pebble 15% of the time");
        helper.assertTrue(pebbles.doesHandBreakingDrop.defaultValue(), "Stone broken by hand always drops a pebble");
        helper.assertTrue(pebbles.isWorldgenOn.defaultValue(), "pebbles.worldgen should default to true");
        helper.assertTrue(pebbles.surfaceTriesPerChunk.defaultValue() == 6, "6 surface tries per chunk: about a desert's dead bushes");
        helper.assertTrue(pebbles.caveTriesPerChunk.defaultValue() == 6, "Up to 6 cave tries per chunk");
        helper.assertTrue(pebbles.worldgenDimensions.defaultValue().equals(List.of("minecraft:overworld")), "Pebbles generate in the Overworld");
        helper.assertTrue(CraftingServerConfig.get().durability.pebbleUses.defaultValue() == 32, "A pebble lasts 32 uses");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void clientConfigShowsHints(GameTestHelper helper) {
        CraftingClientConfig config = CraftingClientConfig.get();
        helper.assertTrue(config.areNextStepHintsOn.defaultValue(), "Next-step hints should default to on");
        helper.assertTrue(config.areHammeringEffectsOn.defaultValue(), "Hammering effects should default to on");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void pebblesStackUntilUsed(GameTestHelper helper) {
        ItemStack pebble = new ItemStack(CraftingItems.stonePebble());
        helper.assertTrue(pebble.getMaxStackSize() == UNUSED_STACK_SIZE, "Unused pebbles stack to 64");
        helper.assertFalse(pebble.hasTag(), "An unused pebble carries no tag, so fresh pebbles stack together");
        UsesDurability.use(pebble, 1);
        helper.assertTrue(pebble.getMaxStackSize() == 1, "A pebble used once stacks to 1");
        helper.assertTrue(pebble.isBarVisible(), "A used pebble shows a durability bar");
        helper.succeed();
    }

    @GameTest(templateNamespace = LIB_NAMESPACE, template = EMPTY_TEMPLATE)
    public static void overhaulConditionReadsCategories(GameTestHelper helper) {
        JsonObject json = new JsonObject();
        json.addProperty(CATEGORY_FIELD, OverhaulCategory.WOOD.configKey);
        OverhaulEnabledCondition condition = OverhaulEnabledCondition.Serializer.INSTANCE.read(json);
        helper.assertTrue(condition.category() == OverhaulCategory.WOOD, "The condition names its category by config key");
        helper.assertTrue(condition.test(ICondition.IContext.EMPTY), "The wood catalog is on by default");
        JsonObject unknown = new JsonObject();
        unknown.addProperty(CATEGORY_FIELD, "not_a_category");
        helper.assertTrue(rejects(unknown), "An unknown category fails to load instead of passing silently");
        helper.succeed();
    }

    private static boolean rejects(JsonObject json) {
        try {
            OverhaulEnabledCondition.Serializer.INSTANCE.read(json);
            return false;
        } catch (JsonParseException expected) {
            return true;
        }
    }
}
