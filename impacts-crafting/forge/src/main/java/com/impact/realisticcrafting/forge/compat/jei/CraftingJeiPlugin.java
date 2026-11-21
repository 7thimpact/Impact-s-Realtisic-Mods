package com.impact.realisticcrafting.forge.compat.jei;

import com.impact.lib.ResourceIds;
import com.impact.lib.forge.compat.jei.LibJeiRecipeTypes;
import com.impact.lib.wood.WoodData;
import com.impact.realisticcrafting.CraftingMod;
import com.impact.realisticcrafting.item.CraftingTool;
import com.impact.realisticcrafting.item.WoodPart;
import com.impact.realisticcrafting.lang.CraftingTranslations;
import com.impact.realisticcrafting.recipe.CraftingRecipeTypes;
import com.impact.realisticcrafting.registry.CraftingItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Crafting's JEI plugin: the Hammering and Dye Vat categories, info pages on pebbles, beds, the vat and loose items,
 * each per-wood part shown once per wood, and every crafting recipe's table and time. JEI finds it on its own.
 */
@JeiPlugin
public final class CraftingJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID = ResourceIds.of(CraftingMod.MOD_ID, "jei");
    private static final String NO_WOOD = "";

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        ISubtypeInterpreter<ItemStack> byWood = new WoodSubtypes();
        perWoodItems().forEach(item -> registration.registerSubtypeInterpreter(item, byWood));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new HammeringCategory(guiHelper), new DyeVatCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipes = Minecraft.getInstance().level.getRecipeManager();
        registration.addRecipes(CraftingJeiRecipeTypes.HAMMERING, recipes.getAllRecipesFor(CraftingRecipeTypes.hammering()));
        registration.addRecipes(CraftingJeiRecipeTypes.DYE_VAT, recipes.getAllRecipesFor(CraftingRecipeTypes.vatDyeing()).stream()
                .flatMap(recipe -> VatDip.of(recipe).stream()).toList());
        addInfo(registration, CraftingItems.pebbles().stream().map(ItemStack::new).toList(), CraftingTranslations.JEI_PEBBLES_INFO);
        List<ItemStack> bedParts = new ArrayList<>(List.of(new ItemStack(CraftingItems.bedFrame()), new ItemStack(CraftingItems.comforter())));
        CraftingItems.sheets().forEach(sheet -> bedParts.add(new ItemStack(sheet)));
        addInfo(registration, bedParts, CraftingTranslations.JEI_BEDS_INFO);
        addInfo(registration, List.of(new ItemStack(CraftingItems.dyeVat())), CraftingTranslations.JEI_DYE_VAT_INFO);
        addInfo(registration, CraftingItems.looseItems().stream().map(ItemStack::new).toList(), CraftingTranslations.JEI_LOOSE_ITEMS_INFO);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        List<ItemLike> hammers = new ArrayList<>(CraftingItems.pebbles());
        hammers.add(CraftingItems.tool(CraftingTool.IRON_HAMMER));
        registration.addRecipeCatalysts(CraftingJeiRecipeTypes.HAMMERING, hammers.toArray(ItemLike[]::new));
        registration.addRecipeCatalysts(CraftingJeiRecipeTypes.DYE_VAT, CraftingItems.dyeVat());
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        registration.addRecipeCategoryDecorator(RecipeTypes.CRAFTING, new CraftInfoDecorator<>());
        registration.addRecipeCategoryDecorator(LibJeiRecipeTypes.TOOL_CRAFTING, new CraftInfoDecorator<>());
    }

    private static void addInfo(IRecipeRegistration registration, List<ItemStack> stacks, String translationKey) {
        registration.addIngredientInfo(stacks, VanillaTypes.ITEM_STACK, Component.translatable(translationKey));
    }

    private static List<Item> perWoodItems() {
        return Stream.of(WoodPart.values()).map(CraftingItems::woodPart).map(Item.class::cast).toList();
    }

    /** Tells per-wood parts apart by their {@code wood} NBT, and nothing else (uses or other mods' keys don't make a new entry). */
    private static final class WoodSubtypes implements ISubtypeInterpreter<ItemStack> {
        @Override
        public Object getSubtypeData(ItemStack stack, UidContext context) {
            return WoodData.get(stack).orElse(null);
        }

        @Override
        public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
            return WoodData.get(stack).map(ResourceLocation::toString).orElse(NO_WOOD);
        }
    }
}
