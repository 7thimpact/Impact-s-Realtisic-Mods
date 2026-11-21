package com.impact.realisticcrafting.item;

import com.impact.realisticcrafting.CraftingMod;

/**
 * The iron goods that come out of the grid unfinished, each with a reason: their fasteners sit in place but aren't
 * driven home (rivets, a piston's push rod), or the joints still need sealing (the blast furnace's mortar). The
 * finishing step happens in hand, with a hammer (or Mortar) in the off hand.
 */
public enum LooseIron {
    CAULDRON("loose_cauldron", "cauldron", "item/cauldron", "Unriveted Cauldron", Finish.RIVETS),
    IRON_DOOR("loose_iron_door", "iron_door", "item/iron_door", "Unriveted Iron Door", Finish.RIVETS),
    MINECART("loose_minecart", "minecart", "item/minecart", "Unriveted Minecart", Finish.RIVETS),
    BLAST_FURNACE("loose_blast_furnace", "blast_furnace", "block/blast_furnace_front", "Unsealed Blast Furnace", Finish.MORTAR),
    PISTON("loose_piston", "piston", "block/piston_top", "Unfitted Piston", Finish.PUSH_ROD),
    SHIELD("loose_shield", "shield", "impactscrafting:item/loose_shield", "Unriveted Shield", Finish.RIVETS);

    public final String path;
    /** The vanilla item it's finished into. */
    public final String vanillaPath;
    /** The sprite its model shows under the loose binding: vanilla's (no namespace) or Crafting's own. */
    public final String vanillaSprite;
    public final String englishName;
    public final Finish finish;

    LooseIron(String path, String vanillaPath, String vanillaSprite, String englishName, Finish finish) {
        this.path = path;
        this.vanillaPath = vanillaPath;
        this.vanillaSprite = vanillaSprite;
        this.englishName = englishName;
        this.finish = finish;
    }

    /** How a loose item is finished: what the tooltip tells the player and what the work sounds like. */
    public enum Finish {
        RIVETS("rivets", "Drive the rivets home: a hammer in the off hand, hold right-click.", LooseItem.HammerKnock.METAL),
        PUSH_ROD("push_rod", "Fit the push rod: a hammer in the off hand, hold right-click.", LooseItem.HammerKnock.METAL),
        MORTAR("mortar", "Seal the joints: Mortar in the off hand, hold right-click. It uses one up.", LooseItem.HammerKnock.MORTAR);

        public final String hintKey;
        public final String englishHint;
        public final LooseItem.HammerKnock knock;

        Finish(String name, String englishHint, LooseItem.HammerKnock knock) {
            this.hintKey = "tooltip." + CraftingMod.MOD_ID + ".finish." + name;
            this.englishHint = englishHint;
            this.knock = knock;
        }
    }
}
