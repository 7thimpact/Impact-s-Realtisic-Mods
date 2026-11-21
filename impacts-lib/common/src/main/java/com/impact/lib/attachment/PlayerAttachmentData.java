package com.impact.lib.attachment;

import net.minecraft.nbt.CompoundTag;

/** Data a mod attaches to every player. Saved with the player as NBT. */
public interface PlayerAttachmentData {
    CompoundTag save();

    void load(CompoundTag tag);

    /** Copies another player's data into this one, for respawning. Defaults to a save and load round trip. */
    default void copyFrom(PlayerAttachmentData other) {
        load(other.save());
    }
}
