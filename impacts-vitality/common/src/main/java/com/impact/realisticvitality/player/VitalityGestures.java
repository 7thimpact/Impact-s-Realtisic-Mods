package com.impact.realisticvitality.player;

import com.impact.lib.ResourceIds;
import com.impact.lib.hold.HoldMotion;
import com.impact.lib.hold.WorkMotions;
import com.impact.realisticvitality.VitalityMod;
import com.impact.realisticvitality.temperature.BodyState;
import com.impact.realisticvitality.temperature.TemperatureTicks;
import com.impact.realisticvitality.thirst.ThirstDrain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * The gestures Vitality shows on players through Lib's work motions, seen by everyone nearby: cupped hands raised to
 * the mouth for a moment after a sip by hand, shivering while Very Cold or Freezing, and wiping the brow every few
 * seconds while Very Hot or Overheating. Crafting's motion outranks them all (Lib shows the strongest).
 */
public final class VitalityGestures {
    public static final ResourceLocation SOURCE = ResourceIds.of(VitalityMod.MOD_ID, "gestures");
    /** How long the cupped hands stay up after a sip by hand. */
    public static final int SIP_GESTURE_TICKS = 20;

    private VitalityGestures() {
    }

    /** Server, every tick for players Vitality affects. */
    public static void tick(ServerPlayer player) {
        WorkMotions.show(player, SOURCE, gestureOf(player));
    }

    /** Server: no gesture, for players Vitality leaves alone (creative, spectators). */
    public static void stop(ServerPlayer player) {
        WorkMotions.show(player, SOURCE, Optional.empty());
    }

    /** The gesture this player shows now: a fresh sip first, then how the body feels. */
    public static Optional<HoldMotion> gestureOf(ServerPlayer player) {
        VitalityState state = VitalityState.of(player);
        long ticksSinceSip = player.level().getGameTime() - state.thirst.lastSipGameTime();
        if (ThirstDrain.isOn() && ticksSinceSip < SIP_GESTURE_TICKS) {
            return Optional.of(HoldMotion.SIP);
        }
        if (!TemperatureTicks.isOn()) {
            return Optional.empty();
        }
        BodyState body = BodyState.of(state.temperature.body());
        if (body.isVeryColdOrWorse()) {
            return Optional.of(HoldMotion.SHIVER);
        }
        return body.isVeryHotOrWorse() ? Optional.of(HoldMotion.WIPE_BROW) : Optional.empty();
    }
}
