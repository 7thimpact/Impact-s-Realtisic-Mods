package com.impact.lib.grinding;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** One item ground, passed to {@link Grinding#onGround} listeners on the server. {@code result} is what the player got. */
public record CompletedGrind(ServerPlayer player, GrindingRecipe recipe, ItemStack result) {
}
