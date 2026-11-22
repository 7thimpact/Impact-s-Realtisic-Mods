package com.impact.realisticinventory.mixin.client;

import com.impact.realisticinventory.inventory.InventoryLocks;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The hotbar number keys do nothing while the locks apply: the main hand is always the one slot. Their presses are
 * used up before vanilla reads them, which leaves other mods' mixins on vanilla's key handling free to apply, and keys
 * bound to other mods' key mappings unaffected.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftHotbarKeysMixin {
    @Shadow
    public LocalPlayer player;

    @Shadow
    @Final
    public Options options;

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void impactsinventory$ignoreHotbarKeys(CallbackInfo callback) {
        if (player == null || !InventoryLocks.isActiveFor(player)) {
            return;
        }
        for (KeyMapping hotbarKey : options.keyHotbarSlots) {
            while (hotbarKey.consumeClick()) {
                // Each press is dropped.
            }
        }
    }
}
