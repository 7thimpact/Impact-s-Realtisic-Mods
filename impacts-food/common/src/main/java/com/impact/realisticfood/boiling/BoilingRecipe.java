package com.impact.realisticfood.boiling;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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

/**
 * {@code impactsfood:boiling}: one item boiled in a pot's water turns into its result after {@code boil_ticks}.
 * {@code vessel} is {@code cauldron}, {@code bucket} or {@code any}.
 */
public final class BoilingRecipe implements Recipe<Container> {
    private static final int INPUT_SLOT = 0;

    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack result;
    private final int boilTicks;
    private final RecipeVessel vessel;

    public BoilingRecipe(ResourceLocation id, Ingredient input, ItemStack result, int boilTicks, RecipeVessel vessel) {
        this.id = id;
        this.input = input;
        this.result = result;
        this.boilTicks = boilTicks;
        this.vessel = vessel;
    }

    public Ingredient input() {
        return input;
    }

    public ItemStack result() {
        return result;
    }

    /** The boil time the recipe's JSON names; {@link BoilingRecipes#boilTicks} applies the config's overrides. */
    public int recipeBoilTicks() {
        return boilTicks;
    }

    public RecipeVessel vessel() {
        return vessel;
    }

    public boolean accepts(ItemStack stack, PotKind pot) {
        return vessel.accepts(pot) && input.test(stack);
    }

    @Override
    public boolean matches(Container container, Level level) {
        return input.test(container.getItem(INPUT_SLOT));
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
        return BoilingRecipes.serializer();
    }

    @Override
    public RecipeType<?> getType() {
        return BoilingRecipes.recipeType();
    }

    // Pots aren't in the recipe book; JEI shows them on Food's page.
    @Override
    public boolean isSpecial() {
        return true;
    }

    public static final class Serializer implements RecipeSerializer<BoilingRecipe> {
        private static final String INPUT_FIELD = "input";
        private static final String RESULT_FIELD = "result";
        private static final String BOIL_TICKS_FIELD = "boil_ticks";
        private static final String VESSEL_FIELD = "vessel";

        @Override
        public BoilingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            String vesselName = GsonHelper.getAsString(json, VESSEL_FIELD, RecipeVessel.ANY.jsonName);
            RecipeVessel vessel = RecipeVessel.byJsonName(vesselName)
                    .orElseThrow(() -> new JsonParseException("Unknown vessel '" + vesselName + "' in " + recipeId));
            return new BoilingRecipe(recipeId, Ingredient.fromJson(json.get(INPUT_FIELD)),
                    ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, RESULT_FIELD)),
                    GsonHelper.getAsInt(json, BOIL_TICKS_FIELD), vessel);
        }

        @Override
        public BoilingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient input = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            int boilTicks = buffer.readVarInt();
            RecipeVessel vessel = buffer.readEnum(RecipeVessel.class);
            return new BoilingRecipe(recipeId, input, result, boilTicks, vessel);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, BoilingRecipe recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeItem(recipe.result);
            buffer.writeVarInt(recipe.boilTicks);
            buffer.writeEnum(recipe.vessel);
        }
    }
}
