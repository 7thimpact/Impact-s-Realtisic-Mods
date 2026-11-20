# Salty's Realistic Forging: notes for Impact's Realistic Vitality

From the decompiled SRF 4.1.6 ("no tools"); shared facts are in `impacts-lib/docs/srf-notes.md`.

## Heat

- **Hot blocks:** none. SRF has no forge or furnace block, and its `smithing_anvil` isn't hot. `#impactslib:heat_sources` is a block tag, so SRF adds no entries to it.
- **Hot items, recorded in case held items ever give off heat (not in the spec):**
  - hot iron: `hotrawironore`, `hotrawironnugget`, `pickedhotrawiron`… `_5`, `stickhotrawiron`… `_5`, `pickedhotironnugget`, `stickhotironnugget`
  - melted metal: `moldwithmeltedgold`, `handlingmeltedgold`, `panwithmeltedcopper`, `pickedpanwithmeltedore`, `moldwithmeltedcopper`, `meltingscrapsinpan`, `meltedscraps`, `meltedscrapsandgold`, `meltedscrapsandgoldinmold`, `moldwithnetheriteingot`
- **Hand burns:** SRF already burns players who hold hot items without tongs, in its own `PlayerTickEvent` handlers.

## Armor

The "no tools" version adds no armor. Vanilla iron armor stays, and `#impactsvitality:conductive_armor` already holds it. If the full version adds iron armor, add those items to `#impactsvitality:conductive_armor` as optional entries.

## Water and bottles

SRF never uses glass bottles and never changes a cauldron's water level. Vitality replacing the glass-bottle entry in `CauldronInteraction.WATER` doesn't affect SRF, because SRF works through events, not `CauldronInteraction`.

## Checked (Vitality milestone 10)

- No heat entries are needed: SRF 4.1.6 ("no tools") has no hot blocks. If the full version adds a forge, add it to `#impactslib:heat_sources` as an optional entry.
- The `vitality-with-srf` setup checks that Vitality's cauldron entries leave SRF's raw gold ore alone and bottles still fill with SRF installed.
