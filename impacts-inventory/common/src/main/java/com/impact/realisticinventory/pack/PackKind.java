package com.impact.realisticinventory.pack;

import com.impact.realisticinventory.config.InventoryServerConfig;

/** The three packs. Each is laid out in 3-wide rows like the pockets; bigger packs cost more to make. */
public enum PackKind {
    SATCHEL("satchel"),
    BACKPACK("backpack"),
    /** Slows walking while worn ({@code packs.framePackSlowness}). */
    FRAME_PACK("frame_pack");

    private final String itemPath;

    PackKind(String itemPath) {
        this.itemPath = itemPath;
    }

    public String itemPath() {
        return itemPath;
    }

    /** How many slots the pack has: 3, 9 and 18 by default (server config). */
    public int slotCount() {
        InventoryServerConfig.PackSettings settings = InventoryServerConfig.get().packs;
        return switch (this) {
            case SATCHEL -> settings.satchelSlots.get();
            case BACKPACK -> settings.backpackSlots.get();
            case FRAME_PACK -> settings.framePackSlots.get();
        };
    }
}
