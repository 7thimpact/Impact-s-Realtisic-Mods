package com.impact.realisticinventory.mixin;

import com.impact.realisticinventory.pack.BackMenuSlot;
import com.impact.realisticinventory.pack.BackSlotMoves;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Appends the back slot to the player's inventory menu (index 46, after vanilla's slots) and handles shift-clicks into it. */
@Mixin(InventoryMenu.class)
public abstract class InventoryMenuBackSlotMixin extends RecipeBookMenu<CraftingContainer> {
    private InventoryMenuBackSlotMixin(MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void impactsinventory$appendBackSlot(Inventory playerInventory, boolean isActive, Player owner, CallbackInfo callback) {
        addSlot(new BackMenuSlot(owner));
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void impactsinventory$moveIntoBackSlot(Player player, int clickedSlotIndex, CallbackInfoReturnable<ItemStack> callback) {
        BackSlotMoves.quickMoveIntoBackSlot((InventoryMenu) (Object) this, player, clickedSlotIndex).ifPresent(callback::setReturnValue);
    }
}
