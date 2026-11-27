# Roadmap

Steps are ordered from least to most effort. The one exception is Impact's Lib, which comes first because every mod requires it. After the core, each Lib system arrives with the first mod that needs it (Lib's versioning lets minor versions add API).

**Platform plan:** every mod is built and established on Minecraft 1.20.1 Forge first (steps 1 to 7). Only after that do we cover Minecraft 1.20.1 through 1.21 on both Forge and NeoForge (steps 8 and 9). **There is no Fabric port.**

| # | Step | Effort | Status |
| --- | --- | --- | --- |
| 0 | Workspace setup | S | Done |
| 1 | Impact's Lib core | M | Done |
| 2 | Impact's Realistic Inventory (IRI) | M | Done, with Vitality and Crafting |
| 3 | Impact's Realistic Food (IRF), plus Lib's food systems | L | Done, with Vitality and Crafting |
| 4 | Impact's Realistic Vitality (IRV) | L+ | Done, with Food, Inventory and Crafting |
| 5 | Impact's Realistic Crafting (IRC), plus Lib's crafting systems, then every mod's "with IRC" features | XL | Done |
| 6 | Salty's Realistic Forging pass for all four mods | M | Done (the uploaded jar is all of SRF that loads in game) |
| 7 | All family test setups pass: 1.20.1 Forge release candidate | M | All 17 setups pass; waiting for your playtest |
| 8 | NeoForge 1.20.1 | S | Planned |
| 9 | Forge and NeoForge, Minecraft 1.20.2 up to 1.21, one version at a time | L | Planned |

Why this order:

- **IRI** has the least content (3 packs, 1 block, 12 achievements) and needs only Lib's core. Its risk sits in the inventory mixins.
- **IRF** has about 150 items, but about 90 of them are generated from one table.
- **IRV** has three linked simulations, 4 block entities and a new HUD.
- **IRC** has about 120 items, worldgen, timed and tiered crafting in every grid, and hundreds of generated recipes that replace vanilla ones.

## 0. Workspace setup

- [x] Specs saved as `SPEC.md`, the coding rules saved in `.claude/skills/`, `CLAUDE.md`, this roadmap
- [x] AI notice: `NOTICE`, README notice, `LICENSE` and `NOTICE` packed into every jar
- [x] Gradle workspace: `common` (VanillaGradle) and `forge` (ForgeGradle 6, Parchment) modules, Java 17

## 1. Impact's Lib core

- [x] **M1 Skeleton:**
  - the mod loads
  - `ImpactsLib.isLoaded`, `registerImpactMod`, `ImpactsModsReadyEvent`
  - `ImpactsLog`, with the rolling `logs/impacts-debug.log` and folding of repeated lines
  - `ImpactsConfig`, with the `impactslib/` folders
  - Lib's own config files
  - the startup log line
- [x] **M2 Commands and the lone-Lib achievement:**
  - `/impacts debug`, `/impacts config`, `/impacts dump player`
  - "Why am I here?" (the `impactslib:no_impact_mods` condition and the `impactslib:joined_alone` trigger)
- [x] **M3 Providers:**
  - `ImpactsItems.giveOrDrop`
  - `BackSlot`, plus the `#impactslib:back_slot_items` tag
  - `HudLayout`
  - `CraftingProgress` API and renderer
  - the `impactslib:main` network channel, including a resync of SERVER config values when they change
- [x] **M4 Achievement pieces and player data:**
  - `ImpactsCounters` and the `impactslib:count` trigger
  - datagen helpers (recipe pairs, advancement builder)
  - player attachments: per-player data saved in vanilla NBT, with death rules (used by counters, then Inventory and Vitality)
- [x] **M5 Test-setup harness:**
  - `test-setups/<name>/` runs a GameTest server with exactly the listed mods, plus SRF and playerAnimator when asked for, then fails on any Impact warning or error
  - `./gradlew runFamilyTestSetups` runs them all
  - "Lib alone" and "Lib with SRF" exist; each mod's setups are added as it lands

## 2. Impact's Realistic Inventory

These are the milestones from `impacts-inventory/SPEC.md`.

- [x] 1 Skeleton
- [x] 2 Two-slot hotbar
- [x] 3 Locked slots and pockets
- [x] 4 Inventory screen
- [x] 5 First-join migration and creative mode
- [x] 6 Packs
- [x] 7 Placed packs
- [x] 8 Integration: IRV parts done in step 4 (Air Tanks and the Hydration Pack in the back slot, Vitality's HUD lined up with the two-slot hotbar, the Frame Pack's thirst, the `inventory-and-vitality` setup); IRC parts in step 5 (pack recipes from Crafting's parts, the arrow in IRI's screen, the `crafting-and-inventory` setup)
- [x] 9 SRF pass: the four GameTests from `docs/srf-notes.md`, and the `inventory-with-srf` setup
- [x] 10 Achievements
- [x] 11 Polish and compatibility: pack tooltips, generated lang, the `inventory-alone` and `inventory-with-srf` setups. JEI pages for packs come with Lib's JEI base plugin (step 3); the other setups with IRI come with their mods.

## 3. Impact's Realistic Food, plus Lib's food systems

- [x] **Lib systems for this step:**
  - `tool_crafting`, with the `impactslib:tool_crafted` trigger and `ToolCrafting.damageTools`
  - uses-based durability
  - fuel and compost registries
  - hold-to-use, with the `impactslib:hold_action` trigger
  - grinding and the four grinder items
  - heat and cold source tags, plus `isHeated`
  - boiling provider
  - the `impactslib:water_bottle` ingredient
  - the water-filled event
  - recipe audit and conflict checker (`/impacts audit`, `/impacts conflicts`)
  - JEI base plugin
These are the milestones from `impacts-food/SPEC.md`.

- [x] 1 Skeleton: configs, debug categories, Crafting detection, the `food-alone` setup
- [x] 2 Knives and cutting: knives, every cut and its byproducts, config overrides for yields, chances and piece cooking times, cooked pieces, knife cutting for seeds, Food's creative tab
- [x] 3 Cauldron boiling: heat sources, warm-up and cool-down, single-item boiling (the egg; styled foods come with milestone 4), evaporation, Lib's boiling answer, the handler pass-through and its SRF check, boiling particles and sounds
- [x] 4 Cooking any food: the FoodVariants table (86 styled foods), layered models with overlays trimmed to each food, every smelting, smoking, campfire and boiling recipe, Burnt Food, Mush, the new raw foods and eggs, style tags and tag inheritance. Its advancements come with milestone 12's 26.
- [x] 5 Soups: soup recipes and batches (the biggest soup wins), sets, Watery Soup, ladling into any bowl, eating from the pot, the new soups, grid stews off, `#impactsfood:soups`, bottles refused at a soup. Porridge's recipe comes with baking (Flour).
- [x] 6 Bucket on a campfire: placing with sneak + water bucket, the 8-boil limit, smaller pot sizes, taking it back (water or empty, NBT kept), popping off when the campfire goes out
- [x] 7 Spit roasting: the Spit item, mounting on a lit campfire, 6 slots on top of the campfire's 4, 1.25x and turned 1.5x speed, taking it down, staying while the campfire is out and dropping when it breaks, saved and synced on the campfire, drawn by vanilla's campfire renderer
- [x] 8 Raw food risk: `#impactsfood:raw_risky`, the Hunger roll on Forge's finished-eating event, per-food overrides, the switch
- [x] 9 Baking: Flour from a stone grinder (with seeds), Dough with any water bottle, Loaf Dough, Dough Bun, Flatbread, Dumpling, bread from Loaf Dough, cake, cookies, pie and porridge from Flour, grid baking off, the grinders' standalone recipes
- [x] 10 IRC integration (done in step 5): knives and the spit from Crafting's parts, Hot Pebbles heated on a campfire from any of Crafting's pebbles, the stone-boiling pot from the Water-filled Wooden Bucket, Stone Soup, and the `crafting-and-food` setup
- [x] 11 IRV, IRI and SRF checks: IRI (byproducts with no room drop, with or without its locked slots), SRF (the cauldron pass-through with SRF's raw gold ore, in the new `food-with-srf` setup) and IRV (the campfire bucket as a Lib water vessel, so Vitality fills Purified Water from it while it boils; the food tags Vitality reads; the `food-and-vitality` setup)
- [x] 12 Polish: all 26 achievements, JEI (Boiling, Soups and Spit Roasting categories; cutting in Lib's Tool Crafting; info on the cooking styles, the spit and the campfire bucket), tooltips (styles, tools, raw pieces, the raw-food risk on vanilla items too), Food's own sound events with subtitles

## 4. Impact's Realistic Vitality

These are the milestones from `impacts-vitality/SPEC.md`. The IRF and IRI parts of milestone 10 happen here, the IRC parts in step 5 and the SRF part in step 6.

- [x] 1 Skeleton: the three configs (need, piece and gear switches in common; every number in server; units and HUD in client), the `impactsvitality:feature_enabled` condition, debug categories, the player attachment with thirst, temperature and air parts, the `impactsvitality:main` channel syncing it, the `vitality-alone` setup
- [x] 2 Thirst: the bar with Hydration, exhaustion from every action that costs food (not from the Hunger effect), the resting drain, Peaceful refill, the Parched effect, dehydration damage on starvation rules with the `impactsvitality:dehydration` damage type (and the other three damage types), no sprinting at 6 or less, the droplet row through Lib's layout (shakes when low, green while Parched)
- [x] 3 Drinks and water gear: the four waters with purity from water blocks, cauldrons and dispensers, the drink table for drinks and foods, Parched, sipping, purifying by furnace, smoker and campfire, Cold Water, brewing, mud, cauldron pouring, `#impactslib:water_bottles`; the six containers (filling, drinking, pouring over yourself, grid filling, the cup on a campfire, the flask's Cold Water and soups, Tidebound refills, the Hydration Pack and its key); the Rain Barrel, the Water Filter with the Charcoal Filter, and the hopper-fed Copper Still
- [x] 4 Health cap: the fuller bar must be at 18 for natural regeneration, the emptier bar caps it at full, three-quarters or half of max health, fast regeneration needs both bars full and both saturations, regeneration exhaustion is split between hunger and thirst, and `healthCap.capAllHealing` caps every heal
- [x] 5 Ambient temperature: biome formula and overrides, Nether offset and flat End, cooling with height, caves drifting to 55°F (75°F at the bottom), smooth night and midday sun, rain and snow under open sky, nearby hot and cold blocks (closer counts more, capped), water, the Wet timer; read once a second per player and synced for the HUD
- [x] 6 Body temperature: drift toward ±100 outside the comfortable range (1 point a second per 10°F) and back to 0 inside it, the nine bands with their effects (Slowness, slower mining, no sprinting, Weakness, Nausea, freezing and heat damage every 3 seconds, none on Peaceful), heat multiplying thirst drain, Cold Water, melon and soups changing body temperature, the dry-food rule, the gauge (pulsing at Cold and Hot) and the frost overlay; `/impacts vitality show|set` for testing
- [x] 7 Insulation: warm, cooling and conductive armor tags (leather, chainmail, iron), Wool, Fur and Leather Linings sewn at an anvil without Crafting (keeping the armor's NBT; the Bone Needle recipe comes with Crafting in step 5), lining tooltips, the Straw Hat (cooling in the sun), being Wet, and the Thermometer's ambient readout under the gauge
- [x] 8 Air: Overworld height bands, the Nether (smoky) and End (thin) from `air_quality` data files any datapack can add to, lava and lava cauldrons fuming within 3 and 1 blocks, a bubble lost every so often with vanilla's refill held back, 2 damage every 2 seconds once out (`thin_air` or `fumes`, none on Peaceful), Water Breathing, Conduit Power and Respiration, and the cloud at the end of the bubble row
- [x] 9 Breathing gear: the Cloth Mask (damp from water, halves smoke), Air Bladder, Bottled Air, Respirator and Charcoal Filters (fitted in a grid, worn down by smoke), Air Tank (thin air with a Respirator on, from the inventory or back slot, refilling in good air), Rebreather (filters and tanks last twice as long), Air Compressor (fuel fills a tank in 5 seconds), Netherite Rebreather (smithing; smoke never affects you) and Shulker Air Tank
- [x] 10 Integration. IRF and IRI parts done: bottles and containers fill from Food's campfire bucket through Lib's new water vessels (purified while it boils), Food's soups, boiled, toasted and dried foods in the drink and food-temperature tables, the Frame Pack (`#impactsvitality:heavy_packs`) adding 10% to exertion thirst, tanks and the Hydration Pack in Inventory's back slot, the HUD lined up with Inventory's two-slot hotbar, and the `food-and-vitality` and `inventory-and-vitality` setups. SRF: no heat entries needed (SRF "no tools" has no hot blocks), and the `vitality-with-srf` setup checks the cauldron. IRC parts done in step 5 (gear from Crafting's parts, needle-sewn linings, the loose Air Tank, drinking from the Wooden Bucket, the `crafting-and-vitality` setup)
- [x] 11 Achievements: all 22 in the "Realistic Vitality" tab, Vitality's six triggers (`drank`, `temperature_state`, `air_lost`, `health_capped`, `dry_food_refused`, `gear_used`), and Lib counters behind "Well Watered" and "In Balance"
- [x] 12 Polish: JEI info pages (water and purity, sipping, containers, water blocks, breathing gear, the Thermometer, the Straw Hat, linings), tooltips, English text for everything, and panting, shivering and coughing sounds with subtitles (client config `display.needSounds`)

## 5. Impact's Realistic Crafting, plus Lib's crafting systems

- [x] **Lib systems for this step:** `WoodData`, and `/impacts dump held` and `/impacts dump block`
- [x] IRC milestones from `impacts-crafting/SPEC.md`. The SRF part of milestone 14 moves to step 6.
  - [x] 1 Skeleton: configs (with a worldgen dimension list and biome blacklist), debug categories, the Stone Pebble (64 unused, 1 once worn), loose pebbles in worldgen (surface and cave features, a biome modifier for Overworld biomes), pebbles from gravel and stone (a global loot modifier, plus a break handler for stone broken by hand), the `impactscrafting:overhaul_enabled` recipe condition, and the `crafting-alone` and `crafting-with-srf` setups
  - [x] 2 Makeshift Table and tiers: both makeshift tables (3×3 grid through vanilla's crafting menu, three wear stages, uses kept on the item, falling apart like an anvil), the tier tags and `TierResolver`, the tier check in every crafting grid (the 2×2 included) with a "Needs a ..." hint over the refused result, the vanilla Crafting Table recipe switched off
  - [x] 3 Tool crafting and starting parts: planks (1 by hand, 2 with a trimming tool) and sticks (vanilla's shape plus a trimming tool) replacing vanilla's, per-wood Trimmed Planks (tinted and named by wood), Trimmed Sticks, Shaped Stone, Stone Hinge, Twine, String Clump, Wood Scraps (a config count per craft, none from nether wood, fuel and compost), the Bone Needle and Flint Chisel; wooden parts burn
  - [x] 4 Tools: the `impactscrafting:hammering` recipe type and Lib hold-to-use action (loose item in the main hand, hammer in the off hand, effects every 10 ticks), loose items, the 10 heads and blades, the 10 loose tools hammered into vanilla's wooden and stone tools, vanilla's tool recipes switched off
  - [x] 5 Timed crafting in every grid: the depth table built from the live recipes at start and after every reload, craft times (10 ticks per depth, capped at 100, fallback 20, recipe overrides, mod multipliers, `craft_ticks`), a synced timer on vanilla's crafting and inventory menus (and the makeshift tables), the output locked and faded until done, shift-click repeats, and Lib's arrow drawn over vanilla's screens
  - [x] 6 Wood and stone branches: Wooden Panel, Wooden Frame, Fence Post and Boat Hull per wood, Chest Top, Oar, both Wooden Buckets (scoop and pour with shared wear, the water-filled event), Stone Casing, Stone Base, Mortar, Leather Strip and Flint Arrowhead, every non-iron wood catalog row (doors to boats), every stone family's chiseled variants from vanilla's block families, brick blocks with Mortar and the non-iron stone machines; about 270 vanilla recipes switched off
  - [x] 7 Iron branch: Iron Saw, Hammer, Tweezers and Chisel; Iron Plate, Rod, Rivets, Hinge and Wheel (with the `iron_plates` and `iron_rods` tags and the stone tier tag); the Crafting Table from 4 planks and the 3 iron tools; the plain iron goods (loose bucket, cauldron, iron door, minecart and blast furnace hammered); hanging signs, the smithing table, stonecutter, glass panes and bottles; the Iron Saw's double wood yields
  - [x] 8 Grinder recipes: Lib's four grinders from Crafting's parts, 28 dye and powder grinds and 9 dye mixes on Lib's grinding type, vanilla's grid dye, bone meal, sugar and blaze powder recipes switched off
  - [x] 9 Dye Vat: the block (water from either water bucket or rain, dye in 8-charge doses up to 32, mixing, tinted water), dipping one item per 10 ticks while right-click is held (wool, carpet, terracotta, glass, panes, concrete powder, candles, shulker boxes, and leather-style items the vanilla way), washing in plain water, Paper Pulp and paper, the `impactscrafting:vat_dyeing` recipe type; every grid dyeing recipe switched off
  - [x] 10 Beds: the Bed Frame (placed like a bed), Comforter and 16 Sheets; building a bed in three layers (a sheet on a bare frame refused with a hint), recoloring a finished bed with another sheet, beds (village beds too) dropping their three parts (`bedsDropParts`), sheets dipped in the Dye Vat; vanilla's bed recipes switched off
  - [x] 11 Redstone and transport: the Redstone Mechanism and Piston Head; the redstone torch, repeater, piston (hammered), dropper, dispenser, note block, compass and TNT; the comparator, daylight detector, observer and jukebox at the Crafting Table tier; rails, the sticky piston and the gold and glowstone recipes stay vanilla
  - [x] 12 Combat, leather, textiles: the Bow Stave (with a Wood Scrap), bow, arrows, crossbow, fishing rod and leads; the loose shield (hammered); the Leather Panel and leather armor, horse armor and books sewn with the Bone Needle; carpet cut with shears (Stone Table), 16 Banner Cloths (dyeable in the vat) and banners, candles, and wool from String Clumps; vanilla's recipes for all of these switched off
  - [x] 13 Polish: the 26-achievement "Realistic Crafting" tab (Crafting's triggers, Lib counters checked against the config, Lib's `hold_action` with an item predicate and a grind listener), JEI Hammering and Dye Vat categories with info pages, wood subtypes and every crafting recipe's table and time, next-step tooltips on every item, Crafting's own sound events with subtitles, and Dye Vat particles
  - [x] 14 Audit (the SRF part is in step 6): the in-game recipe audit labels every vanilla crafting recipe (0 unlabeled, 0 conflicts), the tree check GameTest, endgame materials in the advanced tier, and Crafting versions of the soul campfire, smooth stone slab and bamboo sticks
- [x] **"With IRC" features:**
  - [x] IRI milestone 8: pack recipes with IRC parts (the Satchel takes a second Twine so it doesn't match IRC's leather boots), IRC's crafting arrow in IRI's screen, packs in the Dye Vat, the `crafting-and-inventory` setup
  - [x] IRF milestone 10: recipe switching, stone boiling (Hot Pebbles, the stone-boiling pot), Stone Soup, the `crafting-and-food` setup
  - IRV:
    - [x] gear recipes from Crafting's parts (the Waterskin takes a Leather Strip so it doesn't match Crafting's leather boots), linings sewn with the Bone Needle, the loose Air Tank and its hammering, the `crafting-and-vitality` setup
    - [x] drinking from (and pouring out) the Water-filled Wooden Bucket, with its purity from Lib's water-filled event

## 6. Salty's Realistic Forging pass

- [x] One read of SRF 4.1.6's source ("no tools" version), covering the checklist in each spec
- [x] `docs/srf-notes.md` in each mod
- [x] Optional tag entries and a GameTest for each case, added with each mod: Crafting's hammers and tier tags list SRF's smithing hammers and metal items (`SrfTagEntries`), tested by `CraftingSrfGameTests`; Inventory, Food and Vitality need no tag entries and are checked by their `*-with-srf` setups
- [x] Add SRF detection (`realisticforgingores`) to every startup log line (`StartupSummary.log`)

## 7. Family test setups

- [x] Lib alone, each mod alone, IRC+IRF, IRC+IRV, IRC+IRI, IRF+IRV, IRI+IRV, all four, all four + SRF ("no tools"), plus each mod with SRF: 17 setups, every one passing its GameTests (358 in `all-four`) with no Impact warnings or errors. Every setup with IRI loads Curios too (IRI requires it).
- [x] All four + SRF: `all-four-with-srf` covers it. The uploaded jar, though named "no tools", is all of SRF that loads in game: its registry has no tool or armor forging items (only leftover textures), so there is no separate full version to test (see `impacts-lib/docs/srf-notes.md`). If a build that forges tools and armor turns up, it gets its own setup then.
- [ ] Your playtest of the release candidate on 1.20.1 Forge, then NeoForge 1.20.1 (step 8)

## More packs (Nov 2026)

Asked for by the user: more pack tiers, each a net gain past the Frame Pack.

- [x] Explorer's Pack (18 slots, no slowdown; phantom membranes), Expedition Pack (27; diamonds and gold), Netherite Pack (36, fireproof; smithing upgrade), and the Ender Pack (opens your Ender Chest, worn or placed)
- [x] Recipes without and with Crafting, models, textures, four achievements, GameTests

## Vanilla compatibility review (Nov 2026)

Asked for after the release candidate, before porting: every mixin, event handler, data override, loot modifier and worldgen feature checked against vanilla behavior and other mods.

- [x] No `@Redirect` left (Inventory's pickup notice and hotbar keys, Crafting's tier check): plain injections, which other mods' mixins can share
- [x] Food: heated powder snow cauldrons boil nothing; styled foods stay out of other Impact mods' tags
- [x] Vitality: vanilla anvil repair of leather armor; glass bottles aimed at blocks or beside the dragon's breath act as vanilla's
- [x] Inventory: a swap never shortens an existing cooldown
- [x] Crafting: loose pebbles replaceable (building, trees); occupied beds aren't recolored
- [x] A GameTest for each, all family setups passing
- [x] Vanilla and shared Forge tags: every block in its tool tag (and copper in `needs_stone_tool`), Food's fillets and pork in `fishes` and `piglin_food`, tools in `minecraft:tools`, cloth in `dampens_vibrations`, the placed pack guarded by piglins, and the shared names other mods read (`forge:tools/knives`, `forge:flour`, `forge:dough`, `forge:plates/iron`, `forge:rods/iron`, `forge:rods/wooden`, `forge:armors/helmets`)

## playerAnimator and Curios review (Nov 2026)

- [x] playerAnimator: Lib's layer through playerAnimator's factory, with bent elbows, a torso lean into each blow, and eased fades in, between and out (first person stays vanilla's view: playerAnimator's first-person model filled the screen with the held item)
- [x] A crafting motion while a timed craft runs, seen by everyone nearby (Lib's synced work motions, shown by Crafting)
- [x] Curios: Vitality's Air Tanks and Hydration Pack show on the player's back in the back slot, and Vitality's gear plays its own equip sound in Curios slots
- [x] Inventory: a second pack on the chest (Curios's body slot): sneak-right-click to wear it, N or a button to open it
- [ ] Vitality's back gear in Curios's back slot without Inventory (Lib's Curios-backed back slot)
- [ ] Vitality: the Thermometer works from Curios's charm slot
- [ ] Vitality gestures: shivering when freezing, wiping the brow when overheating, cupped-hand sips

## Playtest feedback (Nov 2026)

Fixes from the user's first playtest, worked through before the remaining milestones.

- [x] Every mod's jar collected in `build/mods/`
- [x] Inventory: one main-hand slot with vanilla's off hand beside it (no more item showing in both), no extra back slot (Curios's only), the pockets as a 3×3 grid in every container screen
- [x] Vitality: no effects in creative or spectator; moist foods never refused as dry; Thin air from Y 192; colder heights and deep caves; water cools you; a pressure-gauge temperature dial with fixed color zones and a warming/cooling arrow
- [x] Vitality: masks and the Straw Hat also fit Curios's head slot (optional; Curios isn't required)
- [x] Food: no style description lines on styled foods
- [x] Crafting: pebbles on grass, sand and other natural ground at dead-bush density, with a pebble for each stone type and for sand
- [x] Crafting: loose items with real finishing steps (boats assembled from hull and oars, the bucket cast in a clay mold, fasteners driven into pistons, doors and minecarts, the blast furnace sealed with mortar)
- [x] Hold-to-use animations (hammering, grinding and the other interactions) visible like SRF's, with or without playerAnimator

## Later (noted by the user, Nov 2026)

- **More temperature gear.** Cooling and warming gear for each climate, beyond the Straw Hat and the armor linings: hats, cloaks, scarves, light desert clothing, insulated coats and boots, ideally worn in Curios slots too.
- **Water purification steps.** Clean, unclean and salt water currently go straight to Purified; later steps (settling, boiling, filtering stages) will expand this.
- **Per-food descriptions.** If food descriptions return, they'll be written per food family (bread, mutton, rabbit, ...), not per style.
- **Vitality depth.** The user finds Vitality "somewhat lackluster" for now and will expand it later.

## Open items

- Lib's `FuelHelper` is `FuelRegistry` plus `CompostRegistry` (clean-naming rule 6), as proposed in the approved plan.
- Dedicated-server start checks: the user accepted the Minecraft EULA for test runs (Nov 2026). `eula=true` goes only into git-ignored run folders.
- SRF: the uploaded jar registers no tool or armor forging items (leftover assets only), so the setups with SRF cover all of it. A build of SRF that forges vanilla tools and armor would need checking against Crafting's tool recipes.
