package com.impact.lib.forge.client.animation;

import com.impact.lib.ImpactsLib;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.log.ImpactsLog;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;

/** Gives every player a playerAnimator layer that moves the arms during hold-to-use actions. Loaded only with playerAnimator. */
public final class PlayerAnimatorHoldMotions {
    // Above playerAnimator's default layers, below emote mods (which use 1000 and up).
    private static final int LAYER_PRIORITY = 500;
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);

    private PlayerAnimatorHoldMotions() {
    }

    public static void register() {
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((player, animationStack) ->
                animationStack.addAnimLayer(LAYER_PRIORITY, new HoldMotionAnimation(player)));
        LOG.debug(LibLogCategories.HOLD_TO_USE, "playerAnimator motions on");
    }
}
