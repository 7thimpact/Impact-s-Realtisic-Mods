package com.impact.realisticcrafting.lang;

import com.impact.realisticcrafting.CraftingMod;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The next-step tooltip of every Crafting item but the loose ones (which say how to hammer them): what it's made from
 * (its parent in the crafting tree) and what it goes on to make. Keyed by hint name: the item's path, or a shared name
 * for families ({@code pebble}, {@code sheet}, {@code banner_cloth}, {@code wooden_tool_head}, {@code stone_tool_head}).
 */
public final class CraftingHints {
    public static final String MADE_FROM_KEY = "tooltip." + CraftingMod.MOD_ID + ".hint.made_from";
    public static final String NEXT_KEY = "tooltip." + CraftingMod.MOD_ID + ".hint.next";
    static final Map<String, Hint> ENGLISH = buildEnglish();

    private CraftingHints() {
    }

    public static boolean has(String hintName) {
        return ENGLISH.containsKey(hintName);
    }

    public static String madeFromKey(String hintName) {
        return "tooltip." + CraftingMod.MOD_ID + ".made_from." + hintName;
    }

    public static String nextKey(String hintName) {
        return "tooltip." + CraftingMod.MOD_ID + ".next." + hintName;
    }

    record Hint(String madeFrom, String next) {
    }

    private static Map<String, Hint> buildEnglish() {
        Map<String, Hint> hints = new LinkedHashMap<>();
        hints.put("pebble", new Hint("loose pebbles on the ground, gravel or stone", "trim planks, hammer loose tools; 9 make their stone"));
        hints.put("makeshift_table", new Hint("4 planks in the 2×2 grid", "crafting with non-metal parts; place it and craft"));
        hints.put("makeshift_stone_table", new Hint("2 flint over 4 smooth stone", "iron, copper and redstone recipes"));
        hints.put("trimmed_planks", new Hint("planks and a trimming tool", "Trimmed Sticks, panels, frames, buckets, Chest Tops"));
        hints.put("trimmed_stick", new Hint("Trimmed Planks and a trimming tool", "tool handles, frames, Bow Staves, ladders, fences"));
        hints.put("wooden_panel", new Hint("2 Trimmed Planks side by side", "doors, trapdoors, signs, Boat Hulls, shields"));
        hints.put("wooden_frame", new Hint("Trimmed Planks and Trimmed Sticks in a ring", "Bed Frames, bookshelves, workstations"));
        hints.put("fence_post", new Hint("2 stacked Trimmed Planks and a trimming tool", "fences and fence gates"));
        hints.put("boat_hull", new Hint("3 Wooden Panels", "a loose boat with an Oar, then hammer it"));
        hints.put("oar", new Hint("2 Trimmed Sticks and Trimmed Planks", "boats and rafts"));
        hints.put("chest_top", new Hint("4 Trimmed Planks", "chests, barrels, beehives"));
        hints.put("wooden_bucket", new Hint("5 Trimmed Planks", "scoop water, or (unused) chests, barrels and the Dye Vat"));
        hints.put("wooden_water_bucket", new Hint("a Wooden Bucket dipped in water", "pour it, or fill a Dye Vat"));
        hints.put("wood_scraps", new Hint("trimming or cutting wood", "burns in a furnace, goes in a composter"));
        hints.put("shaped_stone", new Hint("cobblestone and a trimming tool", "Stone Hinges, Stone Casings, stone tool heads"));
        hints.put("stone_hinge", new Hint("Shaped Stone and a trimming tool", "chests, doors, trapdoors, gates, levers"));
        hints.put("stone_casing", new Hint("8 Shaped Stone in a ring", "furnaces, droppers, observers, pistons"));
        hints.put("stone_base", new Hint("a smooth stone slab and a chisel", "repeaters, comparators, stonecutters"));
        hints.put("mortar", new Hint("clay, sand and gravel", "brick blocks, flower pots, concrete powder"));
        hints.put("wooden_tool_head", new Hint("Trimmed Planks", "set it on Trimmed Sticks for a loose tool"));
        hints.put("stone_tool_head", new Hint("Shaped Stone", "set it on Trimmed Sticks for a loose tool"));
        hints.put("twine", new Hint("2 string, stacked", "ladders, bows, leads, books, candles, sewing"));
        hints.put("string_clump", new Hint("2 string side by side", "2 String Clumps make wool"));
        hints.put("leather_strip", new Hint("leather and a trimming tool", "item frames and books"));
        hints.put("leather_panel", new Hint("2 leather and the Bone Needle", "leather armor and horse armor"));
        hints.put("flint_arrowhead", new Hint("flint and a trimming tool", "arrows and fletching tables"));
        hints.put("bow_stave", new Hint("3 Trimmed Sticks and a trimming tool", "bows and crossbows"));
        hints.put("bone_needle", new Hint("a bone and a trimming tool", "sewing: sheets, comforters, banners, books, leather"));
        hints.put("flint_chisel", new Hint("flint on a Trimmed Stick", "cutting stone: slabs, stairs, walls, bricks"));
        hints.put("iron_chisel", new Hint("an iron ingot on a Trimmed Stick", "cutting stone, and it lasts longer"));
        hints.put("iron_saw", new Hint("a Trimmed Stick and 2 iron ingots", "wood cuts, glass panes, double trimming"));
        hints.put("iron_hammer", new Hint("2 iron ingots over a Trimmed Stick", "quick hammering, Iron Plates and Rivets"));
        hints.put("iron_tweezers", new Hint("an iron ingot over 2 nuggets", "Redstone Mechanisms and the Crafting Table"));
        hints.put("iron_plate", new Hint("an iron ingot and the Iron Hammer", "buckets, minecarts, hinges, Piston Heads"));
        hints.put("iron_rod", new Hint("an iron ingot and the Iron Saw", "rails, iron bars, Iron Wheels, lanterns"));
        hints.put("iron_rivets", new Hint("an iron nugget and the Iron Hammer", "Iron Hinges, chains, shields, mechanisms"));
        hints.put("iron_hinge", new Hint("an Iron Plate and Iron Rivets", "iron doors and iron trapdoors"));
        hints.put("iron_wheel", new Hint("4 Iron Rods and an Iron Plate", "minecarts"));
        hints.put("redstone_mechanism", new Hint("a plate, 2 redstone and rivets, with the Tweezers", "pistons, droppers, dispensers"));
        hints.put("piston_head", new Hint("a Wooden Panel and an Iron Plate", "pistons"));
        hints.put("paper_pulp", new Hint("sugar cane dipped in plain water", "3 Paper Pulp make paper"));
        hints.put("dye_vat", new Hint("an unused Wooden Bucket and 2 Twine", "fill with water and dye, then dip items"));
        hints.put("bed_frame", new Hint("a Wooden Frame and 3 Trimmed Planks", "place it, then lay a Comforter and a Sheet"));
        hints.put("comforter", new Hint("white wool and Twine, sewn", "the middle layer of a bed"));
        hints.put("sheet", new Hint("wool and Twine, sewn", "the top layer of a bed; its color is the bed's"));
        hints.put("banner_cloth", new Hint("6 wool and Twine, sewn", "a banner, on a Trimmed Stick"));
        return hints;
    }
}
