package com.impact.lib.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Lets GameTests end a mock player's spawn protection, which only counts down while the player ticks. */
@Mixin(ServerPlayer.class)
public interface ServerPlayerSpawnProtectionAccessor {
    @Accessor("spawnInvulnerableTime")
    void impactslib$setSpawnInvulnerableTicks(int ticks);
}
