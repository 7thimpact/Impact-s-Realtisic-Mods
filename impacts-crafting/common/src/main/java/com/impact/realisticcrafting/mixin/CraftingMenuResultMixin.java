package com.impact.realisticcrafting.mixin;

import com.impact.realisticcrafting.menu.TableTierCheck;
import com.impact.realisticcrafting.timing.CraftTimers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Where vanilla works out every crafting grid's result (tables, the 2×2 inventory grid and mods that reuse it): the
 * grid's craft timer starts over, a recipe above the station's tier is treated like a locked recipe (the result stays
 * empty), and an accepted recipe starts the timer.
 */
@Mixin(CraftingMenu.class)
public abstract class CraftingMenuResultMixin {
    @Inject(method = "slotChangedCraftingGrid", at = @At("HEAD"))
    private static void impactscrafting$resetTheCraft(AbstractContainerMenu menu, Level level, Player player, CraftingContainer grid,
                                                      ResultContainer result, CallbackInfo callback) {
        if (!level.isClientSide()) {
            CraftTimers.onGridChanged(menu);
        }
    }

    @Redirect(method = "slotChangedCraftingGrid", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/ResultContainer;setRecipeUsed(Lnet/minecraft/world/level/Level;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/crafting/Recipe;)Z"))
    private static boolean impactscrafting$checkTierAndStartTimer(ResultContainer result, Level level, ServerPlayer player, Recipe<?> recipe,
                                                                   AbstractContainerMenu menu, Level menuLevel, Player menuPlayer,
                                                                   CraftingContainer grid, ResultContainer menuResult) {
        boolean isAccepted = TableTierCheck.allows(menu, recipe, player) && result.setRecipeUsed(level, player, recipe);
        if (isAccepted) {
            CraftTimers.onRecipeAccepted(menu, recipe, player);
        }
        return isAccepted;
    }
}
