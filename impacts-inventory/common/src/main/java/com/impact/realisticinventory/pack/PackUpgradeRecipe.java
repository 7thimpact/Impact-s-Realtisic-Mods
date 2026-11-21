package com.impact.realisticinventory.pack;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

/**
 * A shaped recipe that turns a pack into a bigger one (satchel to backpack, backpack to frame pack) and keeps what the
 * old pack held: its contents, color and name all carry over, because the result starts from the old pack's NBT.
 */
public final class PackUpgradeRecipe extends ShapedRecipe {
    public PackUpgradeRecipe(ShapedRecipe shape) {
        super(shape.getId(), shape.getGroup(), shape.category(), shape.getWidth(), shape.getHeight(), shape.getIngredients(),
                shape.getResultItem(RegistryAccess.EMPTY), shape.showNotification());
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingGrid, RegistryAccess registryAccess) {
        ItemStack result = super.assemble(craftingGrid, registryAccess);
        for (int gridSlot = 0; gridSlot < craftingGrid.getContainerSize(); gridSlot++) {
            ItemStack ingredient = craftingGrid.getItem(gridSlot);
            if (PackRules.isPack(ingredient) && ingredient.hasTag()) {
                result.setTag(ingredient.getTag().copy());
                return result;
            }
        }
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PackRecipes.packUpgradeSerializer();
    }

    /** Reads and writes the recipe exactly like a shaped recipe; only the type differs. */
    public static final class Serializer implements RecipeSerializer<PackUpgradeRecipe> {
        @Override
        public PackUpgradeRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return new PackUpgradeRecipe(RecipeSerializer.SHAPED_RECIPE.fromJson(recipeId, json));
        }

        @Override
        public PackUpgradeRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new PackUpgradeRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(recipeId, buffer));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, PackUpgradeRecipe recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
        }
    }
}
