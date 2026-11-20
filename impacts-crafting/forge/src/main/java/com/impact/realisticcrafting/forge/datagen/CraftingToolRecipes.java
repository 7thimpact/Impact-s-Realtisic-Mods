package com.impact.realisticcrafting.forge.datagen;

import com.impact.lib.ResourceIds;
import com.impact.lib.forge.datagen.ToolCraftingRecipeBuilder;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.OverhaulCategory;
import com.impact.realisticcrafting.item.CraftingPart;
import com.impact.realisticcrafting.item.ToolKind;
import com.impact.realisticcrafting.item.ToolMaterial;
import com.impact.realisticcrafting.registry.CraftingItems;
import com.impact.realisticcrafting.registry.CraftingTags;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

/**
 * The wooden and stone tools: Trimmed Planks or Shaped Stone in the head pattern, the head on 2 Trimmed Sticks (1 for
 * a sword's blade) for a loose tool, and the loose tool hammered into the vanilla one. The shovel head is one part on
 * its own. The hoe head takes a trimming tool too, so its 2 parts side by side stay clear of the Wooden Panel.
 */
final class CraftingToolRecipes {
    private static final Ingredient HAMMERS = Ingredient.of(CraftingTags.HAMMERS);
    private static final Ingredient TRIMMING_TOOLS = Ingredient.of(CraftingTags.TRIMMING_TOOLS);

    private CraftingToolRecipes() {
    }

    static void build(Consumer<FinishedRecipe> output) {
        Consumer<FinishedRecipe> combatOutput = CraftingPartRecipes.overhaulRecipe(output, OverhaulCategory.COMBAT);
        for (ToolMaterial material : ToolMaterial.values()) {
            Item part = material == ToolMaterial.WOODEN ? CraftingItems.trimmedPlanks() : CraftingItems.part(CraftingPart.SHAPED_STONE);
            for (ToolKind kind : ToolKind.values()) {
                head(combatOutput, material, kind, part);
                looseTool(combatOutput, material, kind);
                Item vanillaTool = ResourceIds.item(ResourceIds.of("minecraft", kind.vanillaPath(material))).orElseThrow();
                HammeringRecipeBuilder.of(CraftingItems.looseTool(material, kind), HAMMERS, vanillaTool)
                        .save(combatOutput, id(kind.vanillaPath(material) + "_from_hammering"));
            }
        }
    }

    private static void head(Consumer<FinishedRecipe> output, ToolMaterial material, ToolKind kind, Item part) {
        Item head = CraftingItems.toolPart(material, kind);
        ResourceLocation recipeId = id(kind.partPath(material));
        if (kind == ToolKind.HOE) {
            ToolCraftingRecipeBuilder.of(head).pattern("PP").define('P', part).tool(TRIMMING_TOOLS).save(output, recipeId);
            return;
        }
        if (kind == ToolKind.SHOVEL) {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, head).requires(part).unlockedBy("has_part", has(part)).save(output, recipeId);
            return;
        }
        ShapedRecipeBuilder shaped = ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, head).define('P', part).unlockedBy("has_part", has(part));
        switch (kind) {
            case PICKAXE -> shaped.pattern("PPP");
            case AXE -> shaped.pattern("PP").pattern("P ");
            default -> shaped.pattern("P").pattern("P");
        }
        shaped.save(output, recipeId);
    }

    private static void looseTool(Consumer<FinishedRecipe> output, ToolMaterial material, ToolKind kind) {
        Item head = CraftingItems.toolPart(material, kind);
        Item stick = CraftingItems.part(CraftingPart.TRIMMED_STICK);
        ShapedRecipeBuilder shaped = ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, CraftingItems.looseTool(material, kind))
                .define('H', head)
                .define('S', stick)
                .pattern("H")
                .pattern("S")
                .unlockedBy("has_head", has(head));
        if (kind != ToolKind.SWORD) {
            shaped.pattern("S");
        }
        shaped.save(output, id(kind.loosePath(material)));
    }

    private static InventoryChangeTrigger.TriggerInstance has(ItemLike item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(item);
    }

    private static ResourceLocation id(String path) {
        return ResourceIds.of(CraftingMod.MOD_ID, path);
    }
}
