# Impact's Lib — Shared Library Spec

Nov 14, 2026 · @Supa

## Purpose and packaging

Impact's Lib holds everything that more than one Impact mod needs, so [Impact's Realistic Crafting](https://claude.ai/artifact/SaBQus4NifLnCSbvWYGWWL) (IRC), [Impact's Realistic Food](https://claude.ai/artifact/H85Ku1ujPuD8QUSVhTsUAF) (IRF), [Impact's Realistic Inventory](https://claude.ai/artifact/Euz9kvYYXiDVHnaD9xRjkB) (IRI) and [Impact's Realistic Vitality](https://claude.ai/artifact/1MFojaKLikF4TDVS8HSyyS) (IRV) can each run alone, connect with whichever of the others are present, and never register the same item twice.

Future Impact mods will be built on Lib the same way and follow the same rules.

- Mod ID `impactslib`, package `com.impact.lib`, Minecraft 1.20.1, Forge 47.x, Java 17.
- **A standalone mod and a required dependency.** Lib is its own download on CurseForge and Modrinth. Every Impact mod (IRC, IRF, IRI, IRV and any future one) declares it as a required dependency, loaded first (`ordering="AFTER"`), in `mods.toml` (`mandatory=true`, `versionRange="[1.0,2.0)"`) and as a required dependency on each mod page, so launchers install it automatically. No Impact mod bundles a copy.
- Developers get it from Lib's Maven repository: `implementation fg.deobf("com.impact:impactslib:<version>")` in each mod's Gradle build.
- **Lib adds no gameplay by itself.** Its items have recipes only when a mod that uses them is installed. Alone, Lib registers its items and systems and does nothing else.
- **Built for more mods.** Lib is the shared base for the whole Impact family, so its API has to stay stable and documented (see Versioning).

## What lives in Lib

Anything used by two or more Impact mods. Everything else stays in the mod that owns it.

**Shared systems**

| System | What it does | Used by |
| --- | --- | --- |
| `impactslib:tool_crafting` recipe type | Grid recipes where tools stay in the grid and take damage. Tools match in any slot, and a recipe without tools never matches a grid that holds one. Optional `byproducts` (given through Give or drop), `remainders` (for example, return a glass bottle), `craft_ticks`, and `copy_nbt_from` (copies one ingredient's NBT onto the result). Items with no durability can't be tools. `ToolCrafting.damageTools(...)` lets other mods' special recipes use the same tool behavior | IRC (trimming, cutting, sewing), IRF (knife cutting, Dough), IRV (armor linings), IRI (pack sewing with IRC) |
| `impactslib:grinding` recipe type + grinder items | Off-hand bowl, main-hand stick, hold-to-grind. A bowl can also be loaded bundle-style by clicking an item onto it in any inventory screen, which helps with IRI's two hand slots | IRC (dyes, sugar, powders), IRF (flour) |
| Hold-to-use actions | Hold right-click for N ticks: progress, sounds, particles, cancel on release or slot switch. Uses `UseAnim.NONE` plus its own arm animation, never `UseAnim.BLOCK`, whose pose looks like shield blocking and which some mods read as blocking | Lib (grinding), IRC (hammering, Dye Vat dipping), IRF (heating a pebble) |
| Uses-based durability | Uses stored in NBT, no tag while unused, stack size 1 once worn. Other mods change an item's uses only through this API | IRC (pebbles, Wooden Buckets, makeshift tables), IRF (Hot Pebble, stone boiling) |
| Wood type data | Reads and writes the `wood` NBT, maps a wood to its vanilla items (bamboo's odd names too), and builds `forge:partial_nbt` ingredients in datagen | IRC (per-wood parts) |
| Fuel and compost helpers | Burn time from item + NBT (non-flammable woods return 0). Every Impact item made of wood or wool burns. Config overrides and compostable registration | IRC, IRF, IRV |
| Heat and cold sources | `#impactslib:heat_sources` and `#impactslib:cold_sources` tags, and `isHeated(level, pos)`, which also checks lit states (a campfire or furnace only counts while lit) | IRF (boiling, Hot Pebbles), IRV (temperature, Copper Still) |
| Boiling state | `ImpactsLib.isBoiling(level, pos)`. IRF registers the answer for cauldrons, campfire buckets and stone-boiling pots; without IRF it always returns false | IRF (answers), IRV (water purity) |
| Back slot | `BackSlot.isAvailable()`, `BackSlot.get/set(player)` and the tag `#impactslib:back_slot_items`. IRI provides the slot; without IRI, `isAvailable` is false and `get` returns empty | IRI (provides), IRV (Air Tanks, Hydration Pack, Frame Pack thirst) |
| Give or drop | `ImpactsItems.giveOrDrop(player, stack)` inserts through vanilla's inventory, so IRI's locks apply, drops the rest at the player's feet, and logs both | Every mod (byproducts, grinder output, returned containers, IRI's locked-slot redirects) |
| Crafting progress | `CraftingProgress.get(menu)` plus a renderer for the filling arrow and the faded result. IRC writes the progress; any screen (vanilla, IRI's, modded) can draw it | IRC (writes), IRI (draws) |
| Water bottle ingredient | Ingredient type `impactslib:water_bottle`: a vanilla water bottle or anything in `#impactslib:water_bottles` (IRV adds its water items) | IRF (Dough), any recipe that needs water |
| Water-filled event | `ImpactsEvents.WaterFilled(player, stack, level, pos, boiled)` on the Forge event bus, posted when an Impact mod puts water into a container item. Listeners may add their own namespaced NBT to the stack | IRC (Wooden Bucket scoops), IRF (stone-boiling pot hand-back, boiled = true), IRV (stamps purity) |
| HUD layout manager | Publishes the hotbar's bounds and wraps Forge's left and right status-row heights, so rows stack without overlapping (left: health, armor; right: hunger, thirst, air). Vanilla bars keep their positions | IRI (hotbar bounds), IRV (thirst row, air row, temperature gauge) |
| Datagen helpers | Recipe pairs gated by `forge:mod_loaded` and `forge:not`, the recipe audit base, and the advancement builder | Every mod |
| Recipe conflict checker | Fails the build if two recipes can match the same grid, or if two mods override the same vanilla recipe (see Family compatibility and testing) | Every mod |
| JEI base plugin | Shared categories for tool crafting, grinding and hold-to-use, so each mod only adds its own | Every mod |
| Debug logging and `/impacts` | Event logging, on by default, plus debug commands (see Debug logging) | Every mod |
| Shared interactions | The input rules for items that several mods act on (see Family compatibility and testing) | IRC, IRF, IRV |

**Shared items** (namespace `impactslib:`)

- `wooden_grinder_bowl`, `stone_grinder_bowl`, `wooden_grinding_stick`, `stone_grinding_stick`

Their recipes are shipped by the mods, never by Lib. IRC's use Trimmed Planks and Shaped Stone. IRF's use a bowl + planks, 3 cobblestone, and sticks, and carry `forge:not` of `forge:mod_loaded impactscrafting`, so with both mods installed only IRC's load. The items appear in the creative tab of the mod that uses them (IRC's, otherwise IRF's). With neither installed (IRI or IRV alone), they're registered but hidden. Flour and Dough are IRF items, since only IRF uses them.

**What moves out of IRC**

| Was | Now |
| --- | --- |
| `impactscrafting:tool_crafting` | `impactslib:tool_crafting` |
| `impactscrafting:grinding` and the four grinder items | `impactslib:grinding` and `impactslib:` grinder items |
| IRC's hold-to-use, uses-durability, wood NBT and fuel code | Lib classes. IRC calls them |
| IRC's crafting-arrow renderer | Lib's `CraftingProgress`. IRC still decides craft times and writes the progress |
| IRC's flour, dough and whole food catalog | Move to IRF (`impactsfood:`). IRC has no food recipes |

Everything else (pebbles, tables, tiers, timed crafting, hammering, Dye Vat, beds, all parts) stays in IRC. Knives, boiling, soups and the spit stay in IRF.

## Integration API and rules

- **No mod imports another mod's classes.** Every Impact mod talks to the others only through Lib's API, shared items, tags and recipe conditions. Removing any mod must never cause a missing-class crash.
- **API:** `ImpactsLib.isLoaded(modId)` (cached), `registerImpactMod(modId)`, `isHeated(level, pos)`, `isBoiling(level, pos)` and `registerBoilingProvider(...)`, `BackSlot.isAvailable/get/set` and `registerBackSlotProvider(...)`, `ImpactsItems.giveOrDrop(player, stack)`, `CraftingProgress`, `UsesDurability.get/use/isUnused(stack)`, `WoodData.get/set(stack)` and `WoodData.vanillaItem(wood, kind)`, `FuelHelper.register(...)`, `HoldToUse.register(...)`, `ToolCrafting.damageTools(...)`, `ImpactsCounters`, `HudLayout`, `ImpactsLog`, and the `ImpactsEvents.WaterFilled` event.
- **Events:** Lib fires `ImpactsModsReadyEvent` after common setup with the set of Impact mods loaded, so each mod turns its integration features on in one place. Impact mods post `ImpactsEvents.WaterFilled` whenever they fill a container item with water.
- **Recipe conditions:** recipes that change when another Impact mod is present ship in pairs, `forge:mod_loaded <id>` and `forge:not` of it. A mod may ship recipes of another Impact mod's recipe type (IRV's hammering recipe for its loose Air Tank uses IRC's type), always gated by `forge:mod_loaded` of that mod. Tag entries that point at another mod's items are `"required": false`.
- **One owner per vanilla recipe:** only one Impact mod may override a given vanilla recipe. The recipe audit records the owner, and the conflict checker fails if two mods override the same recipe ID.
- **Handlers pass through:** a right-click or interaction handler acts only on items it owns or that appear in the shared interactions table, and returns PASS for everything else, so the next mod, SRF or vanilla still gets the click.
- **Other mods' items:** a mod refers to another Impact mod's items only by registry ID, looked up at runtime, and changes their NBT only through Lib's APIs or under its own namespaced keys (`impactsvitality:purity`). It never removes another mod's keys, except when it empties a container it owns.
- **mods.toml:** Lib is `mandatory=true`, `ordering="AFTER"`, `side="BOTH"`. The other Impact mods and SRF are listed as `mandatory=false` with version ranges, so Forge reports incompatible versions instead of crashing.
- **Networking:** each mod registers its own channel with a protocol version. Lib's `impactslib:main` channel carries shared sync (counters, HUD layout, back slot).
- **Config types:** gameplay values go in each mod's SERVER config (per world, synced to clients), visuals (HUD, units, sounds) in CLIENT, and debug settings in COMMON. Anything a recipe condition reads also goes in COMMON, because recipe conditions are evaluated before a world's server config loads. Items read config values like durability when they're used (for example, by overriding `getMaxDamage`), not at registration. Lib's own config: `fuel.overrides`, `holdToUse.showProgressBar`, `grinder.*` (bowl and stick durability 64/256, grind speed 30/20 ticks), and `debug.*`.
- **Ownership rule for future Impact mods:** a new mod puts an item or system in Lib only when a second mod needs it. Otherwise it stays in the mod that introduced it.

## Configuration

Every Impact mod is configured through Lib's config framework, and every file lives in Lib's folder, so players and pack makers find all Impact settings in one place.

- **Where the files are:** `config/impactslib/` holds the global files: Lib's own, plus `<modid>-common.toml` and `<modid>-client.toml` for each installed Impact mod. Each world's gameplay values are in `<world>/serverconfig/impactslib/<modid>-server.toml`, synced to every player who joins. Pack makers put defaults in `defaultconfigs/impactslib/`, which Forge copies into every new world. Forge loads per-world and global settings at different times, which is why each mod has up to three files instead of one; Lib keeps them in one folder and named the same way.
- **Which file holds what:** `-common` holds feature and recipe toggles (anything a recipe condition reads) and debug settings. `-server` holds every gameplay number. `-client` holds HUD, sound and display options.
- **Everything is configurable.** Every number in every Impact spec (durability, ticks, chances, multipliers, capacities, thresholds) is a config value with that number as its default. Every feature can be switched off on its own, along with its recipes, and turning one off never breaks another. Item and block sets use tags wherever possible; anything else uses lists of `id=value` strings (for example, `timedCrafting.recipeOverrides = ["minecraft:chest=60"]`).
- **Built with** `ImpactsConfig`, a thin builder over Forge's `ForgeConfigSpec`. Every value has a comment, its default and its allowed range, grouped in sections named after the spec's sections. Files register as `impactslib/<modid>-<type>.toml`.
- **Changes while running:** Forge reloads a file when it's saved, and mods read values when they use them, so most changes apply right away. Recipe toggles apply on the next `/reload`. The few values that need a restart say so in their comment.
- **Bad values:** an out-of-range or unreadable value falls back to its default and logs a warning, which shows even with debug off. At startup each mod logs every value that differs from its default.
- **In game:** `/impacts config <mod> [key] [value]` (operator level 2) shows and changes server values and saves them to the file. With the optional Configured mod installed, every Impact file also gets an in-game config screen with no extra code.
- **Lib's own files:** `impactslib-common.toml` holds `debug.allMods` (a master switch over every mod's debug setting, default true), `debug.logFile.enabled` true, `debug.logFile.sizeMb` 10, `debug.logFile.count` 3 and `commands.permissionLevel` 2. `impactslib-server.toml` holds `fuel.overrides` and `grinder.*` (bowl and stick durability 64/256, grind speed 30/20 ticks). `impactslib-client.toml` holds `holdToUse.showProgressBar` and HUD layout offsets.

## Debug logging

Every Impact mod writes what it does to the log, so bugs can be traced from a player's log file. **Debug logging is on by default.**

- **Switches:** each mod's COMMON config has `debug.enabled` (default true), `debug.categories` (default: all), and `debug.verbose` (default false). `/impacts debug <mod|all> <on|off> [category]` changes them while the game runs (operator level 2). Client-side categories (HUD, screens) follow the same command when it's run in single player, or the client config on a server.
- **Where it goes:** `logs/latest.log` at INFO level, so it shows up without changing Forge's log settings, and a separate rolling file, `logs/impacts-debug.log` (10 MB, 3 files kept), with only Impact lines. Warnings and errors are always logged, whether debug is on or off.
- **Format:** one line per event, easy to search: `[Impact/<mod>/<category>] <event> key=value ...`. Example: `[Impact/crafting/hammer] finished player=Steve item=impactscrafting:loose_wooden_pickaxe result=minecraft:wooden_pickaxe hammer=impactscrafting:stone_pebble usesLeft=31 ticks=40 pos=12,64,-30 side=server`.
- **What gets logged:** every action a mod performs that a player could notice. That means crafts, refusals and their reasons, tool wear and breaks, state changes, items given or dropped, and integrations turning on. At startup each mod also logs its version, the Lib version, which Impact mods and SRF it found, which recipe sets are active, config values that differ from the defaults, tag sizes and recipe counts, and the audit and conflict-check summary.
- **No flooding:** systems that run every tick log only changes (a cauldron starting or stopping, a temperature state changing), never each tick. Identical lines within 1 second fold into one with `(xN)`. `debug.verbose` adds the detailed lines, like every ambient temperature breakdown or worldgen placement.
- **Cost:** every debug line is guarded with `ImpactsLog.enabled(mod, category)` before the message is built, and messages are parameterized. With debug off, the cost is one boolean check.
- **API:** `ImpactsLog.of("impactscrafting")` returns a logger with `debug(category, event, keyValues...)`, `warn(...)` and `error(...)`. Each mod registers its category names, so the config and the command can list them.
- **Debug commands:** `/impacts dump player <name>` (thirst, temperature, air, `activeSlot`, back slot, counters), `/impacts dump held` (the held item's ID, NBT, recipe depth, tier and craft time), `/impacts dump block` (cauldron boiling state, table wear, vat contents), `/impacts audit` (which mod replaced or gated each vanilla recipe right now), and `/impacts conflicts` (runs the conflict checker on the live recipe set). Each mod adds its own `dump` sections through Lib.
- **Bug reports:** every mod page asks players to attach `logs/impacts-debug.log`.
- **Lib's own categories:** `startup`, `registry`, `toolcrafting`, `grinder`, `holdtouse`, `uses`, `giveordrop`, `counters` (milestones only), `layout`, `providers`, `conflicts`, `audit`.

## Family compatibility and testing

**Shared interactions.** Some items get actions from more than one Impact mod. Each action uses a different input, so they never collide:

| Item (owner) | Input | Action (mod) |
| --- | --- | --- |
| Water-filled Wooden Bucket (IRC) | Use on a block | Pour the water (IRC) |
| Water-filled Wooden Bucket (IRC) | Sneak + use on the top of a block | Set it down as a stone-boiling pot (IRF) |
| Water-filled Wooden Bucket (IRC) | Hold use while aiming at the air | Drink: 4 drinks per fill, no bucket wear (IRV) |
| Water-filled Wooden Bucket (IRC) | Sneak + use while aiming at the air | Pour it over yourself to cool off (IRV) |
| Glass bottle (vanilla) | Use on water, a cauldron or a Rain Barrel | Fill it with water of that source's purity (IRV) |
| Glass bottle (vanilla) | Use on a cauldron holding soup | Refused (IRF, event priority HIGH) |
| Glass bottle (vanilla) | Use in Good air, not aiming at a fluid or block | Bottle the air (IRV) |
| Stone Pebble (IRC) | Hold use on a lit campfire for 2 seconds | Heat it into a Hot Pebble (IRF) |
| Cauldron or campfire bucket holding a finished soup (IRF) | Empty hand | Eat a serving straight from the pot (IRF) |
| Cauldron or campfire bucket holding a finished soup (IRF) | Any empty bowl | Fill the bowl (IRF) |
| Campfire bucket or stone-boiling pot (IRF) | Sneak + empty hand | Take the bucket back (IRF) |
| Water cauldron (vanilla) | Bottles, bowls and items | IRV fills bottles (Purified if Lib says it's boiling), IRF takes boiling items and serves soup, SRF washes ore. Each handler passes on anything it doesn't recognize |
| Lit campfire (vanilla) | Spit, sneak + water bucket, food, pebble | IRF's spit, IRF's campfire bucket, vanilla campfire cooking, IRF's Hot Pebble |
| Grinder bowl (Lib) | Click an item onto it in any inventory screen | Load it, like a bundle (Lib) |

**Test setups.** Each setup must start a dedicated server, pass every GameTest, pass the recipe audit and conflict checker, and log zero Impact warnings or errors (debug lines are fine).

| Setup | What it proves |
| --- | --- |
| Lib alone | "Why am I here?" appears; nothing is craftable |
| Each mod alone (4 setups) | Every mod runs on its own |
| IRC + IRF | Recipe switching, stone boiling, IRF timed by IRC |
| IRC + IRV | Gear recipes with IRC parts, linings by Bone Needle, drinking from a Wooden Bucket |
| IRC + IRI | The crafting arrow in IRI's screen, hammering with two hand slots, byproducts into pockets |
| IRF + IRV | A boiling pot fills Purified Water, dry foods, soups hydrate, Dough takes Purified Water |
| IRI + IRV | The back-slot API and the HUD layout |
| All four | Everything at once |
| All four + SRF (full), and all four + SRF ("no tools") | Cauldron washing, off-hand steps, no recipe clashes |

**Harness:** one Gradle composite workspace, `impacts-dev`, with Lib and the four mods as included builds and the SRF jar in a local `libs/` folder. It has a `runGameTestServer` task for each setup, each with its own mods folder.

**Recipe conflict checker:** runs in datagen and as `/impacts conflicts`. It loads the full recipe set and reports any two crafting recipes that could match the same grid: the same shape, or the same ingredients for shapeless recipes, with tools counted as ingredients. It also reports any Impact recipe that collides with a remaining vanilla or SRF recipe, and any vanilla recipe overridden by two Impact mods. Any hit fails the build.

**Salty's Realistic Forging (decompiled copy).** Claude Code will get a decompiled copy of SRF 4.1.6 (1.20.1 Forge).

- Keep it outside every Impact repo (for example `reference/srf/`), never commit it, and never copy its code or assets into an Impact mod. It's for reading only.
- From it, record SRF's mod ID, every item and block ID, its recipe and loot JSONs, its dependencies (some builds need playerAnimator), and its MCreator procedures, especially right-click handlers.
- **IRC:** check for recipe ID or output clashes, and find SRF's hammer, tongs, iron sheet and iron bar items for IRC's optional tag entries.
- **IRF:** list every SRF cauldron interaction (items, hand, water-level or blockstate checks) and make sure IRF's handlers pass them through.
- **IRI:** find every place SRF reads or writes a specific inventory slot, assumes a 9-slot hotbar, or gives items (`giveItemToPlayer`, `setItem`). IRI's locks must redirect these, and SRF's main-hand and off-hand steps must work with the two hand slots.
- **IRV:** find SRF blocks or items that are hot (forges, heated metal) and add them to `#impactslib:heat_sources` as optional entries.
- At startup, each mod logs what it found under its `compat` category, for example "SRF 4.1.6 detected; 6 optional tag entries resolved".

## Achievements

Lib has **one achievement of its own**, and only when it's alone:

| Achievement | How to get it | Type |
| --- | --- | --- |
| **Why am I here?** (its own one-entry tab, "Impact's Lib", with a plain book icon) | Join a world where Impact's Lib is installed but no other Impact mod is | task, granted on login |

- Each Impact mod registers itself with Lib during mod construction (`ImpactsLib.registerImpactMod("impactscrafting")`, and so on). Future Impact mods do the same, so the check never needs updating.
- The advancement is gated by a custom recipe/advancement condition, `impactslib:no_impact_mods`, which is true only when that registry is empty. With any Impact mod installed, the advancement and its tab don't exist at all.
- Granted by a login hook through the trigger `impactslib:joined_alone`.

Beyond that, Lib provides the shared pieces every Impact mod's achievements are built from:

| Piece | What it does | Used by |
| --- | --- | --- |
| `impactslib:tool_crafted` trigger | Fires when a `tool_crafting` recipe is taken; can filter by tool tag and result | IRC (Cut to Size, Sew What?), IRF (Cut It Out, Something Fishy) |
| `impactslib:hold_action` trigger | Fires when a hold-to-use action finishes; can filter by action ID and item | IRC (Hammer Time, Pushing It, Grind Set), IRF (Round and Round) |
| `impactslib:count` trigger + per-player counters | Named counters (optionally per item) saved with the player and kept through death. The trigger fires when a counter reaches a value | IRC (Scrap Collector, Master Craftsman), IRF (Day in and Day out, Taste Tester), IRI (Second Nature), IRV (Well Watered, In Balance) |
| Advancement datagen helper | Builds an advancement with an optional `forge:mod_loaded` condition, for achievements that need another Impact mod | IRF (Stone Soup) |
| Tab conventions | Each mod gets one tab named after it; challenges give 50 XP; joke achievements are hidden until earned | All Impact mods |

The counters API is `ImpactsCounters.add(player, "<modid>:<counter>", item, amount)` and `get(...)`. Counters sync to the client only when an achievement or tooltip needs them.

## Versioning, milestones and acceptance

- **Versioning:** semantic versions. Every Impact mod declares a Lib range like `[1.0,2.0)`. Minor versions only add API. A mod that needs a newer addition raises its lower bound (for example `[1.2,2.0)`). Breaking API changes bump the major version and ship together with updated releases of every Impact mod. Each mod logs the Lib version it found at startup.
- **Client and server:** both must run the same Lib minor version. Lib's channel version is its minor version, so Forge refuses a mismatched join with a clear message instead of failing later.

**Milestones**

- Skeleton mod, standalone release setup (CurseForge, Modrinth, Maven), `ImpactsLib.isLoaded`, the ready event, `ImpactsLog` with the debug config, and `ImpactsConfig` with the shared `impactslib` config folder. Logging and config come first so every later system logs and is configurable from day one.
- `tool_crafting` recipe type with tool damage, byproducts, remainders and `copy_nbt_from`.
- Uses-based durability, wood type data, fuel and compost helpers.
- Hold-to-use framework.
- Grinder items, the grinding type, and bundle-style bowl loading.
- Heat and cold source tags, and `isHeated` with lit-state checks.
- Providers and helpers: boiling state, back slot, crafting progress, Give or drop, the water bottle ingredient, the water-filled event, and the HUD layout manager. Each returns a safe default when no provider is registered.
- Datagen helpers (recipe pairs, audit base), the recipe conflict checker, and the JEI base plugin.
- `/impacts` commands: `debug`, `dump`, `audit`, `conflicts`, `config`.
- The `impacts-dev` workspace and the GameTest setups from Family compatibility and testing.
- API docs: a short API.md with an example of each system, for the next Impact mods.
- Move IRC onto Lib: switch IDs, delete the duplicated code, run IRC's tests.

**Acceptance criteria**

- With Lib alone, the game starts and nothing is craftable from Lib.
- Launching any Impact mod without Lib shows Forge's "missing dependency: Impact's Lib" screen, not a crash.
- With IRC + IRF, there's exactly one set of grinder items and one set of grinder recipes.
- Removing any Impact mod from a world leaves the others working. Its items disappear with Forge's usual missing-entries warning, and nothing crashes.
- An old IRC world loads after the move to Lib, with old IDs remapped (`MissingMappingsEvent`).
- With Lib alone, joining a world grants "Why am I here?". With any Impact mod installed, that achievement and its tab don't appear.
- With no provider registered, `isBoiling` returns false, `BackSlot.get` returns an empty stack and `CraftingProgress.get` returns nothing. None of them throw.
- `giveOrDrop` never deletes an item. Whatever doesn't fit (a full inventory, or slots IRI has locked) drops at the player's feet, and both outcomes are logged.
- With default settings, every Lib system writes its documented debug lines to `latest.log` and `logs/impacts-debug.log`. With `debug.enabled=false`, only warnings and errors appear. `/impacts debug` turns categories on and off without a restart.
- Every Impact config file sits under `config/impactslib/` or `serverconfig/impactslib/`. Every number in every spec has a config value with that default, an edited file applies without a restart (recipe toggles on `/reload`), and an out-of-range value falls back to its default with a warning.
- Every test setup in Family compatibility and testing passes.

## Implementation changes

Changes made during implementation, following the handoff rule "follow the API and note the change".

- **Platforms (decided Nov 14, 2026):** 1.20.1 Forge first, then NeoForge 1.20.1, then Forge and NeoForge up to 1.21. No Fabric. Every mod is split into a `common` module (vanilla code) and a `forge` module, and a `neoforge` module gets added at port time.
- **Build order:** Lib is built in slices. The core comes first, and each later system lands with the first mod that uses it (see ROADMAP.md). The Versioning section allows this, since minor versions only add API.
- **Saved data:** player and world data use vanilla NBT (mixins, `SavedData`) in common code instead of Forge capabilities, because NeoForge replaces capabilities in 1.20.5.
- **ImpactsConfig:** the builder API is in common. On Forge it wraps `ForgeConfigSpec`, as specified.
- **ImpactsModsReadyEvent:** posted on the Forge event bus from enqueued common-setup work, so it fires after every mod's common setup. Common code can listen through `ImpactsLib.onModsReady(...)`.
- **FuelHelper (planned):** to be named `FuelRegistry`, with compost registration split out as `CompostRegistry` (clean-naming rule 6). It lands with the IRF slice.
- **Old IRC IDs:** assumed never released, so no `MissingMappingsEvent` remap is planned.
- **ForgeModConstruction (added):** Forge 47.4 deprecates the static loading-context getters. Every Impact mod's Forge entry point takes `FMLJavaModLoadingContext` in its constructor and runs its construction steps through `ForgeModConstruction.run(context, ...)`, so Lib registers that mod's config files with the right mod.
- **Config value checks:** Forge clamps an out-of-range number to its nearest bound. Lib checks ranges itself instead, so the value falls back to its default as this spec says. A warning names the key, the value and the allowed range, and the user's value stays in the file.
- **Config log lines:** when a file loads, its non-default values are logged under `startup`. On each reload, only the values that changed are logged, under a new shared category, `config`, which every Impact mod uses. Range warnings use `config` too.
- **SRF detection in startup lines:** waits for the SRF pass (ROADMAP step 6), because SRF's mod ID is only known from its jar.
