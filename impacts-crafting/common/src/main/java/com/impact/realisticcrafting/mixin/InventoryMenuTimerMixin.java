package com.impact.realisticcrafting.mixin;

import com.impact.realisticcrafting.timing.CraftTimer;
import com.impact.realisticcrafting.timing.CraftTimerHolder;
import com.impact.realisticcrafting.timing.CraftTimers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Gives the inventory's 2×2 grid a craft timer, synced in two data slots. Inventory's own screen keeps this menu. */
@Mixin(InventoryMenu.class)
public abstract class InventoryMenuTimerMixin extends RecipeBookMenu<CraftingContainer> implements CraftTimerHolder {
    private static final int RESULT_SLOT = 0;

    @Unique
    private final CraftTimer impactscrafting$craftTimer = new CraftTimer();

    protected InventoryMenuTimerMixin(MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Override
    public CraftTimer impactscrafting$craftTimer() {
        return impactscrafting$craftTimer;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void impactscrafting$syncTheTimer(Inventory playerInventory, boolean isServerSide, Player owner, CallbackInfo callback) {
        addDataSlot(impactscrafting$craftTimer.elapsedSlot);
        addDataSlot(impactscrafting$craftTimer.totalSlot);
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void impactscrafting$waitForTheTimer(Player player, int slotIndex, CallbackInfoReturnable<ItemStack> callback) {
        if (slotIndex == RESULT_SLOT && CraftTimers.isOutputLocked(this)) {
            callback.setReturnValue(ItemStack.EMPTY);
        }
    }
}
