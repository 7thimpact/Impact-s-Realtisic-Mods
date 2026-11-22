package com.impact.realisticvitality.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** What a player's use click is aimed at, within vanilla's reach (how far vanilla's own items look). */
public final class LookTarget {
    private static final double SURVIVAL_REACH_BLOCKS = 4.5;
    private static final double CREATIVE_REACH_BLOCKS = 5.0;

    private LookTarget() {
    }

    /** Whether the click hits no block (and, with {@code fluids} other than NONE, no fluid either). */
    public static boolean isAimedAtNothing(Level level, Player player, ClipContext.Fluid fluids) {
        Vec3 eyes = player.getEyePosition();
        double reachBlocks = player.getAbilities().instabuild ? CREATIVE_REACH_BLOCKS : SURVIVAL_REACH_BLOCKS;
        Vec3 reach = eyes.add(player.getViewVector(1.0F).scale(reachBlocks));
        BlockHitResult target = level.clip(new ClipContext(eyes, reach, ClipContext.Block.OUTLINE, fluids, player));
        return target.getType() == HitResult.Type.MISS;
    }
}
