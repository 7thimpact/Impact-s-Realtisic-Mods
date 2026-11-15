# Impact's Lib API

How the other Impact mods use Lib. Each section has a short example. The rule behind all of it: Impact mods never import each other's classes, only Lib's.

## Forge entry point

Every Impact mod's Forge entry point receives Forge's loading context and runs its loader-independent construction steps through `ForgeModConstruction.run`. That lets Lib register the mod's config files with the right mod.

```java
@Mod(InventoryMod.MOD_ID)
public final class ImpactsInventoryForge {
    public ImpactsInventoryForge(FMLJavaModLoadingContext context) {
        ForgeModConstruction.run(context, InventoryStartup::onModConstruction);
    }
}
```

## Registering with Lib and finding other mods

```java
ImpactsLib.registerImpactMod("impactsinventory");   // during mod construction
boolean hasCrafting = ImpactsLib.isLoaded("impactscrafting");   // cached lookup
ImpactsLib.onModsReady(impactMods -> enableIntegrations(impactMods));   // after every mod's common setup
```

On Forge, `ImpactsModsReadyEvent` is also posted on `MinecraftForge.EVENT_BUS` at the same moment.

## Config files

All files live under Lib's folder: `config/impactslib/<modid>-common.toml`, `config/impactslib/<modid>-client.toml` and `<world>/serverconfig/impactslib/<modid>-server.toml`.

```java
public final class InventoryServerConfig {
    public final HotbarSettings hotbar;

    private InventoryServerConfig(ConfigSpecBuilder builder) {
        builder.pushSection("hotbar", "The two-slot hotbar.");
        hotbar = new HotbarSettings(builder);
        builder.popSection();
        builder.register();
    }

    public static InventoryServerConfig register() {
        return new InventoryServerConfig(ImpactsConfig.builder("impactsinventory", ConfigType.SERVER));
    }

    public static final class HotbarSettings {
        public final ConfigValue<Integer> swapCooldownTicks;

        private HotbarSettings(ConfigSpecBuilder builder) {
            swapCooldownTicks = builder.defineInteger("swapCooldownTicks", 0, 0, 200,
                    "Ticks after switching slots before the new main-hand item can attack or be used.");
        }
    }
}
```

- Read values when you use them (`config.hotbar.swapCooldownTicks.get()`), never at registration. Saved changes then apply without a restart.
- Before the file loads, `get()` returns the default.
- **Bad values:**
  - A value outside its range falls back to its default, with a warning that names the key, the value and the allowed range. The user's value stays in the file.
  - A value Forge can't read is reset to its default by Forge.
- **Automatic config lines:**
  - When a file loads, every value that differs from its default is logged under `startup`.
  - On a reload, every changed value is logged under `config`.
- Every COMMON file carries the debug section: `DebugConfigSection debug = DebugConfigSection.define(builder);`. Defining it connects it to the mod's `ImpactsLog` and to `/impacts debug`.
- Every value can parse typed text (`value.parse("12")`), check its range (`value.isAllowed(12)`) and describe what's allowed (`value.allowedValues()`). `/impacts config` uses all three.

## Debug logging

```java
private static final ImpactsLog LOG = ImpactsLog.of("impactsinventory");

LOG.registerCategories(List.of("startup", "hotbar", "pickup"));
LOG.debug("hotbar", "switched", "from", 1, "to", 2, "source", "scroll");
// [Impact/inventory/hotbar] switched from=1 to=2 source=scroll

if (LOG.isEnabled("pickup")) {
    LOG.debug("pickup", "refused", "item", describeStack(stack));   // guard expensive values
}
LOG.verbose("screen", "slot layout applied", "slots", slotCount);   // only with debug.verbose
LOG.warn("locks", "redirected locked slot write", "index", 20, "caller", callerName);   // always written
```

- Debug lines go to `logs/latest.log` at INFO level, and to `logs/impacts-debug.log` (rolling, holds only Impact lines).
- Identical lines within one second fold into one line ending in `(xN)`.
- Debug lines follow the mod's `debug.enabled`, `debug.categories` and `debug.verbose`, plus Lib's `debug.allMods` master switch. Warnings and errors are always written.
- Every mod uses `ImpactsLog.STARTUP_CATEGORY` (`startup`) and `ImpactsLog.CONFIG_CATEGORY` (`config`).

## The /impacts command

Operators (`commands.permissionLevel` in Lib's COMMON file, default 2) can use:

| Command | What it does |
| --- | --- |
| `/impacts debug` | Shows every mod's debug switches, and the `debug.allMods` master switch |
| `/impacts debug <mod\|all>` | Shows one mod's switches, or every mod's |
| `/impacts debug <mod\|all> <on\|off> [category]` | Turns debug lines, or one category, on or off. Saved to the mod's COMMON file |
| `/impacts config <mod>` | Lists the mod's SERVER values with their defaults and allowed ranges |
| `/impacts config <mod> <key> [value]` | Shows one value, or changes it and saves the world's file. Unreadable and out-of-range values are refused |
| `/impacts dump player <name>` | Every Impact mod's state for one player |

`<mod>` takes the full mod ID (`impactsinventory`) or the short name from log lines (`inventory`). A category can only be turned on or off once the mod has registered it with `ImpactsLog.registerCategories`.

## Player dump sections

Each mod adds its own part of `/impacts dump player`:

```java
ImpactsDump.registerPlayerSection("inventory", player -> ImpactsDump.entries(
        "activeSlot", hotbarState.activeSlot(player),
        "backSlot", BackSlot.get(player)));
```

## Resource IDs and advancement triggers

- Create every `ResourceLocation` through `ResourceIds.of(namespace, path)`. Forge 47.4 deprecates the constructor ahead of 1.21, so a port changes only that one method.
- Register advancement triggers through `LoaderPlatform.current().registerCriterionTrigger(trigger)`, on the main thread (from `ImpactsLib.onModsReady`). Vanilla keeps the registration method private.

## GameTests with a logged-in player

`MockPlayers.joinWorld(helper.getLevel())` joins a mock player through a real login, so login hooks run. Vanilla's `makeMockServerPlayerInLevel` fails under Forge, because its connection has no network channel. Always call `MockPlayers.leaveWorld(player)` in a `finally` block.
