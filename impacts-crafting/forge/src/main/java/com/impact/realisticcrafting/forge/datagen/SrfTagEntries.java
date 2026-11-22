package com.impact.realisticcrafting.forge.datagen;

import com.impact.lib.ResourceIds;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.List;

/**
 * Salty's Realistic Forging's items that Crafting's tags list as optional entries ({@code "required": false}), from
 * {@code docs/srf-notes.md}: its smithing hammers finish loose items, its iron and copper items are stone tier, and its
 * gold, gem and netherite items are advanced. Entries for items a version of SRF lacks are simply skipped.
 */
final class SrfTagEntries {
    private static final String SRF_MOD_ID = "realisticforgingores";
    static final List<ResourceLocation> HAMMERS = ids("smithing_hammer", "smithing_stone_hammer", "smithing_wood_hammer");
    static final List<ResourceLocation> STONE_TIER = ids("raw_iron_ore", "cleanedironore", "hotrawironore", "ironoredust", "rawironnugget",
            "hotrawironnugget", "pickedhotrawiron", "pickedhotrawiron_2", "pickedhotrawiron_3", "pickedhotrawiron_4", "pickedhotrawiron_5",
            "stickhotrawiron", "stickhotrawiron_2", "stickhotrawiron_3", "stickhotrawiron_4", "stickhotrawiron_5", "pickedhotironnugget",
            "stickhotironnugget", "pickedironnugget", "pickedironiingot", "copperore", "copperdust", "panwithcopperore", "panwithcopperdust",
            "panwithmeltedcopper", "pickedpanwithmeltedore", "moldwithmeltedcopper", "copperingotinmold", "smithing_hammer", "blacksmithstongs",
            "carvinghammer", "chisel", "pan", "bigpan", "iron_sledgehammer");
    static final List<ResourceLocation> ADVANCED_TIER = ids("rawgoldore", "purerawgoldore", "moldwithragoldore", "moldwithmeltedgold",
            "handlingmeltedgold", "shapedgold", "pickedshapedgold", "meltedscrapsandgold", "meltedscrapsandgoldinmold", "roughdiamond",
            "roughcutdiamond", "moldeddiamond", "moldeddiamond_2", "diamondonanvil", "carveddiamondore", "carveddiamondore_2",
            "carveddiamonddeepslate", "carveddiamonddeepslate_2", "roughemerald", "cutroughemerald", "notpolishedemerald", "polishedemerald",
            "carvedemeraldblock", "carvedemeraldblock_2", "carvedemeralddeepslate", "carvedeemeralddeepslate_2", "moldwithnetheriteingot",
            "diamond_sledge_hammerr");

    private SrfTagEntries() {
    }

    private static List<ResourceLocation> ids(String... paths) {
        return Arrays.stream(paths).map(path -> ResourceIds.of(SRF_MOD_ID, path)).toList();
    }
}
