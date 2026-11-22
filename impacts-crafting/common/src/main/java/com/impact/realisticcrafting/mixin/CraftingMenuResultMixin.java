package com.impact.realisticcrafting.mixin;

import com.impact.realisticcrafting.menu.TableTierCheck;
import com.impact.realisticcrafting.timing.CraftTimers;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Where vanilla works out every crafting grid's result (tables, the 2×2 inventory grid and mods that reuse it): the
 * grid's craft timer starts over, and once vanilla has set the result, a recipe above the station's tier is taken back
 * out (the result stays empty, as for a locked recipe) while an accepted one starts the timer. Plain injections at the
 * start and the end, so other mods' mixins on this method still apply.
 */
@Mixin(CraftingMenu.class)
public abstract class CraftingMenuResultMixin {
    private static final int RESULT_SLOT = 0;

    @Inject(method = "slotChangedCraftingGrid", at = @At("HEAD"))
    private static void impactscrafting$resetTheCraft(AbstractContainerMenu menu, Level level, Player player, CraftingContainer grid,
                                                      ResultContainer result, CallbackInfo callback) {
        if (!level.isClientSide()) {
            CraftTimers.onGridChanged(menu);
        }
    }

    @Inject(method = "slotChangedCraftingGrid", at = @At("TAIL"))
    private static void impactscrafting$checkTierAndStartTimer(AbstractContainerMenu menu, Level level, Player player, CraftingContainer grid,
                                                               ResultContainer result, CallbackInfo callback) {
        Recipe<?> recipe = result.getRecipeUsed();
        boolean hasResult = !result.getItem(RESULT_SLOT).isEmpty() && recipe != null;
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer) || !hasResult) {
            return;
        }
        if (TableTierCheck.allows(menu, recipe, serverPlayer)) {
            CraftTimers.onRecipeAccepted(menu, recipe, serverPlayer);
            return;
        }
        result.setRecipeUsed(null);
        result.setItem(RESULT_SLOT, ItemStack.EMPTY);
        menu.setRemoteSlot(RESULT_SLOT, ItemStack.EMPTY);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), RESULT_SLOT, ItemStack.EMPTY));
    }
}
