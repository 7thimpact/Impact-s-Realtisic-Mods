# Impact's Realistic Inventory — Mod Spec

Nov 14, 2026 · @Supa

## Overview

Impact's Realistic Inventory (IRI) shrinks what a player carries to what two hands and a few pockets could hold. The hotbar becomes **two slots**, where the slot you're not using *is* your off hand. The inventory becomes **9 slots in a 3×3 grid**, laid out like a crafting table. Satchels and backpacks let players carry more, at a cost.

**Target platform**

- Minecraft **1.20.1**, **Forge** (Java 17), the same as the other Impact mods.
- Mod ID: `impactsinventory`, package `com.impact.realisticinventory`. Display name: Impact's Realistic Inventory.
- **Requires Impact's Lib** (`impactslib`) as a separate download, like every Impact mod.
- Optional: [Impact's Realistic Crafting](https://claude.ai/artifact/SaBQus4NifLnCSbvWYGWWL) (IRC), [Impact's Realistic Food](https://claude.ai/artifact/H85Ku1ujPuD8QUSVhTsUAF) (IRF), [Impact's Realistic Vitality](https://claude.ai/artifact/1MFojaKLikF4TDVS8HSyyS) (IRV), JEI, and Salty's Realistic Forging (SRF). It has to work with every vanilla and modded container screen.

**Goals**

1. What you hold is a real choice: two hands, and swapping between them is how you switch tools.
2. What you carry is limited, so trips, storage and backpacks matter.
3. Nothing vanilla breaks: off-hand shields, totems, arrows, maps and the other Impact mods' off-hand mechanics all keep working.
4. No item is ever silently deleted when inventory space runs out.

**Non-goals for v1:** item weight or encumbrance, item sizes (one item per slot regardless of size), changes to chests or other containers.

## The two-slot hotbar

The hotbar has two slots, 1 and 2. The active slot is your main hand and the other slot is your off hand. Switching slots swaps your hands: with a sword in slot 1 (active) and a pickaxe in slot 2, switching to slot 2 puts the pickaxe in your main hand and the sword in your off hand.

| You do | Result |
| --- | --- |
| Press 1 or 2 | That slot becomes active. If it already is, nothing happens |
| Scroll the mouse wheel either way | Switch to the other slot |
| Press the swap-hands key (F) | Switch to the other slot, the same as scrolling |
| Press 3 to 9 | Nothing. Those keys are freed for other bindings |
| Use an item while the other slot holds a shield, torch, totem, map or arrows | Works as vanilla off-hand use, because the other slot is the off hand |

**Rules**

- **Items stay put on screen.** Switching moves the highlight, not the items: the sword stays drawn in slot 1 and the pickaxe in slot 2. Only which hand they're in changes.
- Switching plays vanilla's re-equip animation on both hands and resets the attack cooldown, like changing held items in vanilla.
- Switching while eating, drinking, drawing a bow or doing any hold-to-use action (IRC hammering, grinding) cancels that action.
- An empty slot is an empty hand. Two empty slots means both hands are free (needed for IRF's spit turning and similar empty-hand actions).
- Pick block (middle click) puts the item in the active slot. Whatever was there moves to the pockets through Lib's Give or drop, or drops at your feet if they're full.
- Config `hotbar.swapCooldownTicks` (default 0) can add a short delay before the new main-hand item can attack or be used, for a slower, more deliberate feel.

**HUD**

- The hotbar shows two slots side by side, centered, labeled 1 and 2. The active slot gets vanilla's highlight frame.
- The separate vanilla off-hand slot beside the hotbar is hidden, since slot 2 or slot 1 already shows it.
- The health, hunger, armor and air bars and the XP bar stay where vanilla draws them, so other HUD mods still line up. IRI publishes the hotbar's bounds to Lib's HUD layout, and Impact HUD elements (IRV's thirst row and gauge) position themselves from it.

**Implementation** (keeps vanilla's hand model intact, so every mod that reads the off hand still works)

- Under the hood, the main hand is always vanilla inventory index 0 (`selected` is locked to 0), and the off hand is vanilla's off-hand slot (index 40).
- Switching slots **swaps the two stacks** between index 0 and the off-hand slot, the same way vanilla's F key does, and sends the matching packet.
- A per-player value, `activeSlot` (1 or 2), saved and synced, decides which hand each HUD slot shows. When `activeSlot` is 1, slot 1 shows the main hand and slot 2 the off hand. When it's 2, the reverse. That's why items look like they stay put.
- Mixins: lock `Inventory.selected` to 0; route scroll, number keys and swap-hands to the switch; replace the hotbar HUD render through Forge's `RenderGuiOverlayEvent`.
- Anything else that sets `Inventory.selected` (auto-tool-switch mods, some MCreator procedures) is ignored, and the attempt is logged with the calling class.
- IRI registers with Lib (`registerImpactMod`) and publishes the hotbar's bounds to Lib's HUD layout.

## The 9-slot inventory

The inventory holds 9 stacks ("pockets") in a 3×3 grid laid out like a crafting table's grid. With the two hand slots, a player carries 11 stacks, plus armor and one back slot for a satchel or backpack.

&#91;embedded content: inventory screen layout · 2 hands, 9 pockets, 4 armor, 1 back slot\]

The 2×2 crafting grid stays, separate from the pockets, so early crafting (and IRC's Makeshift Table recipe) still works. It's vanilla's own grid in vanilla's inventory menu (IRI only moves the slots), so with IRC installed it's timed and tier-checked as usual, and IRI's screen draws IRC's filling arrow through Lib's `CraftingProgress` renderer.

**Rules**

- **Pickups:** items go to the hand slots first (only to stack onto what's there, or into an empty active slot), then to the pockets. When everything is full, items stay on the ground. The action bar says "Pockets full" (once every few seconds, not on every item).
- **Stack sizes don't change.** A pocket holds a full stack of 64 cobblestone, so the limit is on how many *kinds* of things you carry.
- **In other containers** (chests, furnaces, modded machines), your pockets appear where vanilla's player inventory goes, as one row of 9, and your main hand appears as the first hotbar slot. To move something into your off hand from a container, hover it and press the swap-hands key (F), as in vanilla.
- **Shift-click** from a container fills pockets, then the hand slots.
- **Crafting results** that don't fit drop at the player's feet instead of being lost.

**Implementation** (vanilla's 36-slot inventory stays the same size, so mods that index into it don't break)

- Index 0 is the main hand. Indexes 9–17 (vanilla's first inventory row) are the 9 pockets. Indexes 1–8 and 18–35 are **locked**: always empty, and nothing can go into them.
- **Locking, inventory side:** mixins on `Inventory.add`, `getFreeSlot`, `getSlotWithRemainingSpace` and `setItem` skip locked indexes. A `setItem` aimed at a locked index (from any mod, including SRF's procedures) never stores the item there. It goes through Lib's Give or drop instead, and the redirect is logged with the caller.
- **Locking, menu side:** mixins on `Slot` make `isActive()`, `mayPlace()` and `mayPickup()` return false for locked player slots, so **every** container screen, vanilla or modded, hides them and refuses clicks, shift-click moves and drag-splits into them.
- **Locking, item handlers:** mixins on Forge's `PlayerMainInvWrapper` make `insertItem` hand the stack back untouched and `isItemValid` return false for locked indexes, so mods that insert through the player's item-handler capability (including `ItemHandlerHelper.giveItemToPlayer`) skip them and fall back to dropping.
- **Your own inventory screen:** IRI keeps vanilla's `InventoryMenu` (`player.inventoryMenu`), so everything that reads it, like IRC's crafting timer, the recipe book and other mods' slot lookups, still works. A client-side mixin moves the pocket slots into the 3×3 layout (`Slot.x` and `Slot.y` through a `@Mutable` accessor). Only the screen is new: `RealisticInventoryScreen` replaces `InventoryScreen` (through `ScreenEvent.Opening`), draws the layout above, and draws IRC's crafting arrow with Lib's renderer. Pockets 9–17 show as a 3×3 grid there and as a row of 9 everywhere else.
- **The back slot** is stored in a player capability (no Curios dependency), drawn on the player model, and saved with the player. It's added to `InventoryMenu` as one extra slot after vanilla's 46 (index 46), so no vanilla index moves, and a `quickMoveStack` mixin handles shift-clicks. IRI registers it as Lib's back-slot provider. It accepts IRI's packs and anything in `#impactslib:back_slot_items` (IRV's Air Tanks and Hydration Pack).

## Carrying more: satchels and backpacks

11 stacks fills up fast, so the back slot holds one pack. Each pack is laid out in 3-wide rows, like the pockets, and bigger packs cost more to make.

| Pack | Slots | Recipe (IRI alone) | Recipe (with IRC) | Drawback |
| --- | --- | --- | --- | --- |
| Satchel | 3 (1 row) | 3 leather + 2 string | 2 Leather Panels + Twine + (Bone Needle) | None |
| Backpack | 9 (3×3) | Satchel + 4 leather + chest | Satchel + 2 Leather Panels + Chest Top + Twine + (Bone Needle) | None |
| Frame Pack | 18 (two 3×3 grids) | Backpack + 4 iron ingots + 2 sticks | Backpack + Wooden Frame + 2 Iron Rods + Iron Rivets, at the Makeshift Stone Table | 10% slower walking while worn |

**Using packs**

- **Wearing:** put a pack in the back slot, or right-click while holding it. It shows on the player's back.
- **Opening:** press B (a keybind) or click the button next to the back slot to open the worn pack in its own screen, with the pack's slots above your hands and pockets. The inventory screen never shows pack slots, so vanilla's inventory menu never changes size when a pack is worn or taken off.
- **Pickups** go to the hand slots, then pockets, then the worn pack (config `pickup.intoPack`, default true).
- **Placing:** sneak-right-click a block while holding a pack to set it down. A placed pack works like a small chest. Sneak-right-click it with an empty hand to pick it back up with its contents.
- **No nesting:** packs and shulker boxes can't go inside a pack (config `packs.allowShulkers`, default false).
- **Contents** are stored in the pack item's NBT (`Items`, like a shulker box), so a pack keeps everything when dropped, moved or placed. Packs stack to 1.
- **Death:** a worn pack drops as one item with its contents, unless `keepInventory` is on.
- **Dyeing:** packs implement DyeableLeatherItem, so they dye like leather armor: in a crafting grid, or with IRC (which turns grid dyeing off) in the Dye Vat.
- Bows and crossbows only pull arrows from the hand slots and pockets, as vanilla only searches the player inventory. Arrows in a pack don't count.

## Working with other mods and edge cases

**Other Impact mods** (connected through Impact's Lib and recipe conditions, never direct code references)

| Mod | How IRI fits |
| --- | --- |
| IRC | Hammering works naturally: loose tool in the active slot, pebble or Iron Hammer in the other. The grinder bowl goes in the other slot, and items can also be loaded into it bundle-style. IRC's 2×2 grid stays vanilla's, so it's timed and tier-checked, and IRI's screen draws IRC's arrow through Lib. Byproducts like Wood Scraps arrive through Lib's Give or drop, which skips locked slots. Pack recipes switch to IRC parts (using `#impactscrafting:iron_plates` and `#impactscrafting:iron_rods`), the Frame Pack needs the Makeshift Stone Table, and packs dye in IRC's Dye Vat |
| IRF | Knife cutting, eating and buckets on campfires all use the active slot. Turning a spit needs an empty active slot. Two empty slots means both hands are free. Cutting byproducts that don't fit drop at your feet |
| IRV | IRI is Lib's back-slot provider, so IRV's Air Tanks and Hydration Pack go in the back slot (they're in `#impactslib:back_slot_items`). IRV's thirst row, air row and temperature gauge line up with IRI's hotbar through Lib's HUD layout. A worn Frame Pack adds 10% to IRV's thirst drain from exertion. Drinking uses the active hand slot |
| Impact's Lib | IRI uses its recipe conditions, `ImpactsLib.isLoaded`, the shared achievement triggers and per-player counters (for Second Nature). It provides Lib's back slot, publishes the hotbar bounds, routes every locked-slot redirect through Give or drop, and draws crafting progress with Lib's renderer |

**Salty's Realistic Forging.** SRF was made in MCreator, and its procedures may write to fixed inventory slots, loop over the hotbar (0–8), assume 36 slots, change the selected slot, or give items directly. Claude Code reviews the decompiled source for each of these. IRI's locks redirect every one through Lib's Give or drop, nothing is lost, and each case gets a GameTest. SRF's two-handed steps (a hammer in one hand, tongs or the workpiece in the other) work as written, because the inactive hand slot is the off hand.

**Vanilla edge cases**

| Case | Behavior |
| --- | --- |
| Creative mode | Vanilla 9-slot hotbar and full inventory (config `creativeVanilla`, default true). Switching back to survival runs the overflow rule below |
| Spectator mode | Unaffected |
| First join with IRI (existing worlds or mid-game installs) | Items in now-locked slots move to the pockets, then a worn pack, then a chest placed at the player's feet. If there's no room for a chest, they drop in a pile. A one-time chat message explains it. Nothing is deleted |
| Death | Hands, pockets, armor and pack drop as in vanilla. `keepInventory` keeps everything, including `activeSlot` |
| Totem of Undying | Works from either hand, as vanilla |
| Shields | Block with a shield in the inactive slot (off hand), as vanilla |
| Maps, fireworks with elytra, torches | Work from the inactive slot, as vanilla off-hand use |
| Drop key (Q) | Drops from the active slot |
| Packets | Every lock is enforced on the server. A client that tries to move items into a locked slot is refused, and its inventory is resynced |
| Other mods writing to locked slots (`setItem`, item-handler inserts, `giveItemToPlayer`, MCreator procedures) | Never stored there. The item goes to the hands and pockets, then a worn pack, then drops at the player's feet, through Lib's Give or drop. Each redirect is logged with the calling class |
| Mods that change the selected hotbar slot (auto-tool switchers) | Ignored: `selected` stays 0, and the attempt is logged. Auto-tool-switch mods are listed as incompatible on the mod page |
| Other inventory mods (Curios, other backpack mods, inventory sorters) | Not blocked. Sorters must respect `Slot.isActive()`. IRI lists other backpack mods as incompatible on its mod page |

## Achievements

IRI's advancement tab, "Realistic Inventory", has 12 achievements. **Goals** mark big steps; **challenges** give 50 XP.

| Achievement | How to get it | Unlocks after | Type |
| --- | --- | --- | --- |
| **Hands Full** (tab root, Satchel icon) | Switch between your two hotbar slots | — | task |
| Quick Draw | Hit a mob within 1 second of switching slots | Hands Full | task |
| Sword and Board | Block an attack with a shield in your off hand while holding a sword | Hands Full | task |
| Juggler | Switch slots 10 times in 5 seconds | Hands Full | task |
| Second Nature | Switch slots 1,000 times | Juggler | challenge |
| Pocket Space | Fill all 9 pockets | Hands Full | task |
| No Room at All | Walk over an item with every slot full | Pocket Space | task (hidden) |
| Packing Light | Wear a Satchel | Pocket Space | task |
| Pack Mule | Wear a Backpack | Packing Light | task |
| Heavy Lifting | Wear a Frame Pack | Pack Mule | goal |
| Home Away From Home | Set a pack down as a block | Packing Light | task |
| Travel Light | Enter the Nether with empty pockets and no pack | Hands Full | challenge |

**Implementation:** datagen advancements. IRI triggers: `impactsinventory:slot_switched` (with time since the last switch, for Juggler and Quick Draw), `impactsinventory:pickup_refused`, `impactsinventory:pack_worn` (with pack type), `impactsinventory:pack_placed`. Second Nature uses Lib's per-player counter. Sword and Board uses vanilla's `entity_hurt_player` with a blocked-damage check, plus the held items.

## Items, config, technical notes and handoff

**Items and blocks:** `satchel`, `backpack`, `frame_pack` (dyeable, stack 1, contents in NBT) and one block, `placed_pack`, with a block entity that remembers the pack type, color and contents.

**Config:** `impactsinventory-server.toml` (per world, synced to clients): `hotbar.swapCooldownTicks` 0, `creativeVanilla` true, `pickup.intoPack` true, `pickup.fullMessage` true, `packs.allowShulkers` false, `packs.framePackSlowness` 0.1, `firstJoin.overflow` `chest` (or `drop`). `impactsinventory-common.toml`: `packs.enabled` true (it gates the pack recipes, and recipe conditions load before a world's server config) and `debug.*`.

**Extended configuration** (Lib's framework; files under `config/impactslib/` and `serverconfig/impactslib/`, see Lib's Configuration). Every number in this spec is configurable. `impactsinventory-server.toml` also holds each pack's slot count (3, 9, 18), `packs.nestingBlacklist` (items that can't go in a pack; default packs and shulker boxes), the pickup order (hands, pockets, pack), the "Pockets full" message interval, and whether `keepInventory` keeps `activeSlot`. `impactsinventory-common.toml` switches off packs, placed packs and the back slot on their own, along with their recipes. `impactsinventory-client.toml` holds the hotbar's position and scale, slot labels, the pack keybind's default, and whether the worn pack renders on the player model.

**Technical**

- **Build:** ForgeGradle 6, Forge 47.x, Parchment mappings, Mixin, Java 17. Impact's Lib is a required `mods.toml` dependency (`implementation fg.deobf("com.impact:impactslib:<version>")`). JEI 15.x is optional.
- **Packages:** `com.impact.realisticinventory` → `registry`, `hotbar` (switch logic, `activeSlot` capability, key handling), `inventory` (slot locks, pickup order, locked-slot redirects, first-join migration), `client` (HUD overlay, `RealisticInventoryScreen`, `PackScreen`), `pack` (items, back-slot capability and Lib provider, placed block, `PackMenu`, renderer), `mixin`, `network` (switch packet client to server; `activeSlot` and back-slot sync server to client), `compat` (IRC and IRV recipe conditions, JEI), `debug` (log categories), `config`, `datagen`.
- **Mixins:** `Inventory` (lock `selected` to 0; skip locked indexes in `add`, `getFreeSlot` and `getSlotWithRemainingSpace`; redirect `setItem` into locked indexes), `Slot` (`isActive`, `mayPlace` and `mayPickup` for locked player slots), `PlayerMainInvWrapper` (`insertItem`, `isItemValid`), `InventoryMenu` (append the back slot; `quickMoveStack`), client-side slot positions, `Minecraft` and `MouseHandler` (scroll and number keys switch slots), and the vanilla hotbar overlay (replaced).
- **Tests:** GameTests for switching (stacks swap correctly, `activeSlot` flips, off-hand shield still blocks), pickups refused when full, the first-join overflow chest, pack contents surviving placing and picking up, `setItem(20, …)` and `giveItemToPlayer` with full pockets (the item lands in a pocket or at the player's feet), and the back slot holding an IRV Air Tank. They run in every family test setup that includes IRI. Manual checks: a vanilla chest, a furnace and a modded machine screen all hide locked slots.

**Debug logging.** IRI logs through Lib's `ImpactsLog` (see Lib's Debug logging). It's on by default (`debug.enabled = true` in `impactsinventory-common.toml`), and every line starts with `[Impact/inventory/<category>]`. The categories:

- `startup`: version, Lib version, which Impact mods and SRF were found, creative and pack settings.
- `hotbar`: each switch (from and to, main- and off-hand items, and whether it came from a key, the scroll wheel or F), hold-to-use actions it cancelled, and blocked attempts by other code to change `selected` (with the caller).
- `pickup`: items refused when full (item, count) and items sent into a worn pack.
- `locks`: every redirect away from a locked slot (index, item, caller, where it went) and refused client packets.
- `migration`: first-join and creative-to-survival moves (item counts, chest position or drops).
- `packs`: packs worn and taken off, opened, placed and picked up (contents count), and nesting refused.
- `screen` (client): the inventory and pack screens opening. With `debug.verbose`, the slot layout applied.

**Milestones**

**Before milestone 1:** Impact's Lib is built and published to mavenLocal.

1. **Skeleton:** mod loads with Lib as a dependency; config; IRI's debug categories.
2. **Two-slot hotbar:** switching, the stack swap, `activeSlot`, keys and scroll, the new HUD and Lib's hotbar bounds.
3. **Locked slots and pockets:** inventory locks, the `Slot` and item-handler guards, locked-slot redirects through Give or drop, pickup order, the "Pockets full" message.
4. **Inventory screen:** vanilla's `InventoryMenu` with moved slots, the 3×3 pocket layout, hands, armor, 2×2 crafting with IRC's arrow through Lib, the appended back slot, and Lib's back-slot provider.
5. **First-join migration** and the creative-mode switch.
6. **Packs:** satchel, backpack, frame pack, back-slot rendering, the pack screen, pickups into the pack.
7. **Placed packs.**
8. **Integration:** IRC recipe switching and tiers; checks with IRC's hammering, grinding and timer, IRF's knives and spit, and IRV's back-slot items and HUD.
9. **SRF pass:** the decompiled-source review in `docs/srf-notes.md`, and a GameTest for each slot write SRF makes.
10. **Achievements.**
11. **Polish and compatibility:** JEI, tooltips, lang, testing with common container mods, and the family test setups that include IRI.

**Acceptance criteria**

- [ ] With a sword in slot 1 (active) and a pickaxe in slot 2, switching to slot 2 puts the pickaxe in the main hand and the sword in the off hand, and both stay drawn in their own slots.
- [ ] Scroll, keys 1 and 2, and the swap-hands key all switch slots; keys 3 to 9 do nothing.
- [ ] A shield in the inactive slot blocks attacks, and a totem in either slot saves the player.
- [ ] Only 9 pockets accept items; every vanilla and modded container screen hides the locked slots.
- [ ] With every slot full, walking over items leaves them on the ground and shows "Pockets full".
- [ ] Items another mod writes into a locked slot (`setItem`, an item-handler insert, `giveItemToPlayer`) are never lost: they land in a pocket, a worn pack or at the player's feet, and the redirect is logged.
- [ ] Joining an old world moves overflow items into a chest at the player's feet, and nothing is lost.
- [ ] A worn Backpack adds 9 slots; placing it and picking it up keeps its contents; a pack can't go inside a pack.
- [ ] IRC hammering works with the loose tool in one slot and a pebble in the other, and IRC's 2×2 grid in IRI's screen is timed, with a filling arrow.
- [ ] With IRV, an Air Tank and the Hydration Pack go in the back slot, and IRV's HUD lines up with the two-slot hotbar without overlapping anything.
- [ ] Creative mode shows the vanilla inventory.
- [ ] A dedicated server starts with IRI alone and with every Impact mod installed, and IRI passes every family test setup that includes it, SRF included.
- [ ] With default settings, every event under Debug logging writes its line. With `debug.enabled = false`, IRI logs only warnings and errors.

**Open questions**

- None. Decided Nov 14, 2026: the pockets stay storage only, only the Frame Pack slows you down, there are no belt pouches or quivers, and no item takes both hands.

**Handoff:** build after Impact's Lib (published to mavenLocal). Export as Markdown and save it as `SPEC.md` in the IRI repo. Debug logging is on by default: read `logs/impacts-debug.log` first when something misbehaves, and add log lines with every new feature. The decompiled SRF copy is reference only (see Lib's checklist). IRI is the Impact mod most likely to clash with SRF, so search SRF's source for every fixed slot index, hotbar loop (0–8), `selected` change and item give, and record each in `docs/srf-notes.md` with a GameTest.

## Implementation changes

Changes made during implementation, following the handoff rule "follow the API and note the change".

- **Platforms (decided Nov 14, 2026):** 1.20.1 Forge first, then NeoForge 1.20.1, then Forge and NeoForge up to 1.21. No Fabric. The mod is split into a `common` module (vanilla code) and a `forge` module, and a `neoforge` module gets added at port time.
- **Saved data:** wherever this spec says "capability", the data is stored with vanilla NBT (mixins on the owning entity or block entity, or `SavedData` for world data) in common code. NeoForge replaces capabilities in 1.20.5, so this keeps the later ports small. Behavior is unchanged: saved, synced and copied exactly as the spec describes.
- **Back slot in every menu (milestone 4):** the back slot is appended to every player's inventory menu, on both sides, even when `packs.backSlot` is off. It then hides and refuses items, so a client and a server with different common settings still agree on the menu's size. With the back slot off, a worn item is moved out (hands, pockets, or the ground) when the player joins.
- **Creative mode and the back slot (added):** vanilla's creative inventory tab places slots by index and would draw index 46 on top of a hotbar slot, so IRI moves it to the right of the player model, mirroring the off-hand slot, with its own slot frame. Vanilla's server accepts creative edits only for indexes 1 to 45, so IRI accepts index 46 itself, with the back slot's rules.
- **Inventory screen layout (milestone 4):** the spec's layout image wasn't part of the handoff. The screen keeps vanilla's top half (armor, player, 2×2 grid and output), puts the back slot in vanilla's off-hand frame beside the player, centers the 3×3 pockets below it and the two hands under the pockets, with the hotbar's highlight around the active hand. It's drawn from pieces of vanilla's inventory texture, so resource packs restyle it.
- **Number keys in IRI's screen (added):** hovering a slot and pressing 1 or 2 swaps it with the hand drawn as hotbar slot 1 or 2, matching the HUD. In every other container, vanilla's mapping stays: 1 swaps with the main hand (shown as the first hotbar slot) and F with the off hand; 2 to 9 are refused, because they point at locked indexes.
- **Crafting arrow in IRI's screen:** vanilla's inventory screen has a smaller arrow (16×13) than a furnace's, so Lib's renderer gained `renderInventoryArrow` for it. IRI's screen implements Lib's `DrawsOwnCraftingProgress`, so IRC's hook over vanilla screens skips it and the arrow is never drawn twice.
- **First-join move (milestone 5):** the move runs whenever the locks start to apply to a player: the first join with IRI, every login, and creative mode ending (any change that turns the locks on). Locked items go into the pockets, then a chest, then the ground. The chest goes beside the player when there's room (a spot a player could build in, including water), otherwise where the player stands; one single chest always fits the 26 locked slots. The first time, a chat line explains the new inventory; later moves say "Survival mode: ..." instead. A message only appears when something moved. The worn-pack step joins with packs (milestone 6).
- **Stray items (added):** an item that appears in a locked slot while the locks apply (a mod writing the inventory's lists directly, bypassing `setItem`) is moved out on the next tick through Lib's Give or drop, and logged under `locks`.
