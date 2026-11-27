package com.impact.lib.mixin.client;

import com.impact.lib.hold.HoldMotionPoses;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * First person: during a hold-to-use action, or work a mod shows (a craft at a table), the working hand moves with the
 * motion (the hammer lifts back and comes down on each blow, the grinding stick circles), so hammering looks like
 * hammering and not like holding right-click. This runs with or without playerAnimator.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererHoldMotionMixin {
    @Unique
    private boolean impactslib$isHandMoved;

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void impactslib$moveWorkingHand(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swingProgress,
                                           ItemStack stack, float equipProgress, PoseStack poseStack, MultiBufferSource buffers, int light,
                                           CallbackInfo callback) {
        impactslib$isHandMoved = false;
        Optional<HoldMotionPoses.ActiveMotion> active = HoldMotionPoses.activeMotion(player, partialTick);
        if (active.isEmpty()) {
            return;
        }
        Optional<HoldMotionPoses.HandOffset> offset = HoldMotionPoses.handOffset(active.get().motion(), hand == InteractionHand.MAIN_HAND,
                active.get().time());
        if (offset.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(offset.get().right(), offset.get().up(), offset.get().forward());
        poseStack.mulPose(Axis.XP.rotationDegrees(offset.get().tiltDegrees()));
        impactslib$isHandMoved = true;
    }

    @Inject(method = "renderArmWithItem", at = @At("RETURN"))
    private void impactslib$restoreWorkingHand(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swingProgress,
                                              ItemStack stack, float equipProgress, PoseStack poseStack, MultiBufferSource buffers, int light,
                                              CallbackInfo callback) {
        if (impactslib$isHandMoved) {
            poseStack.popPose();
            impactslib$isHandMoved = false;
        }
    }
}
