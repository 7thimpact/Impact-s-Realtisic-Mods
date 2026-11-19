package com.impact.realisticfood.mixin;

import com.impact.realisticfood.cooking.CookingTimes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

/** Food's cooking recipes take their times from Food's server config (see {@link CookingTimes}). */
@Mixin(AbstractCookingRecipe.class)
public abstract class AbstractCookingRecipeMixin {
    @Shadow
    @Final
    protected ResourceLocation id;

    @Inject(method = "getCookingTime", at = @At("HEAD"), cancellable = true)
    private void impactsfood$configuredCookingTime(CallbackInfoReturnable<Integer> callback) {
        OptionalInt configured = CookingTimes.configuredTicks(id);
        if (configured.isPresent()) {
            callback.setReturnValue(configured.getAsInt());
        }
    }
}
