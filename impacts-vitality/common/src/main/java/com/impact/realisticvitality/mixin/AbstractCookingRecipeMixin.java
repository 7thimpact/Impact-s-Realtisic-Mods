package com.impact.realisticvitality.mixin;

import com.impact.realisticvitality.config.VitalityServerConfig;
import com.impact.realisticvitality.thirst.WaterContainers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A Wooden Cup purifies on a campfire in the time the server config sets ({@code containers.cupPurifyingTicks}). */
@Mixin(AbstractCookingRecipe.class)
abstract class AbstractCookingRecipeMixin {
    @Shadow
    @Final
    protected ResourceLocation id;

    @Inject(method = "getCookingTime", at = @At("HEAD"), cancellable = true)
    private void impactsvitality$cupPurifyingTime(CallbackInfoReturnable<Integer> callback) {
        if (WaterContainers.CUP_PURIFYING_RECIPES.contains(id)) {
            callback.setReturnValue(VitalityServerConfig.get().containers.cupPurifyingTicks.get());
        }
    }
}
