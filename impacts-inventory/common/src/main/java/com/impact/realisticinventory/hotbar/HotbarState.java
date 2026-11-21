package com.impact.realisticinventory.hotbar;

import com.impact.lib.attachment.PlayerAttachmentData;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayDeque;
import java.util.Deque;

/** When the player last swapped hands, and the swaps of the last few seconds ("Quick Draw" and "Juggler"). Nothing is saved. */
public final class HotbarState implements PlayerAttachmentData {
    private static final long NEVER_SWITCHED = Long.MIN_VALUE;

    private long lastSwitchGameTime = NEVER_SWITCHED;
    private final Deque<Long> recentSwitchGameTimes = new ArrayDeque<>();

    public boolean hasEverSwitched() {
        return lastSwitchGameTime != NEVER_SWITCHED;
    }

    public long lastSwitchGameTime() {
        return lastSwitchGameTime;
    }

    /** Records a swap and returns how many swaps happened within the window, this one included. */
    public int recordSwitch(long gameTime, long windowTicks) {
        lastSwitchGameTime = gameTime;
        recentSwitchGameTimes.addLast(gameTime);
        while (!recentSwitchGameTimes.isEmpty() && gameTime - recentSwitchGameTimes.peekFirst() > windowTicks) {
            recentSwitchGameTimes.removeFirst();
        }
        return recentSwitchGameTimes.size();
    }

    @Override
    public CompoundTag save() {
        return new CompoundTag();
    }

    @Override
    public void load(CompoundTag tag) {
        // Older saves kept an active hotbar slot here; there is no such thing any more.
    }
}
