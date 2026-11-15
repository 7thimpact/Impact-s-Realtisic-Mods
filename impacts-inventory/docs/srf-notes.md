# Salty's Realistic Forging: notes for Impact's Realistic Inventory

From the decompiled SRF 4.1.6 ("no tools"); shared facts are in `impacts-lib/docs/srf-notes.md`. The spec expected SRF to write to fixed slots. It doesn't, which makes Inventory's job easier.

## What SRF does with the player's inventory

| Operation | Uses | Effect under Inventory |
| --- | --- | --- |
| `setItemInHand(MAIN_HAND, …)` | about 68 | Writes inventory index 0, the main hand |
| `setItemInHand(OFF_HAND, …)` | 4 (netherite steps) | Writes the off-hand slot |
| `ItemHandlerHelper.giveItemToPlayer` | about 40 | Goes through `PlayerMainInvWrapper`, whose locks skip locked indexes, then drops at the feet |
| `Inventory.clearOrCountMatchingItems` | 48 | Removes items matching a predicate. Locked slots are always empty, so nothing changes |
| `Inventory.contains` | 3 | Read only |
| `Inventory.setChanged` | many | Harmless |

SRF never calls `Inventory.setItem` or `getItem` with an index, never changes `selected`, never loops over the hotbar, and never assumes 36 slots.

## Steps that use both hands

| Step | Main hand | Off hand |
| --- | --- | --- |
| Hammering hot iron (several stages) | `pickedhotrawiron*` or `stickhotrawiron*` (hot iron in tongs or two sticks) | `smithing_hammer`, `smithing_stone_hammer` or `smithing_wood_hammer`, aiming at `smithing_anvil` or `big_stone` |
| Brushing raw iron ore | `raw_iron_ore` | `wheatbroom` |
| Netherite pan steps | pan items | written by SRF with `setItemInHand(OFF_HAND)` |

All of these work as written: the inactive hand slot is the off hand.

## Events SRF listens to

- `RightClickItem` (38 handlers), main hand only.
- `RightClickBlock` (4).
- `PlayerTickEvent` (4): hot items burn the player and are swapped in the main hand.
- `EntityItemPickupEvent` (2): vanilla raw ores are turned into SRF items with a removal and a give.

## GameTests to add (milestone 9)

Each one runs with or without SRF installed, because it repeats the same vanilla or Forge call SRF makes:

1. `giveItemToPlayer` with full pockets: the item lands in a pocket, a worn pack, or at the player's feet.
2. `setItemInHand(MAIN_HAND)` writes index 0, and `setItemInHand(OFF_HAND)` writes the off-hand slot, with `activeSlot` 1 and 2.
3. `clearOrCountMatchingItems` removes matching items from the hands and pockets.
4. The pickup conversion path (remove, then give) leaves nothing in a locked slot.
