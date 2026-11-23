package com.impact.realisticinventory.pack;

import com.impact.realisticinventory.config.InventoryServerConfig;
import net.minecraft.util.StringRepresentable;

/**
 * The packs, from the first satchel to the endgame. Each holding pack is laid out in 3-wide rows like the pockets, and
 * each upgrade keeps the smaller pack's contents and dye. Past the Frame Pack every tier is a plain gain: more room, no
 * slowdown, and for the Netherite Pack no fire. The Ender Pack holds nothing itself: it opens its wearer's Ender Chest.
 */
public enum PackKind implements StringRepresentable {
    SATCHEL("satchel"),
    BACKPACK("backpack"),
    /** Slows walking while worn ({@code packs.framePackSlowness}). */
    FRAME_PACK("frame_pack"),
    /** The Frame Pack made light with phantom membranes: as roomy, without the slowdown. */
    EXPLORERS_PACK("explorers_pack"),
    EXPEDITION_PACK("expedition_pack"),
    /** Survives fire and lava, like netherite gear. */
    NETHERITE_PACK("netherite_pack"),
    ENDER_PACK("ender_pack");

    /** Vanilla's undyed leather color. */
    private static final int LEATHER_BROWN = 0xA06540;
    private final String itemPath;

    PackKind(String itemPath) {
        this.itemPath = itemPath;
    }

    public String itemPath() {
        return itemPath;
    }

    /** The name in a placed pack's block state. */
    @Override
    public String getSerializedName() {
        return itemPath;
    }

    public boolean slowsWalking() {
        return this == FRAME_PACK;
    }

    public boolean isFireproof() {
        return this == NETHERITE_PACK;
    }

    public boolean opensEnderChest() {
        return this == ENDER_PACK;
    }

    /**
     * The color of an undyed pack: vanilla's leather brown for the first three, then olive canvas, slate-blue canvas,
     * netherite gray and ender purple, so each tier reads apart at a glance. Dye replaces it, as on leather armor.
     */
    public int defaultColor() {
        return switch (this) {
            case SATCHEL, BACKPACK, FRAME_PACK -> LEATHER_BROWN;
            case EXPLORERS_PACK -> 0x8A8455;
            case EXPEDITION_PACK -> 0x4F6A8A;
            case NETHERITE_PACK -> 0x4D4246;
            case ENDER_PACK -> 0x3E2F5B;
        };
    }

    /** How many slots the pack has: 3, 9, 18, 18, 27 and 36 by default (server config), and none for the Ender Pack. */
    public int slotCount() {
        InventoryServerConfig.PackSettings settings = InventoryServerConfig.get().packs;
        return switch (this) {
            case SATCHEL -> settings.satchelSlots.get();
            case BACKPACK -> settings.backpackSlots.get();
            case FRAME_PACK -> settings.framePackSlots.get();
            case EXPLORERS_PACK -> settings.explorersPackSlots.get();
            case EXPEDITION_PACK -> settings.expeditionPackSlots.get();
            case NETHERITE_PACK -> settings.netheritePackSlots.get();
            case ENDER_PACK -> 0;
        };
    }
}
