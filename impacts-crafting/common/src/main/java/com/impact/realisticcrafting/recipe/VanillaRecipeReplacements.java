package com.impact.realisticcrafting.recipe;

import com.google.gson.JsonElement;
import com.impact.lib.ResourceIds;
import com.impact.lib.log.ImpactsLog;
import com.impact.lib.wood.WoodData;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.config.CraftingCommonConfig;
import com.impact.realisticcrafting.config.OverhaulCategory;
import com.impact.realisticcrafting.debug.CraftingLogCategories;
import com.impact.realisticcrafting.item.ToolKind;
import com.impact.realisticcrafting.item.ToolMaterial;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The vanilla recipes Crafting replaces, each with its catalog category. Datagen writes a switched-off copy of each
 * (for the recipe audit and data pack tools), and the recipe manager drops them as recipes load, so they're off even
 * where another pack (Forge's own copies of tool and fence recipes) would win over Crafting's file.
 */
public final class VanillaRecipeReplacements {
    private static final String MINECRAFT_NAMESPACE = "minecraft";
    private static final ImpactsLog LOG = ImpactsLog.of(CraftingMod.MOD_ID);
    private static final Map<ResourceLocation, OverhaulCategory> REPLACED = buildReplaced();

    private VanillaRecipeReplacements() {
    }

    /** Every replaced vanilla recipe ID, with the category whose switch turns the replacement on. */
    public static Map<ResourceLocation, OverhaulCategory> all() {
        return REPLACED;
    }

    /** Whether the vanilla recipe is replaced right now: its category is on and it isn't in {@code recipes.keepVanilla}. */
    public static boolean isReplaced(ResourceLocation recipeId) {
        OverhaulCategory category = REPLACED.get(recipeId);
        return category != null && category.isOn() && !CraftingCommonConfig.get().keptVanillaRecipes.get().contains(recipeId.toString());
    }

    /** The recipe files to load: the ones read, minus the vanilla recipes Crafting replaces right now. */
    public static Map<ResourceLocation, JsonElement> withoutReplaced(Map<ResourceLocation, JsonElement> recipeFiles) {
        Map<ResourceLocation, JsonElement> kept = new LinkedHashMap<>(recipeFiles);
        int before = kept.size();
        kept.keySet().removeIf(VanillaRecipeReplacements::isReplaced);
        LOG.debug(CraftingLogCategories.AUDIT, "vanilla recipes switched off", "count", before - kept.size(), "listed", REPLACED.size());
        return kept;
    }

    private static Map<ResourceLocation, OverhaulCategory> buildReplaced() {
        Map<ResourceLocation, OverhaulCategory> replaced = new LinkedHashMap<>();
        put(replaced, OverhaulCategory.WOOD, "crafting_table", "stick");
        WoodData.VANILLA_WOODS.forEach(wood -> put(replaced, OverhaulCategory.WOOD, wood.getPath() + "_planks"));
        for (ToolMaterial material : ToolMaterial.values()) {
            for (ToolKind kind : ToolKind.values()) {
                put(replaced, OverhaulCategory.COMBAT, kind.vanillaPath(material));
            }
        }
        return Collections.unmodifiableMap(replaced);
    }

    private static void put(Map<ResourceLocation, OverhaulCategory> replaced, OverhaulCategory category, String... recipeNames) {
        for (String recipeName : recipeNames) {
            replaced.put(ResourceIds.of(MINECRAFT_NAMESPACE, recipeName), category);
        }
    }
}
