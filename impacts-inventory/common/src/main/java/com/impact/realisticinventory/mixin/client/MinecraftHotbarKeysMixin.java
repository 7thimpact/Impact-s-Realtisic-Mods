package com.impact.realisticinventory.mixin.client;

import com.impact.realisticinventory.inventory.InventoryLocks;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** The hotbar number keys do nothing while the locks apply: the main hand is always the one slot, and the keys are free for other uses. */
@Mixin(Minecraft.class)
public abstract class MinecraftHotbarKeysMixin {
    @Redirect(method = "handleKeybinds", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
            target = "Lnet/minecraft/world/entity/player/Inventory;selected:I"))
    private void impactsinventory$ignoreHotbarKeys(Inventory inventory, int keyIndex) {
        if (!InventoryLocks.isActiveFor(inventory.player)) {
            inventory.selected = keyIndex;
        }
    }
}
