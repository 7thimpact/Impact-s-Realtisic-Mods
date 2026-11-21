package com.impact.lib.hold;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

/**
 * The arm poses of each hold motion, worked out from time: shared by playerAnimator's layer and Lib's own animation
 * (third-person arm rotations and first-person hand offsets), so both look alike. Rotations are in radians.
 */
public final class HoldMotionPoses {
    private static final float HAMMER_RAISED_PITCH = -2.5F;
    private static final float HAMMER_SWING = 1.6F;
    private static final float HAMMER_RAISE_SHARE = 0.7F;
    private static final float HOLD_PITCH = -0.8F;
    private static final float HOLD_INWARD_YAW = 0.3F;
    private static final float STIR_PITCH = -1.1F;
    private static final float STIR_SWAY = 0.2F;
    private static final float BOWL_PITCH = -0.9F;
    private static final float BOWL_YAW = 0.35F;
    private static final float TURN_PITCH = -1.3F;
    private static final float TURN_RADIUS = 0.3F;
    private static final float FIRST_PERSON_RAISE = 0.12F;
    private static final float FIRST_PERSON_PULL_BACK = 0.03F;
    private static final float FIRST_PERSON_TILT_DEGREES = 30.0F;
    private static final float FIRST_PERSON_CIRCLE = 0.06F;
    private static final float FULL_TURN = (float) (Math.PI * 2.0);

    private HoldMotionPoses() {
    }

    /** One arm's rotation in third person. */
    public record ArmRotation(float pitch, float yaw, float roll) {
    }

    /** How far to move one hand in first person, in the hand's own space, and how much to tilt it (degrees around X). */
    public record HandOffset(float right, float up, float forward, float tiltDegrees) {
    }

    /** The motion of the action the player is holding right now, if any. Works for other players too. */
    public static Optional<HoldMotion> currentMotion(Player player) {
        if (!player.isUsingItem()) {
            return Optional.empty();
        }
        return HoldToUse.actionFor(player.getUseItem()).map(HoldAction::motion);
    }

    /** Ticks into the hold, with the frame's fraction. */
    public static float holdTime(Player player, float partialTick) {
        return player.getTicksUsingItem() + partialTick;
    }

    /** The arm's rotation: the working arm moves, the other holds its item out in front, turned in toward the body. */
    public static ArmRotation armRotation(HoldMotion motion, boolean isMainArm, boolean isLeftArm, float time, ArmRotation vanilla) {
        boolean isWorking = motion.worksWith(isMainArm);
        float inward = isLeftArm ? 1.0F : -1.0F;
        return switch (motion) {
            case HAMMER -> isWorking
                    ? new ArmRotation(HAMMER_RAISED_PITCH + HAMMER_SWING * hammerStroke(motion, time), inward * HOLD_INWARD_YAW * 0.5F, vanilla.roll())
                    : new ArmRotation(HOLD_PITCH, inward * HOLD_INWARD_YAW, vanilla.roll());
            case STIR -> isWorking
                    ? new ArmRotation(STIR_PITCH + STIR_SWAY * Mth.cos(angle(motion, time)), STIR_SWAY * Mth.sin(angle(motion, time)), vanilla.roll())
                    : new ArmRotation(BOWL_PITCH, inward * BOWL_YAW, vanilla.roll());
            case TURN -> {
                float angle = angle(motion, time) + (isMainArm ? 0.0F : FULL_TURN / 2.0F);
                yield new ArmRotation(TURN_PITCH + TURN_RADIUS * Mth.sin(angle), TURN_RADIUS * Mth.cos(angle), vanilla.roll());
            }
        };
    }

    /** The first-person hand movement: the hammer lifts back and comes down, the stick circles, both hands crank. */
    public static Optional<HandOffset> handOffset(HoldMotion motion, boolean isMainHand, float time) {
        if (!motion.worksWith(isMainHand)) {
            return Optional.empty();
        }
        return Optional.of(switch (motion) {
            case HAMMER -> {
                float raised = 1.0F - hammerStroke(motion, time);
                yield new HandOffset(0.0F, FIRST_PERSON_RAISE * raised, -FIRST_PERSON_PULL_BACK * raised, FIRST_PERSON_TILT_DEGREES * raised);
            }
            case STIR, TURN -> new HandOffset(FIRST_PERSON_CIRCLE * Mth.cos(angle(motion, time)), 0.0F,
                    FIRST_PERSON_CIRCLE * Mth.sin(angle(motion, time)), 0.0F);
        });
    }

    // 0 with the hammer raised, 1 at the blow: a slow lift for most of the stroke, then a quick drop that lands as it ends.
    private static float hammerStroke(HoldMotion motion, float time) {
        float phase = (time % motion.strokeTicks) / motion.strokeTicks;
        if (phase < HAMMER_RAISE_SHARE) {
            return 1.0F - phase / HAMMER_RAISE_SHARE;
        }
        return (phase - HAMMER_RAISE_SHARE) / (1.0F - HAMMER_RAISE_SHARE);
    }

    private static float angle(HoldMotion motion, float time) {
        return time / motion.strokeTicks * FULL_TURN;
    }
}
