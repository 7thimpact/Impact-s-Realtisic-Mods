package com.impact.realisticcrafting.recipe;

import com.google.gson.JsonObject;
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

/**
 * {@code impactscrafting:hammering}: an in-world recipe. Hold the loose {@code input} in the main hand and a
 * {@code hammer} in the off hand, hold right-click for {@code use_ticks} (default: the hammer's config time), and the
 * input becomes the {@code result}; the hammer takes {@code hammer_damage} (default 1). Other Impact mods may ship
 * these for their own loose items, gated by {@code forge:mod_loaded impactscrafting}.
 */
public final class HammeringRecipe implements Recipe<Container> {
    /** No {@code use_ticks}: the hammer's time from the config applies. */
    public static final int HAMMER_DECIDES_TICKS = -1;
    public static final int INPUT_SLOT = 0;
    public static final int HAMMER_SLOT = 1;
    private static final int DEFAULT_HAMMER_DAMAGE = 1;

    private final ResourceLocation id;
    private final Ingredient input;
    private final Ingredient hammer;
    private final ItemStack result;
    private final int useTicks;
    private final int hammerDamage;

    public HammeringRecipe(ResourceLocation id, Ingredient input, Ingredient hammer, ItemStack result, int useTicks, int hammerDamage) {
        this.id = id;
        this.input = input;
        this.hammer = hammer;
        this.result = result;
        this.useTicks = useTicks;
        this.hammerDamage = hammerDamage;
    }

    public Ingredient input() {
        return input;
    }

    public Ingredient hammer() {
        return hammer;
    }

    public ItemStack result() {
        return result;
    }

    public int useTicks() {
        return useTicks;
    }

    public int hammerDamage() {
        return hammerDamage;
    }

    public boolean matches(ItemStack loose, ItemStack held) {
        return input.test(loose) && hammer.test(held);
    }

    @Override
    public boolean matches(Container container, Level level) {
        return matches(container.getItem(INPUT_SLOT), container.getItem(HAMMER_SLOT));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, input, hammer);
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
        return CraftingRecipeTypes.hammeringSerializer();
    }

    @Override
    public RecipeType<?> getType() {
        return CraftingRecipeTypes.hammering();
    }

    public static final class Serializer implements RecipeSerializer<HammeringRecipe> {
        private static final String INPUT_FIELD = "input";
        private static final String HAMMER_FIELD = "hammer";
        private static final String RESULT_FIELD = "result";
        private static final String USE_TICKS_FIELD = "use_ticks";
        private static final String HAMMER_DAMAGE_FIELD = "hammer_damage";

        @Override
        public HammeringRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            return new HammeringRecipe(recipeId,
                    Ingredient.fromJson(GsonHelper.getNonNull(json, INPUT_FIELD)),
                    Ingredient.fromJson(GsonHelper.getNonNull(json, HAMMER_FIELD)),
                    ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, RESULT_FIELD)),
                    GsonHelper.getAsInt(json, USE_TICKS_FIELD, HAMMER_DECIDES_TICKS),
                    GsonHelper.getAsInt(json, HAMMER_DAMAGE_FIELD, DEFAULT_HAMMER_DAMAGE));
        }

        @Override
        public HammeringRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new HammeringRecipe(recipeId, Ingredient.fromNetwork(buffer), Ingredient.fromNetwork(buffer), buffer.readItem(),
                    buffer.readInt(), buffer.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, HammeringRecipe recipe) {
            recipe.input.toNetwork(buffer);
            recipe.hammer.toNetwork(buffer);
            buffer.writeItem(recipe.result);
            buffer.writeInt(recipe.useTicks);
            buffer.writeVarInt(recipe.hammerDamage);
        }
    }
}
