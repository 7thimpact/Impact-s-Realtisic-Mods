package com.impact.realisticinventory.hotbar;

import com.impact.lib.attachment.PlayerAttachmentData;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Which hotbar slot is active (1 or 2) and when the player last switched. Slot 1 shows the main hand while it is
 * active, and the off hand while slot 2 is; that is why items look like they stay put when switching.
 */
public final class HotbarState implements PlayerAttachmentData {
    public static final int FIRST_SLOT = 1;
    public static final int SECOND_SLOT = 2;
    private static final String ACTIVE_SLOT_KEY = "activeSlot";
    private static final long NEVER_SWITCHED = Long.MIN_VALUE;

    private int activeSlot = FIRST_SLOT;
    private long lastSwitchGameTime = NEVER_SWITCHED;
    // Not saved: only the last few seconds matter (the "Juggler" achievement).
    private final Deque<Long> recentSwitchGameTimes = new ArrayDeque<>();

    public int activeSlot() {
        return activeSlot;
    }

    public void setActiveSlot(int slot) {
        activeSlot = slot == SECOND_SLOT ? SECOND_SLOT : FIRST_SLOT;
    }

    public static int otherSlot(int slot) {
        return slot == FIRST_SLOT ? SECOND_SLOT : FIRST_SLOT;
    }

    public boolean hasEverSwitched() {
        return lastSwitchGameTime != NEVER_SWITCHED;
    }

    public long lastSwitchGameTime() {
        return lastSwitchGameTime;
    }

    /** Records a switch and returns how many switches happened within the window, this one included. */
    public int recordSwitch(long gameTime, long windowTicks) {
        lastSwitchGameTime = gameTime;
        recentSwitchGameTimes.addLast(gameTime);
        while (!recentSwitchGameTimes.isEmpty() && gameTime - recentSwitchGameTimes.peekFirst() > windowTicks) {
            recentSwitchGameTimes.removeFirst();
        }
        return recentSwitchGameTimes.size();
    }

    public void reset() {
        activeSlot = FIRST_SLOT;
        lastSwitchGameTime = NEVER_SWITCHED;
        recentSwitchGameTimes.clear();
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(ACTIVE_SLOT_KEY, activeSlot);
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        setActiveSlot(tag.contains(ACTIVE_SLOT_KEY) ? tag.getInt(ACTIVE_SLOT_KEY) : FIRST_SLOT);
    }
}
