# Salty's Realistic Forging: shared notes

Read from a decompiled copy of **SRF 4.1.6 for Forge 1.20.1, "no tools" version** (`realisticforgingores - no tools -4.1.6-forge-1.20.1.jar`). The copy is reference only: it is kept outside the repository, and nothing was copied from it. Each mod's own `docs/srf-notes.md` covers what concerns that mod.

## Identity and dependencies

- Mod ID `realisticforgingores`, version 4.1.6, made with MCreator.
- Optional dependency on playerAnimator (mod ID `playeranimator`). SRF's hammering code calls playerAnimator's classes directly on the client.
- Its own network channel carries animation messages.
- **What the "no tools" jar holds (checked Nov 2026, after the user pointed out it may be complete):** its item registry (`RealisticforgingoresModItems`) registers 70 items: the ore chain (raw ores, dusts, pans, molds, ingots in molds, gems) and the smithing tools (three smithing hammers, tongs, carving hammer, point chisel, sledgehammers). It registers none of the tool and armor forging items (sword blades, pickaxe, axe, shovel and hoe heads, helmet and chestplate parts, armor molds), even though their textures, procedures and two "craft iron sword" advancements are packed in the jar: they're leftovers from the developer's full build that the game never loads. Its only vanilla recipe overrides are the ore smelting and blasting recipes and `minecraft:netherite_ingot`; vanilla tool and armor recipes are untouched. Every "SRF" test setup uses this jar, and it covers everything this version does in game.

## Content

- **Items:**
  - iron chain: `raw_iron_ore`, `cleanedironore`, `hotrawironore`, `ironoredust`, `rawironnugget`, `hotrawironnugget`, `pickedhotrawiron`… `_5`, `stickhotrawiron`… `_5`, `pickedhotironnugget`, `stickhotironnugget`, `pickedironnugget`, `pickedironiingot`
  - gold chain: `rawgoldore`, `purerawgoldore`, `moldwithragoldore`, `moldwithmeltedgold`, `handlingmeltedgold`, `shapedgold`, `pickedshapedgold`, `barshapedmold`, `damagedmold`, `pickeddamagedmold`, `emptymold`
  - copper chain: `copperore`, `copperdust`, `pan`, `panwithcopperore`, `panwithcopperdust`, `panwithmeltedcopper`, `pickedpanwithmeltedore`, `moldwithmeltedcopper`, `copperingotinmold`
  - diamond and emerald chain: `roughdiamond`, `roughcutdiamond`, `moldeddiamond`, `moldeddiamond_2`, `roughemerald`, `cutroughemerald`, `notpolishedemerald`, `polishedemerald`
  - netherite chain: `bigpan`, `scrapsinpan`, `meltingscrapsinpan`, `meltedscraps`, `meltedscrapsandgold`, `meltedscrapsandgoldinmold`, `moldwithnetheriteingot`
  - tools: `smithing_hammer`, `smithing_stone_hammer`, `smithing_wood_hammer`, `blacksmithstongs`, `two_sticks`, `carvinghammer`, `chisel`, `sandpaper`, `wheatbroom`, `woodpestle`, `iron_sledgehammer`, `diamond_sledge_hammerr`
  - other: `pan_with_cookie`
- **Blocks:** `smithing_anvil`, `big_stone` (generated in the world, a crude anvil), `carveddiamondore`, `carveddiamondore_2`, `carveddiamonddeepslate`, `carveddiamonddeepslate_2`, `carvedemeraldblock`, `carvedemeraldblock_2`, `carvedemeralddeepslate`, `diamondonanvil`.
- **Recipes in its own namespace:** 20, listed in `impacts-crafting/docs/srf-notes.md`. Two of them (`ironsledgehammerrecipe`, `sledgehammerrecipe`) point at a missing item, `realisticforgingores:deleted_mod_element`, and fail to load. That error comes from SRF, not from Impact.
- **Vanilla recipes it overrides:**
  - `minecraft:netherite_ingot` (its result becomes air, so the recipe is disabled)
  - every vanilla smelting and blasting recipe that turns ore or raw metal into copper, gold, iron, diamond or emerald
- **Vanilla loot tables it replaces:** the copper, gold, iron, diamond and emerald ores (stone and deepslate), nether gold ore and ancient debris.
- **Tags it adds to:** `minecraft:pickaxes` (both sledgehammers), `minecraft:mineable/pickaxe` and `minecraft:needs_stone_tool` (its blocks).
- **Worldgen:** `big_stonefeature`, added through a Forge biome modifier.

## Recipe ownership for Lib's audit

Lib's recipe audit must record SRF as the owner of `minecraft:netherite_ingot` and of the ore smelting and blasting recipes above. No Impact mod may override them.

## How SRF uses playerAnimator

On the client, SRF registers a player animation layer through playerAnimator's animation data factory. It then plays keyframe animations from `assets/realisticforgingores/player_animation/*.json` on that layer, and sends a packet so other players see them. Lib can use the same public playerAnimator API with its own layer and its own animations, as an optional dependency.
