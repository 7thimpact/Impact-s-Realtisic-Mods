package com.impact.realisticcrafting.item;

/** The five hammered tools. The sword's part is a blade, not a head. */
public enum ToolKind {
    PICKAXE("pickaxe", "head"),
    AXE("axe", "head"),
    SHOVEL("shovel", "head"),
    HOE("hoe", "head"),
    SWORD("sword", "blade");

    public final String path;
    public final String partSuffix;

    ToolKind(String path, String partSuffix) {
        this.path = path;
        this.partSuffix = partSuffix;
    }

    /** {@code wooden_pickaxe_head}, {@code stone_sword_blade}. */
    public String partPath(ToolMaterial material) {
        return material.path + "_" + path + "_" + partSuffix;
    }

    /** {@code loose_wooden_pickaxe}. */
    public String loosePath(ToolMaterial material) {
        return "loose_" + vanillaPath(material);
    }

    /** {@code wooden_pickaxe}: the vanilla tool the loose one is hammered into. */
    public String vanillaPath(ToolMaterial material) {
        return material.path + "_" + path;
    }
}
