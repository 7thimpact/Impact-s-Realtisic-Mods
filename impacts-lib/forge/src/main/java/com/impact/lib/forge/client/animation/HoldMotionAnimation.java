package com.impact.lib.forge.client.animation;

import com.impact.lib.hold.HoldAction;
import com.impact.lib.hold.HoldMotion;
import com.impact.lib.hold.HoldToUse;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Arm motions for hold-to-use actions, worked out from time rather than from animation files: stirring circles the
 * main hand over the off hand, hammering raises and drops it, turning cranks both hands. Rotations are in radians.
 */
final class HoldMotionAnimation implements IAnimation {
    private static final String RIGHT_ARM = "rightArm";
    private static final String LEFT_ARM = "leftArm";
    private static final float STIR_SPEED = 0.6F;
    private static final float STIR_PITCH = -1.1F;
    private static final float STIR_SWAY = 0.2F;
    private static final float BOWL_PITCH = -0.9F;
    private static final float BOWL_YAW = 0.35F;
    private static final float HAMMER_SPEED = 0.35F;
    private static final float HAMMER_RAISED_PITCH = -2.4F;
    private static final float HAMMER_SWING = 1.4F;
    private static final float HOLD_PITCH = -0.8F;
    private static final float TURN_SPEED = 0.5F;
    private static final float TURN_PITCH = -1.3F;
    private static final float TURN_RADIUS = 0.3F;

    private final AbstractClientPlayer player;
    private float ageTicks;

    HoldMotionAnimation(AbstractClientPlayer player) {
        this.player = player;
    }

    @Override
    public boolean isActive() {
        return currentMotion().isPresent();
    }

    @Override
    public void tick() {
        ageTicks++;
    }

    @Override
    public void setupAnim(float tickDelta) {
    }

    @Override
    public @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0) {
        Optional<HoldMotion> motion = currentMotion();
        boolean isArmRotation = type == TransformType.ROTATION && (modelName.equals(RIGHT_ARM) || modelName.equals(LEFT_ARM));
        if (motion.isEmpty() || !isArmRotation) {
            return value0;
        }
        String mainArm = player.getMainArm() == HumanoidArm.RIGHT ? RIGHT_ARM : LEFT_ARM;
        float time = ageTicks + tickDelta;
        boolean isMainArm = modelName.equals(mainArm);
        return switch (motion.get()) {
            case STIR -> isMainArm ? stirringArm(time, value0) : bowlArm(modelName, value0);
            case HAMMER -> isMainArm ? hammeringArm(time, value0) : new Vec3f(HOLD_PITCH, value0.getY(), value0.getZ());
            case TURN -> turningArm(time + (isMainArm ? 0.0F : (float) Math.PI / TURN_SPEED), value0);
        };
    }

    private Optional<HoldMotion> currentMotion() {
        if (!player.isUsingItem()) {
            return Optional.empty();
        }
        return HoldToUse.actionFor(player.getUseItem()).map(HoldAction::motion);
    }

    private static Vec3f stirringArm(float time, Vec3f value0) {
        float angle = time * STIR_SPEED;
        return new Vec3f(STIR_PITCH + STIR_SWAY * (float) Math.cos(angle), STIR_SWAY * (float) Math.sin(angle), value0.getZ());
    }

    // The off arm holds the bowl in front, turned in toward the body's middle.
    private static Vec3f bowlArm(String modelName, Vec3f value0) {
        float inward = modelName.equals(LEFT_ARM) ? BOWL_YAW : -BOWL_YAW;
        return new Vec3f(BOWL_PITCH, inward, value0.getZ());
    }

    private static Vec3f hammeringArm(float time, Vec3f value0) {
        float stroke = Math.abs((float) Math.sin(time * HAMMER_SPEED));
        return new Vec3f(HAMMER_RAISED_PITCH + HAMMER_SWING * stroke, value0.getY(), value0.getZ());
    }

    private static Vec3f turningArm(float time, Vec3f value0) {
        float angle = time * TURN_SPEED;
        return new Vec3f(TURN_PITCH + TURN_RADIUS * (float) Math.sin(angle), TURN_RADIUS * (float) Math.cos(angle), value0.getZ());
    }
}
