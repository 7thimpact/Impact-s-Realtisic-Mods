# Impact's Realistic Crafting — Mod Spec

Nov 14, 2026 · @Supa

## Overview

Impact's Realistic Crafting (IRC) turns one-click vanilla crafting into a hands-on, multi-step process: trim raw materials into parts, assemble the parts into a "loose" item at the crafting table, then finish it by hammering it in the world. It's built as a companion to [Salty's Realistic Forging](https://www.curseforge.com/minecraft/mc-mods/salty-s-realistic-forging) (SRF). SRF covers ores and metal tools. IRC covers wood, stone, crafting tables and the rest of vanilla's crafting recipes.

**Target platform**

- Minecraft **1.20.1**, **Forge** (Java 17). That's SRF's main build (4.1.6). SRF also ships for NeoForge 1.20.4; a port to that is a later option, not in v1.
- Mod ID: `impactscrafting`. Display name: Impact's Realistic Crafting.
- Runs on both sides. Logic is server-side; the client handles animation, sound and particles.
- SRF is a **soft dependency**. It was made in MCreator and has no API, so IRC integrates only through item IDs, tags and recipes. It never calls SRF code.

**Goals**

1. Almost every vanilla recipe is rebuilt from shared parts that branch like a tree: raw → starting part → shared part → vanilla item.
2. Crafting takes time, and better tables unlock as you progress: Makeshift Table → Makeshift Stone Table → Crafting Table.
3. Fits SRF's feel: uses the main hand and off hand together, and teaches players with tooltips and advancements.
4. Recipes are data-driven, so pack makers can add or change them with JSON.

**Non-goals for v1:** metal tools and armor (SRF's job), automation support, a guidebook.

**Impact mod family**

IRC is one of five mods in the Impact family. Each works alone and connects automatically with the others.

- **[Impact's Lib](https://claude.ai/artifact/Av8WHmTv7tbEK6XNLkYXaQ):** shared systems and items. It's a standalone mod that IRC lists as a required dependency in `mods.toml` and on its mod pages, so launchers install it alongside. Build it first. Tool crafting, grinding, the grinder items, hold-to-use, uses-durability, wood NBT, fuel helpers, the crafting-progress arrow, Give or drop and debug logging all live there (see its "What moves out of IRC" table). Any leftover `impactscrafting:` ID for one of those systems means the `impactslib:` one.
- **[Impact's Realistic Food](https://claude.ai/artifact/H85Ku1ujPuD8QUSVhTsUAF) (IRF):** all food lives there: knives, boiling, soups, spit roasting, cooking styles and baking, including Flour and Dough. IRC has no food recipes. When both are installed, IRF's recipes switch to IRC parts, obey IRC's tiers, and are timed like every other grid recipe. IRF's stone boiling uses IRC's Stone Pebble and Water-filled Wooden Bucket.
- **[Impact's Realistic Inventory](https://claude.ai/artifact/Euz9kvYYXiDVHnaD9xRjkB) (IRI):** two hand slots and 9 pockets. IRI keeps vanilla's inventory menu and replaces only the screen, so IRC's 2×2 timer and tier check keep working, and IRI's screen draws IRC's arrow through Lib. Hammering and grinding work as written, because IRI's inactive hand slot is the off hand.
- **[Impact's Realistic Vitality](https://claude.ai/artifact/1MFojaKLikF4TDVS8HSyyS) (IRV):** thirst, temperature and air. With IRC, its gear recipes use IRC parts, armor linings are sewn with the Bone Needle, its loose Air Tank is finished by IRC hammering, and players can drink from a Water-filled Wooden Bucket.
- No Impact mod imports another's classes. They connect only through Lib, tags and `forge:mod_loaded` recipe conditions, and only one Impact mod may override a given vanilla recipe (Lib's conflict checker enforces this).

## Core crafting flow

Tools follow four steps: **Trim → Shape → Assemble (loose) → Hammer**. The wooden pickaxe is the reference case. Build it end-to-end first.

**Getting Stone Pebbles**

The pebble is the first tool in the mod, so there are three ways to get one and none of them need a tool. These are the only sources; no recipe makes pebbles. Pebbles can be crafted into cobblestone, though: 9 Stone Pebbles in a 3×3 grid at a Makeshift Table → 1 Cobblestone (\`impactscrafting:pebbles\_to\_cobblestone\`, shaped, so the 2×2 inventory grid can't do it). Worn pebbles work, and their durability is ignored. This gives a player with no pickaxe a way into stone: pebbles → cobblestone → Shaped Stone → stone tools.

| Source | How | Drop |
| --- | --- | --- |
| Loose pebbles (new `loose_pebble` block) | Generates naturally on exposed stone: mountainsides, stony shores and peaks, river beds, cave floors. Breaks instantly by hand | Always 1 pebble |
| Gravel | Break gravel, by hand or with a shovel | 25% chance |
| Stone | Break any stone-type block (stone, cobblestone, deepslate, granite, diorite, andesite, tuff), by hand or with a pickaxe | By hand: always 1 pebble (stone drops nothing else by hand). With a pickaxe: 15% chance, on top of the normal drop |

- **Stacking:** unused pebbles stack to 64. Using a pebble once (trimming or hammering) makes it worn: it then stacks to 1 and shows a durability bar. Implementation: don't use vanilla item damage; store uses in NBT and return 1 from `getMaxStackSize(ItemStack)` once worn.
- **The loose pebble block:** a small flat cluster of stones with no collision and a few random model variants. It can be waterlogged, so it sits on river beds. It pops off if the block under it is removed. Players can't place it.
- **Worldgen:** a placed feature added to every Overworld biome by a biome modifier. It only places on top of a block in `#impactscrafting:pebble_base` (the stone types above plus calcite and mossy cobblestone) that has air or water above it. Default density: up to 4 attempts per chunk on the surface and 6 on cave floors.
- **Drops:** gravel and stone drops come from global loot modifiers. Silk Touch gives no pebble and Fortune has no effect. Dropped pebbles are always unused.
- **Config:** `pebbles.gravelChance` 0.25, `pebbles.stonePickaxeChance` 0.15, `pebbles.worldgen` true, `pebbles.surfaceTriesPerChunk` 4, `pebbles.caveTriesPerChunk` 6.

**Wooden pickaxe walkthrough**

1. **Gather.** Pick up a Stone Pebble (see above) and chop some logs. Turn the logs into planks with the pebble (2 per log), or by hand at a poor rate (1 per log).
2. **Trim the wood.** At a Makeshift Table, combine planks + Stone Pebble (shapeless) → 1 Trimmed Planks. The pebble stays in the grid and loses 1 durability.
3. **Shape the head.** Put 3 Trimmed Planks in the top row → 1 Wooden Pickaxe Head.
4. **Trim sticks.** Combine Trimmed Planks + Stone Pebble → 2 Trimmed Sticks. The pebble loses 1 durability.
5. **Assemble.** Put the Wooden Pickaxe Head in the top middle and 2 Trimmed Sticks below it (the vanilla handle slots) → Loose Wooden Pickaxe.
6. **Hammer.** Hold the Loose Wooden Pickaxe in your main hand and a Stone Pebble in your off hand. Hold right-click for about 2 seconds → Wooden Pickaxe. The pebble loses 1 durability.

The same pattern covers every wooden and stone tool. Only the head recipe changes (see the next section).

## Crafting stations

Three tables and the 2×2 inventory grid decide which recipes you can craft, based on the metals in the recipe. A table refuses any recipe that has an ingredient above its tier. When it does, the output slot stays empty and a tooltip names the table you need. "No ores" means no ores and nothing made from them: no ingots, nuggets, gems, raw ores or dusts. The two makeshift tables wear out like an anvil.

| Station | Recipe | Made at | Accepts | Uses |
| --- | --- | --- | --- | --- |
| 2×2 inventory grid | — | — | Non-metal ingredients only | Unlimited |
| Makeshift Table (new block) | 4 planks in a 2×2 (the vanilla crafting table's shape) | 2×2 inventory grid | Non-metal ingredients only | 128 crafts |
| Makeshift Stone Table (new block) | `FF` / `SS` / `SS`: 2 flint on top of 4 smooth stone | Makeshift Table | Non-metal ingredients plus iron, copper and redstone | 256 crafts |
| Crafting Table (vanilla) | 4 planks + Iron Saw + Iron Hammer + Iron Tweezers, shapeless. The tools stay in the grid, damaged | Makeshift Stone Table | Everything | Unlimited |

All three tables have a 3×3 grid and timed crafting. Smooth stone still comes from smelting stone in a furnace, so the furnace has to be craftable at the Makeshift Table.

**Makeshift table wear (like an anvil)**

- Each craft taken from a makeshift table uses 1 of its uses. Shift-clicking uses 1 per item crafted.
- Like the anvil, the table shows three stages: new, worn (below 2/3 of its uses) and falling apart (below 1/3). Each stage is a blockstate (`wear` 0–2) with its own texture. A block entity stores the exact count.
- At 0 uses the table breaks with the anvil's destroy sound. Items in the grid drop on the ground, and nothing else drops.
- Breaking and picking up a table keeps its remaining uses on the item (NBT, shown in the tooltip), so placing it again doesn't reset it.
- There's no repair. Build a new one, or move up to the Crafting Table, which never wears out.
- Config: `makeshiftTableUses` 128, `makeshiftStoneTableUses` 256.

**Iron tools** (made at the Makeshift Stone Table; S = Trimmed Stick, I = iron ingot, N = iron nugget)

| Tool | Starting pattern | Durability | Also works as |
| --- | --- | --- | --- |
| Iron Saw | `SII` in one row | 128 | Trimming tool. Gives double output when trimming planks |
| Iron Hammer | `II` / `_S` | 128 | Hammer. Hammering takes 20 ticks instead of 40 |
| Iron Tweezers | `N_N` / `_I_` | 128 | Ingredient for the Crafting Table and fine-detail parts (v2) |

These patterns are a starting point. Claude Code should change any that clash with vanilla recipes (for example, shears are 2 iron ingots placed diagonally) or with SRF.

**Tier rules**

- Each ingredient gets a tier from tags. `impactscrafting:tier/stone` holds iron and copper (ingots, nuggets, raw ore, ore, storage blocks), redstone dust and redstone blocks, IRC's iron tools and iron parts (Iron Plate, Rod, Rivets, Hinge, Wheel, Redstone Mechanism), and SRF's iron and copper items as optional entries. `impactscrafting:tier/advanced` holds `forge:ores`, `forge:raw_materials`, `forge:ingots`, `forge:nuggets`, `forge:gems`, `forge:dusts` and `forge:storage_blocks`, minus anything already in tier/stone. Anything not in either tag is non-metal.
- The tags hold materials and IRC's own iron tools and parts, not finished goods. Armor and other mods' finished metal items (an IRF Iron Knife, an IRV Canteen) aren't in them, so lining armor (IRV) or cutting food with an iron knife (IRF) works at any table. Those mods' recipes still take the tier of their materials: an Iron Knife made from an Iron Plate needs the Stone Table.
- A recipe's tier is the highest tier of any of its ingredients, including tool ingredients. The check applies to every crafting recipe from every mod, read from its ingredients at runtime. Special recipes with no ingredient list count as non-metal.
- The 2×2 inventory grid follows the Makeshift Table's rules, but it never wears out. With IRI installed it's still vanilla's 2×2 grid, so nothing changes.
- The vanilla crafting table recipe is disabled. Crafting tables already in the world, like those in villages, still work fully.

## Vanilla recipe overhaul

IRC replaces almost every vanilla crafting recipe with a multi-part one. Each catalog entry below lists the item's parts, the table it needs, and how it's finished: **table** (done when crafted), **hammer** (loose item, hammered in hand), or **placed** (built in the world).

Only these stay vanilla. They're still timed and still locked to their table tier:

- **Storage blocks:** packing 9 items into 1 block and unpacking it again (ingots, coal, wheat, kelp, slime, honey, snow, and so on).
- **Metal tools and armor:** SRF handles these. Gold recipes (golden apples, clocks, powered rails) keep their vanilla recipe but need the Crafting Table. Shears, flint and steel and the anvil keep their vanilla recipes at the Stone Table.
- **Endgame items:** enchanting table, beacon, end crystal, ender chest, conduit, respawn anchor, lodestone, netherite upgrades. They keep vanilla recipes and need the Crafting Table.
- **Other stations:** smelting, stonecutter, smithing table, loom patterns, brewing. The stonecutter stays a faster way to cut stone, and SRF depends on it.
- **Special recipes:** map and book cloning, firework stars and rockets, armor trims, banner pattern copying.

Recipes from other mods are only locked by tier. Their ingredients don't change.

In the catalog, a tool in parentheses stays in the grid and takes damage (`tool_crafting`).

### Crafting tree

Every recipe climbs the same tree: a raw material becomes a starting part, the starting part becomes a shared part, and shared parts combine into vanilla items.

&#91;embedded content: IRC crafting tree · 5 starting parts, 16 shared parts\]

Where branches meet, an item needs both: a dropper is a Stone Casing plus a Redstone Mechanism, and a Dye Vat is a Wooden Bucket lashed with Twine. When adding a new recipe, reuse an existing shared part before inventing a new one.

**Chest walkthrough** (at a Makeshift Table; P = Trimmed Planks)

1. **Wooden Bucket:** P\_P / PPP (5 Trimmed Planks) → 1 Wooden Bucket. This is the chest's base. It also branches into the barrel, composter, beehive and Dye Vat, and it can scoop and pour water (but not lava). Empty and full are two items, `wooden_bucket` and `wooden_water_bucket`. It has 32 uses, and each scoop or pour costs 1. Unused empty buckets stack to 16; a used or filled bucket stacks to 1. Only unused, empty buckets work as a crafting part.
2. **Chest Top:** PP / PP (4 Trimmed Planks) → 1 Chest Top.
3. **Stone Hinges:** Shaped Stone + Stone Pebble (trimming) → 2 Stone Hinges.
4. **Assemble:** HTH / *B* (Chest Top between 2 Stone Hinges, Wooden Bucket below) → 1 Chest. The chest is finished as soon as it's crafted; it doesn't need hammering.

### Parts and tools library

About 40 shared parts and tools carry the whole overhaul, and they form a tree. Each part branches from one parent and into several items, so learning one part unlocks a whole family. Most vanilla items become 2–4 of these put together.

**Tools** (they stay in the grid and lose durability)

| Tool | Recipe | Uses | Used for |
| --- | --- | --- | --- |
| Stone Pebble | Loose pebbles, gravel (25%), stone (15%) | 32 | Trimming, early hammering |
| Flint Chisel | Flint + Trimmed Stick | 48 | Cutting stone: slabs, stairs, walls, bricks, buttons, polished blocks |
| Iron Chisel | Iron ingot + Trimmed Stick (Stone Table) | 192 | Same as the flint chisel |
| Iron Saw | See Crafting stations | 128 | Wood cuts (slabs, stairs, fence posts), glass panes, double-output trimming |
| Iron Hammer | See Crafting stations | 128 | Fast hammering, flattening iron plates and rivets |
| Iron Tweezers | See Crafting stations | 128 | Fine parts: Redstone Mechanisms, clocks, compasses |
| Bone Needle | Bone + (trimming tool) | 64 | Sewing: bed sheets, comforters, banners, books, leather armor |
| Grinder Bowl and Grinding Stick (Lib items, IRC recipes) | See Grinding | 64 wood / 256 stone | Dyes and powders |

**Parts**

| Part | Recipe (its parent) | Table | Branches into |
| --- | --- | --- | --- |
| Trimmed Planks | Planks + (trimming tool) | Makeshift | Trimmed Sticks, Wooden Panel, Wooden Frame, Wooden Bucket, Chest Top, Fence Post, tool heads, bowls, buttons |
| Trimmed Sticks | Trimmed Planks + (trimming tool) → 2 | Makeshift | Tool handles, Wooden Frame, Bow Stave, Oars, ladders, rails, fences, levers, chisels, grinding sticks |
| Wooden Panel (per wood type) | 2 Trimmed Planks side by side | Makeshift | Doors, trapdoors, signs, Boat Hull, Piston Head, shields, smoker hood, lectern, note block, fence gates |
| Wooden Frame (per wood type) | 4 Trimmed Planks in the corners + 4 Trimmed Sticks on the edges, center empty → 2 | Makeshift | Bed Frame, bookshelves, loom, cartography, fletching and smithing tables, paintings, item frames, note block |
| Wooden Bucket | 5 Trimmed Planks `P_P` / `PPP` | Makeshift | Chest, barrel, composter, beehive, Dye Vat. Also carries water, as the Water-filled Wooden Bucket |
| Chest Top | 4 Trimmed Planks `PP` / `PP` | Makeshift | Chest, barrel, beehive |
| Fence Post (per wood type) | Trimmed Planks + (trimming tool) → 2 | Makeshift | Fences, fence gates |
| Boat Hull, Oar | 3 Wooden Panels (`P_P` / `_P_`); 2 Trimmed Sticks + Trimmed Planks | Makeshift | Boats, rafts |
| Bow Stave | 3 Trimmed Sticks + (trimming tool) | Makeshift | Bow, crossbow (and through the bow, the dispenser) |
| Shaped Stone | Cobblestone + (trimming tool) | Makeshift | Stone Hinge, Stone Casing, stone tool heads, Stone Grinder Bowl and Stick |
| Stone Hinge | Shaped Stone + (trimming tool) → 2 | Makeshift | Chest, doors, trapdoors, fence gates, furnace door, lever |
| Stone Casing | 8 Shaped Stone in a ring | Makeshift | Furnace (then smoker and blast furnace), dropper (then dispenser), observer, piston |
| Stone Base | Smooth stone slab + (chisel) → 2 | Makeshift | Repeater, comparator, daylight detector, stonecutter, grindstone, armor stand |
| Mortar | Clay ball + sand + gravel → 4 | Makeshift | Every brick block, flower pot, concrete powder |
| Iron Plate | Iron ingot + (Iron Hammer) | Stone | Bucket (then cauldron and hopper), minecart, Iron Hinge, Piston Head, Redstone Mechanism, shield, crossbow, smithing table, stonecutter, blast furnace, compass |
| Iron Rod | Iron ingot + (Iron Saw) → 2 | Stone | Rails, iron bars, Iron Wheel, tripwire hook, lantern |
| Iron Rivets | Iron nugget + (Iron Hammer) → 2 | Stone | Iron Hinge, chain, lantern, shield, Redstone Mechanism |
| Iron Hinge | Iron Plate + Iron Rivets → 2 | Stone | Iron door, iron trapdoor |
| Iron Wheel | 4 Iron Rods + Iron Plate | Stone | Minecart |
| Piston Head | Wooden Panel + Iron Plate | Stone | Piston, then sticky piston |
| Redstone Mechanism | Iron Plate + 2 redstone + Iron Rivets + (Iron Tweezers) | Stone | Piston, dropper, dispenser, observer |
| Twine | 2 string stacked vertically (shaped). Also: 2 string side by side → 1 String Clump | Makeshift | Ladders, scaffolding, bows, crossbow, fishing rod, leads, books, candles, loom, Dye Vat, every sewn item. String Clumps make wool (2 clumps → 1 wool) |
| Leather Strip | Leather + (trimming tool) → 3 | Makeshift | Item frames, books |
| Leather Panel | 2 leather + (Bone Needle) | Makeshift | Leather armor, leather horse armor |
| Banner Cloth, Comforter, Sheet | Wool + Twine + (Bone Needle) | Makeshift | Banners, beds |
| Paper Pulp | Sugar cane dipped in a plain-water Dye Vat | Dye Vat | Paper (then books, maps, TNT, cartography table) |

### Grinding (dyes and powders)

All dyes and powders come from a grinder held in the off hand. No dye or powder can be crafted in a grid.

**The grinder**

| Item | Recipe (P = Trimmed Planks, St = Shaped Stone, S = Trimmed Stick) | Uses |
| --- | --- | --- |
| Wooden Grinder Bowl | Bowl + Trimmed Planks (a thicker base) | 64 |
| Stone Grinder Bowl | `St_St` / `_St_` | 256 |
| Wooden Grinding Stick | Trimmed Stick + (trimming tool), rounded off | 64 |
| Stone Grinding Stick | `St` / `S` (Shaped Stone on top of a Trimmed Stick) | 256 |

**How it works**

Grinding is Impact's Lib's system: the `impactslib:grinding` recipe type and the four `impactslib:` grinder items. IRC ships the grinder-item recipes above and the grinding recipes below. Without IRC, IRF ships simpler grinder recipes; with both installed, only IRC's load. With IRI, the bowl sits in the inactive hand slot, which is the off hand.

1. **Load:** hold the bowl in your off hand. Right-click with a grindable item in your main hand to load it, or click the item onto the bowl in any inventory screen, like a bundle. A bowl holds up to 16 of one item, or 2 different dyes for mixing. The contents show on the bowl's model and in its tooltip.
2. **Grind:** hold a grinding stick in your main hand and hold right-click. One item is ground every 30 ticks with a wooden stick, or every 20 with a stone stick. The output goes to the player through Lib's Give or drop.
3. **Wear:** the bowl and the stick each lose 1 durability per item ground.
4. **Stop:** letting go, or switching hand slots, loses progress on the current item only. Loaded items stay in the bowl. Shift-right-click with an empty main hand to empty the bowl.

A bowl's tier sets what it can grind. Wood handles soft items like flowers, berries and sugar cane, and can mix dyes. Stone is needed for hard items: bone, wheat, lapis, blaze rods. A stone stick works in a wooden bowl, but the grind only goes as far as the bowl's tier allows.

**Grinding recipes** (wood unless marked stone; doubled outputs match vanilla)

| Output | Inputs |
| --- | --- |
| Bone Meal ×3 | Bone (stone) |
| White Dye | Bone Meal, Lily of the Valley |
| Light Gray Dye | Azure Bluet, Oxeye Daisy, White Tulip |
| Black Dye | Ink Sac, Wither Rose |
| Brown Dye | Cocoa Beans |
| Red Dye | Poppy, Red Tulip, Beetroot, Rose Bush (×2) |
| Orange Dye | Orange Tulip, Torchflower |
| Yellow Dye | Dandelion, Sunflower (×2) |
| Blue Dye | Cornflower, Lapis Lazuli (stone) |
| Light Blue Dye | Blue Orchid |
| Magenta Dye | Allium, Lilac (×2) |
| Pink Dye | Pink Tulip, Pink Petals, Peony (×2) |
| Cyan Dye ×2 | Pitcher Plant |
| Green Dye | Not ground: smelt cactus (vanilla) |
| Sugar | Sugar Cane |
| Blaze Powder ×2 | Blaze Rod (stone) |

**Dye mixing** (load 2 different dyes, get 2 dyes out per grind): red + yellow → orange · red + white → pink · blue + white → light blue · blue + green → cyan · blue + red → purple · purple + pink → magenta · green + white → lime · black + white → gray · gray + white → light gray.

Recipe type: `impactslib:grinding`, defined by Lib. Fields: ingredients (1 or 2), result, count, min\_tier (wood or stone), grind\_ticks (optional). IRC's grinding recipes cover every dye, sugar, bone meal, blaze powder and the dye mixes.

### Beds (placed in three layers)

A bed is built in the world from three parts, in order: frame, comforter, sheet. The sheet's color becomes the bed's color.

| Part | Recipe | Table | Branches from |
| --- | --- | --- | --- |
| Bed Frame (placeable) | Wooden Frame + 3 Trimmed Planks (slats) | Makeshift | Wooden Frame |
| Comforter | 3 **white** wool + Twine + (Bone Needle). Only white wool works | Makeshift | Twine, sewing |
| Sheet (16 colors) | 3 wool of one color + Twine + (Bone Needle). A white sheet can also be dyed in the Dye Vat | Makeshift | Twine, sewing, Dye Vat |

**Placing**

1. Place the Bed Frame like a bed: 2 blocks long, facing away from you. You can't sleep in it yet.
2. Right-click the frame with a Comforter. It goes on top, and either half works.
3. Right-click it with a Sheet. Both halves turn into the vanilla bed of the sheet's color (for example `minecraft:red_bed`), facing the same way.

**Rules**

- Wrong order (a sheet on a bare frame) is refused, with an action-bar hint: "Lay the comforter first."
- Breaking an unfinished bed drops the frame plus whatever is on it. Breaking a finished bed drops all three parts (config `bedsDropParts`, default true). This includes village beds.
- To change a bed's color, right-click a finished bed with a different sheet. The new sheet goes on and the old one drops. The vanilla "bed + dye" recipe is off.
- Only a finished bed sets your spawn point or explodes in the Nether and the End.

**Implementation:** `BedFrameBlock` with `FACING`, `PART` (head/foot) and `COMFORTER` (boolean). Using a sheet replaces both halves with the vanilla `BedBlock` states (`FACING`, `PART`, `OCCUPIED=false`) for that `DyeColor`. A loot modifier on vanilla beds swaps the bed drop for the three parts.

### Dye Vat

All dyeing happens in a Dye Vat. It's a new block built from the Wooden Bucket, and every grid dyeing recipe is off. It's separate from the cauldron so it can't clash with SRF, which washes ore in cauldrons.

- **Recipe:** Wooden Bucket + 2 Twine (as hoops) → Dye Vat. Makeshift Table.
- **Water:** fill it with a Water-filled Wooden Bucket (which comes back empty), an iron water bucket, or slowly with rain.
- **Dye:** each dye adds 8 charges of its color, up to 32. A dye that mixes with the current color (see the mixing list) changes the vat's color. Any other dye is refused.
- **Dipping:** hold right-click with a dyeable item. Each item takes 10 ticks and 1 charge, and holding works through a whole stack.
- **Dyeable:** wool, carpet, terracotta, glass, glass panes, concrete powder, candles, shulker boxes, Bed Sheets, Banner Cloth, and every leather-style item: anything that implements `DyeableLeatherItem` (leather armor, IRI's packs, other mods' leather gear) or is in `#impactscrafting:vat_dyeable`. Leather-style items take the vat's color through vanilla's `DyeableLeatherItem.dyeArmor`, so colors blend like vanilla leather dyeing.
- **Plain water:** washes the dye off leather-style items and banners, and soaks sugar cane into Paper Pulp (20 ticks each).
- **Emptying:** shift-right-click with an empty hand.
- **Recipe type** `impactscrafting:vat_dyeing`: input (tag), results keyed by dye color, charges (default 1), dip\_ticks (default 10). Plain-water recipes use the color key none. Leather-style items are handled in code, not by recipes.

### Catalog: wood and building

Almost every wooden item comes from four parts: Trimmed Planks, Trimmed Sticks, Wooden Panel and Wooden Frame. The Wooden Bucket and Chest Top add the containers.

| Vanilla item | IRC recipe | Table | Finish |
| --- | --- | --- | --- |
| Planks | Log + (trimming tool) → 2; Iron Saw → 4. A bare log alone → 1, so a new player is never stuck | 2×2 | table |
| Sticks | Planks + (trimming tool) → 2; Iron Saw → 4 | 2×2 | table |
| Wooden slabs, stairs | Planks + (Iron Saw or pebble) → 2 slabs, or 1 stair | Makeshift | table |
| Wooden button, pressure plate | Trimmed Planks + (trimming tool) → 4 buttons, or 1 plate | Makeshift | table |
| Fence (3) | 2 Fence Posts + 2 Trimmed Sticks (rails) | Makeshift | table |
| Fence gate | 2 Fence Posts + Wooden Panel + Stone Hinge | Makeshift | table |
| Door (3) | 3 Wooden Panels + 2 Stone Hinges | Makeshift | table |
| Trapdoor (2) | 2 Wooden Panels + Stone Hinge | Makeshift | table |
| Sign (3) | Wooden Panel on top of a Trimmed Stick | Makeshift | table |
| Hanging sign (6) | Wooden Panel + Chain | Stone | table |
| Ladder (3) | 3 Trimmed Sticks + Twine | Makeshift | table |
| Scaffolding (6) | 3 bamboo + Twine | Makeshift | table |
| Bowl (2) | Trimmed Planks + (trimming tool) | Makeshift | table |
| Chest | Wooden Bucket + Chest Top + 2 Stone Hinges (walkthrough above) | Makeshift | table |
| Trapped chest | Chest + Tripwire Hook | Stone | table |
| Barrel | Wooden Bucket + Chest Top | Makeshift | table |
| Composter | Wooden Bucket + 2 Trimmed Sticks (slats) | Makeshift | table |
| Beehive | Wooden Bucket + Chest Top + 3 honeycomb | Makeshift | table |
| Bookshelf | Wooden Frame + 3 Books | Makeshift | table |
| Chiseled bookshelf | Wooden Frame + 2 Wooden Panels | Makeshift | table |
| Lectern | Bookshelf + Wooden Panel | Makeshift | table |
| Loom | Wooden Frame + 2 Twine | Makeshift | table |
| Cartography table | Wooden Frame + 2 paper | Makeshift | table |
| Fletching table | Wooden Frame + 2 Flint Arrowheads | Makeshift | table |
| Smithing table | Wooden Frame + 2 Iron Plates | Stone | table |
| Painting | Wooden Frame + white wool | Makeshift | table |
| Item frame (2) | Wooden Frame + Leather Strip | Makeshift | table |
| Armor stand | 3 Trimmed Sticks + Stone Base | Makeshift | table |
| Boat, raft | Boat Hull (3 Wooden Panels, `P_P` / `_P_`) + 2 Oars (2 Trimmed Sticks + Trimmed Planks) → loose boat | Makeshift | hammer |
| Chest boat | Boat + Chest | Makeshift | table |
| Campfire | 3 logs + 3 Trimmed Sticks + coal or charcoal | Makeshift | table |
| Crafting Table | 4 planks + (Iron Saw, Iron Hammer, Iron Tweezers) | Stone | table |

Every wooden item keeps its wood type through its parts (`wood` NBT on Trimmed Planks, Wooden Panel, Wooden Frame, Fence Post). An oak panel makes an oak door.

### Catalog: stone, bricks and glass

Stone branches through Shaped Stone into three parts: the Stone Hinge, the Stone Casing (8 Shaped Stone in a ring) and the Stone Base. Brick blocks all share Mortar. The Stone Casing is the housing for every stone machine, both here and in redstone.

| Vanilla item | IRC recipe | Table | Finish |
| --- | --- | --- | --- |
| Stone slabs, stairs, walls (every stone type) | Block + (chisel) → 2 slabs, or 1 stair, or 1 wall. The stonecutter still works too | Makeshift | table |
| Stone bricks, polished, chiseled, cut and smooth-cut variants | Block + (chisel) → 1 | Makeshift | table |
| Stone button, stone pressure plate | Stone + (chisel) → 2 buttons, or 1 plate | Makeshift | table |
| Bricks, nether bricks, red nether bricks, mud bricks (blocks) | 4 of that brick + Mortar | Makeshift | table |
| Flower pot | 3 bricks + Mortar | Makeshift | table |
| Furnace | Stone Casing + Stone Hinge (the door) | Makeshift | table |
| Smoker | Furnace + 2 Wooden Panels (the hood) | Makeshift | table |
| Blast furnace | Furnace + 3 Iron Plates + 2 smooth stone | Stone | hammer |
| Stonecutter | Stone Base + Iron Plate (the blade) | Stone | table |
| Grindstone | Stone Base (the wheel) + 2 Trimmed Sticks + 2 Trimmed Planks | Makeshift | table |
| Lever | Stone Hinge + Trimmed Stick | Makeshift | table |
| Glass panes, stained glass panes (3) | Glass + (Iron Saw) | Stone | table |
| Glass bottle | Glass + (Iron Tweezers) | Stone | table |
| White concrete powder (8) | 4 sand + 4 gravel + Mortar. Other colors come from the Dye Vat | Makeshift | table |

Packing recipes like sandstone, quartz blocks and packed ice stay vanilla (see the untouched list). Pebbles → cobblestone (9 pebbles, 3×3, Makeshift Table) feeds this whole branch. It's the one recipe where pebbles are an ingredient instead of a tool.

### Catalog: iron goods, redstone and transport

Iron branches into four parts: Iron Plate, Iron Rod, Iron Rivets and Iron Hinge. Redstone machines all combine the Redstone Mechanism with a housing: the Stone Casing, the Stone Base or a Wooden Frame. Redstone dust, iron and copper are Stone Table tier, so most redstone works there. Quartz, gold, glowstone and diamond are advanced, so comparators, observers, daylight detectors, powered rails, clocks, redstone lamps and jukeboxes need the Crafting Table.

| Vanilla item | IRC recipe | Table | Finish |
| --- | --- | --- | --- |
| Bucket | 3 Iron Plates → loose bucket | Stone | hammer |
| Cauldron | Bucket + 4 Iron Plates | Stone | hammer |
| Hopper | Bucket (the funnel) + 2 Iron Plates + Chest | Stone | table |
| Iron bars (8) | 3 Iron Rods | Stone | table |
| Chain | 3 Iron Rivets | Stone | table |
| Lantern, soul lantern | Torch + 2 Iron Rods + Iron Rivets | Stone | table |
| Iron door | 2 Iron Plates + Iron Hinge → loose door | Stone | hammer |
| Iron trapdoor | Iron Plate + Iron Hinge | Stone | table |
| Heavy pressure plate | Iron Plate + (Iron Saw) | Stone | table |
| Tripwire hook (2) | Iron Rod + Trimmed Stick + Trimmed Planks | Stone | table |
| Rails (8) | 4 Iron Rods + 2 Trimmed Sticks (ties) | Stone | table |
| Minecart | 3 Iron Plates (the tub) + 2 Iron Wheels → loose minecart | Stone | hammer |
| Chest, hopper, furnace and TNT minecarts | Minecart + the block (vanilla shape) | Stone | table |
| Redstone torch | Trimmed Stick + redstone | Stone | table |
| Repeater | Stone Base + 2 redstone torches + redstone | Stone | table |
| Piston | Piston Head (Wooden Panel + Iron Plate) + Stone Casing + Redstone Mechanism → loose piston | Stone | hammer |
| Sticky piston | Piston + slimeball | Stone | table |
| Dropper | Stone Casing + Redstone Mechanism | Stone | table |
| Dispenser | Dropper + Bow | Stone | table |
| Note block | Wooden Frame + Wooden Panel + redstone | Stone | table |
| Compass | Iron Plate + redstone + (Iron Tweezers) | Stone | table |
| Detector rail, activator rail, target | Vanilla recipes (iron and redstone only) | Stone | table |
| TNT | 4 gunpowder + 4 sand + paper (the casing) | Makeshift | table |
| Comparator | Stone Base + 3 redstone torches + quartz | Crafting | table |
| Daylight detector | Stone Base + 2 glass + quartz | Crafting | table |
| Observer | Stone Casing + Redstone Mechanism + quartz | Crafting | table |
| Jukebox | Note Block + diamond | Crafting | table |
| Powered rail, clock, redstone lamp | Vanilla recipes (gold or glowstone) | Crafting | table |

Shears, flint and steel and the anvil keep their vanilla recipes. They only use iron, so they're made at the Stone Table.

### Catalog: combat, tools and leather

Ranged weapons branch from the Bow Stave and Twine. Leather goods branch from the Leather Panel and Leather Strip, sewn with the Bone Needle.

| Vanilla item | IRC recipe | Table | Finish |
| --- | --- | --- | --- |
| Wooden and stone tools | Head + Trimmed Sticks → loose tool (core flow) | Makeshift | hammer |
| Bow | Bow Stave (3 Trimmed Sticks + (trimming tool)) + 2 Twine | Makeshift | table |
| Arrow (2) | Flint Arrowhead + Trimmed Stick + feather | Makeshift | table |
| Flint Arrowhead (2) | Flint + (trimming tool) | Makeshift | table |
| Crossbow | Bow Stave + Wooden Panel (stock) + Iron Plate + Tripwire Hook + Twine | Stone | table |
| Shield | 3 Wooden Panels + Iron Plate + Iron Rivets → loose shield | Stone | hammer |
| Fishing rod | 3 Trimmed Sticks + Twine | Makeshift | table |
| Carrot on a stick, warped fungus on a stick | Fishing rod + the item (vanilla) | Makeshift | table |
| Lead (2) | 3 Twine + slimeball | Makeshift | table |
| Leather Panel | 2 leather + (Bone Needle) | Makeshift | table |
| Leather helmet / chestplate / leggings / boots | 3 / 4 / 4 / 2 Leather Panels + Twine + (Bone Needle), shapeless | Makeshift | table |
| Leather horse armor | 4 Leather Panels + Twine + (Bone Needle) | Makeshift | table |
| Book | 3 paper + Leather Strip + Twine + (Bone Needle) | Makeshift | table |
| Brush, lightning rod, recovery compass | Vanilla recipes. Copper and redstone are Stone Table tier. The spyglass needs the Crafting Table, because amethyst counts as a gem | Stone | table |

Leather armor uses shapeless recipes because a chestplate's 8-slot shape leaves no room for the Twine and needle.

### Catalog: food

IRC doesn't change food. Every food recipe, including flour, bread, cake, cookies, pie, stews and seeds, belongs to [Impact's Realistic Food](https://claude.ai/artifact/H85Ku1ujPuD8QUSVhTsUAF). Without IRF, vanilla food recipes stay as they are, but they're still timed and locked to their table tier (golden foods need the Crafting Table). Sugar is the exception: IRC grinds it, because it also feeds potions.

### Catalog: textiles, decor and utility

Cloth branches from wool and Twine through sewing (the Bone Needle). Color always comes from the grinder, then the Dye Vat.

| Vanilla item | IRC recipe | Table | Finish |
| --- | --- | --- | --- |
| Bed | Bed Frame + Comforter + Sheet (see Beds) | Makeshift | placed |
| Carpet (3) | 2 wool + (shears) | Stone | table |
| Dyed wool, carpet, glass, panes, terracotta, concrete powder, candles, shulker boxes | Dip in the Dye Vat | — | Dye Vat |
| Banner | Banner Cloth (6 wool of one color + Twine + (Bone Needle)) + Trimmed Stick. Patterns stay at the loom | Makeshift | table |
| Candle | Twine (the wick) + honeycomb | Makeshift | table |
| Paper (3) | 3 Paper Pulp | Makeshift | table |
| Dyes, bone meal, blaze powder | Ground (see Grinding) | grinder | ground |
| Torch, soul torch | Vanilla: stick + coal or charcoal (+ soul soil). Kept so early lighting stays easy | 2×2 | table |
| Wool | 2 String Clumps. A String Clump is 2 string side by side. Grid wool-from-string is off | Makeshift | table |
| End rod, ender chest, beacon, conduit, enchanting table and other endgame items | Vanilla recipes | Crafting | table |

## New items, blocks, tags and recipe types

IRC adds about 120 items (counting 16 sheet colors and the tool heads), 5 blocks, 6 tags and 4 recipe types. The table below lists the blocks and core items. Every shared part is listed in the Parts and tools library.

**Items**

| ID | Stack | What it does | How you get it |
| --- | --- | --- | --- |
| loose\_pebble (block) | — | Natural pebble cluster on exposed stone. Breaks instantly by hand into 1 Stone Pebble | Worldgen only |
| makeshift\_table (block) | 64 | 3×3 crafting, non-metal recipes only | 4 planks in the 2×2 grid |
| makeshift\_stone\_table (block) | 64 | 3×3 crafting, non-metal and iron recipes | 2 flint + 4 smooth stone |
| dye\_vat (block) | 64 | Dyeing, washing, paper pulp | Wooden Bucket + 2 Twine |
| bed\_frame (block) | 1 | First layer of a bed | Wooden Frame + 3 Trimmed Planks |
| stone\_pebble | 64 unused, 1 once worn | Trimming tool and early hammer. 32 uses | Loose pebbles, gravel (25%), stone (15%). See Getting Stone Pebbles |
| wooden\_bucket | 16 unused, 1 once worn | Empty Wooden Bucket. Scoops water; a crafting part while unused. 32 uses | 5 Trimmed Planks |
| wooden\_water\_bucket | 1 | Water-filled Wooden Bucket. Pours water and fills the Dye Vat. Other Impact mods add actions (see Lib's shared interactions) | Scoop water with a wooden\_bucket |
| flint\_chisel, iron\_chisel | 1 | Stone cutting. 48 / 192 uses | Flint or iron ingot + Trimmed Stick |
| bone\_needle | 1 | Sewing. 64 uses | Bone + a trimming tool |
| iron\_saw, iron\_hammer, iron\_tweezers | 1 | Iron tools. 128 uses each | Makeshift Stone Table |
| impactslib:wooden\_grinder\_bowl, impactslib:stone\_grinder\_bowl | 1 | Lib's off-hand grinder. 64 / 256 uses. IRC ships the recipes | See Grinding |
| impactslib:wooden\_grinding\_stick, impactslib:stone\_grinding\_stick | 1 | Lib's main-hand grinder. 64 / 256 uses. IRC ships the recipes | See Grinding |
| comforter, {color}\_sheet | 16 | Bed layers 2 and 3 | Wool + Twine + Bone Needle |
| wooden\_{tool}*head, stone*{tool}\_head | 16 | Shaped tool heads | Trimmed Planks or Shaped Stone in the head pattern |
| loose\_wooden\_{tool}, loose\_stone\_{tool} | 1 | Unfinished tools. Can't mine or attack, can't be enchanted | Head + Trimmed Sticks |
| Other loose items: loose\_boat, loose\_bucket, loose\_minecart, loose\_piston, loose\_shield, loose\_iron\_door, and so on | 1 | Unfinished, needs hammering | See the catalog rows marked "hammer" |
| wood\_scraps | 64 | Furnace fuel (100 ticks), compostable (30%) | Byproduct of every wood trimming or cutting recipe |
| string\_clump | 64 | Wool ingredient (2 clumps → 1 wool) | 2 string side by side |

`{tool}` = pickaxe, axe, shovel, hoe, sword. The sword's part is called a blade (`wooden_sword_blade`), not a head.

**Head patterns** (based on the vanilla tool's top part)

| Tool | Pattern (P = trimmed part) |
| --- | --- |
| Pickaxe | `PPP` top row |
| Axe | `PP` / `P_` (top-left 2×2, minus the bottom-right) |
| Shovel | `P` single, top middle |
| Hoe | `PP` top row, left two |
| Sword blade | `P` / `P` vertical, 2 high |

The shovel and sword patterns would clash with other recipes as-is. Use shapeless "part + pebble" recipes for those two if they do.

**Tags**

- `impactscrafting:hammers`: items that can finish loose items. Holds stone\_pebble and iron\_hammer, plus SRF's hammer as an optional entry (`"required": false`).
- `impactscrafting:trimming_tools`: items that can trim. Holds stone\_pebble, minecraft:flint and iron\_saw, plus any SRF pebble-like item as an optional entry.
- `impactscrafting:loose_tools`: every loose item. Used for tooltips and JEI.
- `impactscrafting:tier/stone` and `impactscrafting:tier/advanced`: decide which table a recipe needs (see Crafting stations).
- `impactscrafting:iron_plates` and `impactscrafting:iron_rods`: IRC's Iron Plate and Iron Rod, plus SRF's iron sheet or plate and rod or bar items as optional entries, if the decompiled source shows SRF has them. Every IRC recipe that takes an Iron Plate or Iron Rod uses these tags, and so do other Impact mods' IRC-version recipes. Both tags are inside tier/stone.
- `impactscrafting:base_materials`: items that count as depth 0 for timed crafting even though a recipe can make them: logs, cobblestone, stone, string, wool, leather, ingots, gems and raw ores. Datagen fills it from vanilla items and `forge:` tags. See Timed crafting.
- `impactscrafting:vat_dyeable`: extra items the Dye Vat colors through `display.color` NBT, for modded gear that doesn't implement `DyeableLeatherItem`.
- `impactscrafting:pebble_base`: blocks that loose pebbles can generate on, and that drop pebbles when broken (minus calcite and mossy cobblestone, which only host worldgen).

**Recipe types**

- `impactslib:tool_crafting`, defined by Lib: a crafting-grid recipe, shaped or shapeless, whose tools stay in the grid and take damage (fields in Lib's spec). Trimming, cutting, sewing and the Crafting Table recipe all use it. An optional craft\_ticks overrides the timer.
- `impactscrafting:hammering`: an in-world recipe. Fields: input (loose item), hammer (tag), result, use\_ticks (default 40), hammer\_damage (default 1). Other Impact mods may ship hammering recipes for their own loose items, gated by `forge:mod_loaded impactscrafting` (IRV's loose Air Tank).
- `impactslib:grinding`, defined by Lib: see Grinding.
- `impactscrafting:vat_dyeing`: see Dye Vat.

Bed assembly is block logic, not a recipe type.

## Mechanics

**Trimming**

- Trimming and cutting use Lib's `tool_crafting` type. Tools aren't used up: they come back with the recipe's damage added. A tool that reaches 0 durability breaks and plays the item-break sound.
- Shift-clicking repeats the craft until the inputs run out or the tool breaks, with the timer running before each one (see Timed crafting).
- **Wood Scraps:** every recipe that trims or cuts wood also gives 1 Wood Scrap through Lib's Give or drop: into the inventory, or at the player's feet when there's no room (with IRI, locked slots never count as room). That covers log → planks, planks → Trimmed Planks, sticks, Trimmed Sticks, Fence Posts, bowls, Bow Staves, and wooden slabs, stairs and buttons. Lib's `byproducts` field does this.
- Wood Scraps stack to 64. They burn as furnace fuel for 100 ticks (half an item) and fill a composter with a 30% chance. Config: woodScrapsPerCraft 1.

**Timed crafting (every grid, including the 2×2)**

- Every crafting grid is timed: the 2×2 inventory grid, both makeshift tables and the Crafting Table. That covers every crafting recipe from every mod: IRC's, vanilla's, IRF's, IRV's, IRI's, SRF's and any other.
- **Time follows the tree.** Base materials are depth 0. A recipe's depth is 1 + the depth of its deepest ingredient. An ingredient that accepts several items (a tag like `#minecraft:planks`) takes its shallowest item. Tools in the grid don't count: Lib tools, and any ingredient whose items all come back from the craft (`getCraftingRemainingItem` returns the same item). Craft time = 10 ticks × depth, capped at 100 ticks (5 seconds).
- **Depth 0:** anything in `#impactscrafting:base_materials`, anything a smelting, blasting, smoking or campfire recipe makes, and anything no crafting or hammering recipe makes. So logs, cobblestone, string, wool and iron ingots are depth 0, even though IRC can craft cobblestone from pebbles and wool from String Clumps. A hammering recipe passes its loose item's depth on to its result, so a bucket is as deep as its loose bucket. Stonecutting and smithing are ignored.
- **Built at runtime, not by datagen.** IRC builds the depth table from the live recipe manager on `ServerStartedEvent` and after every `/reload` (`OnDatapackSyncEvent`), so every installed mod's and data pack's recipes get a time. It solves the table as a fixpoint: base materials start at 0 and every other item at "unknown". Each recipe whose ingredients are all known proposes 1 + its deepest ingredient for its output, and an item keeps its smallest proposal. This repeats until nothing changes. Loops (ingot ↔ block, nuggets ↔ ingot, wheat ↔ hay bale) settle on the shortest real path, because a loop can never lower a depth that's already known. The table lives on the server; the client gets each running craft's total ticks with the progress.
- **Fallback:** recipes the table can't place (special recipes with no ingredient list, or ingredients stuck in a loop with no base material) take `timedCrafting.fallbackTicks` (20). A recipe's `craft_ticks` field always wins.
- The tree check (a GameTest) uses the same depth code as the game, so the two never disagree.
- **Examples:** planks (depth 1) take 10 ticks; Trimmed Planks (2) take 20; a Wooden Bucket (3) takes 30; a chest (4) takes 40; a dispenser (dropper at 3, bow at 5, so 6) takes 60.
- **Progress arrow:** while a craft runs, the arrow between the grid and the output fills from left to right, like a furnace's smelting arrow. The output item shows faded and can't be taken until the arrow is full. IRC writes the progress to Lib's `CraftingProgress`, and Lib's renderer draws it. IRC hooks the renderer into vanilla's CraftingScreen and InventoryScreen and the makeshift tables' screens, and IRI's screen calls the same renderer, so the arrow shows in either.
- The craft resets if the grid changes or the menu closes. Shift-click crafts one item at a time, with the timer running before each.
- **Built with:** mixins on CraftingMenu, InventoryMenu and ResultSlot (mayPickup and shift-click on the result refuse until done) and two synced data slots (elapsed and total ticks). There are no screen mixins: the arrow comes from Lib's renderer through `ScreenEvent.Render.Post`. The two makeshift tables open a TieredCraftingMenu (a CraftingMenu subclass with a tier, wear and its own stillValid). The tier check runs in slotsChanged and leaves the output empty when the recipe's tier is too high.
- **Config:** timedCrafting.enabled true, timedCrafting.ticksPerDepth 10, timedCrafting.maxTicks 100, timedCrafting.fallbackTicks 20.

**Hammering**

- A loose item in the main hand plus an item from `#impactscrafting:hammers` in the off hand turns right-click into a hold-to-use action through Lib's framework (`UseAnim.NONE` with Lib's arm animation), held for use\_ticks. With IRI, the off hand is the inactive hand slot, so nothing changes.
- Every 10 ticks: swing the off hand, play a wood-knock sound (a stone-click sound for stone tools), and spawn a few block particles.
- When it finishes (server-side): replace the main-hand stack with the result, damage the hammer, and play an anvil-land sound at low pitch.
- Letting go early, or switching hand slots, cancels it, and you lose all progress. It works anywhere, so no block or anvil is needed.
- Without a hammer in the off hand, a loose item does nothing on right-click. Its tooltip shows the hint: "Hammer with a pebble in your off hand."
- Hammering recipes from other Impact mods (IRV's loose Air Tank) work exactly the same way.

**Loose items**

- Stack size 1, no tool tier. Mining speed and damage match an empty hand. They can't be enchanted, repaired or used in an anvil.

**Fuel**

Every wooden or wool IRC item burns in a furnace, smoker or blast furnace, and hoppers can feed it in. Vanilla items that IRC crafts are the real vanilla items, so they keep vanilla burn times automatically. IRC's own items burn roughly in proportion to the wood or wool inside them. 200 ticks smelts 1 item.

| IRC item | Burn time (ticks) | Items smelted |
| --- | --- | --- |
| Wood Scraps, Trimmed Sticks, Wooden Grinding Stick | 100 | 0.5 |
| Fence Post | 150 | 0.75 |
| Wooden tool heads, loose wooden tools, Bow Stave, Oar, Wooden Grinder Bowl | 200 | 1 |
| Trimmed Planks, Wooden Panel, Makeshift Table, Comforter, Sheets, Banner Cloth | 300 | 1.5 |
| Wooden Frame | 400 | 2 |
| Chest Top, Wooden Bucket (empty), Bed Frame, Dye Vat (empty) | 600 | 3 |
| Boat Hull | 900 | 4.5 |

- **Never burn:** anything made from crimson or warped wood (vanilla's `minecraft:non_flammable_wood` rule), a Water-filled Wooden Bucket, a Dye Vat holding water, and all stone, iron, string, leather, paper and food parts. Twine and String Clumps don't burn, because string doesn't in vanilla. Nether woods give no Wood Scraps when trimmed.
- **Implementation:** burn times go through Lib's FuelHelper: each IRC item class overrides `getBurnTime(ItemStack, RecipeType)` and calls it. For per-wood items it reads the wood NBT and returns 0 when that wood is in `#minecraft:non_flammable_wood`. Don't add IRC parts to vanilla tags like `minecraft:planks` to make them burn; that would also let them into vanilla recipes.
- **Composting:** register Wood Scraps (30%) through Lib's compost helper during common setup.
- **Config:** fuel.enabled true. Per-item overrides go in Lib's shared `fuel.overrides` list (item ID = ticks).

**Item data (NBT) rules**

IRC items carry NBT for wood type and wear. That NBT has to be exact, or items won't stack, match recipes or act like their vanilla versions.

1. **Vanilla outputs are clean.** Every vanilla item IRC crafts (an oak door, a chest, a red bed) comes out with no IRC NBT at all: no wood, uses or craft data. It has to be identical to the same item found in a village chest: same stacking, tags, burn time, compost value and use in other mods' recipes. Test this with `ItemStack.isSameItemSameTags` against a fresh vanilla stack.
2. **Wood type format.** Per-wood parts (Trimmed Planks, Wooden Panel, Wooden Frame, Fence Post, Boat Hull) store `{"wood": "minecraft:oak"}`, using the namespaced WoodType name. That covers oak, spruce, birch, jungle, acacia, dark\_oak, mangrove, cherry, bamboo, crimson and warped, and modded wood types follow the same pattern. The output resolves to the matching vanilla item (`minecraft:<wood>_door`). Bamboo's odd names are mapped by hand: bamboo\_raft, bamboo\_chest\_raft, bamboo\_planks.
3. **Recipes match on wood.** Datagen writes one recipe per wood type using Forge's `forge:partial_nbt` ingredient, so a birch panel can never make an oak door. Mixed woods in one craft are refused.
4. **Unused means no tag.** Pebbles, Wooden Buckets and makeshift tables have no uses tag at all when unused (not uses: 0). That way fresh drops, crafted items and picked-up tables all stack together. The tag only appears after first use.
5. **Buckets keep their wear.** Scooping turns a `wooden_bucket` into a `wooden_water_bucket` and copies its whole tag. Emptying it (pouring, filling the Dye Vat, or another mod emptying it) gives back a `wooden_bucket` with only the uses tag, so other mods' water data, like IRV's purity, leaves with the water. After a scoop, IRC posts Lib's water-filled event, so other mods can add their own namespaced tags.
6. **Other mods' tags.** IRC never removes or rewrites NBT keys it doesn't own, except when a bucket is emptied as above. Keys from other Impact mods are namespaced (`impactsvitality:purity`).
7. **Colors are separate items, not NBT.** The 16 Sheets are 16 registered items (`impactscrafting:red_sheet`), just like vanilla wool, so they work with tags, JEI and sorting mods without NBT checks.
8. **Display.** Per-wood items need one creative-tab entry per wood, a JEI subtype interpreter on wood (so each wood shows separately), a translated name with the wood as an argument ("Oak Wooden Panel"), and a model override keyed on wood for the right texture.
9. **Fuel, compost and tags read the NBT,** as described under Fuel. Nothing should treat a crimson panel like an oak one.

**Vanilla recipe changes**

- Every catalog recipe replaces its vanilla recipe. The vanilla recipe is disabled with a data-pack override that adds a false `forge:conditions`, and there's one config toggle per catalog category. The toggles live in the COMMON config and are read by a recipe condition, `impactscrafting:overhaul_enabled`, because recipe conditions are evaluated before a world's server config loads.
- Recipes on the untouched list keep their vanilla form, but they're still timed and locked to their table tier.
- Food recipes belong to IRF. IRC never overrides them, and the recipe audit labels them "food (IRF)". Without IRF they stay vanilla, timed and tiered like any other recipe.
- Vanilla sticks stay as an item, but now need a trimming tool. Torches keep their vanilla recipe so early lighting stays easy.

**Player feedback**

- Tooltips on every IRC item that name its next step.
- An achievement tab, "Realistic Crafting" (see Achievements).
- Translations go in `en_us.json` only for v1.

## Achievements

IRC's advancement tab, "Realistic Crafting", has 26 achievements that follow the progression: pebble → Makeshift Table → stone tools → Makeshift Stone Table → iron tools → Crafting Table, with side branches for containers, dyes, beds and redstone. **Goals** mark big steps; **challenges** are long grinds and give 50 XP.

| Achievement | How to get it | Unlocks after | Type |
| --- | --- | --- | --- |
| **Humble Beginnings** (tab root, Stone Pebble icon) | Pick up a Stone Pebble | — | task |
| Rock Solid | Craft 9 pebbles into cobblestone | Humble Beginnings | task |
| Cut to Size | Trim planks into Trimmed Planks | Humble Beginnings | task |
| Scrap Collector | Collect 64 Wood Scraps | Cut to Size | task |
| Makeshift Mindset | Place a Makeshift Table | Humble Beginnings | task |
| Patience Is a Virtue | Wait out a full 5-second craft | Makeshift Mindset | task |
| Falling Apart | Use a makeshift table until it breaks | Makeshift Mindset | task |
| Loose Ends | Craft a loose tool | Makeshift Mindset | task |
| Hammer Time | Hammer a loose tool into a finished tool | Loose Ends | task |
| Stone Age | Finish a stone tool | Hammer Time | task |
| Hinged | Craft a Stone Hinge | Rock Solid | task |
| Bucket List | Craft a Wooden Bucket | Cut to Size | task |
| Storage Solution | Craft a chest from a Wooden Bucket, a Chest Top and Stone Hinges | Bucket List | task |
| Set in Stone | Place a Makeshift Stone Table | Stone Age | goal |
| Tools of the Trade | Own an Iron Saw, Iron Hammer and Iron Tweezers at once | Set in Stone | task |
| The Real Deal | Craft a Crafting Table | Tools of the Trade | goal |
| Grind Set | Grind your first dye | Cut to Size | task |
| Color Theory | Mix two dyes in a grinder | Grind Set | task |
| Dip It | Dye an item in a Dye Vat | Grind Set | task |
| Sew What? | Craft something with a Bone Needle | Humble Beginnings | task |
| Making the Bed | Build a bed from frame, comforter and sheet | Sew What? | task |
| Full Spectrum | Make all 16 sheet colors | Making the Bed | challenge |
| Mechanically Inclined | Craft a Redstone Mechanism | Tools of the Trade | task |
| Pushing It | Hammer a loose piston into a piston | Mechanically Inclined | task |
| Every Branch | Craft every shared part in the Parts and tools library | The Real Deal | challenge |
| Master Craftsman | Craft 1,000 items with IRC recipes | The Real Deal | challenge |

**Implementation**

- All advancements come from datagen. Vanilla triggers cover most (`inventory_changed`, `placed_block`, `recipe_crafted`).
- Shared triggers from Impact's Lib: `impactslib:tool_crafted` (trimming, sewing), `impactslib:hold_action` (hammering a specific loose item, grinding), `impactslib:count` (Wood Scraps, Master Craftsman).
- IRC triggers: `impactscrafting:table_broken`, `impactscrafting:long_craft` (a craft of 100 ticks or more), `impactscrafting:dyes_mixed`, `impactscrafting:vat_dyed`, `impactscrafting:bed_assembled` (with the sheet color, for Full Spectrum).
- Every Branch checks a list generated from the Parts and tools library, so adding a part updates the achievement automatically.

## Scope and Salty's Realistic Forging compatibility

IRC owns crafting tables and all crafting except metal gear. SRF owns ores and metal gear. IRC only uses the iron ingots that SRF (or vanilla) produces, and the two mods never edit the same recipe.

| Area | Handled by | Milestone |
| --- | --- | --- |
| Shared systems, logging and providers | Impact's Lib | Before milestone 1 |
| Pebbles, tool crafting, hammering, timed crafting | IRC | 1–5 |
| Makeshift Table and the tier system | IRC | 2 |
| Wooden and stone tools | IRC | 4 |
| Wood, stone, brick and glass catalog | IRC | 6 |
| Makeshift Stone Table, iron tools, iron goods, Crafting Table | IRC | 7 |
| Grinder recipes and dyes (the grinder items and type are Lib's) | IRC | 8 |
| Dye Vat | IRC | 9 |
| Beds | IRC | 10 |
| Redstone and transport catalog | IRC | 11 |
| Combat, leather and textiles catalog | IRC | 12 |
| Food | IRF | never |
| Hotbar, pockets and packs | IRI | never |
| Thirst, temperature and air | IRV | never |
| Ore processing and smelting | SRF | never |
| Iron, gold, diamond and netherite tools and armor | SRF (full version) | never |
| Metal tools when the player runs SRF's "no tools" version | IRC (optional module) | after 14, off by default |

**Compatibility rules**

- Never override or remove any recipe in SRF's namespace.
- **Use the decompiled source.** Claude Code gets a decompiled copy of SRF 4.1.6 (1.20.1 Forge). Keep it outside the IRC repo, never copy its code or assets, and follow the checklist in Lib's spec (Family compatibility and testing).
- From the source, record SRF's mod ID, its hammer and tongs, any iron sheet, plate, rod or bar items, any pebble-like item, and every crafting recipe whose output IRC also makes. Write them to `docs/srf-notes.md` in the IRC repo.
- Put SRF items in IRC tags only as `"required": false` entries: its hammer in `#impactscrafting:hammers`, a pebble-like item in `#impactscrafting:hammers` and `#impactscrafting:trimming_tools`, its iron sheets and rods in `#impactscrafting:iron_plates` and `#impactscrafting:iron_rods`, and its iron and copper items in `#impactscrafting:tier/stone`.
- Read SRF's right-click procedures. If SRF acts on an item that IRC also gives a right-click action (a loose item, or a pebble or hammer in the off hand), make sure both still work, and add a GameTest for each case.
- Recipe clashes get fixed by changing IRC's pattern, and Lib's conflict checker must pass with SRF installed. Polymorph is the fallback SRF itself recommends.
- Test with both SRF versions (full and "no tools") plus JEI, alone and in Lib's family test setups. At startup, IRC logs what it found under its `compat` category.

## Technical architecture

Standard Forge 1.20.1 MDK project, data-generated wherever possible.

- **Build:** ForgeGradle 6, Forge 47.x, Parchment mappings, Mixin, Java 17. Impact's Lib is a required dependency (`implementation fg.deobf("com.impact:impactslib:<version>")`). Optional dependency on JEI 15.x for 1.20.1.
- **Packages:** `com.impact.realisticcrafting` → registry, block (LoosePebbleBlock, MakeshiftTableBlock, MakeshiftStoneTableBlock, DyeVatBlock + block entity, BedFrameBlock), menu (TieredCraftingMenu, CraftingTier), recipe (HammeringRecipe, VatDyeingRecipe, TierResolver), timing (CraftDepthTable, its rebuild hooks, the progress writer), item (LooseItem, PebbleItem, CraftingToolItem, WoodenBucketItem, WoodenWaterBucketItem, SheetItem), worldgen (LoosePebbleFeature, placed features, biome modifier), loot (pebble drops from gravel and stone, bed parts), event (hammering, bed layering, bucket use), mixin (crafting timer, vanilla table and 2×2 tier check), compat (JEI, SRF), debug (log categories), config, datagen. Tool crafting, grinding and the grinder items come from Lib.
- **Datagen:** there are hundreds of recipes, so every recipe, model, blockstate, loot table, tag, lang entry, advancement, worldgen JSON and vanilla override must be generated. Run `./gradlew runData`. Don't hand-edit the generated JSON. Write the catalog as one data class per category, so each catalog table in this spec maps to one file.
- **Recipe audit:** a datagen step lists every vanilla crafting recipe and labels it as replaced, gated, untouched or food (IRF). It writes recipe\_audit.md and fails the build if any recipe is unlabeled. Lib's conflict checker runs right after it.
- **Tree check:** a GameTest that fails if a catalog recipe uses a part that isn't in the Parts and tools library, if a library part has no use, or if any IRC recipe ends up on the fallback time. It uses the same depth code as the game.
- **Config:** `impactscrafting-server.toml` (gameplay, per world, synced to clients): durabilities (pebble 32, iron tools 128, needle 64, chisels 48/192), the pebbles.\* keys (see Getting Stone Pebbles), hammerUseTicks 40, ironHammerUseTicks 20, vatChargesPerDye 8, timedCrafting.\* (enabled true, ticksPerDepth 10, maxTicks 100, fallbackTicks 20), makeshiftTableUses 128, makeshiftStoneTableUses 256, woodenBucketUses 32, woodScrapsPerCraft 1, tableTiers.enabled true, bedsDropParts true. `impactscrafting-common.toml`: one recipe toggle per catalog category (overhaul.wood, overhaul.stone, overhaul.iron, overhaul.redstone, overhaul.combat, overhaul.textiles, overhaul.dyes, overhaul.beds) and debug.\* (see Debug logging). Grinder durability and speed are in Lib's config.
- **JEI:** IRC adds two categories of its own (Hammering, Dye Vat) to Lib's shared Tool crafting and Grinding categories, plus info pages for bed layering and for where to find pebbles. Every recipe shows which table it needs and its craft time. JEI's "uses" view makes the tree browsable, so each part's uses should be complete.
- **Art:** 16×16 placeholders for v1. Give each part family a consistent look (all iron parts share a palette, all stone parts share a texture), so the tree reads visually too.
- **Tests:** Forge GameTests for pebble drops, tool damage, hammering, the timer reset, depth times (planks 10, chest 40, and a loop like iron ingot ↔ iron block), tier refusal, grinding, dye mixing in the vat, bucket wear across a scoop and a pour, and bed layering. They run in every family test setup that includes IRC. Also check in runClient that loose pebbles show up on a mountain, a stony shore and a cave floor in a new world.

**Extended configuration** (Lib's framework; files under `config/impactslib/` and `serverconfig/impactslib/`, see Lib's Configuration). The Config bullet above lists the main keys, but every number and feature in this spec is configurable. `impactscrafting-server.toml` also holds: timed crafting per grid (2×2, makeshift tables, Crafting Table), `timedCrafting.recipeOverrides` (`recipe=ticks`) and `timedCrafting.modMultipliers` (`modid=1.0`); tier checks per table, with `tiers.extraStone` and `tiers.extraAdvanced` item lists; every tool's durability and damage per use; hammering ticks per hammer; pebble drop chances from gravel and stone, the hand-break drop, and worldgen frequency, dimensions and a biome blacklist; Dye Vat charges per dye (8), capacity (32), dip ticks (10), rain fill rate and Paper Pulp ticks; Wood Scraps per craft, burn time and compost chance; table wear stages; and bed parts dropping. `impactscrafting-common.toml` holds one recipe toggle per catalog category, `recipes.disabled` (IRC recipe IDs to drop), `recipes.keepVanilla` (vanilla recipe IDs IRC leaves alone), feature toggles (pebble worldgen, hammering, timed crafting, table tiers, Dye Vat, beds) and debug.\*. `impactscrafting-client.toml` holds the progress arrow's style, hammering particles and sounds, and the next-step tooltip hints.

**Debug logging.** IRC logs through Lib's `ImpactsLog` (see Lib's Debug logging). It's on by default (`debug.enabled = true` in `impactscrafting-common.toml`), and every line starts with `[Impact/crafting/<category>]`. The categories:

- `startup`: version, Lib version, which Impact mods and SRF were found, active catalog toggles, tier tag sizes.
- `compat`: each SRF optional entry, resolved or missing.
- `depth`: the depth table after every rebuild (recipe count, deepest recipe, recipes on the fallback and why, time taken). With `debug.verbose`, one line per recipe.
- `timer`: crafts started (player, grid, recipe ID, depth, ticks), finished, or reset (grid changed or menu closed).
- `tier`: refused recipes (recipe ID, table tier, needed tier, the ingredient that raised it).
- `wear`: table uses left, wear stage changes and tables breaking; tool damage and breaks.
- `hammer`: hammering started, cancelled (ticks done) or finished (input, result, hammer, uses left).
- `pebbles`: pebbles dropped from gravel and stone (block, tool). With `debug.verbose`, every chance roll and worldgen placement.
- `bucket`: scoops and pours (uses left) and the water-filled event being posted.
- `vat`: water and dye added, colors mixed or refused, each dip (input, result, charges left), washing and Paper Pulp.
- `beds`: each layer placed or refused (and why), beds finished (color), parts dropped.
- `byproducts`: Wood Scraps given or dropped, through Lib's Give or drop.
- `audit`: replaced, gated, untouched and food recipe counts, and the conflict-check result.
- `/impacts dump held` adds a held item's recipe depth, tier and craft time; `/impacts dump block` adds a table's wear or a vat's contents.

## Milestones, acceptance and handoff

Build in this order. Each milestone should compile, run and be playable before the next one starts.

**Before milestone 1:** build Impact's Lib through its own milestones and publish it to mavenLocal. IRC compiles against it from day one and never duplicates a Lib system.

1. **Skeleton:** MDK set up, mod loads with Lib, config files are created, IRC's debug categories are registered, stone\_pebble exists, and all three pebble sources work (loose pebbles in worldgen, gravel, stone).
2. **Makeshift Table and tiers:** the block, TieredCraftingMenu, tier tags and TierResolver. The 2×2 grid follows the same rules. The makeshift tables wear out like an anvil.
3. **Tool crafting and starting parts:** IRC's recipes on Lib's tool\_crafting type; Trimmed Planks, Trimmed Sticks, Shaped Stone, Stone Hinge, Twine, Bone Needle, Flint Chisel.
4. **Tools:** hammering, then the wooden pickaxe end-to-end, then all 10 wooden and stone tools.
5. **Timed crafting** in every grid, including the 2×2: the runtime depth table, the fallback, and the filling arrow through Lib.
6. **Wood and stone branches:** Wooden Panel, Wooden Frame, both Wooden Bucket items, Chest Top, Fence Post, Stone Casing, Stone Base, Mortar, and every non-iron row of the wood and stone catalogs.
7. **Iron branch:** Makeshift Stone Table, iron tools, Iron Plate, Rod, Rivets, Hinge and Wheel (with the iron\_plates and iron\_rods tags), the Crafting Table recipe, and the plain iron goods.
8. **Grinder recipes:** IRC's grinder-item recipes and every dye, powder and dye-mix recipe, on Lib's grinding type.
9. **Dye Vat:** the block, water from the Wooden Bucket, dyeing (including every leather-style item), mixing, washing, Paper Pulp.
10. **Beds:** Bed Frame, Comforter, Sheets, layering, drops, recoloring.
11. **Redstone and transport:** Redstone Mechanism, Piston Head, the rest of the catalog rows.
12. **Combat, leather, textiles:** the remaining catalogs.
13. **Polish:** JEI categories, tooltips that name the next step and the parent part, an advancement tree shaped like the crafting tree, lang, sounds, particles.
14. **Audit and SRF pass:** a clean recipe audit and conflict check, the decompiled-SRF review and `docs/srf-notes.md` (see Compatibility), optional tag entries, and the family test setups that include IRC, with both SRF versions.

**Acceptance criteria**

- [ ] A new survival world can reach a working wooden pickaxe using only IRC steps, starting from punching trees and picking up loose pebbles.
- [ ] A new world can go from Makeshift Table → Makeshift Stone Table → iron tools → Crafting Table with no vanilla shortcuts.
- [ ] Unused pebbles stack to 64; a pebble used once stacks to 1. Breaking stone by hand always drops a pebble.
- [ ] The recipe audit labels every vanilla crafting recipe, and the tree check and Lib's conflict checker pass.
- [ ] The Makeshift Table refuses every recipe with iron, copper, redstone, gold, gems, raw ores or dusts. The Stone Table accepts iron, copper and redstone but refuses quartz, gold and diamond.
- [ ] A Makeshift Table breaks after 128 crafts and shows three wear stages along the way. Picking it up keeps its count.
- [ ] Every grid is timed, including the 2×2. Planks take 10 ticks and a chest takes 40, and the arrow fills like a furnace's while it runs.
- [ ] Recipes from other mods get times from the live recipe graph: IRF's Dough is timed by its depth, a recipe added by a data pack and `/reload` gets a time without a restart, and loops like iron ingot ↔ iron block don't hang or fall back.
- [ ] The crafting timer resets when the grid changes, and items can't be duplicated by shift-clicking or closing the menu.
- [ ] A chest can only be made from a Wooden Bucket, a Chest Top and 2 Stone Hinges, and it needs no hammering.
- [ ] A Wooden Bucket keeps its uses through scooping and pouring, and only an unused empty bucket works as a crafting part.
- [ ] Trimming planks gives a Wood Scrap, which burns in a furnace and goes in a composter.
- [ ] 2 String Clumps make 1 wool; the grid wool-from-string recipe is off.
- [ ] Grinding: a wooden bowl refuses bone; a poppy grinds to red dye; red + yellow in the bowl grinds to 2 orange.
- [ ] Dye Vat: 1 dye colors 8 wool; adding red to a yellow vat makes it orange; leather armor and IRI's packs dye, and plain water washes them.
- [ ] Beds: frame → comforter → sheet becomes the bed in the sheet's color; a sheet on a bare frame is refused; breaking a finished bed drops all three parts.
- [ ] Loose items can't be used until hammered, and letting go early gives nothing and costs no durability.
- [ ] The game runs with SRF (full and no-tools), JEI and IRC together, and Lib's conflict checker finds no clashes.
- [ ] With IRI installed, the 2×2 grid is still timed and tier-checked, the arrow fills in IRI's screen, and byproducts that don't fit drop at the player's feet.
- [ ] IRC runs with only Lib installed, and passes every family test setup that includes it.
- [ ] A dedicated server starts without crashing on client-only classes.
- [ ] Every flammable IRC item burns for its listed time, including fed in by a hopper. Crimson and warped parts, filled buckets and string parts don't burn.
- [ ] An oak door crafted through IRC stacks with an oak door from a village and is equal by `ItemStack.isSameItemSameTags`. The same holds for every catalog output.
- [ ] A birch panel can't make an oak door, and every per-wood part shows as its own entry in JEI and the creative tab.
- [ ] Fresh pebbles, Wooden Buckets and makeshift tables carry no uses tag and stack with each other.
- [ ] With default settings, every event under Debug logging writes its line. With `debug.enabled = false`, IRC logs only warnings and errors.

**Decisions (answered by Impact, Nov 14, 2026)**

- Pebbles stack to 64 while unused, and to 1 once worn. Breaking stone by hand always drops one. 9 pebbles still make 1 cobblestone.
- Redstone dust is Stone Table tier. Quartz, gold, glowstone and diamond need the Crafting Table.
- The Makeshift Stone Table accepts iron and copper. Both makeshift tables wear out like an anvil.
- Torches keep their vanilla recipe. String makes String Clumps, and String Clumps make wool.
- The untouched list is right. Shears, flint and steel and the anvil keep their vanilla recipes.
- Every grid is timed, including the 2×2. Time scales with depth in the tree, and the arrow fills like a furnace's.
- Multi-part items like the chest don't need hammering.
- Platform: Forge 1.20.1 only for now. A port comes later.
- Wooden Buckets have durability (32 uses).
- Wood trimming gives Wood Scraps (furnace fuel, compostable).
- Sticks: both regular sticks and Trimmed Sticks stay.
- Confirmed as written: the Dye Vat, Wooden Bucket water, loose pebbles can't be placed, sticks need a trimming tool, gold needs the Crafting Table, the durability and timing numbers, recipe shapes picked by Claude Code, bed sheets, and village beds dropping their parts.
- From the family review (Nov 14, 2026): tool crafting, grinding and the grinder items live in Lib. Craft times come from the live recipe graph at every reload, so every mod's recipes are timed. Empty and water-filled Wooden Buckets are two items. The Dye Vat dyes anything leather-style. Iron parts count as stone tier; armor and other mods' finished metal items don't.
- No open questions remain.

**Handoff notes for Claude Code**

- Build Impact's Lib first and publish it to mavenLocal. IRC depends on it.
- Export this doc as Markdown and save it as SPEC.md in the repo root. Point CLAUDE.md at it.
- Tell Claude Code: "Implement milestone N from SPEC.md, run ./gradlew build and runData, and stop for review." Go one milestone per session.
- The decompiled SRF source is reference only. Keep it outside the repo (for example `../reference/srf/`), read it for IDs, recipes and right-click handlers, never copy from it, and record what you find in `docs/srf-notes.md`.
- Debug logging is on by default. When something misbehaves, read `logs/impacts-debug.log` first. Every new feature adds its log lines in the same change.
- If the spec and Forge 1.20.1 APIs disagree, follow the API and note the change in SPEC.md.

**Sources:** [SRF on CurseForge](https://www.curseforge.com/minecraft/mc-mods/salty-s-realistic-forging) · [SRF on Modrinth](https://modrinth.com/project/7MUJfTPy)

## Implementation changes

Changes made during implementation, following the handoff rule "follow the API and note the change".

- **Platforms (decided Nov 14, 2026):** 1.20.1 Forge first, then NeoForge 1.20.1, then Forge and NeoForge up to 1.21. No Fabric. The mod is split into a `common` module (vanilla code) and a `forge` module, and a `neoforge` module gets added at port time.
- **Saved data:** wherever this spec says "capability", the data is stored with vanilla NBT (mixins on the owning entity or block entity, or `SavedData` for world data) in common code. NeoForge replaces capabilities in 1.20.5, so this keeps the later ports small. Behavior is unchanged: saved, synced and copied exactly as the spec describes.
- **Hand-broken stone (milestone 1):** Minecraft rolls no loot at all for stone broken without the right tool, so a global loot modifier can't see it. The always-one pebble comes from a block-break handler instead (Forge's `BlockEvent.BreakEvent`, lowest priority, skipped when cancelled or in creative). The 15% stone chance stays in the loot modifier and needs a tool, so explosions, which roll stone's loot with no tool, drop no pebble. Gravel's 25% applies to any loot roll without Silk Touch.
- **Worldgen (milestone 1):** each loose pebble feature makes its own tries across the chunk (the counts come from the server config), so its placed feature has no placement modifiers and the config applies without a data pack. Surface pebbles run in the `top_layer_modification` step, cave pebbles in `underground_decoration`. Two extended keys from "worldgen frequency, dimensions and a biome blacklist": `pebbles.worldgenDimensions` (default `minecraft:overworld`) and `pebbles.worldgenBiomeBlacklist` (biome IDs or `#tags`, default empty). The biome modifier still adds the features to Overworld biomes only, as the spec says, so the dimension list chooses among dimensions that use them.
- **Loose pebble block (milestone 1):** three cluster models (a few small stones each), each turned a random quarter, are generated by datagen. Waterlogged pebbles leave their water when they pop off.
- **Recipe switches (milestone 2):** `impactscrafting:overhaul_enabled` takes an optional `recipe` field. Crafting's own recipes carry their ID (so `recipes.disabled` drops them); each replaced vanilla recipe is vanilla's own file, copied by datagen into `data/minecraft/recipes/` under `forge:not` of the condition with the vanilla ID (so `recipes.keepVanilla` brings it back). The Makeshift Table recipe and the switched-off vanilla Crafting Table recipe belong to the `wood` category, the Makeshift Stone Table to `stone`. To keep the vanilla Crafting Table recipe, also list `impactscrafting:makeshift_table` in `recipes.disabled`, since both are 4 planks.
- **Tiers (milestone 2):** an ingredient that accepts several items takes the lowest tier among them (the cheapest way to fill it); the recipe takes the highest tier among its ingredients. The config's extra lists win over the tags. Tiers are worked out per recipe object, so a data pack reload starts over. The check sits where vanilla works out a crafting grid's result, so it covers the 2×2 grid, every table and other mods' menus that reuse vanilla's crafting code; menus Crafting doesn't know count as a Crafting Table. The refused result slot is tinted red and its tooltip names the station; the client works this out itself from its recipes, tags and the synced config.
- **Table config (milestone 2):** `makeshiftTableUses` and `makeshiftStoneTableUses` sit in a `tables` section with the wear stage edges (`tables.wornBelow` 2/3, `tables.fallingApartBelow` 1/3). `tableTiers.enabled` gets one switch per grid (`tableTiers.inventoryGrid`, `tableTiers.makeshiftTable`, `tableTiers.makeshiftStoneTable`), and `tiers.extraStone` and `tiers.extraAdvanced` take item IDs or `#tags`.
- **Makeshift tables (milestone 2):** the menu is vanilla's crafting menu (recipe book, shift-clicking and the screen all work as usual) under its own menu type. A table saves its uses spent as `uses`, like Lib's uses-tracked items, and its item shows uses left in the tooltip and a durability bar. When a table falls apart, the crafting player's grid items drop on the ground; anyone else using the same table gets their grid items back as the menu closes.

