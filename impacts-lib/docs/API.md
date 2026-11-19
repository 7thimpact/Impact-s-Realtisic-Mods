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
- **`key=value` lists:** `KeyValueEntries.find`, `findInt` and `findDouble` read lists such as `["minecraft:beef=3"]`. Entries that don't parse are skipped, and the first entry for a key wins.

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

`ResourceIds.itemId(item)` and `ResourceIds.blockId(block)` give an item's or block's registry ID from loader-independent code.

- Create every `ResourceLocation` through `ResourceIds.of(namespace, path)`. Forge 47.4 deprecates the constructor ahead of 1.21, so a port changes only that one method.
- Register advancement triggers through `LoaderPlatform.current().registerCriterionTrigger(trigger)`, on the main thread (from `ImpactsLib.onModsReady`). Vanilla keeps the registration method private.
- For an event with no conditions of its own, use `new EventTrigger(ResourceIds.of(modId, "pack_placed"))`: fire it with `trigger(player)`, and use `anyPlayer()` as the criterion in data generation.
- Registries: `LoaderPlatform.current().registrar(modId)` registers items, blocks, menus and recipe serializers during mod construction; `blockEntityType(...)` and `menuTypeWithData(...)` build the types vanilla keeps closed, and `openMenuWithData(...)` opens a menu with extra data.

## GameTests with a logged-in player

`MockPlayers.joinWorld(helper.getLevel())` joins a mock player through a real login, so login hooks run. Vanilla's `makeMockServerPlayerInLevel` fails under Forge, because its connection has no network channel. Always call `MockPlayers.leaveWorld(player)` in a `finally` block.

**Config values in tests:** never `set` a config value in a GameTest. Saving the file makes Forge reload it on another thread, which can put a test's value back after the test ends (and keep it in the test world's file). Use `ConfigOverrides.withValue(value, override, () -> check)`, which holds the value in memory only (`ConfigValue.overrideInMemory` and `clearOverride`).

`CraftingGrids.of(stacks...)` builds a 3x3 crafting grid filled from the top left, for matching and crafting recipes in tests. Set the crafting player with Forge's `ForgeHooks.setCraftingPlayer(player)` (and clear it in a `finally` block) to test byproducts.

## Give or drop

```java
ImpactsItems.giveOrDrop(player, new ItemStack(ModItems.WOOD_SCRAPS.get()));
```

Inserts through vanilla's inventory, so Inventory's locked slots apply, and drops whatever doesn't fit at the player's feet. Never deletes an item, logs both outcomes under `giveordrop`, and does nothing on the client.

## Back slot

```java
if (BackSlot.isAvailable() && BackSlot.accepts(stack)) {
    BackSlot.set(player, stack);
}
ItemStack worn = BackSlot.get(player);   // empty when no mod provides a back slot
```

Inventory registers the provider with `BackSlot.registerBackSlotProvider("impactsinventory", provider)`. Other mods make an item fit by adding it to `#impactslib:back_slot_items`. `isAvailable()` is false when no mod provides a back slot, or when the provider has it switched off (Inventory's `packs.backSlot`). `set` returns false and changes nothing then, and also when that player has no back slot; the caller still holds the stack, so give it back with `ImpactsItems.giveOrDrop`. Inventory's back slot is Curios's `back` slot, so Curios-aware mods see it too.

## Crafting progress

Crafting registers a source that reads its menus' synced progress: `CraftingProgress.registerSource("impactscrafting", menu -> ...)`. Any screen draws it:

```java
CraftingProgress.get(menu).ifPresent(progress -> {
    CraftingProgressRenderer.renderArrow(graphics, left + arrowX, top + arrowY, progress);
    CraftingProgressRenderer.renderFadedOutput(graphics, left + outputX, top + outputY, progress);
});
```

- `renderArrow` fills a furnace-style arrow (24 by 16 pixels): crafting tables, furnaces and most modded screens.
- `renderInventoryArrow` fills the smaller arrow (16 by 13 pixels) of vanilla's inventory screen and of Inventory's screen.
- A screen that draws the progress itself implements `DrawsOwnCraftingProgress`. Code that draws over other mods' or vanilla's screens skips those screens, so the arrow is never drawn twice.

## HUD layout (client)

```java
HudLayout.publishHotbarBounds("impactsinventory", (width, height) -> new HudBounds(...));   // Inventory
HudBounds hotbar = HudLayout.hotbarBounds(screenWidth, screenHeight);                      // Vitality's gauge
int thirstRowTop = HudLayout.claimRightRow(screenHeight, ROW_HEIGHT);                    // above hunger
```

Rows stack on Forge's own left and right row heights, so they never overlap vanilla's bars or each other. Lib's client config can shift every Impact row (`hud.rowOffsetX`, `hud.rowOffsetY`).

## Network channels

```java
NetworkChannel channel = LoaderPlatform.current().createChannel(ResourceIds.of(MOD_ID, "main"), PROTOCOL_VERSION);
channel.registerClientbound(ActiveSlotMessage.class, ActiveSlotMessage.CODEC, message -> InventoryClient.applyActiveSlot(message));
channel.registerServerbound(SwitchSlotMessage.class, SwitchSlotMessage.CODEC, (message, sender) -> HotbarSwitch.apply(sender, message));
```

Register every message on both sides, in the same order, during mod construction. Handlers run on the main thread. Keep client-only classes out of the registering class: call into a separate client class from inside the handler. Lib's own `impactslib:main` channel (version = Lib's major.minor) resends changed SERVER config files to remote players.

## Player attachments

```java
public static final PlayerAttachment<ThirstData> THIRST = PlayerAttachments.register(new PlayerAttachment<>(
        ResourceIds.of(MOD_ID, "thirst"), ThirstData::new, DeathCopyRule.NEVER));

ThirstData thirst = PlayerAttachments.get(player, THIRST);
```

Data classes implement `PlayerAttachmentData` (`save`, `load`). Lib's mixin saves every attachment in the player's NBT under `impactslib:attachments` and copies it on respawn by its `DeathCopyRule`: `ALWAYS`, `LIKE_INVENTORY` (kept with keepInventory) or `NEVER`. Returning from the End keeps everything. Register attachments during mod construction. Syncing to the client is each mod's job.

## Counters and the count trigger

```java
ImpactsCounters.add(player, "impactsfood:styled_food_eaten", stack.getItem(), 1);
int eaten = ImpactsCounters.get(player, "impactsfood:styled_food_eaten", Items.BREAD);
```

Counters are kept through death. Advancements use `impactslib:count` through `CountTrigger.TriggerInstance`:
- `total(counter, value)`
- `item(counter, itemId, value)`
- `anyItem(counter, value)`: any single item reached the value ("Day in and Day out")
- `distinctItems(counter, value)`: that many different items ("Taste Tester")

## Advancements and conditional data

- `ImpactsAdvancementBuilder.of(id, icon).parent(...).criterion(...).challenge().build()` follows the tab conventions. Its translation keys come from the ID: `advancements.<modid>.<path>.title` and `.description`.
- Each mod's Forge data generation extends `ImpactsAdvancementProvider` and can attach Forge conditions, for example `ConditionalRecipes.modLoaded("impactscrafting")`.
- `ConditionalRecipes.pair(output, "impactscrafting", withoutCrafting, withCrafting)` writes both versions of a recipe, so exactly one loads.

## Mixins

A mod with mixins puts `<modid>.mixins.json` in its common resources. The build then adds the refmap and the `MixinConfigs` manifest entry. Name every added member `<modid>$name` (for example `impactslib$attachments`).

## Tool crafting (`impactslib:tool_crafting`)

A crafting recipe, shaped (`pattern` + `key`) or shapeless (`ingredients`), whose `tools` stay in the grid in any slot and take `tool_damage` (default 1). A tool is anything with durability: vanilla damage, or Lib's uses.

```json
{
  "type": "impactslib:tool_crafting",
  "ingredients": [{"item": "minecraft:beef"}],
  "tools": [{"tag": "impactsfood:knives"}],
  "result": {"item": "impactsfood:raw_beef_cut", "count": 2},
  "byproducts": [{"item": "minecraft:bone", "chance": 0.15}],
  "remainders": [{"ingredient": {"type": "impactslib:water_bottle"}, "result": {"item": "minecraft:glass_bottle"}}],
  "craft_ticks": 20,
  "copy_nbt_from": {"tag": "impactscrafting:wooden_panels"}
}
```

- Byproducts go to the crafting player through Give or drop; `chance` defaults to 1.
- The `impactslib:tool_crafted` trigger takes optional `result`, `tool` and `byproduct` item predicates.
- Other mods' special recipes get the same tool behavior from `ToolCrafting.damageTools(grid, isTool, damage)`.
- Tool recipes stay out of the vanilla recipe book (it can't place tools); JEI shows them in Lib's category.
- **Config overrides:** `ToolCrafting.registerOverrides(namespace, overrides)` lets a mod's config change the result count (`resultCount`) and byproduct chances (`byproductChance`, where 0 turns a byproduct off) of every tool recipe in its namespace. Food uses it for `cutting.yieldOverrides`, `cutting.byproductChanceOverrides` and `cutting.byproducts`.
- **Craft listeners:** `ToolCrafting.onCrafted(craft -> ...)` runs on the server after every tool craft. The `CompletedToolCraft` record holds the player, the recipe, the result, the tools before and after (an empty stack for a tool that broke) and the byproducts given.
- **Data generation:** `ToolCraftingRecipeBuilder.of(result, count)` (in Lib's Forge module) writes these recipes: `requires(...)` for shapeless ones, `pattern(...)` and `define(...)` for shaped ones, then `tool`, `toolDamage`, `byproduct`, `remainder`, `craftTicks`, `copyNbtFrom` and `save(output, id)`. Wrap the output with `ConditionalRecipes.withConditions` for conditions.

## Creative tabs

`CreativeTabs.register(modId, iconSupplier, itemsSupplier)` registers the mod's tab as `<modId>:items` during mod construction, titled by the translation key `CreativeTabs.titleKey(modId)` (`itemGroup.<modId>`). The suppliers are read when the tab is filled.

## Uses and configured durability

- `UsesTrackedItem` (implement `maxUses(stack)`) plus `UsesDurability.get/use/isUnused/copyUses`: uses spent live in the `uses` NBT key, absent while unused, and a used item stacks to 1. Override the item's bar methods with `UsesDurability.isBarVisible/barWidth/barColor`.
- `ConfiguredDurabilityItem` (implement `configuredMaxDamage()`): vanilla durability read from a config value whenever it's needed. Register the item with any durability above 0.

## Fuel, compost, heat and boiling

```java
FuelRegistry.register(ModItems.BURNT_FOOD.get(), 400);                   // or a function of the stack
CompostRegistry.register(ModItems.MUSH, 0.65F);                           // applied once every mod is ready
ImpactsLib.isHeated(level, pos);                                          // #impactslib:heat_sources, lit if lightable
ImpactsLib.registerBoilingProvider("impactsfood", (level, pos) -> ...);   // ImpactsLib.isBoiling(level, pos)
```

`fuel.overrides` in Lib's server config (`item=ticks`) wins over registered burn times. A compost chance can also be a supplier read from config (`CompostRegistry.register(item, () -> chance)`); Lib applies chances once every mod is ready and again at every server start. `#impactslib:cold_sources` holds ice and snow for Vitality.

## Eating without the hold

`LoaderPlatform.current().postItemUseFinished(entity, used, useTicks, result)` tells other mods an entity finished using an item (Forge's `LivingEntityUseItemEvent.Finish`). Food calls it for a serving eaten straight from a pot, after the soup's own `finishUsingItem`, so mods that react to eating (Vitality's thirst and warmth) see it like a bowl.

## Water bottles and the water-filled event

- Ingredient `{"type": "impactslib:water_bottle"}` matches a vanilla water bottle or anything in `#impactslib:water_bottles`.
- `ImpactsEvents.postWaterFilled(new WaterFilled(player, stack, level, pos, boiled))` after a mod fills a container item. Listen in common code with `ImpactsEvents.onWaterFilled(...)`, or on Forge's bus with `ImpactsWaterFilledEvent`.

## Hold-to-use

An item opts in by registering a `HoldAction` and delegating its use methods:

```java
HoldToUse.register(ModItems.IRON_HAMMER.get(), new HammeringAction());     // once every mod is ready
// In the item: use -> HoldToUse.start(...), onUseTick -> HoldToUse.tick(...), releaseUsing -> HoldToUse.release(...),
// getUseDuration -> HoldToUse.USE_DURATION_TICKS, getUseAnimation -> HoldToUse.USE_ANIMATION (UseAnim.NONE).
```

- A `HoldAction` gives its `id()`, `cycleTicks(player, held)`, `canContinue(player, held)` (both sides, every tick), `completeCycle(serverPlayer, held)` (returns whether to keep going) and a `motion()` (`STIR`, `HAMMER`, `TURN`).
- Vanilla's item-use hold ends it: letting go, switching hands, eating. The cycle in progress is lost, nothing else.
- Each finished cycle swings the arm, fires `impactslib:hold_action` (optional `action` ID) and logs under `holdtouse`. With playerAnimator installed, the arms follow the action's motion. A bar under the crosshair shows the cycle (`holdToUse.showProgressBar`).

## Grinding (`impactslib:grinding`)

- Items: `impactslib:wooden_grinder_bowl`, `stone_grinder_bowl` (off hand; 64 / 256 uses), `wooden_grinding_stick`, `stone_grinding_stick` (main hand, hold right-click; 30 / 20 ticks per item). The mods ship their recipes; Lib ships none.
- A bowl holds up to `grinder.bowlCapacity` (16) items of at most 2 kinds. Load it by right-clicking with the item in the main hand, or by right-clicking the item onto the bowl in any inventory screen. Sneak-right-click with an empty main hand empties it.
- Recipe JSON: `ingredients` (1 or 2), `result` (with `count`), `min_tier` (`wood` or `stone`), optional `grind_ticks` and `byproducts`. The bowl's tier limits the grind.

## Recipe audit and conflict checker

- `/impacts audit`: which Impact mod replaced or turned off each vanilla recipe right now. Forge merges every mod's data into one pack, so the audit asks the loader which mod files hold each `data/minecraft/recipes/` file (`LoaderPlatform.modsWithDataFile`).
- To turn a vanilla recipe off, write it under its own ID with a condition that fails while your feature is on (Food writes `minecraft:melon_seeds` gated by `forge:not` of its cutting switch). The audit then counts it as turned off.
- `/impacts conflicts`: any two crafting recipes that could match the same grid, where at least one is an Impact recipe, plus any vanilla recipe two Impact mods both override.
- Both run at every server start and `/reload`. Each conflict logs a warning, so a conflict fails every family test setup.

## JEI base plugin

Lib's plugin (`impactslib:jei`) shows every `tool_crafting` and `grinding` recipe in two shared categories. Mods add catalysts to them through `LibJeiRecipeTypes.TOOL_CRAFTING` and `LibJeiRecipeTypes.GRINDING` in their own JEI plugins. A mod that has JEI pages sets `mod_uses_jei=true` in its `gradle.properties` (compile-only API); `mod_runs_with_jei=true` also loads JEI in its runs.
