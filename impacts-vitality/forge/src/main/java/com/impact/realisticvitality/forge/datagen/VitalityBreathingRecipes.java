package com.impact.realisticvitality.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.lib.forge.datagen.ConditionalRecipes;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.air.AirTank;
import com.impact.realisticvitality.air.BreathingMask;
import com.impact.realisticvitality.config.VitalityGear;
import com.impact.realisticvitality.config.VitalityNeed;
import com.impact.realisticvitality.forge.condition.FeatureEnabledCondition;
import com.impact.realisticvitality.registry.VitalityBlocks;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.registry.VitalityRecipeSerializers;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.function.Consumer;

/**
 * Breathing gear recipes without Crafting installed (the versions made from Crafting's parts arrive with Crafting), the
 * ones that stay the same with it (Shulker Air Tank, Netherite Rebreather), and fitting filters into masks in a grid.
 */
final class VitalityBreathingRecipes {
    private VitalityBreathingRecipes() {
    }

    static void build(Consumer<FinishedRecipe> output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, mask(BreathingMask.CLOTH_MASK))
                .pattern("WSW")
                .define('W', ItemTags.WOOL)
                .define('S', Items.STRING)
                .unlockedBy("has_string", has(Items.STRING))
                .save(alone(output, VitalityGear.CLOTH_MASK));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, VitalityItems.airBladder())
                .requires(Items.LEATHER)
                .requires(Items.STRING)
                .unlockedBy("has_leather", has(Items.LEATHER))
                .save(alone(output, VitalityGear.AIR_BLADDER));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, mask(BreathingMask.RESPIRATOR))
                .pattern(" S ")
                .pattern("LGL")
                .define('S', Items.STRING)
                .define('L', Items.LEATHER)
                .define('G', Items.GLASS_PANE)
                .unlockedBy("has_leather", has(Items.LEATHER))
                .save(alone(output, VitalityGear.RESPIRATOR));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, VitalityItems.tank(AirTank.AIR_TANK))
                .pattern("I I")
                .pattern("IGI")
                .pattern(" I ")
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GLASS)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(alone(output, VitalityGear.AIR_TANK));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, mask(BreathingMask.REBREATHER))
                .pattern("GRG")
                .pattern(" B ")
                .define('G', Items.GOLD_INGOT)
                .define('R', mask(BreathingMask.RESPIRATOR))
                .define('B', VitalityItems.airBladder())
                .unlockedBy("has_respirator", has(mask(BreathingMask.RESPIRATOR)))
                .save(alone(output, VitalityGear.REBREATHER));
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, VitalityBlocks.airCompressor())
                .pattern("CPC")
                .pattern("CFC")
                .define('C', Items.COPPER_INGOT)
                .define('P', Items.PISTON)
                .define('F', Items.FURNACE)
                .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
                .save(alone(output, VitalityGear.AIR_COMPRESSOR));
        sameWithCrafting(output);
    }

    private static void sameWithCrafting(Consumer<FinishedRecipe> output) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, VitalityItems.tank(AirTank.SHULKER_AIR_TANK))
                .requires(VitalityItems.tank(AirTank.AIR_TANK))
                .requires(Items.SHULKER_SHELL, 2)
                .unlockedBy("has_shulker_shell", has(Items.SHULKER_SHELL))
                .save(gear(output, VitalityGear.SHULKER_AIR_TANK));
        SmithingTransformRecipeBuilder.smithing(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of(mask(BreathingMask.REBREATHER)),
                        Ingredient.of(Items.NETHERITE_INGOT), RecipeCategory.COMBAT, VitalityItems.mask(BreathingMask.NETHERITE_REBREATHER))
                .unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT))
                .save(gear(output, VitalityGear.NETHERITE_REBREATHER), ResourceIds.of(VitalityMod.MOD_ID, "netherite_rebreather_smithing"));
        SpecialRecipeBuilder.special(VitalityRecipeSerializers.maskFilter())
                .save(conditional(output, FeatureEnabledCondition.of(VitalityNeed.AIR.configKey)), VitalityMod.MOD_ID + ":mask_filter");
    }

    private static ItemLike mask(BreathingMask mask) {
        return VitalityItems.mask(mask);
    }

    private static Consumer<FinishedRecipe> alone(Consumer<FinishedRecipe> output, VitalityGear gear) {
        return conditional(output, FeatureEnabledCondition.of(gear.itemPath), ConditionalRecipes.modNotLoaded(VitalityMod.CRAFTING_MOD_ID));
    }

    private static Consumer<FinishedRecipe> gear(Consumer<FinishedRecipe> output, VitalityGear gear) {
        return conditional(output, FeatureEnabledCondition.of(gear.itemPath));
    }

    private static InventoryChangeTrigger.TriggerInstance has(ItemLike item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(item);
    }

    private static Consumer<FinishedRecipe> conditional(Consumer<FinishedRecipe> output, ICondition... conditions) {
        return recipe -> output.accept(ConditionalRecipes.withConditions(recipe, conditions));
    }
}
