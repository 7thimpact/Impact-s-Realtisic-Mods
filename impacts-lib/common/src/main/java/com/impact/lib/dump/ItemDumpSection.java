package com.impact.lib.dump;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/** One mod's part of {@code /impacts dump held}: what that mod knows about the held item. Empty to leave the section out. */
@FunctionalInterface
public interface ItemDumpSection {
    Map<String, Object> describe(ItemStack stack, ServerPlayer player);
}
