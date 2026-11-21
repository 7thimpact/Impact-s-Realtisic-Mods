package com.impact.realisticcrafting.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.realisticcrafting.config.OverhaulCategory;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.recipe.VatDyeingRecipe;
import com.impact.realisticcrafting.registry.CraftingItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.has;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.id;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.overhaulRecipe;
import static com.impact.realisticcrafting.forge.datagen.CraftingRecipeOutputs.unusedWoodenBucket;

/**
 * The Dye Vat: its recipe (an unused Wooden Bucket lashed with 2 Twine), what dips in it (wool, carpet, terracotta,
 * glass, panes, concrete powder, candles and shulker boxes in every color; sugar cane soaks into Paper Pulp in plain
 * water), and paper from Paper Pulp. Every grid dyeing recipe is switched off. Leather-style items are dyed in code.
 */
final class CraftingDyeVatRecipes {
    private static final String VAT_FOLDER = "dye_vat/";
    private static final int PAPER_PULP_DIP_TICKS = 20;

    private CraftingDyeVatRecipes() {
    }

    static void build(Consumer<FinishedRecipe> output) {
        Item twine = CraftingItems.part(CraftingPart.TWINE);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, CraftingItems.dyeVat()).requires(unusedWoodenBucket()).requires(twine, 2)
                .unlockedBy("has_wooden_bucket", has(CraftingItems.woodenBucket())).save(overhaulRecipe(output, OverhaulCategory.DYES), id("dye_vat"));
        Consumer<FinishedRecipe> dyes = overhaulRecipe(output, OverhaulCategory.DYES);
        colored(dyes, "wool", Ingredient.of(ItemTags.WOOL), color -> vanilla(color + "_wool"));
        colored(dyes, "carpet", Ingredient.of(ItemTags.WOOL_CARPETS), color -> vanilla(color + "_carpet"));
        colored(dyes, "terracotta", Ingredient.of(ItemTags.TERRACOTTA), color -> vanilla(color + "_terracotta"));
        colored(dyes, "glass", withColors(Items.GLASS, color -> vanilla(color + "_stained_glass")), color -> vanilla(color + "_stained_glass"));
        colored(dyes, "glass_pane", withColors(Items.GLASS_PANE, color -> vanilla(color + "_stained_glass_pane")),
                color -> vanilla(color + "_stained_glass_pane"));
        colored(dyes, "concrete_powder", withColors(Items.WHITE_CONCRETE_POWDER, color -> vanilla(color + "_concrete_powder")),
                color -> vanilla(color + "_concrete_powder"));
        colored(dyes, "candle", Ingredient.of(ItemTags.CANDLES), color -> vanilla(color + "_candle"));
        colored(dyes, "shulker_box", withColors(Items.SHULKER_BOX, color -> vanilla(color + "_shulker_box")), color -> vanilla(color + "_shulker_box"));
        Consumer<FinishedRecipe> textiles = overhaulRecipe(output, OverhaulCategory.TEXTILES);
        Item paperPulp = CraftingItems.part(CraftingPart.PAPER_PULP);
        VatDyeingRecipeBuilder.of(Ingredient.of(Items.SUGAR_CANE)).result(VatDyeingRecipe.PLAIN_WATER_KEY, paperPulp).dipTicks(PAPER_PULP_DIP_TICKS)
                .save(textiles, id(VAT_FOLDER + "paper_pulp"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.PAPER, 3).requires(paperPulp, 3).unlockedBy("has_paper_pulp", has(paperPulp))
                .save(textiles, id("paper"));
    }

    private static void colored(Consumer<FinishedRecipe> dyes, String name, Ingredient input, Function<String, Item> byColor) {
        VatDyeingRecipeBuilder recipe = VatDyeingRecipeBuilder.of(input);
        Arrays.stream(DyeColor.values()).forEach(color -> recipe.result(color.getName(), byColor.apply(color.getName())));
        recipe.save(dyes, id(VAT_FOLDER + name));
    }

    // The plain item and every colored one, so any color can be dipped into another.
    private static Ingredient withColors(Item plain, Function<String, Item> byColor) {
        List<Item> items = new ArrayList<>();
        items.add(plain);
        Arrays.stream(DyeColor.values()).map(color -> byColor.apply(color.getName())).filter(item -> item != plain).forEach(items::add);
        return Ingredient.of(items.toArray(Item[]::new));
    }

    private static Item vanilla(String path) {
        return ResourceIds.item(ResourceIds.of("minecraft", path)).orElseThrow();
    }
}
