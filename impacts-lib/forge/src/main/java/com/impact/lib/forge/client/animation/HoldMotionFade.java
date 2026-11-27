package com.impact.lib.forge.client.animation;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.util.Ease;

/** playerAnimator's fade between hold motions, and from and back to rest: eased in and out over a few ticks. */
final class HoldMotionFade extends AbstractFadeModifier {
    static final int FADE_TICKS = 4;

    HoldMotionFade() {
        super(FADE_TICKS);
    }

    @Override
    protected float getAlpha(String modelName, TransformType type, float progress) {
        return Ease.INOUTSINE.invoke(progress);
    }
}
