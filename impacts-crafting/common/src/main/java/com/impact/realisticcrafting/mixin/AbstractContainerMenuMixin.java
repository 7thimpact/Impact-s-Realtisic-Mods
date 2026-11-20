package com.impact.realisticcrafting.mixin;

import com.impact.realisticcrafting.timing.CraftTimers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Shift-clicking a crafting output queues repeated crafts; any other click in the menu cancels them. */
@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Inject(method = "doClick", at = @At("HEAD"))
    private void impactscrafting$queueOrCancelRepeats(int slotIndex, int button, ClickType clickType, Player player, CallbackInfo callback) {
        CraftTimers.onClicked((AbstractContainerMenu) (Object) this, slotIndex, clickType);
    }
}
