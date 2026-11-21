package com.impact.realisticcrafting.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.lib.forge.datagen.ToolCraftingRecipeBuilder;
import com.impact.realisticcrafting.config.OverhaulCategory;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.LooseIron;
import com.impact.realisticcrafting.item.WoodPart;
import com.impact.realisticcrafting.registry.CraftingItems;
import com.impact.realisticcrafting.registry.CraftingTags;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.boneNeedles;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.has;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.id;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.overhaulRecipe;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.partRecipe;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.trimmingTools;

/**
 * The combat, leather and textiles catalogs: the Bow Stave, bow, arrows, crossbow, shield (hammered), fishing rod and
 * lead; the Leather Panel and leather goods sewn with the Bone Needle (pieces told apart by how many panels and Twine
 * they take); carpet cut with shears, Banner Cloth and banners, candles, and wool from String Clumps.
 */
final class CraftingCombatRecipes {
    private static final int HELMET_PANELS = 3;
    private static final int CHESTPLATE_PANELS = 5;
    private static final int LEGGINGS_PANELS = 4;
    private static final int BOOTS_PANELS = 2;
    private static final int HORSE_ARMOR_PANELS = 4;
    private static final int CARPETS = 3;

    private CraftingCombatRecipes() {
    }

    static void build(Consumer<FinishedRecipe> output) {
        Consumer<FinishedRecipe> parts = partRecipe(output);
        Consumer<FinishedRecipe> combat = overhaulRecipe(output, OverhaulCategory.COMBAT);
        Consumer<FinishedRecipe> textiles = overhaulRecipe(output, OverhaulCategory.TEXTILES);
        Item stick = CraftingItems.part(CraftingPart.TRIMMED_STICK);
        Item twine = CraftingItems.part(CraftingPart.TWINE);
        Item stave = CraftingItems.part(CraftingPart.BOW_STAVE);
        Item leatherPanel = CraftingItems.part(CraftingPart.LEATHER_PANEL);

        ToolCraftingRecipeBuilder.of(stave, 1).requires(stick).requires(stick).requires(stick).tool(trimmingTools())
                .byproduct(CraftingItems.part(CraftingPart.WOOD_SCRAPS), 1, 1.0F).save(parts, id("bow_stave"));
        ToolCraftingRecipeBuilder.of(leatherPanel, 1).requires(Items.LEATHER).requires(Items.LEATHER).tool(boneNeedles()).save(parts, id("leather_panel"));

        shapeless(Items.BOW, 1).requires(stave).requires(twine, 2).unlockedBy("has_stave", has(stave)).save(combat, id("bow"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.ARROW, 2).pattern("A").pattern("S").pattern("F")
                .define('A', CraftingItems.part(CraftingPart.FLINT_ARROWHEAD)).define('S', stick).define('F', Items.FEATHER)
                .unlockedBy("has_arrowhead", has(CraftingItems.part(CraftingPart.FLINT_ARROWHEAD))).save(combat, id("arrow"));
        shapeless(Items.CROSSBOW, 1).requires(stave).requires(CraftingItems.woodPart(WoodPart.WOODEN_PANEL)).requires(CraftingTags.IRON_PLATES)
                .requires(Items.TRIPWIRE_HOOK).requires(twine).unlockedBy("has_stave", has(stave)).save(combat, id("crossbow"));
        shapeless(CraftingItems.looseIron(LooseIron.SHIELD), 1).requires(CraftingItems.woodPart(WoodPart.WOODEN_PANEL), 3)
                .requires(CraftingTags.IRON_PLATES).requires(CraftingItems.part(CraftingPart.IRON_RIVETS))
                .unlockedBy("has_iron_plate", has(CraftingTags.IRON_PLATES)).save(combat, id(LooseIron.SHIELD.path));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.FISHING_ROD).pattern("  S").pattern(" ST").pattern("S  ").define('S', stick)
                .define('T', twine).unlockedBy("has_twine", has(twine)).save(combat, id("fishing_rod"));
        shapeless(Items.LEAD, 2).requires(twine, 3).requires(Tags.Items.SLIMEBALLS).unlockedBy("has_twine", has(twine)).save(combat, id("lead"));
        sewn(combat, Items.LEATHER_HELMET, leatherPanel, HELMET_PANELS, 1);
        sewn(combat, Items.LEATHER_CHESTPLATE, leatherPanel, CHESTPLATE_PANELS, 1);
        sewn(combat, Items.LEATHER_LEGGINGS, leatherPanel, LEGGINGS_PANELS, 1);
        sewn(combat, Items.LEATHER_BOOTS, leatherPanel, BOOTS_PANELS, 1);
        sewn(combat, Items.LEATHER_HORSE_ARMOR, leatherPanel, HORSE_ARMOR_PANELS, 2);
        ToolCraftingRecipeBuilder.of(Items.BOOK, 1).requires(Items.PAPER).requires(Items.PAPER).requires(Items.PAPER)
                .requires(CraftingItems.part(CraftingPart.LEATHER_STRIP)).requires(twine).tool(boneNeedles()).save(combat, id("book"));

        for (DyeColor color : DyeColor.values()) {
            Item wool = vanilla(color.getName() + "_wool");
            ToolCraftingRecipeBuilder.of(vanilla(color.getName() + "_carpet"), CARPETS).pattern("WW").define('W', wool).tool(Ingredient.of(Items.SHEARS))
                    .save(textiles, id(color.getName() + "_carpet"));
            ToolCraftingRecipeBuilder.of(CraftingItems.bannerCloth(color), 1).pattern("WWW").pattern("WWW").pattern(" T ").define('W', wool).define('T', twine)
                    .tool(boneNeedles()).save(textiles, id(color.getName() + "_banner_cloth"));
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, vanilla(color.getName() + "_banner")).pattern("C").pattern("S")
                    .define('C', CraftingItems.bannerCloth(color)).define('S', stick).unlockedBy("has_banner_cloth", has(CraftingItems.bannerCloth(color)))
                    .save(textiles, id(color.getName() + "_banner"));
        }
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Items.CANDLE).pattern("T").pattern("H").define('T', twine).define('H', Items.HONEYCOMB)
                .unlockedBy("has_twine", has(twine)).save(textiles, id("candle"));
        shapeless(Items.WHITE_WOOL, 1).requires(CraftingItems.part(CraftingPart.STRING_CLUMP), 2)
                .unlockedBy("has_string_clump", has(CraftingItems.part(CraftingPart.STRING_CLUMP))).save(textiles, id("white_wool_from_string_clumps"));
    }

    private static void sewn(Consumer<FinishedRecipe> combat, Item result, Item panel, int panels, int twines) {
        ToolCraftingRecipeBuilder recipe = ToolCraftingRecipeBuilder.of(result, 1);
        for (int count = 0; count < panels; count++) {
            recipe.requires(panel);
        }
        for (int count = 0; count < twines; count++) {
            recipe.requires(CraftingItems.part(CraftingPart.TWINE));
        }
        recipe.tool(boneNeedles()).save(combat, id(ResourceIds.itemId(result).getPath()));
    }

    private static ShapelessRecipeBuilder shapeless(Item result, int count) {
        return ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, result, count);
    }

    private static Item vanilla(String path) {
        return ResourceIds.item(ResourceIds.of("minecraft", path)).orElseThrow();
    }
}
