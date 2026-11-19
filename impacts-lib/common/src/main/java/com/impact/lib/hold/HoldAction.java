package com.impact.lib.hold;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A hold-to-use action: the player holds right-click with the item, and each finished cycle of ticks does the action
 * once (grind one item, strike one hammer blow). Letting go or switching hands cancels the cycle in progress.
 */
public interface HoldAction {
    /** Names the action in logs and in the {@code impactslib:hold_action} trigger, for example {@code impactslib:grinding}. */
    ResourceLocation id();

    /** Ticks one cycle takes for this player and held item. */
    int cycleTicks(Player player, ItemStack held);

    /** Whether the action can start or go on. Checked on both sides when it starts and every tick. */
    boolean canContinue(Player player, ItemStack held);

    /** Server side: one cycle finished. Returns whether to keep going (false stops the hold). */
    boolean completeCycle(ServerPlayer player, ItemStack held);

    default HoldMotion motion() {
        return HoldMotion.STIR;
    }
}
