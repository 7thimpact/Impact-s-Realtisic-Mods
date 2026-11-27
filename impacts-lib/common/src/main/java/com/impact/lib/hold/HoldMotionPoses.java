package com.impact.lib.hold;

import com.impact.lib.config.LibClientConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;

/**
 * The poses of each hold motion and gesture, worked out from time: shared by playerAnimator's layer and Lib's own
 * animation (third-person arm rotations and first-person hand offsets), so both look alike. playerAnimator also bends
 * the elbows and leans the torso ({@link #elbowBend}, {@link #torsoLean}), which the vanilla model can't. Rotations are
 * in radians.
 */
public final class HoldMotionPoses {
    private static final float HAMMER_RAISED_PITCH = -2.5F;
    private static final float HAMMER_SWING = 1.6F;
    private static final float HAMMER_RAISE_SHARE = 0.7F;
    private static final float HAMMER_RAISED_ELBOW = 1.4F;
    private static final float HAMMER_BLOW_ELBOW = 0.15F;
    private static final float HAMMER_BLOW_LEAN = 0.25F;
    private static final float HOLD_PITCH = -0.8F;
    private static final float HOLD_INWARD_YAW = 0.3F;
    private static final float HOLD_ELBOW = 0.5F;
    private static final float STIR_PITCH = -1.1F;
    private static final float STIR_SWAY = 0.2F;
    private static final float STIR_ELBOW = 0.6F;
    private static final float STIR_ELBOW_SWAY = 0.15F;
    private static final float STIR_LEAN = 0.1F;
    private static final float BOWL_PITCH = -0.9F;
    private static final float BOWL_YAW = 0.35F;
    private static final float BOWL_ELBOW = 0.9F;
    private static final float TURN_PITCH = -1.3F;
    private static final float TURN_RADIUS = 0.3F;
    private static final float TURN_ELBOW = 0.5F;
    private static final float TURN_ELBOW_SWAY = 0.25F;
    private static final float TURN_LEAN = 0.08F;
    private static final float CRAFT_PITCH = -0.75F;
    private static final float CRAFT_INWARD_YAW = 0.25F;
    private static final float CRAFT_REACH = 0.18F;
    private static final float CRAFT_SPREAD = 0.12F;
    private static final float CRAFT_ELBOW = 0.7F;
    private static final float CRAFT_ELBOW_SWAY = 0.25F;
    private static final float CRAFT_LEAN = 0.15F;
    private static final float CRAFT_LEAN_SWAY = 0.03F;
    private static final float SIP_PITCH = -1.75F;
    private static final float SIP_INWARD_YAW = 0.55F;
    private static final float SIP_BOB = 0.06F;
    private static final float SIP_ELBOW = 1.4F;
    private static final float WIPE_PITCH = -2.3F;
    private static final float WIPE_YAW_START = -0.2F;
    private static final float WIPE_YAW_END = 0.9F;
    private static final float WIPE_ELBOW = 1.5F;
    // A wipe within each stroke, in ticks: the hand comes up, sweeps across the brow, comes down, and rests until the next.
    private static final float WIPE_RAISED_AT = 5.0F;
    private static final float WIPE_SWEPT_AT = 11.0F;
    private static final float WIPE_LOWERED_AT = 15.0F;
    private static final float SHIVER_PITCH = -0.75F;
    private static final float SHIVER_HUG_YAW = 0.75F;
    private static final float SHIVER_TREMBLE = 0.04F;
    private static final float SHIVER_ELBOW = 1.7F;
    private static final float SHIVER_HUNCH = 0.2F;
    private static final float SHIVER_HUNCH_TREMBLE = 0.015F;
    private static final float FIRST_PERSON_RAISE = 0.12F;
    private static final float FIRST_PERSON_PULL_BACK = 0.03F;
    private static final float FIRST_PERSON_TILT_DEGREES = 30.0F;
    private static final float FIRST_PERSON_CIRCLE = 0.06F;
    private static final float FIRST_PERSON_BOB = 0.04F;
    private static final float FIRST_PERSON_SIP_INWARD = 0.12F;
    private static final float FIRST_PERSON_SIP_RAISE = 0.12F;
    private static final float FIRST_PERSON_WIPE_RAISE = 0.35F;
    private static final float FIRST_PERSON_WIPE_TILT_DEGREES = -25.0F;
    private static final float FIRST_PERSON_TREMBLE = 0.012F;
    private static final float FIRST_PERSON_HUG_DROP = 0.05F;
    private static final float FULL_TURN = (float) (Math.PI * 2.0);
    private static final float HALF_TURN = FULL_TURN / 2.0F;

    private HoldMotionPoses() {
    }

    /** A motion playing on a player: which one, and how many ticks in (with the frame's fraction). */
    public record ActiveMotion(HoldMotion motion, float time) {
    }

    /** One arm's rotation in third person. */
    public record ArmRotation(float pitch, float yaw, float roll) {
    }

    /** How far to move one hand in first person, in the hand's own space, and how much to tilt it (degrees around X). */
    public record HandOffset(float right, float up, float forward, float tiltDegrees) {
    }

    /**
     * Client: the motion playing on this player, their hold-to-use action's or else what a mod shows on them through
     * {@link WorkMotions} (a craft at a table, a shiver), unless this client hides that motion
     * ({@code holdToUse.hiddenWorkMotions}). Works for other players too.
     */
    public static Optional<ActiveMotion> activeMotion(Player player, float partialTick) {
        Optional<HoldMotion> holdMotion = holdMotion(player);
        if (holdMotion.isPresent()) {
            return Optional.of(new ActiveMotion(holdMotion.get(), player.getTicksUsingItem() + partialTick));
        }
        List<String> hiddenMotions = LibClientConfig.get().holdToUse.hiddenWorkMotions.get();
        return WorkMotions.clientWork(player)
                .filter(work -> !hiddenMotions.contains(work.motion().serializedName()))
                .map(work -> new ActiveMotion(work.motion(), player.level().getGameTime() - work.startGameTime() + partialTick));
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
                float angle = armAngle(motion, isMainArm, time);
                yield new ArmRotation(TURN_PITCH + TURN_RADIUS * Mth.sin(angle), TURN_RADIUS * Mth.cos(angle), vanilla.roll());
            }
            case CRAFT -> {
                float angle = armAngle(motion, isMainArm, time);
                yield new ArmRotation(CRAFT_PITCH + CRAFT_REACH * Mth.sin(angle), inward * (CRAFT_INWARD_YAW + CRAFT_SPREAD * Mth.cos(angle)), vanilla.roll());
            }
            case SIP -> new ArmRotation(SIP_PITCH + SIP_BOB * Mth.sin(angle(motion, time)), inward * SIP_INWARD_YAW, vanilla.roll());
            case WIPE_BROW -> isWorking ? wipingArm(motion, inward, time, vanilla) : vanilla;
            case SHIVER -> new ArmRotation(SHIVER_PITCH + SHIVER_TREMBLE * Mth.sin(armAngle(motion, isMainArm, time)), inward * SHIVER_HUG_YAW,
                    vanilla.roll());
        };
    }

    /**
     * playerAnimator: how far the arm bends at the elbow. The hammer arm winds back with its forearm folded and
     * straightens on the blow; the holding arm and the working hands stay softly bent.
     */
    public static float elbowBend(HoldMotion motion, boolean isMainArm, float time) {
        boolean isWorking = motion.worksWith(isMainArm);
        return switch (motion) {
            case HAMMER -> isWorking ? Mth.lerp(hammerStroke(motion, time), HAMMER_RAISED_ELBOW, HAMMER_BLOW_ELBOW) : HOLD_ELBOW;
            case STIR -> isWorking ? STIR_ELBOW + STIR_ELBOW_SWAY * Mth.sin(angle(motion, time)) : BOWL_ELBOW;
            case TURN -> TURN_ELBOW + TURN_ELBOW_SWAY * Mth.sin(armAngle(motion, isMainArm, time));
            case CRAFT -> CRAFT_ELBOW + CRAFT_ELBOW_SWAY * Mth.sin(armAngle(motion, isMainArm, time));
            case SIP -> SIP_ELBOW;
            case WIPE_BROW -> isWorking ? WIPE_ELBOW * wipeRaise(motion, time) : 0.0F;
            case SHIVER -> SHIVER_ELBOW;
        };
    }

    /** playerAnimator: how far the upper body bends forward at the waist, into each hammer blow and over the work. */
    public static float torsoLean(HoldMotion motion, float time) {
        return switch (motion) {
            case HAMMER -> HAMMER_BLOW_LEAN * hammerStroke(motion, time);
            case STIR -> STIR_LEAN;
            case TURN -> TURN_LEAN;
            case CRAFT -> CRAFT_LEAN + CRAFT_LEAN_SWAY * Mth.sin(angle(motion, time));
            case SIP, WIPE_BROW -> 0.0F;
            case SHIVER -> SHIVER_HUNCH + SHIVER_HUNCH_TREMBLE * Mth.sin(angle(motion, time));
        };
    }

    /**
     * The first-person hand movement: the hammer lifts back and comes down, the stick circles, both hands crank or
     * work, cupped hands come up to the middle to drink, the wiping hand rises out of view, and cold hands tremble.
     */
    public static Optional<HandOffset> handOffset(HoldMotion motion, boolean isMainHand, boolean isRightSide, float time) {
        if (!motion.worksWith(isMainHand)) {
            return Optional.empty();
        }
        float towardMiddle = isRightSide ? -1.0F : 1.0F;
        return Optional.of(switch (motion) {
            case HAMMER -> {
                float raised = 1.0F - hammerStroke(motion, time);
                yield new HandOffset(0.0F, FIRST_PERSON_RAISE * raised, -FIRST_PERSON_PULL_BACK * raised, FIRST_PERSON_TILT_DEGREES * raised);
            }
            case STIR, TURN -> new HandOffset(FIRST_PERSON_CIRCLE * Mth.cos(angle(motion, time)), 0.0F,
                    FIRST_PERSON_CIRCLE * Mth.sin(angle(motion, time)), 0.0F);
            case CRAFT -> new HandOffset(0.0F, FIRST_PERSON_BOB * Mth.sin(armAngle(motion, isMainHand, time)), 0.0F, 0.0F);
            case SIP -> new HandOffset(towardMiddle * FIRST_PERSON_SIP_INWARD, FIRST_PERSON_SIP_RAISE + FIRST_PERSON_BOB * Mth.sin(angle(motion, time)),
                    0.0F, 0.0F);
            case WIPE_BROW -> {
                float raise = wipeRaise(motion, time);
                yield new HandOffset(0.0F, FIRST_PERSON_WIPE_RAISE * raise, 0.0F, FIRST_PERSON_WIPE_TILT_DEGREES * raise);
            }
            case SHIVER -> new HandOffset(FIRST_PERSON_TREMBLE * Mth.sin(armAngle(motion, isMainHand, time)), -FIRST_PERSON_HUG_DROP, 0.0F, 0.0F);
        });
    }

    // The wiping arm: up in front of the face, then across the brow from the outside in, then back down to rest.
    private static ArmRotation wipingArm(HoldMotion motion, float inward, float time, ArmRotation vanilla) {
        float raise = wipeRaise(motion, time);
        float strokeTime = time % motion.strokeTicks;
        float sweep = Mth.clamp((strokeTime - WIPE_RAISED_AT) / (WIPE_SWEPT_AT - WIPE_RAISED_AT), 0.0F, 1.0F);
        float wipingYaw = inward * Mth.lerp(sweep, WIPE_YAW_START, WIPE_YAW_END);
        return new ArmRotation(Mth.lerp(raise, vanilla.pitch(), WIPE_PITCH), Mth.lerp(raise, vanilla.yaw(), wipingYaw), vanilla.roll());
    }

    // 0 with the wiping arm at rest, 1 with it up at the brow.
    private static float wipeRaise(HoldMotion motion, float time) {
        float strokeTime = time % motion.strokeTicks;
        if (strokeTime < WIPE_RAISED_AT) {
            return strokeTime / WIPE_RAISED_AT;
        }
        if (strokeTime < WIPE_SWEPT_AT) {
            return 1.0F;
        }
        return Mth.clamp(1.0F - (strokeTime - WIPE_SWEPT_AT) / (WIPE_LOWERED_AT - WIPE_SWEPT_AT), 0.0F, 1.0F);
    }

    // The item-use hold's motion, when the player is holding a hold-to-use item's use.
    private static Optional<HoldMotion> holdMotion(Player player) {
        if (!player.isUsingItem()) {
            return Optional.empty();
        }
        return HoldToUse.actionFor(player.getUseItem()).map(HoldAction::motion);
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

    // Two-handed motions: the off arm runs half a turn behind the main arm, so the hands take turns.
    private static float armAngle(HoldMotion motion, boolean isMainArm, float time) {
        return angle(motion, time) + (isMainArm ? 0.0F : HALF_TURN);
    }
}
