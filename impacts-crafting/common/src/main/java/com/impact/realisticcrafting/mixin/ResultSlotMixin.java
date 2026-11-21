package com.impact.realisticcrafting.mixin;

import com.impact.realisticcrafting.advancement.CraftingAchievements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Every crafting result taken, in any grid, reports its recipe and count ("Master Craftsman" counts Crafting's own recipes). */
@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin {
    @Shadow
    @Final
    private Player player;
    @Shadow
    private int removeCount;

    // Runs before vanilla clears the recipe used and the count taken.
    @Inject(method = "checkTakeAchievements", at = @At("HEAD"))
    private void impactscrafting$reportCraftTaken(ItemStack taken, CallbackInfo callback) {
        if (!(((Slot) (Object) this).container instanceof RecipeHolder holder)) {
            return;
        }
        Recipe<?> recipe = holder.getRecipeUsed();
        if (recipe != null) {
            CraftingAchievements.onCraftTaken(player, recipe, taken, removeCount > 0 ? removeCount : taken.getCount());
        }
    }
}
