package com.impact.lib.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * An Impact mod just put water into a container item (Crafting's Wooden Bucket scoop, Food's stone-boiling pot giving
 * its bucket back). Listeners may add their own namespaced NBT to the stack (Vitality stamps purity); {@code boiled}
 * says the water was boiled.
 */
public record WaterFilled(Player player, ItemStack filledStack, Level level, BlockPos position, boolean boiled) {
}
