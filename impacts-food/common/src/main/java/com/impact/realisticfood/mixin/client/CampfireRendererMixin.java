package com.impact.realisticfood.mixin.client;

import com.impact.realisticfood.client.SpitRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Vanilla's campfire renderer also draws a mounted spit, so the campfire keeps its own renderer. */
@Mixin(CampfireRenderer.class)
public abstract class CampfireRendererMixin {
    @Shadow
    @Final
    private ItemRenderer itemRenderer;

    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/CampfireBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("TAIL"))
    private void impactsfood$drawSpit(CampfireBlockEntity campfire, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light,
                                      int overlay, CallbackInfo callback) {
        SpitRenderer.render(campfire, partialTick, poseStack, buffers, light, overlay, itemRenderer);
    }
}
