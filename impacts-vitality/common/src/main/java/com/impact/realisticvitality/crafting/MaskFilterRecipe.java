package com.impact.realisticvitality.crafting;

import com.impact.realisticvitality.air.MaskFilters;
import com.impact.realisticvitality.item.BreathingMaskItem;
import com.impact.realisticvitality.item.CharcoalFilterItem;
import com.impact.realisticvitality.registry.VitalityRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * {@code impactsvitality:mask_filter}: a Respirator or Rebreather and a Charcoal Filter in any crafting grid fit the
 * filter. The mask's old filter, if any, comes back in the new filter's slot.
 */
public final class MaskFilterRecipe extends CustomRecipe {
    public MaskFilterRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        return find(grid).isPresent();
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registryAccess) {
        return find(grid).map(parts -> {
            ItemStack fitted = parts.mask().copyWithCount(1);
            MaskFilters.setFilter(fitted, parts.filter().copyWithCount(1));
            return fitted;
        }).orElse(ItemStack.EMPTY);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer grid) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(grid.getContainerSize(), ItemStack.EMPTY);
        Optional<Parts> parts = find(grid);
        if (parts.isPresent()) {
            remaining.set(parts.get().filterSlot(), MaskFilters.filterIn(parts.get().mask()));
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return VitalityRecipeSerializers.maskFilter();
    }

    private static Optional<Parts> find(CraftingContainer grid) {
        ItemStack mask = ItemStack.EMPTY;
        ItemStack filter = ItemStack.EMPTY;
        int filterSlot = -1;
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            ItemStack stack = grid.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            boolean isFilterMask = stack.getItem() instanceof BreathingMaskItem maskItem && maskItem.mask.takesFilter;
            if (isFilterMask && mask.isEmpty()) {
                mask = stack;
            } else if (stack.getItem() instanceof CharcoalFilterItem && filter.isEmpty()) {
                filter = stack;
                filterSlot = slot;
            } else {
                return Optional.empty();
            }
        }
        return mask.isEmpty() || filter.isEmpty() ? Optional.empty() : Optional.of(new Parts(mask, filter, filterSlot));
    }

    private record Parts(ItemStack mask, ItemStack filter, int filterSlot) {
    }
}
