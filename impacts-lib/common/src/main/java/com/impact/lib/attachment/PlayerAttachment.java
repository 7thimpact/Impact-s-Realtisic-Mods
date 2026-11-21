package com.impact.lib.attachment;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/** A registered kind of player data: its ID (also its NBT key), how to create it, and what survives death. */
public record PlayerAttachment<T extends PlayerAttachmentData>(ResourceLocation id, Supplier<T> factory, DeathCopyRule deathCopyRule) {
}
