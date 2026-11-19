# Salty's Realistic Forging: notes for Impact's Realistic Food

From the decompiled SRF 4.1.6 ("no tools"); shared facts are in `impacts-lib/docs/srf-notes.md`.

## SRF's cauldron interactions

All of them need a water cauldron and one of SRF's own items in the **main hand**. None of them changes the cauldron's water level.

| SRF procedure | Event | Main-hand items |
| --- | --- | --- |
| Cleaning gold ore | `RightClickItem` | `rawgoldore`, `purerawgoldore` |
| Cleaning iron dust | `RightClickItem` | `ironoredust`, `rawironnugget` |
| Cooling iron | `RightClickItem` | `pickedhotrawiron_5`, `stickhotrawiron_5`, `pickedhotironnugget`, `stickhotironnugget`, `pickedironiingot`, `pickedironnugget` (gives vanilla iron ingots and nuggets) |
| Cooling melted copper | `RightClickBlock` | `moldwithmeltedcopper`, `copperingotinmold` |
| Cooling melted gold | `RightClickBlock` | `handlingmeltedgold`, `pickedshapedgold`, `pickeddamagedmold` |
| Cooling netherite | `RightClickBlock` | `meltedscrapsandgoldinmold`, `moldwithnetheriteingot` |

Six more procedures do the same in open water ("river"), not in a cauldron.

## What Food must do

- Food's cauldron handler acts only on items in a boiling or soup recipe, on bowls, and on empty hands at a finished soup. None of SRF's items is any of those, so they pass through.
- **Never cancel `RightClickBlock` for an item Food doesn't own.** Three of SRF's cauldron steps run in `RightClickItem`, which never fires if an earlier `RightClickBlock` handler cancels the click.
- Items thrown into a boiling cauldron that match no recipe are left alone, SRF's included.
- **GameTest (done, milestone 3):** `clicksaddonlyitemsfoodboils` uses a boiling cauldron with a stick and checks that the click passes, the item stays in the hand and the cauldron doesn't change. With SRF installed it repeats this with `realisticforgingores:rawgoldore`, looked up by registry ID. `droppedunknownitemsstayput` checks that a dropped item no recipe takes stays where it fell.

## Recipe clashes

- SRF's `two_sticks` is a shapeless recipe of exactly two sticks. Food's standalone Spit recipe ("2 sticks") would match the same grid, so **Food's standalone Spit is 3 sticks in a row** instead. The version with Crafting installed uses 2 Trimmed Sticks and doesn't clash.
- SRF's `woodpestle` is a stick and planks placed diagonally. Food's standalone grinder recipes must not use a two-item stick-and-planks shape.
