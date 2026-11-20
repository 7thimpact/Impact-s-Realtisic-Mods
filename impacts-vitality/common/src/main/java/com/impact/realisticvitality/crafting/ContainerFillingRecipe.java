package com.impact.realisticvitality.crafting;

import com.impact.lib.ResourceIds;
import com.impact.realisticvitality.registry.VitalityItems;
import com.impact.realisticvitality.registry.VitalityRecipeSerializers;
import com.impact.realisticvitality.registry.VitalityTags;
import com.impact.realisticvitality.thirst.BottledWater;
import com.impact.realisticvitality.thirst.ContainerContents;
import com.impact.realisticvitality.thirst.DrinkValues;
import com.impact.realisticvitality.thirst.WaterContainer;
import com.impact.realisticvitality.thirst.WaterContainers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * {@code impactsvitality:container_filling}: a drink container and water bottles in any crafting grid pour the bottles
 * into the container (one drink each), leaving glass bottles. A container that keeps temperature also takes Cold Water
 * (staying cold) or bowls of soup (leaving the bowls). It never matches past the container's capacity.
 */
public final class ContainerFillingRecipe extends CustomRecipe {
    public ContainerFillingRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        return fill(grid).isPresent();
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registryAccess) {
        return fill(grid).orElse(ItemStack.EMPTY);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer grid) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(grid.getContainerSize(), ItemStack.EMPTY);
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            remaining.set(slot, emptiedVessel(grid.getItem(slot)));
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return VitalityRecipeSerializers.containerFilling();
    }

    private static Optional<ItemStack> fill(CraftingContainer grid) {
        Optional<ItemStack> containerStack = onlyContainer(grid);
        if (containerStack.isEmpty()) {
            return Optional.empty();
        }
        WaterContainer container = WaterContainers.containerOf(containerStack.get()).orElseThrow();
        Optional<ContainerContents> contents = Optional.of(ContainerContents.read(containerStack.get()));
        int pouredCount = 0;
        for (ItemStack stack : grid.getItems()) {
            if (stack.isEmpty() || stack == containerStack.get()) {
                continue;
            }
            contents = contents.flatMap(current -> pour(current, stack, container));
            pouredCount++;
        }
        boolean fits = contents.filter(filled -> filled.drinks() <= WaterContainers.capacity(container)).isPresent();
        if (pouredCount == 0 || !fits) {
            return Optional.empty();
        }
        ItemStack result = containerStack.get().copyWithCount(1);
        contents.get().writeTo(result);
        return Optional.of(result);
    }

    private static Optional<ItemStack> onlyContainer(CraftingContainer grid) {
        Optional<ItemStack> found = Optional.empty();
        for (ItemStack stack : grid.getItems()) {
            if (WaterContainers.containerOf(stack).isEmpty()) {
                continue;
            }
            if (found.isPresent()) {
                return Optional.empty();
            }
            found = Optional.of(stack);
        }
        return found;
    }

    private static Optional<ContainerContents> pour(ContainerContents contents, ItemStack poured, WaterContainer container) {
        Optional<BottledWater> bottledWater = VitalityItems.bottledWater(poured);
        if (bottledWater.isPresent()) {
            BottledWater water = bottledWater.get();
            return contents.withWater(1, water.purity, water.isCold, container.keepsTemperature);
        }
        if (DrinkValues.isVanillaWaterBottle(poured)) {
            return contents.withWater(1, DrinkValues.purityOf(poured).orElseThrow(), false, container.keepsTemperature);
        }
        if (container.keepsTemperature && poured.is(VitalityTags.SOUPS)) {
            return contents.withSoup(ResourceIds.itemId(poured.getItem()));
        }
        return Optional.empty();
    }

    private static ItemStack emptiedVessel(ItemStack poured) {
        if (VitalityItems.bottledWater(poured).isPresent() || DrinkValues.isVanillaWaterBottle(poured)) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }
        if (poured.is(VitalityTags.SOUPS)) {
            return new ItemStack(Items.BOWL);
        }
        return ItemStack.EMPTY;
    }
}
