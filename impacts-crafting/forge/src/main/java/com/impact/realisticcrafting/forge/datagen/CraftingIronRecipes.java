package com.impact.realisticcrafting.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.lib.forge.datagen.ToolCraftingRecipeBuilder;
import com.impact.lib.forge.datagen.WoodIngredients;
import com.impact.lib.wood.WoodData;
import com.impact.lib.wood.WoodKind;
import com.impact.realisticcrafting.config.OverhaulCategory;
import com.impact.realisticcrafting.item.BucketMold;
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
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
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
 * The iron branch, made at the Makeshift Stone Table: the iron tools (the Tweezers' pattern is turned over, since
 * SRF's pan already uses the spec's), Iron Plate, Rod, Rivets, Hinge and Wheel, the Crafting Table, the plain iron goods
 * (some hammered from a loose item), and the iron rows of the wood and stone catalogs. Iron recipes take
 * {@code #impactscrafting:iron_plates} and {@code #impactscrafting:iron_rods}, so SRF's plates and rods fit too.
 */
final class CraftingIronRecipes {
    private static final int BUCKET_INGOTS = 3;
    private static final int FURNACE_TICKS = 200;
    private static final float MOLD_FIRING_EXPERIENCE = 0.1F;
    private static final float MELTING_EXPERIENCE = 0.35F;
    private static final int PANES_PER_GLASS = 3;
    private static final int HANGING_SIGNS = 6;

    private CraftingIronRecipes() {
    }

    static void build(Consumer<FinishedRecipe> output) {
        Consumer<FinishedRecipe> parts = partRecipe(output);
        Consumer<FinishedRecipe> stone = overhaulRecipe(output, OverhaulCategory.STONE);
        tools(parts);
        ironParts(parts);
        ironGoods(overhaulRecipe(output, OverhaulCategory.IRON), stone, overhaulRecipe(output, OverhaulCategory.REDSTONE),
                overhaulRecipe(output, OverhaulCategory.COMBAT));
        woodRows(overhaulRecipe(output, OverhaulCategory.WOOD));
        stoneRows(stone);
    }

    private static void tools(Consumer<FinishedRecipe> parts) {
        Item stick = CraftingItems.part(CraftingPart.TRIMMED_STICK);
        shaped(CraftingItems.tool(CraftingTool.IRON_SAW), 1).pattern("SII").define('S', stick).define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON)).save(parts, id("iron_saw"));
        shaped(CraftingItems.tool(CraftingTool.IRON_HAMMER), 1).pattern("II").pattern(" S").define('S', stick).define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON)).save(parts, id("iron_hammer"));
        shaped(CraftingItems.tool(CraftingTool.IRON_TWEEZERS), 1).pattern(" I ").pattern("N N").define('I', Tags.Items.INGOTS_IRON)
                .define('N', Tags.Items.NUGGETS_IRON).unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON)).save(parts, id("iron_tweezers"));
        shaped(CraftingItems.tool(CraftingTool.IRON_CHISEL), 1).pattern("I").pattern("S").define('S', stick).define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON)).save(parts, id("iron_chisel"));
    }

    private static void ironParts(Consumer<FinishedRecipe> parts) {
        ToolCraftingRecipeBuilder.of(CraftingItems.part(CraftingPart.IRON_PLATE), 1).requires(Ingredient.of(Tags.Items.INGOTS_IRON))
                .tool(tool(CraftingTool.IRON_HAMMER)).save(parts, id("iron_plate"));
        ToolCraftingRecipeBuilder.of(CraftingItems.part(CraftingPart.IRON_ROD), 2).requires(Ingredient.of(Tags.Items.INGOTS_IRON))
                .tool(tool(CraftingTool.IRON_SAW)).save(parts, id("iron_rod"));
        ToolCraftingRecipeBuilder.of(CraftingItems.part(CraftingPart.IRON_RIVETS), 2).requires(Ingredient.of(Tags.Items.NUGGETS_IRON))
                .tool(tool(CraftingTool.IRON_HAMMER)).save(parts, id("iron_rivets"));
        shapeless(CraftingItems.part(CraftingPart.IRON_HINGE), 2).requires(CraftingTags.IRON_PLATES).requires(CraftingItems.part(CraftingPart.IRON_RIVETS))
                .unlockedBy("has_iron_plate", has(CraftingTags.IRON_PLATES)).save(parts, id("iron_hinge"));
        shaped(CraftingItems.part(CraftingPart.IRON_WHEEL), 1).pattern(" R ").pattern("RPR").pattern(" R ").define('R', CraftingTags.IRON_RODS)
                .define('P', CraftingTags.IRON_PLATES).unlockedBy("has_iron_rod", has(CraftingTags.IRON_RODS)).save(parts, id("iron_wheel"));
    }

    private static void ironGoods(Consumer<FinishedRecipe> iron, Consumer<FinishedRecipe> stone, Consumer<FinishedRecipe> redstone,
                                  Consumer<FinishedRecipe> combat) {
        Item rivets = CraftingItems.part(CraftingPart.IRON_RIVETS);
        Item hinge = CraftingItems.part(CraftingPart.IRON_HINGE);
        Item stick = CraftingItems.part(CraftingPart.TRIMMED_STICK);
        bucketCasting(iron);
        // Riveted goods come out with their rivets in place but not driven home; hammering finishes them.
        shapeless(CraftingItems.looseIron(LooseIron.CAULDRON), 1).requires(Items.BUCKET).requires(Ingredient.of(CraftingTags.IRON_PLATES), 4)
                .requires(rivets, 2).unlockedBy("has_bucket", has(Items.BUCKET)).save(iron, id(LooseIron.CAULDRON.path));
        shapeless(CraftingItems.looseIron(LooseIron.IRON_DOOR), 1).requires(Ingredient.of(CraftingTags.IRON_PLATES), 2).requires(hinge)
                .requires(rivets, 2).unlockedBy("has_iron_hinge", has(hinge)).save(iron, id(LooseIron.IRON_DOOR.path));
        shapeless(CraftingItems.looseIron(LooseIron.MINECART), 1).requires(Ingredient.of(CraftingTags.IRON_PLATES), 3)
                .requires(CraftingItems.part(CraftingPart.IRON_WHEEL), 2).requires(rivets, 2)
                .unlockedBy("has_iron_wheel", has(CraftingItems.part(CraftingPart.IRON_WHEEL))).save(iron, id(LooseIron.MINECART.path));
        Item mortar = CraftingItems.part(CraftingPart.MORTAR);
        for (LooseIron looseIron : LooseIron.values()) {
            Item finished = ResourceIds.item(ResourceIds.of("minecraft", looseIron.vanillaPath)).orElseThrow();
            boolean isSealed = looseIron.finish == LooseIron.Finish.MORTAR;
            HammeringRecipeBuilder finishing = HammeringRecipeBuilder.of(CraftingItems.looseIron(looseIron),
                    isSealed ? Ingredient.of(mortar) : Ingredient.of(CraftingTags.HAMMERS), finished);
            if (isSealed) {
                finishing.consumesTool();
            }
            finishing.save(outputFor(looseIron, iron, stone, redstone, combat), id(looseIron.vanillaPath + "_from_hammering"));
        }
        shapeless(Items.HOPPER, 1).requires(Items.BUCKET).requires(Ingredient.of(CraftingTags.IRON_PLATES), 2).requires(Items.CHEST)
                .unlockedBy("has_bucket", has(Items.BUCKET)).save(iron, id("hopper"));
        shapeless(Items.IRON_BARS, 8).requires(Ingredient.of(CraftingTags.IRON_RODS), 3).unlockedBy("has_iron_rod", has(CraftingTags.IRON_RODS))
                .save(iron, id("iron_bars"));
        shapeless(Items.CHAIN, 1).requires(rivets, 3).unlockedBy("has_rivets", has(rivets)).save(iron, id("chain"));
        shapeless(Items.LANTERN, 1).requires(Items.TORCH).requires(Ingredient.of(CraftingTags.IRON_RODS), 2).requires(rivets)
                .unlockedBy("has_rivets", has(rivets)).save(iron, id("lantern"));
        shapeless(Items.SOUL_LANTERN, 1).requires(Items.SOUL_TORCH).requires(Ingredient.of(CraftingTags.IRON_RODS), 2).requires(rivets)
                .unlockedBy("has_rivets", has(rivets)).save(iron, id("soul_lantern"));
        shapeless(Items.IRON_TRAPDOOR, 1).requires(CraftingTags.IRON_PLATES).requires(hinge).unlockedBy("has_iron_hinge", has(hinge))
                .save(iron, id("iron_trapdoor"));
        ToolCraftingRecipeBuilder.of(Items.HEAVY_WEIGHTED_PRESSURE_PLATE, 1).requires(Ingredient.of(CraftingTags.IRON_PLATES))
                .tool(tool(CraftingTool.IRON_SAW)).save(iron, id("heavy_weighted_pressure_plate"));
        shapeless(Items.TRIPWIRE_HOOK, 2).requires(CraftingTags.IRON_RODS).requires(stick).requires(CraftingItems.trimmedPlanks())
                .unlockedBy("has_iron_rod", has(CraftingTags.IRON_RODS)).save(iron, id("tripwire_hook"));
        shapeless(Items.RAIL, 8).requires(Ingredient.of(CraftingTags.IRON_RODS), 4).requires(stick, 2).unlockedBy("has_iron_rod", has(CraftingTags.IRON_RODS))
                .save(iron, id("rail"));
    }

    // Clay mold, fired; filled with iron, melted; cooled in water (in game) for the bucket and the mold back.
    private static void bucketCasting(Consumer<FinishedRecipe> iron) {
        Item unfired = CraftingItems.bucketMold(BucketMold.UNFIRED);
        Item fired = CraftingItems.bucketMold(BucketMold.FIRED);
        Item filled = CraftingItems.bucketMold(BucketMold.FILLED);
        Item molten = CraftingItems.bucketMold(BucketMold.MOLTEN);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, unfired).pattern("C C").pattern("CCC").define('C', Items.CLAY_BALL)
                .unlockedBy("has_clay", has(Items.CLAY_BALL)).save(iron, id(BucketMold.UNFIRED.path));
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(unfired), RecipeCategory.MISC, fired, MOLD_FIRING_EXPERIENCE, FURNACE_TICKS)
                .unlockedBy("has_unfired_mold", has(unfired)).save(iron, id(BucketMold.FIRED.path + "_from_firing"));
        shapeless(filled, 1).requires(fired).requires(Ingredient.of(Tags.Items.INGOTS_IRON), BUCKET_INGOTS)
                .unlockedBy("has_bucket_mold", has(fired)).save(iron, id(BucketMold.FILLED.path));
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(filled), RecipeCategory.MISC, molten, MELTING_EXPERIENCE, FURNACE_TICKS)
                .unlockedBy("has_filled_mold", has(filled)).save(iron, id(BucketMold.MOLTEN.path + "_from_smelting"));
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(filled), RecipeCategory.MISC, molten, MELTING_EXPERIENCE, FURNACE_TICKS / 2)
                .unlockedBy("has_filled_mold", has(filled)).save(iron, id(BucketMold.MOLTEN.path + "_from_blasting"));
    }

    // A loose item's hammering follows its own catalog's switch: the blast furnace stone, the piston redstone, the shield combat.
    private static Consumer<FinishedRecipe> outputFor(LooseIron looseIron, Consumer<FinishedRecipe> iron, Consumer<FinishedRecipe> stone,
                                                     Consumer<FinishedRecipe> redstone, Consumer<FinishedRecipe> combat) {
        return switch (looseIron) {
            case BLAST_FURNACE -> stone;
            case PISTON -> redstone;
            case SHIELD -> combat;
            default -> iron;
        };
    }

    private static void woodRows(Consumer<FinishedRecipe> wood) {
        ToolCraftingRecipeBuilder.of(Items.CRAFTING_TABLE, 1).requires(Ingredient.of(ItemTags.PLANKS)).requires(Ingredient.of(ItemTags.PLANKS))
                .requires(Ingredient.of(ItemTags.PLANKS)).requires(Ingredient.of(ItemTags.PLANKS)).tool(tool(CraftingTool.IRON_SAW))
                .tool(tool(CraftingTool.IRON_HAMMER)).tool(tool(CraftingTool.IRON_TWEEZERS)).save(wood, id("crafting_table"));
        shapeless(Items.SMITHING_TABLE, 1).requires(CraftingItems.woodPart(WoodPart.WOODEN_FRAME)).requires(Ingredient.of(CraftingTags.IRON_PLATES), 2)
                .unlockedBy("has_iron_plate", has(CraftingTags.IRON_PLATES)).save(wood, id("smithing_table"));
        for (ResourceLocation woodType : WoodData.VANILLA_WOODS) {
            Item hangingSign = WoodData.vanillaItem(woodType, WoodKind.HANGING_SIGN).orElseThrow();
            shapeless(hangingSign, HANGING_SIGNS).requires(WoodIngredients.of(CraftingItems.woodPart(WoodPart.WOODEN_PANEL), woodType)).requires(Items.CHAIN)
                    .unlockedBy("has_chain", has(Items.CHAIN)).save(wood, id(woodType.getPath() + "_hanging_sign"));
        }
    }

    private static void stoneRows(Consumer<FinishedRecipe> stone) {
        shapeless(CraftingItems.looseIron(LooseIron.BLAST_FURNACE), 1).requires(Items.FURNACE).requires(Ingredient.of(CraftingTags.IRON_PLATES), 3)
                .requires(Items.SMOOTH_STONE, 2).unlockedBy("has_furnace", has(Items.FURNACE)).save(stone, id(LooseIron.BLAST_FURNACE.path));
        shapeless(Items.STONECUTTER, 1).requires(CraftingItems.part(CraftingPart.STONE_BASE)).requires(CraftingTags.IRON_PLATES)
                .unlockedBy("has_stone_base", has(CraftingItems.part(CraftingPart.STONE_BASE))).save(stone, id("stonecutter"));
        ToolCraftingRecipeBuilder.of(Items.GLASS_PANE, PANES_PER_GLASS).requires(Items.GLASS).tool(tool(CraftingTool.IRON_SAW)).save(stone, id("glass_pane"));
        for (DyeColor color : DyeColor.values()) {
            Item glass = ResourceIds.item(ResourceIds.of("minecraft", color.getName() + "_stained_glass")).orElseThrow();
            Item pane = ResourceIds.item(ResourceIds.of("minecraft", color.getName() + "_stained_glass_pane")).orElseThrow();
            ToolCraftingRecipeBuilder.of(pane, PANES_PER_GLASS).requires(glass).tool(tool(CraftingTool.IRON_SAW))
                    .save(stone, id(color.getName() + "_stained_glass_pane"));
        }
        ToolCraftingRecipeBuilder.of(Items.GLASS_BOTTLE, 1).requires(Items.GLASS).tool(tool(CraftingTool.IRON_TWEEZERS)).save(stone, id("glass_bottle"));
    }

    private static ShapedRecipeBuilder shaped(Item result, int count) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result, count);
    }

    private static ShapelessRecipeBuilder shapeless(Item result, int count) {
        return ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result, count);
    }
}
