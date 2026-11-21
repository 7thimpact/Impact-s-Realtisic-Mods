package com.impact.realisticcrafting.forge.datagen;

import com.impact.lib.forge.datagen.ToolCraftingRecipeBuilder;
import com.impact.realisticcrafting.config.OverhaulCategory;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.CraftingTool;
import com.impact.realisticcrafting.item.LooseIron;
import com.impact.realisticcrafting.item.WoodPart;
import com.impact.realisticcrafting.registry.CraftingItems;
import com.impact.realisticcrafting.registry.CraftingTags;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.has;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.id;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.overhaulRecipe;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.partRecipe;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.tool;

/**
 * Redstone and transport: the Redstone Mechanism and Piston Head, and every redstone machine as a housing (Stone
 * Casing, Stone Base or a Wooden Frame) plus the mechanism or torches. Quartz, gold, glowstone and diamond make the
 * comparator, daylight detector, observer and jukebox Crafting Table recipes. Rails, the sticky piston and the gold and
 * glowstone recipes stay vanilla.
 */
final class CraftingRedstoneRecipes {
    private CraftingRedstoneRecipes() {
    }

    static void build(Consumer<FinishedRecipe> output) {
        Consumer<FinishedRecipe> parts = partRecipe(output);
        Consumer<FinishedRecipe> redstone = overhaulRecipe(output, OverhaulCategory.REDSTONE);
        Item mechanism = CraftingItems.part(CraftingPart.REDSTONE_MECHANISM);
        Item pistonHead = CraftingItems.part(CraftingPart.PISTON_HEAD);
        Item casing = CraftingItems.part(CraftingPart.STONE_CASING);
        Item base = CraftingItems.part(CraftingPart.STONE_BASE);
        Item frame = CraftingItems.woodPart(WoodPart.WOODEN_FRAME);
        Item panel = CraftingItems.woodPart(WoodPart.WOODEN_PANEL);

        ToolCraftingRecipeBuilder.of(mechanism, 1).requires(Ingredient.of(CraftingTags.IRON_PLATES)).requires(Ingredient.of(Tags.Items.DUSTS_REDSTONE))
                .requires(Ingredient.of(Tags.Items.DUSTS_REDSTONE)).requires(CraftingItems.part(CraftingPart.IRON_RIVETS))
                .tool(tool(CraftingTool.IRON_TWEEZERS)).save(parts, id("redstone_mechanism"));
        shapeless(pistonHead, 1).requires(panel).requires(CraftingTags.IRON_PLATES).unlockedBy("has_iron_plate", has(CraftingTags.IRON_PLATES))
                .save(parts, id("piston_head"));

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Items.REDSTONE_TORCH).pattern("R").pattern("S").define('R', Tags.Items.DUSTS_REDSTONE)
                .define('S', CraftingItems.part(CraftingPart.TRIMMED_STICK)).unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE))
                .save(redstone, id("redstone_torch"));
        shapeless(Items.REPEATER, 1).requires(base).requires(Items.REDSTONE_TORCH, 2).requires(Tags.Items.DUSTS_REDSTONE)
                .unlockedBy("has_stone_base", has(base)).save(redstone, id("repeater"));
        shapeless(CraftingItems.looseIron(LooseIron.PISTON), 1).requires(pistonHead).requires(casing).requires(mechanism).requires(CraftingTags.IRON_RODS)
                .unlockedBy("has_piston_head", has(pistonHead)).save(redstone, id("loose_piston"));
        shapeless(Items.DROPPER, 1).requires(casing).requires(mechanism).unlockedBy("has_mechanism", has(mechanism)).save(redstone, id("dropper"));
        shapeless(Items.DISPENSER, 1).requires(Items.DROPPER).requires(Items.BOW).unlockedBy("has_dropper", has(Items.DROPPER)).save(redstone, id("dispenser"));
        shapeless(Items.NOTE_BLOCK, 1).requires(frame).requires(panel).requires(Tags.Items.DUSTS_REDSTONE).unlockedBy("has_frame", has(frame))
                .save(redstone, id("note_block"));
        ToolCraftingRecipeBuilder.of(Items.COMPASS, 1).requires(Ingredient.of(CraftingTags.IRON_PLATES)).requires(Ingredient.of(Tags.Items.DUSTS_REDSTONE))
                .tool(tool(CraftingTool.IRON_TWEEZERS)).save(redstone, id("compass"));
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Items.TNT).pattern("GSG").pattern("SPS").pattern("GSG").define('G', Items.GUNPOWDER)
                .define('S', Items.SAND).define('P', Items.PAPER).unlockedBy("has_gunpowder", has(Items.GUNPOWDER)).save(redstone, id("tnt"));
        shapeless(Items.COMPARATOR, 1).requires(base).requires(Items.REDSTONE_TORCH, 3).requires(Tags.Items.GEMS_QUARTZ)
                .unlockedBy("has_stone_base", has(base)).save(redstone, id("comparator"));
        shapeless(Items.DAYLIGHT_DETECTOR, 1).requires(base).requires(Items.GLASS, 2).requires(Tags.Items.GEMS_QUARTZ)
                .unlockedBy("has_stone_base", has(base)).save(redstone, id("daylight_detector"));
        shapeless(Items.OBSERVER, 1).requires(casing).requires(mechanism).requires(Tags.Items.GEMS_QUARTZ).unlockedBy("has_mechanism", has(mechanism))
                .save(redstone, id("observer"));
        shapeless(Items.JUKEBOX, 1).requires(Items.NOTE_BLOCK).requires(Tags.Items.GEMS_DIAMOND).unlockedBy("has_note_block", has(Items.NOTE_BLOCK))
                .save(redstone, id("jukebox"));
    }

    private static ShapelessRecipeBuilder shapeless(Item result, int count) {
        return ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, result, count);
    }
}
