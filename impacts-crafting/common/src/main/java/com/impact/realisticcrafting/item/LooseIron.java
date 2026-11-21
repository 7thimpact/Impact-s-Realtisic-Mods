package com.impact.realisticcrafting.item;

/** The iron goods that come out of the grid loose and are hammered into the vanilla item. */
public enum LooseIron {
    BUCKET("loose_bucket", "bucket", "item/bucket"),
    CAULDRON("loose_cauldron", "cauldron", "item/cauldron"),
    IRON_DOOR("loose_iron_door", "iron_door", "item/iron_door"),
    MINECART("loose_minecart", "minecart", "item/minecart"),
    BLAST_FURNACE("loose_blast_furnace", "blast_furnace", "block/blast_furnace_front");

    public final String path;
    /** The vanilla item it's hammered into. */
    public final String vanillaPath;
    /** The vanilla sprite its model shows under the loose binding. */
    public final String vanillaSprite;

    LooseIron(String path, String vanillaPath, String vanillaSprite) {
        this.path = path;
        this.vanillaPath = vanillaPath;
        this.vanillaSprite = vanillaSprite;
    }
}
