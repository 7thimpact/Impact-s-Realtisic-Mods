package com.impact.lib.grinding;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.impact.lib.crafting.Byproduct;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code impactslib:grinding}: what a grinder bowl's contents turn into. Fields: {@code ingredients} (1, or 2 for mixing),
 * {@code result} (with its count), {@code min_tier} ({@code wood} or {@code stone}), optional {@code grind_ticks}
 * (otherwise the stick decides) and optional {@code byproducts} (Flour's wheat seeds). The container is the bowl's
 * contents, one stack per kind of item.
 */
public final class GrindingRecipe implements Recipe<Container> {
    public static final int STICK_DECIDES_TICKS = -1;
    private static final int MAXIMUM_INGREDIENTS = 2;

    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;
    private final GrinderTier minimumTier;
    private final int grindTicks;
    private final List<Byproduct> byproducts;

    public GrindingRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, ItemStack result, GrinderTier minimumTier, int grindTicks,
                          List<Byproduct> byproducts) {
        this.id = id;
        this.ingredients = ingredients;
        this.result = result;
        this.minimumTier = minimumTier;
        this.grindTicks = grindTicks;
        this.byproducts = List.copyOf(byproducts);
    }

    public GrinderTier minimumTier() {
        return minimumTier;
    }

    public int grindTicks() {
        return grindTicks;
    }

    public List<Byproduct> byproducts() {
        return byproducts;
    }

    /** The bowl must hold exactly the recipe's kinds of items, one kind per ingredient, in any order. */
    @Override
    public boolean matches(Container bowlContents, Level level) {
        List<ItemStack> kinds = new ArrayList<>();
        for (int slot = 0; slot < bowlContents.getContainerSize(); slot++) {
            if (!bowlContents.getItem(slot).isEmpty()) {
                kinds.add(bowlContents.getItem(slot));
            }
        }
        if (kinds.size() != ingredients.size()) {
            return false;
        }
        boolean isInOrder = ingredients.get(0).test(kinds.get(0)) && (kinds.size() == 1 || ingredients.get(1).test(kinds.get(1)));
        boolean isSwapped = kinds.size() == MAXIMUM_INGREDIENTS && ingredients.get(0).test(kinds.get(1)) && ingredients.get(1).test(kinds.get(0));
        return isInOrder || isSwapped;
    }

    /** Whether the item is one of this recipe's ingredients, so a bowl may take it. */
    public boolean uses(ItemStack stack) {
        return ingredients.stream().anyMatch(ingredient -> ingredient.test(stack));
    }

    @Override
    public ItemStack assemble(Container bowlContents, RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Grinding.serializer();
    }

    @Override
    public RecipeType<?> getType() {
        return Grinding.recipeType();
    }

    public static final class Serializer implements RecipeSerializer<GrindingRecipe> {
        private static final String INGREDIENTS_FIELD = "ingredients";
        private static final String RESULT_FIELD = "result";
        private static final String MIN_TIER_FIELD = "min_tier";
        private static final String GRIND_TICKS_FIELD = "grind_ticks";
        private static final String BYPRODUCTS_FIELD = "byproducts";

        @Override
        public GrindingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            NonNullList<Ingredient> ingredients = NonNullList.create();
            GsonHelper.getAsJsonArray(json, INGREDIENTS_FIELD).forEach(ingredient -> ingredients.add(Ingredient.fromJson(ingredient)));
            if (ingredients.isEmpty() || ingredients.size() > MAXIMUM_INGREDIENTS) {
                throw new JsonParseException("A grinding recipe takes 1 or 2 ingredients: " + recipeId);
            }
            String tierName = GsonHelper.getAsString(json, MIN_TIER_FIELD, GrinderTier.WOOD.getSerializedName());
            GrinderTier tier = GrinderTier.byName(tierName).orElseThrow(() -> new JsonParseException("Unknown grinder tier " + tierName + " in " + recipeId));
            List<Byproduct> byproducts = new ArrayList<>();
            GsonHelper.getAsJsonArray(json, BYPRODUCTS_FIELD, new JsonArray()).forEach(entry -> byproducts.add(Byproduct.fromJson(entry.getAsJsonObject())));
            return new GrindingRecipe(recipeId, ingredients, ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, RESULT_FIELD)), tier,
                    GsonHelper.getAsInt(json, GRIND_TICKS_FIELD, STICK_DECIDES_TICKS), byproducts);
        }

        @Override
        public GrindingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            NonNullList<Ingredient> ingredients = NonNullList.create();
            ingredients.addAll(buffer.readList(Ingredient::fromNetwork));
            ItemStack result = buffer.readItem();
            GrinderTier tier = buffer.readEnum(GrinderTier.class);
            int grindTicks = buffer.readInt();
            return new GrindingRecipe(recipeId, ingredients, result, tier, grindTicks, buffer.readList(Byproduct::fromNetwork));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, GrindingRecipe recipe) {
            buffer.writeCollection(recipe.ingredients, (ingredientBuffer, ingredient) -> ingredient.toNetwork(ingredientBuffer));
            buffer.writeItem(recipe.result);
            buffer.writeEnum(recipe.minimumTier);
            buffer.writeInt(recipe.grindTicks);
            buffer.writeCollection(recipe.byproducts, (byproductBuffer, byproduct) -> byproduct.toNetwork(byproductBuffer));
        }
    }
}
