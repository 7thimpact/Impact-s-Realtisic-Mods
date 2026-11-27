package com.impact.lib.forge.client.animation;

import com.impact.lib.hold.HoldMotion;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import net.minecraft.client.player.AbstractClientPlayer;

import java.util.Optional;

/** One player's hold-motion layer in playerAnimator: follows the motion playing on them, fading in, between and out. */
final class HoldMotionLayer extends ModifierLayer<HoldMotionAnimation> {
    private static final boolean FADES_FROM_REST = true;

    private final AbstractClientPlayer player;

    HoldMotionLayer(AbstractClientPlayer player) {
        this.player = player;
    }

    /** Client tick: starts, switches or stops the animation when the motion playing on the player changes. */
    void follow(Optional<HoldMotion> motion) {
        Optional<HoldMotion> playing = Optional.ofNullable(getAnimation()).map(HoldMotionAnimation::motion);
        if (playing.equals(motion)) {
            return;
        }
        HoldMotionAnimation next = motion.map(newMotion -> new HoldMotionAnimation(player, newMotion)).orElse(null);
        replaceAnimationWithFade(new HoldMotionFade(), next, FADES_FROM_REST);
    }
}
