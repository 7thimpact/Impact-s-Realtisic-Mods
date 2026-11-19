package com.impact.lib.item;

/**
 * An item whose vanilla durability comes from a config value, read each time it's needed (Food's knives, Crafting's
 * tools). Register it with any durability above 0, so vanilla treats it as damageable; Lib's mixin then answers
 * {@code ItemStack.getMaxDamage()} with this value.
 */
public interface ConfiguredDurabilityItem {
    int configuredMaxDamage();
}
