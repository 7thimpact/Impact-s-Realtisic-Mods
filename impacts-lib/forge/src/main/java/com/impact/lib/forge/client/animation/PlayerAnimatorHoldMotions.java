package com.impact.lib.forge.client.animation;

import com.impact.lib.ImpactsLib;
import com.impact.lib.ResourceIds;
import com.impact.lib.debug.LibLogCategories;
import com.impact.lib.hold.HoldMotionPoses;
import com.impact.lib.log.ImpactsLog;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

/**
 * Gives every player a playerAnimator layer for hold motions (hold-to-use actions, and the work mods show, like
 * crafting), and every client tick tells each player's layer which motion plays on them. Loaded only with playerAnimator.
 */
public final class PlayerAnimatorHoldMotions {
    static final ResourceLocation LAYER_ID = ResourceIds.of(ImpactsLib.MOD_ID, "hold_motion");
    // Above playerAnimator's default layers, below emote mods (which use 1000 and up).
    private static final int LAYER_PRIORITY = 500;
    private static final float TICK_START = 0.0F;
    private static final ImpactsLog LOG = ImpactsLog.of(ImpactsLib.MOD_ID);

    private PlayerAnimatorHoldMotions() {
    }

    public static void register() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(LAYER_ID, LAYER_PRIORITY, HoldMotionLayer::new);
        MinecraftForge.EVENT_BUS.addListener(PlayerAnimatorHoldMotions::onClientTick);
        LOG.debug(LibLogCategories.HOLD_TO_USE, "playerAnimator motions on", "layer", LAYER_ID, "fadeTicks", HoldMotionFade.FADE_TICKS);
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (event.phase != TickEvent.Phase.END || level == null) {
            return;
        }
        level.players().forEach(PlayerAnimatorHoldMotions::updateLayer);
    }

    private static void updateLayer(AbstractClientPlayer player) {
        if (PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER_ID) instanceof HoldMotionLayer layer) {
            layer.follow(HoldMotionPoses.activeMotion(player, TICK_START).map(HoldMotionPoses.ActiveMotion::motion));
        }
    }
}
