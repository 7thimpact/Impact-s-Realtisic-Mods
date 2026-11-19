package com.impact.realisticfood.mixin;

import com.impact.realisticfood.cooking.CookingTimes;
import com.impact.realisticfood.food.StyleLog;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

/**
 * Food's cooking recipes take their times from Food's server config (see {@link CookingTimes}), and every cook that
 * produces a base, styled, burnt or mushy food gets a {@code styles} log line.
 */
@Mixin(AbstractCookingRecipe.class)
public abstract class AbstractCookingRecipeMixin {
    @Shadow
    @Final
    protected ResourceLocation id;

    @Shadow
    @Final
    protected RecipeType<?> type;

    @Inject(method = "assemble", at = @At("RETURN"))
    private void impactsfood$logStyle(Container container, RegistryAccess registryAccess, CallbackInfoReturnable<ItemStack> callback) {
        StyleLog.onCooked(type.toString(), container.getItem(0), callback.getReturnValue());
    }

    @Inject(method = "getCookingTime", at = @At("HEAD"), cancellable = true)
    private void impactsfood$configuredCookingTime(CallbackInfoReturnable<Integer> callback) {
        OptionalInt configured = CookingTimes.configuredTicks(id);
        if (configured.isPresent()) {
            callback.setReturnValue(configured.getAsInt());
        }
    }
}
