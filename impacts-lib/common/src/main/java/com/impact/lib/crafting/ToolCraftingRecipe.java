package com.impact.lib.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.impact.lib.platform.LoaderPlatform;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * {@code impactslib:tool_crafting}: a crafting-grid recipe, shaped or shapeless, whose tools stay in the grid and take
 * damage. Tools match in any slot; the rest of the grid must match the shape exactly, so a grid holding a tool the
 * recipe doesn't list never matches. Optional fields: {@code byproducts} (given through Give or drop),
 * {@code remainders} (what an ingredient leaves behind), {@code craft_ticks} (Crafting's timer) and
 * {@code copy_nbt_from} (an ingredient whose NBT the result takes).
 */
public final class ToolCraftingRecipe implements CraftingRecipe {
    public static final int NO_CRAFT_TICKS = -1;

    private final CraftingRecipe shape;
    private final List<Ingredient> tools;
    private final int toolDamage;
    private final List<Byproduct> byproducts;
    private final List<CraftRemainder> remainders;
    private final int craftTicks;
    private final Optional<Ingredient> nbtSource;

    public ToolCraftingRecipe(CraftingRecipe shape, List<Ingredient> tools, int toolDamage, List<Byproduct> byproducts,
                              List<CraftRemainder> remainders, int craftTicks, Optional<Ingredient> nbtSource) {
        this.shape = shape;
        this.tools = List.copyOf(tools);
        this.toolDamage = toolDamage;
        this.byproducts = List.copyOf(byproducts);
        this.remainders = List.copyOf(remainders);
        this.craftTicks = craftTicks;
        this.nbtSource = nbtSource;
    }

    /** The vanilla shaped or shapeless recipe the non-tool items must match. */
    public CraftingRecipe shape() {
        return shape;
    }

    public List<Ingredient> tools() {
        return tools;
    }

    public int toolDamage() {
        return toolDamage;
    }

    public List<Byproduct> byproducts() {
        return byproducts;
    }

    /** Crafting's timer reads this; {@link #NO_CRAFT_TICKS} means "use the computed time". */
    public int craftTicks() {
        return craftTicks;
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        Optional<Set<Integer>> toolSlots = ToolCrafting.findToolSlots(grid, tools);
        return toolSlots.isPresent() && shape.matches(gridWithout(grid, toolSlots.get()), level);
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registryAccess) {
        Set<Integer> toolSlots = ToolCrafting.findToolSlots(grid, tools).orElse(Set.of());
        ItemStack result = shape.assemble(gridWithout(grid, toolSlots), registryAccess);
        nbtSource.flatMap(source -> firstMatching(grid, toolSlots, source))
                .filter(ItemStack::hasTag)
                .ifPresent(source -> result.setTag(source.getTag().copy()));
        return result;
    }

    /** Tools come back damaged (or break), ingredients leave their remainders, and the crafting player gets the byproducts. */
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer grid) {
        Set<Integer> toolSlots = ToolCrafting.findToolSlots(grid, tools).orElse(Set.of());
        Optional<Player> player = ToolCrafting.craftingPlayer();
        NonNullList<ItemStack> remaining = NonNullList.withSize(grid.getContainerSize(), ItemStack.EMPTY);
        List<ItemStack> usedTools = new ArrayList<>();
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            ItemStack stack = grid.getItem(slot);
            if (toolSlots.contains(slot)) {
                usedTools.add(stack.copy());
                remaining.set(slot, ToolCrafting.wearTool(stack, toolDamage, player.orElse(null)));
            } else {
                remaining.set(slot, remainderFor(stack));
            }
        }
        player.ifPresent(crafter -> ToolCrafting.finishCraft(crafter, this, shape.getResultItem(crafter.level().registryAccess()), usedTools));
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return shape.canCraftInDimensions(width, height);
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return shape.getResultItem(registryAccess);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return shape.getIngredients();
    }

    @Override
    public ResourceLocation getId() {
        return shape.getId();
    }

    @Override
    public String getGroup() {
        return shape.getGroup();
    }

    @Override
    public CraftingBookCategory category() {
        return shape.category();
    }

    // The recipe book can't place tools, so these stay out of it; JEI shows them in Lib's own category.
    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ToolCrafting.serializer();
    }

    private ItemStack remainderFor(ItemStack stack) {
        Optional<CraftRemainder> remainder = remainders.stream().filter(candidate -> candidate.ingredient().test(stack)).findFirst();
        if (remainder.isPresent()) {
            return remainder.get().result().copy();
        }
        return LoaderPlatform.current().craftingRemainder(stack);
    }

    private static Optional<ItemStack> firstMatching(CraftingContainer grid, Set<Integer> toolSlots, Ingredient source) {
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            if (!toolSlots.contains(slot) && source.test(grid.getItem(slot))) {
                return Optional.of(grid.getItem(slot));
            }
        }
        return Optional.empty();
    }

    // The same grid with the tools taken out, for the vanilla shape to match against.
    private static CraftingContainer gridWithout(CraftingContainer grid, Set<Integer> toolSlots) {
        NonNullList<ItemStack> items = NonNullList.withSize(grid.getContainerSize(), ItemStack.EMPTY);
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            items.set(slot, toolSlots.contains(slot) ? ItemStack.EMPTY : grid.getItem(slot));
        }
        return new TransientCraftingContainer(UnshownMenu.INSTANCE, grid.getWidth(), grid.getHeight(), items);
    }

    /** A menu nobody sees, which vanilla's crafting container needs to exist. */
    private static final class UnshownMenu extends AbstractContainerMenu {
        static final UnshownMenu INSTANCE = new UnshownMenu();

        private UnshownMenu() {
            super(null, -1);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slotIndex) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return false;
        }
    }

    /** Reads the vanilla shape (by its own fields) plus the tool-crafting fields. */
    public static final class Serializer implements RecipeSerializer<ToolCraftingRecipe> {
        private static final String PATTERN_FIELD = "pattern";
        private static final String TOOLS_FIELD = "tools";
        private static final String TOOL_DAMAGE_FIELD = "tool_damage";
        private static final String BYPRODUCTS_FIELD = "byproducts";
        private static final String REMAINDERS_FIELD = "remainders";
        private static final String CRAFT_TICKS_FIELD = "craft_ticks";
        private static final String COPY_NBT_FROM_FIELD = "copy_nbt_from";
        private static final int DEFAULT_TOOL_DAMAGE = 1;

        @Override
        public ToolCraftingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            CraftingRecipe shape = json.has(PATTERN_FIELD)
                    ? RecipeSerializer.SHAPED_RECIPE.fromJson(recipeId, json)
                    : RecipeSerializer.SHAPELESS_RECIPE.fromJson(recipeId, json);
            List<Ingredient> tools = new ArrayList<>();
            GsonHelper.getAsJsonArray(json, TOOLS_FIELD, new JsonArray()).forEach(tool -> tools.add(Ingredient.fromJson(tool)));
            List<Byproduct> byproducts = new ArrayList<>();
            GsonHelper.getAsJsonArray(json, BYPRODUCTS_FIELD, new JsonArray()).forEach(entry -> byproducts.add(Byproduct.fromJson(entry.getAsJsonObject())));
            List<CraftRemainder> remainders = new ArrayList<>();
            GsonHelper.getAsJsonArray(json, REMAINDERS_FIELD, new JsonArray()).forEach(entry -> remainders.add(CraftRemainder.fromJson(entry.getAsJsonObject())));
            Optional<Ingredient> nbtSource = json.has(COPY_NBT_FROM_FIELD) ? Optional.of(Ingredient.fromJson(json.get(COPY_NBT_FROM_FIELD))) : Optional.empty();
            return new ToolCraftingRecipe(shape, tools, GsonHelper.getAsInt(json, TOOL_DAMAGE_FIELD, DEFAULT_TOOL_DAMAGE), byproducts, remainders,
                    GsonHelper.getAsInt(json, CRAFT_TICKS_FIELD, NO_CRAFT_TICKS), nbtSource);
        }

        @Override
        public ToolCraftingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            boolean isShaped = buffer.readBoolean();
            CraftingRecipe shape = isShaped
                    ? RecipeSerializer.SHAPED_RECIPE.fromNetwork(recipeId, buffer)
                    : RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(recipeId, buffer);
            List<Ingredient> tools = buffer.readList(Ingredient::fromNetwork);
            int toolDamage = buffer.readVarInt();
            List<Byproduct> byproducts = buffer.readList(Byproduct::fromNetwork);
            List<CraftRemainder> remainders = buffer.readList(CraftRemainder::fromNetwork);
            int craftTicks = buffer.readInt();
            Optional<Ingredient> nbtSource = buffer.readOptional(Ingredient::fromNetwork);
            return new ToolCraftingRecipe(shape, tools, toolDamage, byproducts, remainders, craftTicks, nbtSource);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ToolCraftingRecipe recipe) {
            boolean isShaped = recipe.shape instanceof ShapedRecipe;
            buffer.writeBoolean(isShaped);
            if (isShaped) {
                RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, (ShapedRecipe) recipe.shape);
            } else {
                RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buffer, (ShapelessRecipe) recipe.shape);
            }
            buffer.writeCollection(recipe.tools, (toolBuffer, tool) -> tool.toNetwork(toolBuffer));
            buffer.writeVarInt(recipe.toolDamage);
            buffer.writeCollection(recipe.byproducts, (byproductBuffer, byproduct) -> byproduct.toNetwork(byproductBuffer));
            buffer.writeCollection(recipe.remainders, (remainderBuffer, remainder) -> remainder.toNetwork(remainderBuffer));
            buffer.writeInt(recipe.craftTicks);
            buffer.writeOptional(recipe.nbtSource, (sourceBuffer, source) -> source.toNetwork(sourceBuffer));
        }
    }
}
