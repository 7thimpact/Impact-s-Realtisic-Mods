# Roadmap

Steps are ordered from least to most effort. The one exception is Impact's Lib, which comes first because every mod requires it. After the core, each Lib system arrives with the first mod that needs it (Lib's versioning lets minor versions add API).

**Platform plan:** every mod is built and established on Minecraft 1.20.1 Forge first (steps 1 to 7). Only after that do we cover Minecraft 1.20.1 through 1.21 on both Forge and NeoForge (steps 8 and 9). **There is no Fabric port.**

| # | Step | Effort | Status |
| --- | --- | --- | --- |
| 0 | Workspace setup | S | Done |
| 1 | Impact's Lib core | M | Done |
| 2 | Impact's Realistic Inventory (IRI) | M | Done on its own (milestone 8, the IRV and IRC parts, lands in steps 4 and 5) |
| 3 | Impact's Realistic Food (IRF), plus Lib's food systems | L | Done standalone (IRC parts in step 5, IRV parts in step 4) |
| 4 | Impact's Realistic Vitality (IRV) | L+ | In progress |
| 5 | Impact's Realistic Crafting (IRC), plus Lib's crafting systems, then every mod's "with IRC" features | XL | Planned |
| 6 | Salty's Realistic Forging pass for all four mods | M | Source review done; each mod's SRF GameTests land with that mod |
| 7 | All family test setups pass: 1.20.1 Forge release candidate | M | Planned |
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
- [ ] 8 Integration: IRV parts in step 4, IRC parts in step 5
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
- [ ] 10 IRC integration (moves to step 5)
- [ ] 11 IRV, IRI and SRF checks: done for IRI (byproducts with no room drop, with or without its locked slots) and SRF (the cauldron pass-through with SRF's raw gold ore, in the new `food-with-srf` setup); the IRV parts come in step 4
- [x] 12 Polish: all 26 achievements, JEI (Boiling, Soups and Spit Roasting categories; cutting in Lib's Tool Crafting; info on the cooking styles, the spit and the campfire bucket), tooltips (styles, tools, raw pieces, the raw-food risk on vanilla items too), Food's own sound events with subtitles

## 4. Impact's Realistic Vitality

These are the milestones from `impacts-vitality/SPEC.md`. The IRF and IRI parts of milestone 10 happen here, the IRC parts in step 5 and the SRF part in step 6.

- [x] 1 Skeleton: the three configs (need, piece and gear switches in common; every number in server; units and HUD in client), the `impactsvitality:feature_enabled` condition, debug categories, the player attachment with thirst, temperature and air parts, the `impactsvitality:main` channel syncing it, the `vitality-alone` setup
- [x] 2 Thirst: the bar with Hydration, exhaustion from every action that costs food (not from the Hunger effect), the resting drain, Peaceful refill, the Parched effect, dehydration damage on starvation rules with the `impactsvitality:dehydration` damage type (and the other three damage types), no sprinting at 6 or less, the droplet row through Lib's layout (shakes when low, green while Parched)
- [x] 3 Drinks and water gear: the four waters with purity from water blocks, cauldrons and dispensers, the drink table for drinks and foods, Parched, sipping, purifying by furnace, smoker and campfire, Cold Water, brewing, mud, cauldron pouring, `#impactslib:water_bottles`; the six containers (filling, drinking, pouring over yourself, grid filling, the cup on a campfire, the flask's Cold Water and soups, Tidebound refills, the Hydration Pack and its key); the Rain Barrel, the Water Filter with the Charcoal Filter, and the hopper-fed Copper Still
- [x] 4 Health cap: the fuller bar must be at 18 for natural regeneration, the emptier bar caps it at full, three-quarters or half of max health, fast regeneration needs both bars full and both saturations, regeneration exhaustion is split between hunger and thirst, and `healthCap.capAllHealing` caps every heal
- [x] 5 Ambient temperature: biome formula and overrides, Nether offset and flat End, cooling with height, caves drifting to 55°F (75°F at the bottom), smooth night and midday sun, rain and snow under open sky, nearby hot and cold blocks (closer counts more, capped), water, the Wet timer; read once a second per player and synced for the HUD
- [x] 6 Body temperature: drift toward ±100 outside the comfortable range (1 point a second per 10°F) and back to 0 inside it, the nine bands with their effects (Slowness, slower mining, no sprinting, Weakness, Nausea, freezing and heat damage every 3 seconds, none on Peaceful), heat multiplying thirst drain, Cold Water, melon and soups changing body temperature, the dry-food rule, the gauge (pulsing at Cold and Hot) and the frost overlay; `/impacts vitality show|set` for testing
- [ ] 7 Insulation
- [ ] 8 Air
- [ ] 9 Breathing gear
- [ ] 10 Integration: IRF and IRI parts here; IRC parts in step 5; SRF heat entries in step 6
- [ ] 11 Achievements
- [ ] 12 Polish

## 5. Impact's Realistic Crafting, plus Lib's crafting systems

- [ ] **Lib systems for this step:** `WoodData`, and `/impacts dump held` and `/impacts dump block`
- [ ] IRC milestones 1 to 13 from `impacts-crafting/SPEC.md`. The SRF part of milestone 14 moves to step 6.
- [ ] **"With IRC" features:**
  - IRI milestone 8: pack recipes with IRC parts, IRC's crafting arrow in IRI's screen
  - IRF milestone 10: recipe switching, stone boiling
  - IRV: gear recipes, linings sewn with the Bone Needle, drinking from the Wooden Bucket, the loose Air Tank

## 6. Salty's Realistic Forging pass

- [x] One read of SRF 4.1.6's source ("no tools" version), covering the checklist in each spec
- [x] `docs/srf-notes.md` in each mod
- [ ] Optional tag entries and a GameTest for each case, added with each mod
- [x] Add SRF detection (`realisticforgingores`) to every startup log line (`StartupSummary.log`)

## 7. Family test setups

- [ ] Lib alone, each mod alone, IRC+IRF, IRC+IRV, IRC+IRI, IRF+IRV, IRI+IRV, all four, all four + SRF (full), all four + SRF ("no tools"). Every setup with IRI loads Curios too (IRI requires it).

## Open items

- Lib's `FuelHelper` is `FuelRegistry` plus `CompostRegistry` (clean-naming rule 6), as proposed in the approved plan.
- Dedicated-server start checks: the user accepted the Minecraft EULA for test runs (Nov 2026). `eula=true` goes only into git-ignored run folders.
- "All four + SRF (full)" needs the full SRF jar, which the user will upload later. Until then that setup reports the missing jar.
