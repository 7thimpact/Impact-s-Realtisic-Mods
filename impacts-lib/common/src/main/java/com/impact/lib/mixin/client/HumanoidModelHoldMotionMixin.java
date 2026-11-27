package com.impact.lib.mixin.client;

import com.impact.lib.ImpactsLib;
import com.impact.lib.StartupSummary;
import com.impact.lib.hold.HoldMotion;
import com.impact.lib.hold.HoldMotionPoses;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Third person, without playerAnimator: during a hold-to-use action, or work a mod shows (a craft at a table), the arms
 * take the motion's pose (the off arm swings the hammer down, the main arm stirs the bowl...). With playerAnimator
 * installed, its layer does this instead, with elbow bends and fades.
 */
@Mixin(HumanoidModel.class)
public abstract class HumanoidModelHoldMotionMixin<T extends LivingEntity> {
    @Shadow
    @Final
    public ModelPart rightArm;
    @Shadow
    @Final
    public ModelPart leftArm;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void impactslib$poseArmsForHoldMotion(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                                                  float headPitch, CallbackInfo callback) {
        if (!(entity instanceof Player player) || ImpactsLib.isLoaded(StartupSummary.PLAYER_ANIMATOR_MOD_ID)) {
            return;
        }
        Optional<HoldMotionPoses.ActiveMotion> active = HoldMotionPoses.activeMotion(player, Minecraft.getInstance().getFrameTime());
        if (active.isEmpty()) {
            return;
        }
        boolean isRightArmMain = player.getMainArm() == HumanoidArm.RIGHT;
        pose(rightArm, active.get().motion(), isRightArmMain, false, active.get().time());
        pose(leftArm, active.get().motion(), !isRightArmMain, true, active.get().time());
    }

    private static void pose(ModelPart arm, HoldMotion motion, boolean isMainArm, boolean isLeftArm, float time) {
        HoldMotionPoses.ArmRotation rotation = HoldMotionPoses.armRotation(motion, isMainArm, isLeftArm, time,
                new HoldMotionPoses.ArmRotation(arm.xRot, arm.yRot, arm.zRot));
        arm.xRot = rotation.pitch();
        arm.yRot = rotation.yaw();
        arm.zRot = rotation.roll();
    }
}
