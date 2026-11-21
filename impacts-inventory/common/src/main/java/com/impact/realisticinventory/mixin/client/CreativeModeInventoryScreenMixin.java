package com.impact.realisticinventory.mixin.client;

import com.impact.realisticinventory.client.CreativeInventoryBackSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Gives the back slot its own place, with a slot frame, in creative mode's inventory tab. */
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
    private CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "selectTab", at = @At("TAIL"))
    private void impactsinventory$placeBackSlot(CreativeModeTab tab, CallbackInfo callback) {
        CreativeInventoryBackSlot.placeBackSlot(menu);
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void impactsinventory$drawBackSlotFrame(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo callback) {
        if (((CreativeModeInventoryScreen) (Object) this).isInventoryOpen()) {
            CreativeInventoryBackSlot.drawSlotFrame(graphics, leftPos, topPos);
        }
    }
}
