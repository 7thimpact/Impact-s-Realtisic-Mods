package com.impact.realisticinventory.inventory;

import com.impact.lib.attachment.PlayerAttachmentData;
import net.minecraft.nbt.CompoundTag;

/** Whether the player has had Inventory's first-join move, and whether the locks applied to them last tick. */
public final class MigrationState implements PlayerAttachmentData {
    private static final String FIRST_JOIN_DONE_KEY = "firstJoinDone";

    private boolean isFirstJoinDone;
    // Not saved: after a login or respawn the locks count as newly applied, so leftover items get moved out.
    private boolean wereLocksActiveLastTick;

    public boolean isFirstJoinDone() {
        return isFirstJoinDone;
    }

    public void markFirstJoinDone() {
        isFirstJoinDone = true;
    }

    /** Records whether the locks apply now, and returns whether they only started to (first tick, or creative ended). */
    public boolean recordLocksActive(boolean areLocksActive) {
        boolean haveJustStarted = areLocksActive && !wereLocksActiveLastTick;
        wereLocksActiveLastTick = areLocksActive;
        return haveJustStarted;
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(FIRST_JOIN_DONE_KEY, isFirstJoinDone);
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        isFirstJoinDone = tag.getBoolean(FIRST_JOIN_DONE_KEY);
    }
}
