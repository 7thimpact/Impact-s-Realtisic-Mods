package com.impact.realisticvitality.crafting;

import com.impact.lib.ResourceIds;
import com.impact.lib.crafting.ToolCrafting;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.registry.VitalityRecipeSerializers;
import com.impact.realisticvitality.temperature.ArmorLining;
import com.impact.realisticvitality.temperature.Linings;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * {@code impactsvitality:armor_lining} (only with Impact's Realistic Crafting, which gates it in its JSON): an armor
 * piece, one lining's materials (3 wool, 4 rabbit hide or 3 leather), Crafting's Twine and its Bone Needle in any
 * crafting grid sew the lining in. The armor keeps all its NBT. The needle stays in the grid and takes 1 damage through
 * Lib, as with all of Crafting's sewing. Crafting's items are matched by registry ID.
 */
public final class ArmorLiningRecipe extends CustomRecipe {
    private static final ResourceLocation TWINE = ResourceIds.of(VitalityMod.CRAFTING_MOD_ID, "twine");
    private static final ResourceLocation BONE_NEEDLE = ResourceIds.of(VitalityMod.CRAFTING_MOD_ID, "bone_needle");
    private static final int NEEDLE_DAMAGE = 1;
    // Armor, the smallest material count (3), Twine and the needle.
    private static final int FEWEST_ITEMS = 6;

    public ArmorLiningRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        return find(grid).isPresent();
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registryAccess) {
        return find(grid).map(parts -> parts.lining().sewnInto(parts.armor())).orElse(ItemStack.EMPTY);
    }

    // Called as the result is taken, so this is where the sewing is logged.
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer grid) {
        find(grid).ifPresent(parts -> ToolCrafting.craftingPlayer().ifPresent(player ->
                Linings.logSewn(player, parts.lining().sewnInto(parts.armor()), "bone_needle")));
        return ToolCrafting.damageTools(grid, stack -> isCraftingItem(stack, BONE_NEEDLE), NEEDLE_DAMAGE);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= FEWEST_ITEMS;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return VitalityRecipeSerializers.armorLining();
    }

    private static Optional<Parts> find(CraftingContainer grid) {
        PartsFound found = new PartsFound();
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            ItemStack stack = grid.getItem(slot);
            if (!stack.isEmpty() && !found.take(stack)) {
                return Optional.empty();
            }
        }
        return found.complete();
    }

    private static boolean isCraftingItem(ItemStack stack, ResourceLocation itemId) {
        return !stack.isEmpty() && ResourceIds.itemId(stack.getItem()).equals(itemId);
    }

    private record Parts(ItemStack armor, ArmorLining lining) {
    }

    /** What the grid holds so far: one armor piece, one Twine, one needle and one lining's materials, nothing else. */
    private static final class PartsFound {
        private ItemStack armor = ItemStack.EMPTY;
        private ArmorLining lining;
        private int materialCount;
        private int twineCount;
        private int needleCount;

        boolean take(ItemStack stack) {
            if (isCraftingItem(stack, TWINE)) {
                return ++twineCount == 1;
            }
            if (isCraftingItem(stack, BONE_NEEDLE)) {
                return ++needleCount == 1;
            }
            if (stack.getItem() instanceof ArmorItem) {
                boolean isFirstArmor = armor.isEmpty();
                armor = stack;
                return isFirstArmor;
            }
            return takeMaterial(stack);
        }

        private boolean takeMaterial(ItemStack stack) {
            Optional<ArmorLining> material = ArmorLining.ofMaterial(stack);
            if (material.isEmpty() || (lining != null && lining != material.get())) {
                return false;
            }
            lining = material.get();
            materialCount++;
            return true;
        }

        Optional<Parts> complete() {
            boolean isComplete = !armor.isEmpty() && twineCount == 1 && needleCount == 1 && lining != null && materialCount == lining.materialCount();
            return isComplete ? Optional.of(new Parts(armor, lining)) : Optional.empty();
        }
    }
}
