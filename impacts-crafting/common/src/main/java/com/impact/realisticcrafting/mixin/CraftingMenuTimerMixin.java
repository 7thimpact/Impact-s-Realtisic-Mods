package com.impact.realisticcrafting.mixin;

import com.impact.realisticcrafting.timing.CraftTimer;
import com.impact.realisticcrafting.timing.CraftTimerHolder;
import com.impact.realisticcrafting.timing.CraftTimers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Gives vanilla's crafting menu (and the makeshift tables' menu) a craft timer, synced in two data slots. */
@Mixin(CraftingMenu.class)
public abstract class CraftingMenuTimerMixin extends RecipeBookMenu<CraftingContainer> implements CraftTimerHolder {
    private static final int RESULT_SLOT = 0;

    @Unique
    private final CraftTimer impactscrafting$craftTimer = new CraftTimer();

    protected CraftingMenuTimerMixin(MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Override
    public CraftTimer impactscrafting$craftTimer() {
        return impactscrafting$craftTimer;
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    private void impactscrafting$syncTheTimer(int containerId, Inventory playerInventory, ContainerLevelAccess access, CallbackInfo callback) {
        addDataSlot(impactscrafting$craftTimer.elapsedSlot);
        addDataSlot(impactscrafting$craftTimer.totalSlot);
    }

    // Shift-clicking repeats crafts in one loop; each one has to wait for its own timer.
    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void impactscrafting$waitForTheTimer(Player player, int slotIndex, CallbackInfoReturnable<ItemStack> callback) {
        if (slotIndex == RESULT_SLOT && CraftTimers.isOutputLocked(this)) {
            callback.setReturnValue(ItemStack.EMPTY);
        }
    }
}
