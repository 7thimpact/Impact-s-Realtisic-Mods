# Salty's Realistic Forging: notes for Impact's Realistic Crafting

From the decompiled SRF 4.1.6 ("no tools"); shared facts are in `impacts-lib/docs/srf-notes.md`.

## Optional tag entries (all `"required": false`)

| Tag | SRF items |
| --- | --- |
| `#impactscrafting:hammers` | `smithing_hammer`, `smithing_stone_hammer`, `smithing_wood_hammer` |
| `#impactscrafting:trimming_tools` | none: SRF has no pebble-like item (`big_stone` is a placed block) |
| `#impactscrafting:iron_plates` | none: SRF has no iron sheet or plate |
| `#impactscrafting:iron_rods` | none: SRF has no iron rod or bar |
| `#impactscrafting:tier/stone` | iron and copper items: `raw_iron_ore`, `cleanedironore`, `hotrawironore`, `ironoredust`, `rawironnugget`, `hotrawironnugget`, `pickedhotrawiron`… `_5`, `stickhotrawiron`… `_5`, `pickedhotironnugget`, `stickhotironnugget`, `pickedironnugget`, `pickedironiingot`, `copperore`, `copperdust`, `panwithcopperore`, `panwithcopperdust`, `panwithmeltedcopper`, `pickedpanwithmeltedore`, `moldwithmeltedcopper`, `copperingotinmold`, plus its iron tools: `smithing_hammer`, `blacksmithstongs`, `carvinghammer`, `chisel`, `pan`, `iron_sledgehammer` |
| `#impactscrafting:tier/advanced` | gold, gem and netherite items: `rawgoldore`, `purerawgoldore`, `moldwithragoldore`, `moldwithmeltedgold`, `handlingmeltedgold`, `shapedgold`, `pickedshapedgold`, the diamond and emerald chain, the netherite chain, `diamond_sledge_hammerr` |

Adding SRF's gold and gem items to `tier/advanced` goes beyond the spec, which names only iron and copper. It follows the spec's rule that a recipe holding ores, "or anything made from them", needs the right table.

## SRF's crafting recipes (never overridden)

| Recipe | Shape | Clashes with Crafting? |
| --- | --- | --- |
| `pan` | `N_N` / `_I_` (nuggets over an ingot) | **Yes**: the spec's Iron Tweezers pattern is the same. Tweezers get a different pattern |
| `bigpan` | `N_N` / `III` | no |
| `blacksmithstongs` | `N_N` / `_I_` / `I_I` | no |
| `carvinghammer` | `IN` / `S_` | no (Iron Hammer is `II` / `_S`) |
| `chisel` (point chisel) | nugget and two ingots, diagonal | no (Iron Chisel is an ingot + a Trimmed Stick) |
| `smithing_hammer` / `smithing_stone_hammer` / `smithing_wood_hammer` | `MSM` / `_S_` (iron ingots, cobblestone or planks) | no |
| `smithing_anvil` | `IBI` / `_I_` | no |
| `two_sticks` | 2 sticks, shapeless | Crafting uses Trimmed Sticks; see Food's notes for the Spit |
| `woodpestle` | stick and planks, diagonal | no |
| `wheatbroom` | wheat over a stick | no |
| `sandpaper` | sand, paper, gravel | no |
| `barshapedmold` | 1 brick, shapeless | no: every Crafting brick recipe takes Mortar |
| `ironsledgehammerrecipe`, `sledgehammerrecipe` | broken (missing item) | no |

SRF makes none of the outputs Crafting makes, so there are no output clashes. Lib's conflict checker must pass with SRF installed.

## Right-click steps

SRF acts when the main hand holds one of its own items and the off hand holds a smithing hammer, a wheat broom or a pan. Crafting's hammering acts when the main hand holds a loose item and the off hand holds a hammer. They never overlap: loose items aren't SRF inputs, and SRF items aren't loose items. With an SRF smithing hammer in the off hand, Crafting's hammering works through the `hammers` tag.

**GameTest:** hammer a loose tool with an item from `#impactscrafting:hammers`. With SRF installed, repeat it with `realisticforgingores:smithing_hammer`, looked up by registry ID.

## Vanilla recipes SRF owns

SRF owns `minecraft:netherite_ingot` (disabled) and the ore smelting and blasting recipes. Crafting overrides none of them: netherite and smelting are on its untouched list.

## Metal tools without SRF's full version

The "no tools" version keeps vanilla's metal tool recipes, so Crafting's optional metal-tools module ("after milestone 14, off by default") stays out of v1.
