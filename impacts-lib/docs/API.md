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
- Every COMMON file carries the debug section: `DebugConfigSection debug = DebugConfigSection.define(builder);`, then `ImpactsLog.of(MOD_ID).useDebugSettings(debug);`.

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
