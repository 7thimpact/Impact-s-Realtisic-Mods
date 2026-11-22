# Impact's Realistic Mods: working rules

Read this file first in every session, then [ROADMAP.md](ROADMAP.md) (what comes next) and the `SPEC.md` of the mod you are working on.

## The project

- Five mods in one Gradle workspace (the specs call it `impacts-dev`):

  | Folder | Mod ID | Package |
  | --- | --- | --- |
  | `impacts-lib` | `impactslib` (required by every other mod) | `com.impact.lib` |
  | `impacts-inventory` | `impactsinventory` | `com.impact.realisticinventory` |
  | `impacts-food` | `impactsfood` | `com.impact.realisticfood` |
  | `impacts-vitality` | `impactsvitality` | `com.impact.realisticvitality` |
  | `impacts-crafting` | `impactscrafting` | `com.impact.realisticcrafting` |

- Each mod folder holds `SPEC.md` (the design, and the source of truth), `common/` (code and data that compile against vanilla Minecraft) and `forge/` (Forge-only code, `mods.toml`, run configurations). A `neoforge/` module gets added at port time.
- Platform order: Minecraft 1.20.1 Forge until every mod is established, then NeoForge 1.20.1, then Forge and NeoForge for each version from 1.20.2 up to 1.21. **No Fabric.**

## How a session works

- Work one milestone at a time, in ROADMAP.md's order.
- For each milestone: implement it, run the checks below, tick it off in ROADMAP.md, commit and push.
- Stop to report and ask for review after a milestone, unless the user has asked you to keep going. The current goal is to finish all mods: keep going, and save questions for the end of a major task (a whole mod).
- Work on branch `claude/minecraft-mods-dev-tz2j7p`.
- If a spec and the real Minecraft or Forge API disagree, follow the API. Record the change under "Implementation changes" at the end of that mod's SPEC.md.

## Checks before every commit

- `./gradlew build`
- `./gradlew :<mod>:forge:runData` whenever generated data changes. Never hand-edit files under `src/generated/`.
- `./gradlew :<mod>:forge:runGameTestServer`, which must exit with code 0. Each GameTest run deletes the run folder's Impact config files first (`run/config/impactslib`, `run/world/serverconfig/impactslib`), so tests always check the default values.
- `./gradlew runFamilyTestSetups` before finishing a mod: every family setup in `test-setups/` must pass. Setups with SRF need `libs/realisticforgingores-4.1.6.jar`.
- HUD and screen work: take screenshots with `tools/client-screenshot.sh <mod folder or test-setups/<name>> <output.png> [steps]` (a setup's folder starts a client with exactly that setup's mods). It starts the mod's Quick Play client on a virtual display in a copy of the GameTest world with cheats on, then runs the steps: chat commands, `key:<name>` presses, `click:`/`rclick:`/`move:<x>,<y>` mouse steps, `rdown`/`rup` to hold the right button, `wait:<seconds>`, and `shot:<file>` extra screenshots. It needs `xdotool` and ImageMagick (`apt-get install -y xdotool imagemagick`).

## Code rules

- Follow the four skills in `.claude/skills/` on every change: `variable-structure`, `self-documenting-code`, `clean-naming-coding`, `never-nesting`. Use Java casing: camelCase members, PascalCase types, UPPER_SNAKE_CASE constants.
- **Names a spec defines stay exactly as written.** That covers:
  - mod, item, block, tag and trigger IDs
  - NBT keys and config keys
  - the spec's class and package names (`config`, `compat`, `datagen`, `ImpactsConfig`, ...)

  They are the shared interface between the mods. Everything else follows the rules.
- **Loader split.**
  - Game logic, vanilla-class mixins, data and GameTest logic go in `common`.
  - Anything that imports `net.minecraftforge` goes in `forge`, behind a small interface defined in `common` (see `com.impact.lib.platform.LoaderPlatform`).
- **Saved data.** Player and world data are saved with vanilla NBT (mixins, `SavedData`) in `common` instead of Forge capabilities. NeoForge replaces capabilities in 1.20.5, so this avoids rewriting it for later ports.

## Family rules

These come from `impacts-lib/SPEC.md` ("Integration API and rules").

- Every mod runs with only Impact's Lib installed. The one exception: Impact's Realistic Inventory also requires Curios (its back slot is Curios's `back` slot; decided by the user, Nov 2026).
- No Impact mod imports another Impact mod's classes. Mods meet only through Lib's API, shared items, tags and recipe conditions (`forge:mod_loaded`, `forge:not`).
- Refer to other mods' items by registry ID at runtime. Tag entries for them use `"required": false`.
- Interaction handlers act only on items they own (or items in Lib's shared interactions table) and return PASS for everything else.
- NBT written onto other mods' items uses our own namespaced key. Never remove another mod's keys.
- Only one Impact mod may override a given vanilla recipe.
- **Config placement:**
  - SERVER: gameplay numbers.
  - CLIENT: visuals.
  - COMMON: debug settings and anything a recipe condition reads.

  Files live under `config/impactslib/` and `serverconfig/impactslib/`. Every number in a spec is a config value, with that number as its default.
- Each mod registers its own network channel with a protocol version.
- Never delete an item. Anything that might not fit goes through `ImpactsItems.giveOrDrop`.

## Debug logging

- Every feature adds its log lines in the same change, through `ImpactsLog`. Line format: `[Impact/<mod>/<category>] <event> key=value ...`.
- Systems that run every tick log only changes.
- Guard expensive messages with `ImpactsLog.enabled(...)`.
- When something misbehaves, read `logs/impacts-debug.log` first.

## Salty's Realistic Forging (SRF)

- Reference only. Keep its jar and any decompiled copy outside the repo. `libs/` and `reference/` are git-ignored as a safety net.
- Never copy SRF's code or assets.
- Findings are in `<mod>/docs/srf-notes.md`; shared facts are in `impacts-lib/docs/srf-notes.md`.
- To read the jar again: unzip the user's upload into `/home/user/reference/jars/`, decompile with Vineflower, then turn SRG names (`m_12345_`) into readable names with `tools/remap_srg_names.py <mappings.tsrg> <source folder> <output folder>`. The mapping file is ForgeGradle's `impacts-lib/forge/build/createMcpToSrg/output.tsrg`.
- For runtime tests, put the SRF jar in `libs/` (git-ignored). The build loads it, deobfuscated, when it is there.

## playerAnimator

- Optional dependency (`playeranimator`, Maven `dev.kosmx.player-anim:player-animation-lib-forge`). Lib draws hold-to-use motions (hammering, grinding, sewing, …) through playerAnimator when it is installed, and through its own client mixins on the arm and held-item poses (`HoldMotionPoses`) when it isn't, so the motion shows either way.
- Code that touches playerAnimator classes lives in its own Forge client classes, which load only when the mod is present.

## Curios

- Required by Impact's Realistic Inventory only (`curios`, Maven `top.theillusivec4.curios:curios-forge:<curios_version>` from https://maven.theillusivec4.top/). A mod opts in with `mod_requires_curios=true` in its `gradle.properties`: it then compiles against Curios's API jar, its runs load Curios, and every test setup that lists it loads Curios too.
- Impact's Realistic Vitality uses Curios optionally (headwear in Curios's `head` slot), with `mod_uses_curios=true`: it compiles against the API, but its own runs leave Curios out. Its Curios code lives in `com.impact.realisticvitality.forge.compat.curios` and loads only when Curios is present.
- IRI's back slot is Curios's `back` slot. IRI's data gives players that slot (`data/impactsinventory/curios/entities/player_back.json`, gated by `impactsinventory:back_slot_enabled`) and tags its packs `#curios:back`. Curios saves, syncs and drops it.
- Code that imports Curios lives in IRI's `forge` module (`com.impact.realisticinventory.forge.curios`), behind `BackSlotBackend` in `common`.

## AI notice

- Keep `NOTICE` and the AI notice at the top of `README.md`.
- Every jar packs `LICENSE` and `NOTICE`, and every `mods.toml` description mentions that the mod was made with AI.

## Environment notes

- JDK 17 is required. If `/usr/lib/jvm/java-17-openjdk-amd64` is missing, run `apt-get install -y openjdk-17-jdk-headless`. Without one, Gradle downloads a JDK 17 itself (the foojay resolver plugin in `settings.gradle`); keep toolchain auto-download on, since users' machines rely on it.
- Gradle itself may run on Java 21. Toolchains compile and run Minecraft on 17.
