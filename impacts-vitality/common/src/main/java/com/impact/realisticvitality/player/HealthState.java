package com.impact.realisticvitality.player;

/** Whether natural regeneration is stopped at the health cap right now. Not saved: it's worked out again each tick. */
public final class HealthState {
    private boolean isCapped;

    public boolean isCapped() {
        return isCapped;
    }

    public void setCapped(boolean capped) {
        isCapped = capped;
    }
}
