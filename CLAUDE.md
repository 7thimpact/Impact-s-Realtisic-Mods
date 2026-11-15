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

- One milestone per session. ROADMAP.md lists them in order.
- For each milestone: implement it, run the checks below, tick it off in ROADMAP.md, commit, push, then stop and report for review.
- Work on branch `claude/minecraft-mods-dev-tz2j7p`.
- If a spec and the real Minecraft or Forge API disagree, follow the API. Record the change under "Implementation changes" at the end of that mod's SPEC.md.

## Checks before every commit

- `./gradlew build`
- `./gradlew :<mod>:forge:runData` whenever generated data changes. Never hand-edit files under `src/generated/`.
- `./gradlew :<mod>:forge:runGameTestServer`, which must exit with code 0.
- HUD and screen work: take screenshots with `runClient` under `xvfb-run`, using software rendering.

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

- Every mod runs with only Impact's Lib installed.
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

- Reference only. Keep its jar and any decompiled copy outside the repo, for example `/home/user/reference/srf/`. `libs/` and `reference/` are git-ignored as a safety net.
- Never copy SRF's code or assets.
- Record findings in `<mod>/docs/srf-notes.md`.

## AI notice

- Keep `NOTICE` and the AI notice at the top of `README.md`.
- Every jar packs `LICENSE` and `NOTICE`, and every `mods.toml` description mentions that the mod was made with AI.

## Environment notes

- JDK 17 is required. If `/usr/lib/jvm/java-17-openjdk-amd64` is missing, run `apt-get install -y openjdk-17-jdk-headless`.
- Gradle itself may run on Java 21. Toolchains compile and run Minecraft on 17.
