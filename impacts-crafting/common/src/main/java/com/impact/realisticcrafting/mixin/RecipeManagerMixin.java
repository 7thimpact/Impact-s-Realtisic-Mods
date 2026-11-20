package com.impact.realisticcrafting.mixin;

import com.google.gson.JsonElement;
import com.impact.realisticcrafting.recipe.VanillaRecipeReplacements;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;

/** Drops the vanilla recipes Crafting replaces before the recipe manager reads them, whichever pack's copy won. */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @ModifyVariable(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"), argsOnly = true)
    private Map<ResourceLocation, JsonElement> impactscrafting$dropReplacedVanillaRecipes(Map<ResourceLocation, JsonElement> recipeFiles) {
        return VanillaRecipeReplacements.withoutReplaced(recipeFiles);
    }
}
