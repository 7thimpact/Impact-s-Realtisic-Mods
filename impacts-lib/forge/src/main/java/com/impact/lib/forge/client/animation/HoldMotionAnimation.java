package com.impact.lib.forge.client.animation;

import com.impact.lib.hold.HoldMotion;
import com.impact.lib.hold.HoldMotionPoses;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.NotNull;

/**
 * One hold motion on one player, through playerAnimator: the arm poses from {@link HoldMotionPoses} (the same ones Lib
 * draws itself without playerAnimator), plus what only playerAnimator can do: bent elbows and a torso that leans into
 * the work. {@link HoldMotionLayer} swaps it in and out with a fade; once the motion has stopped it holds its last pose,
 * so the fade has something to fade from. First person stays vanilla's view (playerAnimator's first-person model puts
 * the real arms and held item right in front of the eyes, where a big item fills the screen), with Lib's own hand motion.
 */
final class HoldMotionAnimation implements IAnimation {
    private static final String RIGHT_ARM = "rightArm";
    private static final String LEFT_ARM = "leftArm";
    // playerAnimator bends the upper body at the waist with "body"; the arms and head follow it.
    private static final String UPPER_BODY = "body";
    // Bending around axis 0 folds a limb toward its front: a hanging forearm comes forward, the torso leans forward.
    private static final float FORWARD_BEND_AXIS = 0.0F;

    private final AbstractClientPlayer player;
    private final HoldMotion motion;
    private float lastTickTime;

    HoldMotionAnimation(AbstractClientPlayer player, HoldMotion motion) {
        this.player = player;
        this.motion = motion;
    }

    HoldMotion motion() {
        return motion;
    }

    // Always on: the layer decides when it plays, and a stopped motion still holds its pose while fading out.
    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void tick() {
        lastTickTime = time(0.0F);
    }

    @Override
    public void setupAnim(float tickDelta) {
    }

    @Override
    public @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0) {
        boolean isArm = modelName.equals(RIGHT_ARM) || modelName.equals(LEFT_ARM);
        if (isArm && type == TransformType.ROTATION) {
            return armRotation(modelName.equals(LEFT_ARM), time(tickDelta), value0);
        }
        if (isArm && type == TransformType.BEND) {
            return new Vec3f(FORWARD_BEND_AXIS, HoldMotionPoses.elbowBend(motion, isMainArm(modelName.equals(LEFT_ARM)), time(tickDelta)), 0.0F);
        }
        if (modelName.equals(UPPER_BODY) && type == TransformType.BEND) {
            return new Vec3f(FORWARD_BEND_AXIS, HoldMotionPoses.torsoLean(motion, time(tickDelta)), 0.0F);
        }
        return value0;
    }

    private Vec3f armRotation(boolean isLeftArm, float time, Vec3f value0) {
        HoldMotionPoses.ArmRotation vanilla = new HoldMotionPoses.ArmRotation(value0.getX(), value0.getY(), value0.getZ());
        HoldMotionPoses.ArmRotation rotation = HoldMotionPoses.armRotation(motion, isMainArm(isLeftArm), isLeftArm, time, vanilla);
        return new Vec3f(rotation.pitch(), rotation.yaw(), rotation.roll());
    }

    private boolean isMainArm(boolean isLeftArm) {
        return isLeftArm == (player.getMainArm() == HumanoidArm.LEFT);
    }

    // The motion's time while it still plays on the player; after it stops, the time it had on the last tick.
    private float time(float tickDelta) {
        return HoldMotionPoses.activeMotion(player, tickDelta)
                .filter(active -> active.motion() == motion)
                .map(HoldMotionPoses.ActiveMotion::time)
                .orElse(lastTickTime);
    }
}
