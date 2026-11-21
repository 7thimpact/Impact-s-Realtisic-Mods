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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * {@code impactscrafting:vat_dyeing}: an item dipped in the Dye Vat. Fields: {@code input}, {@code results} keyed by dye
 * color (the key {@code none} for plain water), {@code charges} (default 1) and {@code dip_ticks} (default 10). The
 * result keeps the input's NBT (a shulker box keeps its contents). Leather-style items are dyed in code, not by recipes.
 */
public final class VatDyeingRecipe implements Recipe<Container> {
    public static final String PLAIN_WATER_KEY = "none";
    private static final int DEFAULT_CHARGES = 1;
    private static final int DEFAULT_DIP_TICKS = 10;

    private final ResourceLocation id;
    private final Ingredient input;
    private final Map<String, ItemStack> results;
    private final int charges;
    private final int dipTicks;

    public VatDyeingRecipe(ResourceLocation id, Ingredient input, Map<String, ItemStack> results, int charges, int dipTicks) {
        this.id = id;
        this.input = input;
        this.results = Map.copyOf(results);
        this.charges = charges;
        this.dipTicks = dipTicks;
    }

    public Ingredient input() {
        return input;
    }

    public Map<String, ItemStack> results() {
        return results;
    }

    public int charges() {
        return charges;
    }

    public int dipTicks() {
        return dipTicks;
    }

    /** What dipping the item in water of this color (a dye's name, or {@link #PLAIN_WATER_KEY}) gives, if anything. */
    public Optional<ItemStack> resultFor(ItemStack dipped, String colorKey) {
        ItemStack result = results.get(colorKey);
        if (result == null || !input.test(dipped) || ItemStack.isSameItemSameTags(result, dipped.copyWithCount(result.getCount()))) {
            return Optional.empty();
        }
        ItemStack dyed = result.copy();
        if (dipped.hasTag()) {
            dyed.setTag(dipped.getTag().copy());
        }
        return Optional.of(dyed);
    }

    @Override
    public boolean matches(Container container, Level level) {
        return input.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return results.values().stream().findFirst().orElse(ItemStack.EMPTY);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, input);
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
        return CraftingRecipeTypes.vatDyeingSerializer();
    }

    @Override
    public RecipeType<?> getType() {
        return CraftingRecipeTypes.vatDyeing();
    }

    public static final class Serializer implements RecipeSerializer<VatDyeingRecipe> {
        private static final String INPUT_FIELD = "input";
        private static final String RESULTS_FIELD = "results";
        private static final String CHARGES_FIELD = "charges";
        private static final String DIP_TICKS_FIELD = "dip_ticks";

        @Override
        public VatDyeingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Map<String, ItemStack> results = new LinkedHashMap<>();
            GsonHelper.getAsJsonObject(json, RESULTS_FIELD).entrySet()
                    .forEach(entry -> results.put(entry.getKey(), ShapedRecipe.itemStackFromJson(entry.getValue().getAsJsonObject())));
            return new VatDyeingRecipe(recipeId, Ingredient.fromJson(GsonHelper.getNonNull(json, INPUT_FIELD)), results,
                    GsonHelper.getAsInt(json, CHARGES_FIELD, DEFAULT_CHARGES), GsonHelper.getAsInt(json, DIP_TICKS_FIELD, DEFAULT_DIP_TICKS));
        }

        @Override
        public VatDyeingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient input = Ingredient.fromNetwork(buffer);
            Map<String, ItemStack> results = buffer.readMap(LinkedHashMap::new, FriendlyByteBuf::readUtf, FriendlyByteBuf::readItem);
            return new VatDyeingRecipe(recipeId, input, results, buffer.readVarInt(), buffer.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, VatDyeingRecipe recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeMap(recipe.results, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeItem);
            buffer.writeVarInt(recipe.charges);
            buffer.writeVarInt(recipe.dipTicks);
        }
    }
}
