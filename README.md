# Impact's Realistic Mods

> **Made with AI.** This project was made with artificial intelligence: its code, data and documentation were written in large part by Claude Code (an AI coding assistant by Anthropic), directed and reviewed by the project's author. **Any fork or derivative work must keep this disclosure and state its own AI origins and usage.** See [NOTICE](NOTICE).

A family of Minecraft mods that make survival more hands-on. Each mod works on its own with Impact's Lib, and the mods connect automatically when they're installed together.

| Mod | Mod ID | What it adds | Status |
| --- | --- | --- | --- |
| [Impact's Lib](impacts-lib/SPEC.md) | `impactslib` | Shared systems every Impact mod needs. Required by all of them | Core done |
| [Impact's Realistic Inventory](impacts-inventory/SPEC.md) | `impactsinventory` | Two hand slots, nine pockets, satchels and backpacks. Also requires [Curios API](https://www.curseforge.com/minecraft/mc-mods/curios) | Working on its own; integrations come with the other mods |
| [Impact's Realistic Food](impacts-food/SPEC.md) | `impactsfood` | Knives, boiling, soups, spit roasting, cooking styles and baking | Done on 1.20.1 Forge, standalone; Crafting and Vitality integrations come with those mods |
| [Impact's Realistic Vitality](impacts-vitality/SPEC.md) | `impactsvitality` | Thirst, temperature and air | Done on 1.20.1 Forge, standalone; Crafting integration comes with Crafting |
| [Impact's Realistic Crafting](impacts-crafting/SPEC.md) | `impactscrafting` | Multi-step crafting from shared parts, timed and tiered tables | Planned |

The build order and progress are in [ROADMAP.md](ROADMAP.md).

## Platforms

1. Minecraft 1.20.1 on Forge, until every mod is established.
2. Minecraft 1.20.1 on NeoForge.
3. Forge and NeoForge for each Minecraft version from 1.20.2 up to 1.21.

There is no Fabric version.

## Building

You need JDK 17 installed. Gradle finds it through toolchains.

```
./gradlew build
```

Each mod's jar is written to `<mod>/forge/build/libs/`. To run a mod's GameTests on a headless server:

```
./gradlew :impacts-lib:forge:runGameTestServer
```

## Repository layout

| Path | Contents |
| --- | --- |
| `<mod>/SPEC.md` | The mod's design specification |
| `<mod>/common/` | Code and data that don't depend on a mod loader |
| `<mod>/forge/` | Forge-specific code, `mods.toml` and run configurations |
| `CLAUDE.md` | Working rules for AI-assisted development sessions |
| `.claude/skills/` | The project's coding conventions |

## License

Apache License 2.0. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
