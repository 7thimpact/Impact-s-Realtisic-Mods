package com.impact.lib.forge.client.animation;

import com.impact.lib.hold.HoldMotion;
import com.impact.lib.hold.HoldMotionPoses;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * playerAnimator's layer for hold-to-use actions: the arm poses from {@link HoldMotionPoses}, the same ones Lib draws
 * itself when playerAnimator isn't installed (the off arm swings the hammer, the main arm stirs the bowl...).
 */
final class HoldMotionAnimation implements IAnimation {
    private static final String RIGHT_ARM = "rightArm";
    private static final String LEFT_ARM = "leftArm";

    private final AbstractClientPlayer player;

    HoldMotionAnimation(AbstractClientPlayer player) {
        this.player = player;
    }

    @Override
    public boolean isActive() {
        return HoldMotionPoses.currentMotion(player).isPresent();
    }

    @Override
    public void tick() {
    }

    @Override
    public void setupAnim(float tickDelta) {
    }

    @Override
    public @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0) {
        Optional<HoldMotion> motion = HoldMotionPoses.currentMotion(player);
        boolean isArmRotation = type == TransformType.ROTATION && (modelName.equals(RIGHT_ARM) || modelName.equals(LEFT_ARM));
        if (motion.isEmpty() || !isArmRotation) {
            return value0;
        }
        boolean isLeftArm = modelName.equals(LEFT_ARM);
        boolean isMainArm = isLeftArm == (player.getMainArm() == HumanoidArm.LEFT);
        HoldMotionPoses.ArmRotation rotation = HoldMotionPoses.armRotation(motion.get(), isMainArm, isLeftArm,
                HoldMotionPoses.holdTime(player, tickDelta), new HoldMotionPoses.ArmRotation(value0.getX(), value0.getY(), value0.getZ()));
        return new Vec3f(rotation.pitch(), rotation.yaw(), rotation.roll());
    }
}
