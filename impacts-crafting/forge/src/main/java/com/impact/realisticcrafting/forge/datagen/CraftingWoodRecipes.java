package com.impact.realisticcrafting.forge.datagen;

import com.impact.lib.forge.datagen.ToolCraftingRecipeBuilder;
import com.impact.lib.forge.datagen.WoodIngredients;
import com.impact.lib.wood.WoodData;
import com.impact.lib.wood.WoodKind;
import com.impact.realisticcrafting.config.OverhaulCategory;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.WoodPart;
import com.impact.realisticcrafting.registry.CraftingItems;
import com.impact.realisticcrafting.registry.CraftingTags;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Optional;
import java.util.function.Consumer;

import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.has;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.id;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.overhaulRecipe;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.partRecipe;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.trimmingTools;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.unusedWoodenBucket;

/**
 * The wood branch: the Wooden Panel, Wooden Frame, Fence Post, Boat Hull, Oar, Chest Top and Wooden Bucket, then every
 * non-iron row of the wood catalog. Per-wood recipes match one wood only, so an oak panel makes an oak door. Where the
 * spec gives several recipes the same ingredients, shapes keep them apart (each noted in the spec's implementation changes).
 */
final class CraftingWoodRecipes {
    private CraftingWoodRecipes() {
    }

    static void build(Consumer<FinishedRecipe> output) {
        Consumer<FinishedRecipe> parts = partRecipe(output);
        Consumer<FinishedRecipe> wood = overhaulRecipe(output, OverhaulCategory.WOOD);
        WoodData.VANILLA_WOODS.forEach(woodType -> perWood(parts, wood, woodType));
        genericParts(parts);
        catalog(wood);
    }

    private static void perWood(Consumer<FinishedRecipe> parts, Consumer<FinishedRecipe> wood, ResourceLocation woodType) {
        String name = woodType.getPath();
        Ingredient trimmedPlanks = WoodIngredients.of(CraftingItems.trimmedPlanks(), woodType);
        Ingredient panel = WoodIngredients.of(CraftingItems.woodPart(WoodPart.WOODEN_PANEL), woodType);
        Ingredient post = WoodIngredients.of(CraftingItems.woodPart(WoodPart.FENCE_POST), woodType);
        Item planks = WoodData.vanillaItem(woodType, WoodKind.PLANKS).orElseThrow();
        Item trimmedStick = CraftingItems.part(CraftingPart.TRIMMED_STICK);
        Item stoneHinge = CraftingItems.part(CraftingPart.STONE_HINGE);

        ShapedRecipeBuilder panelRecipe = ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CraftingItems.woodPart(WoodPart.WOODEN_PANEL))
                .pattern("PP").define('P', trimmedPlanks).unlockedBy("has_trimmed_planks", has(CraftingItems.trimmedPlanks()));
        NbtResultRecipe.of(panelRecipe, WoodData.tagFor(woodType)).save(parts, id(name + "_wooden_panel"));
        ShapedRecipeBuilder frameRecipe = ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CraftingItems.woodPart(WoodPart.WOODEN_FRAME), 2)
                .pattern("PSP").pattern("S S").pattern("PSP").define('P', trimmedPlanks).define('S', trimmedStick)
                .unlockedBy("has_trimmed_planks", has(CraftingItems.trimmedPlanks()));
        NbtResultRecipe.of(frameRecipe, WoodData.tagFor(woodType)).save(parts, id(name + "_wooden_frame"));
        ToolCraftingRecipeBuilder.of(WoodData.of(CraftingItems.woodPart(WoodPart.FENCE_POST), woodType).copyWithCount(4))
                .pattern("P").pattern("P").define('P', trimmedPlanks).tool(trimmingTools()).byproduct(CraftingItems.part(CraftingPart.WOOD_SCRAPS), 1, 1.0F)
                .save(parts, id(name + "_fence_post"));

        ToolCraftingRecipeBuilder.of(WoodData.vanillaItem(woodType, WoodKind.SLAB).orElseThrow(), 6).pattern("PPP").define('P', planks)
                .tool(trimmingTools()).byproduct(CraftingItems.part(CraftingPart.WOOD_SCRAPS), 1, 1.0F).save(wood, id(name + "_slab"));
        ToolCraftingRecipeBuilder.of(WoodData.vanillaItem(woodType, WoodKind.STAIRS).orElseThrow(), 6).pattern("P  ").pattern("PP ").pattern("PPP")
                .define('P', planks).tool(trimmingTools()).byproduct(CraftingItems.part(CraftingPart.WOOD_SCRAPS), 1, 1.0F).save(wood, id(name + "_stairs"));
        ToolCraftingRecipeBuilder.of(WoodData.vanillaItem(woodType, WoodKind.BUTTON).orElseThrow(), 8).pattern("P ").pattern(" P")
                .define('P', trimmedPlanks).tool(trimmingTools()).byproduct(CraftingItems.part(CraftingPart.WOOD_SCRAPS), 1, 1.0F).save(wood, id(name + "_button"));
        ToolCraftingRecipeBuilder.of(WoodData.vanillaItem(woodType, WoodKind.PRESSURE_PLATE).orElseThrow(), 1).requires(panel).tool(trimmingTools())
                .byproduct(CraftingItems.part(CraftingPart.WOOD_SCRAPS), 1, 1.0F).save(wood, id(name + "_pressure_plate"));
        shapeless(WoodData.vanillaItem(woodType, WoodKind.FENCE).orElseThrow(), 3).requires(post).requires(post).requires(trimmedStick).requires(trimmedStick)
                .unlockedBy("has_fence_post", has(CraftingItems.woodPart(WoodPart.FENCE_POST))).save(wood, id(name + "_fence"));
        shapeless(WoodData.vanillaItem(woodType, WoodKind.FENCE_GATE).orElseThrow(), 1).requires(post).requires(post).requires(panel).requires(stoneHinge)
                .unlockedBy("has_fence_post", has(CraftingItems.woodPart(WoodPart.FENCE_POST))).save(wood, id(name + "_fence_gate"));
        shapeless(WoodData.vanillaItem(woodType, WoodKind.DOOR).orElseThrow(), 3).requires(panel).requires(panel).requires(panel)
                .requires(stoneHinge).requires(stoneHinge).unlockedBy("has_panel", has(CraftingItems.woodPart(WoodPart.WOODEN_PANEL))).save(wood, id(name + "_door"));
        shapeless(WoodData.vanillaItem(woodType, WoodKind.TRAPDOOR).orElseThrow(), 2).requires(panel).requires(panel).requires(stoneHinge)
                .unlockedBy("has_panel", has(CraftingItems.woodPart(WoodPart.WOODEN_PANEL))).save(wood, id(name + "_trapdoor"));
        shaped(WoodData.vanillaItem(woodType, WoodKind.SIGN).orElseThrow(), 3).pattern("P").pattern("S").define('P', panel).define('S', trimmedStick)
                .unlockedBy("has_panel", has(CraftingItems.woodPart(WoodPart.WOODEN_PANEL))).save(wood, id(name + "_sign"));

        Optional<Item> boat = WoodData.vanillaItem(woodType, WoodKind.BOAT);
        if (boat.isPresent()) {
            ShapedRecipeBuilder hullRecipe = ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, CraftingItems.woodPart(WoodPart.BOAT_HULL))
                    .pattern("P P").pattern(" P ").define('P', panel).unlockedBy("has_panel", has(CraftingItems.woodPart(WoodPart.WOODEN_PANEL)));
            NbtResultRecipe.of(hullRecipe, WoodData.tagFor(woodType)).save(parts, id(name + "_boat_hull"));
            // The boat is assembled from its parts: the hull and a pair of oars.
            Ingredient hull = WoodIngredients.of(CraftingItems.woodPart(WoodPart.BOAT_HULL), woodType);
            ShapelessRecipeBuilder.shapeless(RecipeCategory.TRANSPORTATION, boat.get()).requires(hull)
                    .requires(CraftingItems.part(CraftingPart.OAR)).requires(CraftingItems.part(CraftingPart.OAR))
                    .unlockedBy("has_hull", has(CraftingItems.woodPart(WoodPart.BOAT_HULL))).save(wood, id(name + "_boat"));
        }
    }

    private static void genericParts(Consumer<FinishedRecipe> parts) {
        Item trimmedPlanks = CraftingItems.trimmedPlanks();
        shaped(CraftingItems.woodenBucket(), 1).pattern("P P").pattern("PPP").define('P', trimmedPlanks)
                .unlockedBy("has_trimmed_planks", has(trimmedPlanks)).save(parts, id("wooden_bucket"));
        shaped(CraftingItems.part(CraftingPart.CHEST_TOP), 1).pattern("PP").pattern("PP").define('P', trimmedPlanks)
                .unlockedBy("has_trimmed_planks", has(trimmedPlanks)).save(parts, id("chest_top"));
        shaped(CraftingItems.part(CraftingPart.OAR), 1).pattern("P").pattern("S").pattern("S").define('P', trimmedPlanks)
                .define('S', CraftingItems.part(CraftingPart.TRIMMED_STICK)).unlockedBy("has_trimmed_planks", has(trimmedPlanks)).save(parts, id("oar"));
        ToolCraftingRecipeBuilder.of(CraftingItems.part(CraftingPart.LEATHER_STRIP), 3).requires(Items.LEATHER).tool(trimmingTools())
                .save(parts, id("leather_strip"));
        ToolCraftingRecipeBuilder.of(CraftingItems.part(CraftingPart.FLINT_ARROWHEAD), 2).requires(Items.FLINT).tool(trimmingTools())
                .save(parts, id("flint_arrowhead"));
    }

    private static void catalog(Consumer<FinishedRecipe> wood) {
        Item trimmedPlanks = CraftingItems.trimmedPlanks();
        Item trimmedStick = CraftingItems.part(CraftingPart.TRIMMED_STICK);
        Item twine = CraftingItems.part(CraftingPart.TWINE);
        Item chestTop = CraftingItems.part(CraftingPart.CHEST_TOP);
        Item frame = CraftingItems.woodPart(WoodPart.WOODEN_FRAME);
        Item panel = CraftingItems.woodPart(WoodPart.WOODEN_PANEL);
        Ingredient bucket = unusedWoodenBucket();
        ToolCraftingRecipeBuilder.of(Items.BOWL, 6).pattern("P P").pattern(" P ").define('P', trimmedPlanks).tool(trimmingTools())
                .byproduct(CraftingItems.part(CraftingPart.WOOD_SCRAPS), 1, 1.0F).save(wood, id("bowl"));
        shaped(Items.CHEST, 1).pattern("HTH").pattern(" B ").define('H', CraftingItems.part(CraftingPart.STONE_HINGE)).define('T', chestTop)
                .define('B', bucket).unlockedBy("has_chest_top", has(chestTop)).save(wood, id("chest"));
        shapeless(Items.BARREL, 1).requires(bucket).requires(chestTop).unlockedBy("has_chest_top", has(chestTop)).save(wood, id("barrel"));
        shapeless(Items.COMPOSTER, 1).requires(bucket).requires(trimmedStick).requires(trimmedStick)
                .unlockedBy("has_wooden_bucket", has(CraftingItems.woodenBucket())).save(wood, id("composter"));
        shapeless(Items.BEEHIVE, 1).requires(bucket).requires(chestTop).requires(Items.HONEYCOMB, 3)
                .unlockedBy("has_chest_top", has(chestTop)).save(wood, id("beehive"));
        shapeless(Items.BOOKSHELF, 1).requires(frame).requires(Items.BOOK, 3).unlockedBy("has_frame", has(frame)).save(wood, id("bookshelf"));
        shapeless(Items.CHISELED_BOOKSHELF, 1).requires(frame).requires(panel).requires(panel).unlockedBy("has_frame", has(frame))
                .save(wood, id("chiseled_bookshelf"));
        shapeless(Items.LECTERN, 1).requires(Items.BOOKSHELF).requires(panel).unlockedBy("has_panel", has(panel)).save(wood, id("lectern"));
        shapeless(Items.LOOM, 1).requires(frame).requires(twine).requires(twine).unlockedBy("has_frame", has(frame)).save(wood, id("loom"));
        shapeless(Items.CARTOGRAPHY_TABLE, 1).requires(frame).requires(Items.PAPER, 2).unlockedBy("has_frame", has(frame))
                .save(wood, id("cartography_table"));
        shapeless(Items.FLETCHING_TABLE, 1).requires(frame).requires(CraftingItems.part(CraftingPart.FLINT_ARROWHEAD), 2)
                .unlockedBy("has_frame", has(frame)).save(wood, id("fletching_table"));
        shapeless(Items.PAINTING, 1).requires(frame).requires(Items.WHITE_WOOL).unlockedBy("has_frame", has(frame)).save(wood, id("painting"));
        shapeless(Items.ITEM_FRAME, 2).requires(frame).requires(CraftingItems.part(CraftingPart.LEATHER_STRIP)).unlockedBy("has_frame", has(frame))
                .save(wood, id("item_frame"));
        shapeless(Items.ARMOR_STAND, 1).requires(trimmedStick, 3).requires(CraftingItems.part(CraftingPart.STONE_BASE))
                .unlockedBy("has_stone_base", has(CraftingItems.part(CraftingPart.STONE_BASE))).save(wood, id("armor_stand"));
        shaped(Items.CAMPFIRE, 1).pattern(" S ").pattern("SCS").pattern("LLL").define('S', trimmedStick).define('C', ItemTags.COALS)
                .define('L', ItemTags.LOGS).unlockedBy("has_trimmed_stick", has(trimmedStick)).save(wood, id("campfire"));
        shaped(Items.SOUL_CAMPFIRE, 1).pattern(" S ").pattern("SCS").pattern("LLL").define('S', trimmedStick)
                .define('C', ItemTags.SOUL_FIRE_BASE_BLOCKS).define('L', ItemTags.LOGS).unlockedBy("has_trimmed_stick", has(trimmedStick))
                .save(wood, id("soul_campfire"));
        shaped(Items.LADDER, 3).pattern("S S").pattern("STS").pattern("S S").define('S', trimmedStick).define('T', twine)
                .unlockedBy("has_twine", has(twine)).save(wood, id("ladder"));
        shaped(Items.SCAFFOLDING, 6).pattern("BTB").pattern("B B").pattern("B B").define('B', Items.BAMBOO).define('T', twine)
                .unlockedBy("has_twine", has(twine)).save(wood, id("scaffolding"));
    }

    private static ShapedRecipeBuilder shaped(Item result, int count) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result, count);
    }

    private static ShapelessRecipeBuilder shapeless(Item result, int count) {
        return ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result, count);
    }
}
