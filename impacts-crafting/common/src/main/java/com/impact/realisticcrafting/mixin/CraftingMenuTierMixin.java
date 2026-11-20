package com.impact.realisticcrafting.mixin;

import com.impact.realisticcrafting.menu.TableTierCheck;
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
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * The table tier check. Vanilla works out every crafting grid's result here (tables, the 2×2 inventory grid and
 * mods that reuse it); a recipe above the station's tier is treated like a locked recipe, so the result stays empty.
 */
@Mixin(CraftingMenu.class)
public abstract class CraftingMenuTierMixin {
    @Redirect(method = "slotChangedCraftingGrid", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/ResultContainer;setRecipeUsed(Lnet/minecraft/world/level/Level;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/crafting/Recipe;)Z"))
    private static boolean impactscrafting$refuseRecipesAboveTheStation(ResultContainer result, Level level, ServerPlayer player, Recipe<?> recipe,
                                                                         AbstractContainerMenu menu, Level menuLevel, Player menuPlayer,
                                                                         CraftingContainer grid, ResultContainer menuResult) {
        return TableTierCheck.allows(menu, recipe, player) && result.setRecipeUsed(level, player, recipe);
    }
}
