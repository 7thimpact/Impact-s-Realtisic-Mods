package com.impact.realisticfood.soup;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.impact.realisticfood.boiling.RecipeVessel;
import com.impact.realisticfood.config.FoodServerConfig;
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

import java.util.OptionalInt;

/**
 * {@code impactsfood:soup}: one set of {@code ingredients} (in any order) in a boiling pot cooks into
 * {@code bowls_per_set} servings of {@code result} after {@code cook_ticks}. A cauldron takes up to three sets at once.
 * Leave out {@code cook_ticks} to use {@code soup.cookTicks} from the server config.
 */
public final class SoupRecipe implements Recipe<Container> {
    public static final int DEFAULT_BOWLS_PER_SET = 2;

    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;
    private final int bowlsPerSet;
    private final OptionalInt cookTicks;
    private final RecipeVessel vessel;

    public SoupRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, ItemStack result, int bowlsPerSet, OptionalInt cookTicks,
                      RecipeVessel vessel) {
        this.id = id;
        this.ingredients = ingredients;
        this.result = result;
        this.bowlsPerSet = bowlsPerSet;
        this.cookTicks = cookTicks;
        this.vessel = vessel;
    }

    public ItemStack result() {
        return result;
    }

    public int bowlsPerSet() {
        return bowlsPerSet;
    }

    public int cookTicks() {
        return cookTicks.orElse(FoodServerConfig.get().soup.cookTicks.get());
    }

    public RecipeVessel vessel() {
        return vessel;
    }

    /** Whether any of the recipe's ingredients takes the item. */
    public boolean uses(ItemStack stack) {
        return ingredients.stream().anyMatch(ingredient -> ingredient.test(stack));
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    // Pots, not containers, match soups (see SoupMatcher).
    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
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
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SoupRecipes.serializer();
    }

    @Override
    public RecipeType<?> getType() {
        return SoupRecipes.recipeType();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static final class Serializer implements RecipeSerializer<SoupRecipe> {
        private static final String INGREDIENTS_FIELD = "ingredients";
        private static final String RESULT_FIELD = "result";
        private static final String BOWLS_PER_SET_FIELD = "bowls_per_set";
        private static final String COOK_TICKS_FIELD = "cook_ticks";
        private static final String VESSEL_FIELD = "vessel";
        private static final int NO_COOK_TICKS = -1;

        @Override
        public SoupRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            NonNullList<Ingredient> ingredients = NonNullList.create();
            GsonHelper.getAsJsonArray(json, INGREDIENTS_FIELD).forEach(element -> ingredients.add(Ingredient.fromJson(element)));
            if (ingredients.isEmpty()) {
                throw new JsonParseException("A soup needs ingredients: " + recipeId);
            }
            String vesselName = GsonHelper.getAsString(json, VESSEL_FIELD, RecipeVessel.ANY.jsonName);
            RecipeVessel vessel = RecipeVessel.byJsonName(vesselName)
                    .orElseThrow(() -> new JsonParseException("Unknown vessel '" + vesselName + "' in " + recipeId));
            OptionalInt cookTicks = json.has(COOK_TICKS_FIELD) ? OptionalInt.of(GsonHelper.getAsInt(json, COOK_TICKS_FIELD)) : OptionalInt.empty();
            return new SoupRecipe(recipeId, ingredients, ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, RESULT_FIELD)),
                    GsonHelper.getAsInt(json, BOWLS_PER_SET_FIELD, DEFAULT_BOWLS_PER_SET), cookTicks, vessel);
        }

        @Override
        public SoupRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            NonNullList<Ingredient> ingredients = NonNullList.create();
            ingredients.addAll(buffer.readList(Ingredient::fromNetwork));
            ItemStack result = buffer.readItem();
            int bowlsPerSet = buffer.readVarInt();
            int cookTicks = buffer.readInt();
            RecipeVessel vessel = buffer.readEnum(RecipeVessel.class);
            return new SoupRecipe(recipeId, ingredients, result, bowlsPerSet, cookTicks == NO_COOK_TICKS ? OptionalInt.empty() : OptionalInt.of(cookTicks), vessel);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, SoupRecipe recipe) {
            buffer.writeCollection(recipe.ingredients, (ingredientBuffer, ingredient) -> ingredient.toNetwork(ingredientBuffer));
            buffer.writeItem(recipe.result);
            buffer.writeVarInt(recipe.bowlsPerSet);
            buffer.writeInt(recipe.cookTicks.orElse(NO_COOK_TICKS));
            buffer.writeEnum(recipe.vessel);
        }
    }
}
