package com.impact.realisticcrafting.item;

/** The materials of Crafting's hammered tools: wooden ones from Trimmed Planks, stone ones from Shaped Stone. */
public enum ToolMaterial {
    WOODEN("wooden", LooseItem.HammerKnock.WOOD),
    STONE("stone", LooseItem.HammerKnock.STONE);

    public final String path;
    public final LooseItem.HammerKnock knock;

    ToolMaterial(String path, LooseItem.HammerKnock knock) {
        this.path = path;
        this.knock = knock;
    }
}
