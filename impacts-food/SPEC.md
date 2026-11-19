# Impact's Realistic Food — Mod Spec

Nov 14, 2026 · @Supa

## Overview

Impact's Realistic Food (IRF) makes food hands-on: cut meat and fish with a knife, boil food and cook soups in cauldrons and buckets, roast on a spit over a campfire, and bake bread from flour and dough. It's a sister mod to [Impact's Realistic Crafting](https://claude.ai/artifact/SaBQus4NifLnCSbvWYGWWL) (IRC). Each works on its own, and they connect automatically when both are installed.

**Target platform**

- Minecraft 1.20.1, Forge (Java 17), the same as the other Impact mods. A port comes later.
- Mod ID: `impactsfood`, package `com.impact.realisticfood`. Display name: Impact's Realistic Food.
- **Requires Impact's Lib** (`impactslib`), a separate download. It's declared as a required dependency in `mods.toml` (`mandatory=true`, with a version range) and on CurseForge and Modrinth, so launchers install it automatically.
- **IRF owns all food.** Every food feature from IRC (flour, dough, bread, cake, cookies, pie, stews, seeds) now lives here, and IRC leaves food alone.
- **Optional:** [Impact's Realistic Crafting](https://claude.ai/artifact/SaBQus4NifLnCSbvWYGWWL) (IRC), [Impact's Realistic Inventory](https://claude.ai/artifact/Euz9kvYYXiDVHnaD9xRjkB) (IRI), [Impact's Realistic Vitality](https://claude.ai/artifact/1MFojaKLikF4TDVS8HSyyS) (IRV), JEI, and Salty's Realistic Forging (SRF). IRF must not break SRF's cauldron ore washing.

**Goals**

1. Raw food takes preparation: cutting, cleaning, slicing.
2. Cooking has more methods than the furnace: boiling, soups, spit roasting.
3. Eating food raw has a cost.
4. Everything follows IRC's style: tools that wear out, byproducts that feed back into other recipes, and hands-on actions in the world.

**Non-goals for v1:** thirst (Impact's Realistic Vitality covers it), nutrition or diet systems, spoilage, new crops.

## Working alone and together

IRF and IRC never need each other. Anything both of them use lives in Impact's Lib, so there are no duplicate items. When IRF sees IRC, its recipes switch to IRC's parts and follow IRC's rules.

| Feature | IRF alone | IRF + IRC |
| --- | --- | --- |
| Flint Knife | Flint + stick | Flint Arrowhead + Trimmed Stick |
| Iron Knife | Iron ingot + stick | Iron Plate + Trimmed Stick, at the Makeshift Stone Table |
| Spit | 2 sticks | 2 Trimmed Sticks |
| Grinder (from Lib) | Vanilla-material recipes | IRC's recipes (Trimmed Planks, Shaped Stone) |
| Crafting time | Instant | Timed by tree depth, including IRF items |
| Table tiers | None | IRF recipes obey them (iron items need the Stone Table) |
| Bread, cake, cookies, pie, stews | IRF's recipes | IRF's recipes (IRC has no food recipes of its own) |
| Stone boiling in a Wooden Bucket | Not available | Available (see Boiling) |
| Byproducts | Bones, feathers and bone meal from cutting | Same, and they feed IRC parts (Bone Needle, Flint Arrowhead) |

**With IRV and IRI**

| Feature | Without that mod | With it |
| --- | --- | --- |
| Dough's water (IRV) | A vanilla water bottle | Any bottle Lib's `impactslib:water_bottle` ingredient accepts, including IRV's Purified and Unclean Water |
| A bottle filled from a boiling cauldron or pot (IRV) | A vanilla water bottle, as in vanilla | Purified Water. IRF answers Lib's `isBoiling`, and IRV does the filling |
| Soups, boiled and toasted food (IRV) | Hunger only | Also thirst and warmth. IRV reads `#impactsfood:soups`, `#impactsfood:boiled_foods` and `#impactsfood:toasted_foods` |
| Dried food (IRV) | Always edible | Costs thirst and can't be eaten while Hot. IRV reads `#impactsfood:dried_foods` |
| Byproducts with no room (IRI) | Drop at the player's feet | The same, through Lib's Give or drop, which also respects IRI's locked slots |
| Knives, spits and campfire buckets (IRI) | Main hand and off hand as usual | The active hand slot is the main hand; turning a spit needs it empty |

**Rules for staying standalone**

- **No direct code references.** IRF never imports another Impact mod's classes, and no Impact mod imports IRF's. They meet only through Impact's Lib (shared items, recipe types and its API), tags and recipe conditions.
- **Recipe switching uses Forge recipe conditions.** IRF ships two versions of each affected recipe: one gated by `forge:mod_loaded impactscrafting` and one gated by `forge:not` of that. IRC has no food recipes, so nothing on IRC's side needs gating.
- **IRC items by ID.** IRF's IRC-version recipes use IRC items and tags (`#impactscrafting:iron_plates`) only behind `forge:mod_loaded impactscrafting`. Stone boiling finds IRC's items by registry ID (`impactscrafting:stone_pebble`, `impactscrafting:wooden_water_bucket`) and changes their uses only through Lib's `UsesDurability`.
- **Runtime checks** use `ImpactsLib.isLoaded("impactscrafting")`, a cached wrapper around ModList.
- **One owner per item.** If two mods would make an item, it moves to Lib (the grinder), or one mod clearly owns it (IRF owns all food, including Flour and Dough; IRC owns all tools and blocks). Only IRF overrides vanilla food recipes.
- **IRF answers Lib's boiling state** (`registerBoilingProvider`) for water cauldrons, campfire buckets and stone-boiling pots, so other mods can ask without knowing IRF.
- **Testing:** IRF must pass Lib's family test setups: alone, with each other Impact mod, all together, and with SRF. Each one gets a clean recipe audit and conflict check.

## Knives and prep

A knife turns whole raw food into smaller, faster-cooking pieces and gives byproducts. Every cut is a Lib `tool_crafting` recipe: the knife stays in the grid and takes 1 damage.

**Knives**

| Knife | Recipe | Uses |
| --- | --- | --- |
| Flint Knife | See the table above (alone vs with IRC) | 64 |
| Iron Knife | See the table above | 250 |

Both knives go in `#impactsfood:knives`. They work as weak weapons (3 and 4 attack damage) but aren't meant as swords.

**Cutting recipes** (input + knife in any grid)

| Input | Output | Byproduct |
| --- | --- | --- |
| Raw beef | 2 Raw Beef Cuts | 15% bone |
| Raw porkchop | 2 Raw Pork Cuts | 15% bone |
| Raw mutton | 2 Raw Mutton Cuts | 15% bone |
| Raw chicken | 2 Raw Chicken Pieces | 25% feather |
| Raw rabbit | 2 Raw Rabbit Pieces | 10% rabbit hide |
| Raw cod, raw salmon | 2 Raw Cod Fillets, 2 Raw Salmon Fillets | 20% bone meal |
| Potato | 2 Potato Slices | — |
| Carrot | 2 Carrot Slices | — |
| Melon slice, pumpkin | Seeds (the IRC rule moves here) | — |

- Pieces cook in half the time (100 ticks in a furnace, 300 on a campfire) and fill half as much as the whole item. Cooked beef restores 8 hunger, so a Cooked Beef Cut restores 4, with saturation halved too. Cutting never gains food; it trades size for speed and flexibility (soups and spits use pieces).
- Cooking: a furnace turns every raw piece into its cooked version (Cooked Beef Cut, Cooked Cod Fillet, Fried Potato Slices). Boiling water gives its Boiled version instead, and a cooked piece toasts on a campfire (see Cooking any food). Carrot Slices can be eaten raw.
- Byproducts go to the player through Lib's Give or drop, the same way IRC's Wood Scraps do: into the inventory, or at the player's feet when there's no room. With IRC installed, bones feed the Bone Needle, feathers feed arrows, and bone meal feeds the grinder and dyes.
- Grid cutting of whole cooked food is not allowed. You cut it raw.

## Boiling in cauldrons and buckets

Water boils when a heat source sits right under it. Boiling water cooks single items and turns sets of ingredients into soups. The cauldron is the big pot (up to 6 bowls); a bucket on a campfire is the small one (up to 2 bowls).

**Heat sources** (`#impactslib:heat_sources`): fire, soul fire, lit campfire, lit soul campfire, magma block, lava. Modded heat blocks can be added to the tag.

**Cauldron**

1. A vanilla water cauldron (any level) with a heat source directly below it starts heating. Heat sources come from Lib's `#impactslib:heat_sources`, and campfires, fires and furnaces only count while lit (Lib's `isHeated`). After 200 ticks (10 seconds) it boils, with bubble particles and a simmering sound. Take the heat away and it stops boiling 200 ticks later.
2. Boiling single items: drop items in, or right-click the cauldron with them. Each one turns into its boiled result after its boil time and pops up out of the water.
3. Soups: add a soup's ingredients and wait 400 ticks. Then right-click with any empty bowl to fill it, or with an empty hand to eat a serving straight from the pot (only when you could eat, as with any soup). One batch makes up to 6 servings, and every 2 servings, bowled or eaten, use 1 water level. A serving eaten from the pot counts exactly like a bowl: IRF calls `player.eat` with the soup's item stack, then fires Forge's `LivingEntityUseItemEvent.Finish` and vanilla's consume\_item trigger, so IRV's thirst and warmth, achievements and counters all see it. If the ingredients don't match any soup, they become Watery Soup (2 hunger), so a wrong guess still gives you something.
4. Evaporation: a boiling cauldron loses 1 water level every 2400 ticks (2 minutes). When it's empty it stops, and items inside drop out.
5. Handlers pass through: IRF's right-click handler acts only on items in a boiling or soup recipe, and on bowls and empty hands at a finished soup. Everything else gets PASS, so SRF's ore washing, IRV's bottle filling and vanilla's cauldron uses still work. Dropped items that match no recipe are left alone. Test this with SRF and IRV installed.
6. While a cauldron holds soup ingredients or a finished soup, IRF refuses glass bottles (event priority HIGH), so no mod can fill water from a soup.
7. IRF reports each cauldron's boiling state to Lib's boiling provider, so IRV can fill Purified Water from a boiling cauldron without knowing IRF.

**Bucket on a campfire**

- Sneak-right-click the top of a lit campfire with a water bucket. The bucket sits on the fire as a small pot block (`impactsfood:campfire_bucket`).
- It boils after 100 ticks and works like a cauldron with less room: 2 single items at a time, or a soup batch of up to 2 servings, eaten straight from the pot with an empty hand or put in any bowl, like a cauldron's.
- The water lasts 8 boils, and the bucket itself never wears down. Sneak-right-click with an empty hand to take the bucket back: it's still a water bucket if any water is left, or an empty bucket if not. It also pops off if the campfire goes out or is broken.
- **With IRC:** Wooden Buckets can't go on a fire (they'd burn). They use stone boiling instead. Hold a Stone Pebble (`impactscrafting:stone_pebble`) on a lit campfire to heat it into a Hot Pebble. The Hot Pebble remembers the pebble's uses and cools back into that Stone Pebble after 30 seconds in your inventory. A Water-filled Wooden Bucket (`impactscrafting:wooden_water_bucket`) can be set down on top of a block with sneak-right-click as a stone-boiling pot (`impactsfood:stone_boiling_pot`), and dropping a Hot Pebble into it makes it boil for 30 seconds. Each pebble costs 1 bucket use, through Lib's `UsesDurability`.
- The pot keeps the original bucket stack, NBT and all, and gives it back on sneak-right-click with an empty hand. If the water boiled, IRF posts Lib's water-filled event with `boiled = true` as it hands the bucket back, so IRV can mark the water purified.
- IRF reports campfire buckets and stone-boiling pots to Lib's boiling provider too.

**Boiling single items** follows the rules in Cooking any food (next section): raw food becomes its Boiled version, base food becomes its Boiled version, and finished food becomes Mush. The egg is the one non-food that boils (→ Boiled Egg).

**Soups** (cauldron or bucket; ingredients per batch of 2 bowls)

| Soup | Ingredients | Hunger per bowl |
| --- | --- | --- |
| Mushroom stew | Brown mushroom + red mushroom | 6 |
| Beetroot soup | 3 beetroot (vanilla needs 6 in the grid) | 6 |
| Rabbit stew | Rabbit Piece + Carrot Slices + Potato Slices + a mushroom | 10 |
| Bone Broth (new) | 3 bones | 3, plus Regeneration I for 5 seconds |
| Hearty Stew (new) | Any meat cut + Potato Slices + Carrot Slices | 8 |
| Fish Soup (new) | Any fish fillet + Potato Slices | 6 |
| Porridge (new) | 2 Flour + milk bucket (the bucket comes back) | 5 |
| Suspicious stew | Mushrooms + 1 flower | Vanilla effect |

Adding 2 or 3 sets of ingredients makes 4 or 6 bowls (cauldron only). The grid stew recipes are off while IRF is installed.

## Cooking any food

Food cooks in order: **raw → cooked or boiled → toasted**. Cooking or boiling raw food is the first step. Once food is cooked, a campfire toasts it, a furnace dries it, and boiling softens it. Boiled food is a dead end: it can't go back on the heat, only be reboiled into Mush. Bread is already cooked, so in a furnace it becomes Dry Bread (faster to eat), on a campfire Toasted Bread (more filling), and boiled, Boiled Bread (for the achievement "Why was this necessary?").

**The three styles** (stats are relative to the base food)

| Style | Made in | Hunger | Saturation | Eating time |
| --- | --- | --- | --- | --- |
| Dried | Furnace, smoker | Same | ×0.75 | 16 ticks (vanilla food takes 32) |
| Toasted | Campfire, spit | +1 | ×1.25 | 32 ticks |
| Boiled | Cauldron, campfire bucket | Same | ×0.9 | 24 ticks |

Dried trades a little saturation for eating twice as fast, which is handy mid-fight. Toasted rewards the campfire's slower cooking with a bigger meal. Boiled is a middle option, and the only one that works on things you can't put in a furnace.

**How a food moves through the styles**

1. **Raw → cooked.** A furnace, smoker or campfire turns raw food into its base food (the vanilla cooked item), exactly as in vanilla. Other mods that expect cooked beef still get it.
2. **Raw → boiled.** Boiling raw food gives its Boiled version directly: raw beef becomes Boiled Beef.
3. **Cooked → toasted, dried or boiled.** Cooked beef on a campfire becomes Fire-Roasted Beef, in a furnace Beef Jerky, and boiled, Boiled Beef. Foods that are ready to eat with no raw form (bread, apples, berries, carrots, cookies, pie) count as cooked.
4. **Boiled food can't be recooked.** Furnaces, smokers, campfires and spits don't accept Boiled food at all (there's no recipe, so a furnace just won't cook it and a campfire won't take it). Boiling it again turns it into **Mush**.
5. **Toasted and dried food are finished.** More dry heat turns them into **Burnt Food**; boiling them turns them into **Mush**.

&#91;embedded content: cooking chain · raw → cooked or boiled → toasted\]

Toasted or dried food that's boiled also becomes Mush. The arrow from cooked to boiled is why Boiled Bread exists.

**Recipe list.** The raw form becomes the base food in a furnace, smoker or campfire, and the Boiled version when boiled. The base food becomes each of the three versions in the last three columns.

| Base food | Raw form | Furnace / smoker | Campfire / spit | Boiling |
| --- | --- | --- | --- | --- |
| Bread | Loaf Dough | Dry Bread | Toasted Bread | Boiled Bread |
| Cooked beef | Raw beef | Beef Jerky | Fire-Roasted Beef | Boiled Beef |
| Cooked porkchop | Raw porkchop | Pork Jerky | Fire-Roasted Pork | Boiled Pork |
| Cooked mutton | Raw mutton | Mutton Jerky | Fire-Roasted Mutton | Boiled Mutton |
| Cooked chicken | Raw chicken | Chicken Jerky | Fire-Roasted Chicken | Boiled Chicken |
| Cooked rabbit | Raw rabbit | Rabbit Jerky | Fire-Roasted Rabbit | Boiled Rabbit |
| Cooked cod | Raw cod | Dried Cod | Fire-Roasted Cod | Poached Cod |
| Cooked salmon | Raw salmon | Dried Salmon | Fire-Roasted Salmon | Poached Salmon |
| Cooked Tropical Fish (new, 4 hunger) | Raw tropical fish | Dried Tropical Fish | Fire-Roasted Tropical Fish | Poached Tropical Fish |
| Cooked Rotten Flesh (new, 3 hunger, 10% Hunger) | Rotten flesh | Rotten Jerky | Fire-Roasted Rotten Flesh | Boiled Rotten Flesh |
| Cooked cuts and pieces (5) | Raw cuts and pieces | Beef / Pork / Mutton / Chicken / Rabbit Jerky Strip | Fire-Roasted (cut name) | Boiled (cut name) |
| Cooked fish fillets (2) | Raw fish fillets | Dried Cod / Salmon Fillet | Fire-Roasted (fillet name) | Poached (fillet name) |
| Baked potato | Potato | Potato Crisps | Fire-Roasted Potato | Boiled Potato |
| Fried Potato Slices | Potato Slices | Potato Chips | Toasted Potato Slices | Boiled Potato Slices |
| Carrot | — | Dried Carrot | Roasted Carrot | Boiled Carrot |
| Carrot Slices | — | Dried Carrot Slices | Roasted Carrot Slices | Boiled Carrot Slices |
| Beetroot | — | Dried Beetroot | Roasted Beetroot | Boiled Beetroot |
| Apple | — | Dried Apple | Roasted Apple | Stewed Apple |
| Melon slice | — | Dried Melon | Grilled Melon | Stewed Melon |
| Sweet berries | — | Dried Berries | Roasted Berries | Stewed Berries |
| Glow berries | — | Dried Glow Berries | Roasted Glow Berries | Stewed Glow Berries |
| Dried kelp | Kelp | Already dried: Burnt Food | Roasted Kelp | Boiled Kelp |
| Cookie | — | Crunchy Cookie | Toasted Cookie | Soggy Cookie |
| Pumpkin pie | — | Crusty Pumpkin Pie | Toasted Pumpkin Pie | Soggy Pumpkin Pie |

**Doughs and eggs** (these give finished foods directly)

| Input | Furnace / smoker | Campfire / spit | Boiling |
| --- | --- | --- | --- |
| Dough | Dough Bun (3 hunger) | Flatbread (4 hunger) | Dumpling (4 hunger) |
| Loaf Dough | Bread | 3 Flatbread | 3 Dumplings |
| Egg (not a food until cooked) | — (it would crack) | Fried Egg (3 hunger) | Boiled Egg (4 hunger) |

**Finished foods.** Dried, Toasted, Dough Bun, Flatbread and Fried Egg become Burnt Food with dry heat and Mush when boiled. Boiled foods, Boiled Egg, Dumpling, Mush and soups never go back on dry heat; reboiling them gives Mush (Mush itself doesn't change). Burnt Food can be boiled into Mush.

| Overcooked food | Hunger | Saturation | Extra |
| --- | --- | --- | --- |
| Burnt Food | 1 | 0.1 | 50% chance of Nausea for 5 seconds. Burns as furnace fuel (400 ticks) and composts (30%) |
| Mush | 2 | 0.2 | Eats in 16 ticks. Composts (65%) |

**Not cooked** (they keep vanilla behavior): golden apple, enchanted golden apple, golden carrot, chorus fruit (vanilla popping stays), pufferfish, poisonous potato, spider eye, cake, honey bottle, milk, and all bowl foods. Modded foods aren't covered in v1.

**Raw cooking times:** furnace 200 ticks and smoker 100, campfire 600 (all vanilla), faster on a spit; boiling 300 for meat and fish, 100 for bread, 200 for everything else.

**Cooking times:** toasting cooked food on a campfire takes 300 ticks (it's already cooked), and drying it in a furnace takes 200. Achievements for cooking are listed under Achievements.

**Implementation**

- One table in code (`FoodVariants`) lists each base food, its raw form, and the name overrides above. A single registration loop creates every styled item. Unlisted names default to "Dried X", "Toasted X" and "Boiled X". IDs follow `impactsfood:<style>_<base id path>`, for example `impactsfood:dried_bread` ("Dry Bread") and `impactsfood:toasted_cooked_beef` ("Fire-Roasted Beef").
- `FoodProperties` are computed from the base food at registration using the style rules. Styled versions keep the base's effects, but Boiled halves the chance of any negative effect. A `StyledFoodItem` class overrides `getUseDuration` for eating speed.
- **Art:** each styled item's model has 2 layers: the base food's own texture plus one of 3 shared overlays (dried cracks, toast marks, a wet shine). About 90 items need just 3 new textures.
- **Recipes** (all from datagen): `smelting` + `smoking` for Dried, `campfire_cooking` for Toasted (so the spit uses them too), and `impactsfood:boiling` for Boiled. Vanilla campfire recipes for raw food stay as they are (raw → cooked). IRF only adds campfire recipes that turn cooked food into Toasted.
- **Tags:** each styled item inherits its base food's item tags, so "any cooked meat" recipes from other mods still accept it. New tags: `impactsfood:styled_foods`, `impactsfood:finished_foods`.

## Spit roasting

A spit turns a campfire into a bigger, faster grill. It uses the vanilla `minecraft:campfire_cooking` recipes, so anything that cooks on a campfire (including IRF's cuts and fillets) can go on the spit. Raw food on the spit cooks, and cooked food on the spit toasts.

- **Spit item:** 2 sticks (with IRC, 2 Trimmed Sticks). Right-click a lit campfire with it to mount it. The spit renders across the fire.
- **Loading:** right-click the campfire with raw food to skewer it. A spit holds 6 items, on top of the campfire's normal 4 slots.
- **Speed:** spit items cook at 1.25× campfire speed (a 600-tick item takes 480).
- **Turning:** right-click with an empty hand to turn the spit. For the next 100 ticks it cooks at 1.5×, with extra smoke and a sizzle sound. Turning again does nothing until those 100 ticks are up.
- **Taking food off:** cooked items drop off the spit on their own, like the campfire. Sneak-right-click with an empty hand to take the spit down; it drops the spit and anything still on it.
- The spit stays if the campfire goes out, but cooking pauses. If the campfire is broken, the spit drops.
- **Implementation:** a Forge capability on the vanilla `CampfireBlockEntity` stores the spit, its 6 slots, their timers and the turn boost. A block entity renderer draws the spit and its food. No change to the campfire block itself.

## Raw food risk

Eating raw meat or fish can make you sick, the way raw chicken already can in vanilla.

| Food | Chance | Effect |
| --- | --- | --- |
| Raw beef, porkchop, mutton, rabbit, cod and salmon, and all raw cuts, pieces and fillets | 30% | Hunger for 15 seconds |
| Raw chicken, raw chicken pieces | Vanilla (30%) | Vanilla Hunger |
| Rotten flesh, pufferfish, spider eye | Vanilla | Vanilla |

- Applied in `LivingEntityUseItemEvent.Finish`, so vanilla food values don't change and other mods' food tweaks still work.
- Driven by the tag `#impactsfood:raw_risky`, so pack makers can add or remove foods.
- Config: `rawFood.enabled` true, `rawFood.chance` 0.3, `rawFood.durationTicks` 300.

## Baking

IRF owns all baking. Flour and Dough are IRF items (`impactsfood:flour`, `impactsfood:dough`). The grinder that makes flour comes from Impact's Lib, so IRF's grinder recipes use vanilla materials alone and IRC's parts when IRC is installed.

| Item | Recipe | Notes |
| --- | --- | --- |
| Flour | Wheat in a stone grinder, with a 50% chance of 1 wheat seed | IRF ships this grinding recipe |
| Dough (3) | 3 Flour + any water bottle (Lib's impactslib:water\_bottle ingredient: vanilla's, or IRV's Purified or Unclean Water). The glass bottle comes back through Lib's remainders field | Any grid |
| Loaf Dough | 3 Dough in a row | Any grid |
| Bread | Loaf Dough in a furnace or smoker. On a campfire it gives 3 Flatbread; boiled, 3 Dumplings | Grid bread is off |
| Dough Bun, Flatbread, Dumpling | 1 Dough in a furnace, on a campfire, or boiled | See Cooking any food |
| Cake | 3 Flour + 2 sugar + egg + milk bucket (the bucket comes back) |  |
| Cookie (8) | 2 Flour + cocoa beans |  |
| Pumpkin pie | Pumpkin + sugar + egg + Flour |  |
| Sugar | Vanilla recipe. With IRC installed, IRC's grinder recipe replaces it | Sugar also feeds potions, so IRC owns its grinding |

Golden apples, golden carrots and glistering melon keep their vanilla recipes. With IRC, they need the Crafting Table.

## Items, recipe types, tags and config

IRF adds about 150 items (about 90 of them are styled foods made from one table) and 1 block. It adds 2 recipe types and reuses Lib's `tool_crafting` and `grinding`, plus vanilla's `smelting`, `smoking` and `campfire_cooking`.

**Items**

| Group | Items |
| --- | --- |
| Tools | `flint_knife`, `iron_knife`, `spit` |
| Raw pieces | `raw_beef_cut`, `raw_pork_cut`, `raw_mutton_cut`, `raw_chicken_piece`, `raw_rabbit_piece`, `raw_cod_fillet`, `raw_salmon_fillet` |
| Cooked pieces | `cooked_beef_cut`, `cooked_pork_cut`, `cooked_mutton_cut`, `cooked_chicken_piece`, `cooked_rabbit_piece`, `cooked_cod_fillet`, `cooked_salmon_fillet` |
| New base foods | `cooked_tropical_fish`, `cooked_rotten_flesh`, `potato_slices`, `fried_potato_slices`, `carrot_slices` |
| Styled foods (about 90) | `dried_*`, `toasted_*`, `boiled_*` for every base food in the Cooking any food recipe list |
| Baking | `flour`, `dough`, `loaf_dough`, `dough_bun`, `flatbread`, `dumpling` |
| Other finished foods | `fried_egg`, `boiled_egg`, `burnt_food`, `mush` |
| Soups (bowl foods, stack 1, the bowl comes back) | `bone_broth`, `hearty_stew`, `fish_soup`, `porridge`, `watery_soup` |
| IRC only | `hot_pebble` (registered always, only obtainable with IRC) |
| Block | `campfire_bucket` (the bucket-on-campfire pot) and stone\_boiling\_pot (a Water-filled Wooden Bucket set down for stone boiling; IRC only) |

The grinder items are Lib items (`impactslib:`). Everything else above is `impactsfood:`.

**Recipe types**

- `impactsfood:boiling`: `input`, `result`, `boil_ticks`, `vessel` (`cauldron`, `bucket` or `any`).
- `impactsfood:soup`: `ingredients` (an unordered set), `result` (a bowl item), `bowls_per_set` (default 2), `cook_ticks` (default 400), `vessel`.

**Tags**

- `impactsfood:knives`: Flint and Iron Knife.
- `impactsfood:raw_risky`: foods that can cause Hunger when eaten raw.
- `impactsfood:meat_cuts` and `impactsfood:fish_fillets`: let soups say "any cut" or "any fillet".
- `impactsfood:soups`: every bowl soup and stew IRF adds. Vanilla's stews aren't in it, because IRV gives those its own, smaller values. IRV reads it for thirst and warmth.
- `impactsfood:styled_foods` and `impactsfood:finished_foods`: used by the Burnt Food and Mush rules and by achievements. Sub-tags `impactsfood:boiled_foods`, `impactsfood:toasted_foods` and `impactsfood:dried_foods` split them by style. Day in and Day out counts the first two, and IRV reads all three.
- `impactslib:heat_sources`: lives in Lib so other Impact mods can use it.

**Fuel and item data:** IRF follows the Lib rules. Food doesn't burn, except Burnt Food, which burns for 400 ticks. The Spit burns like a stick (100 ticks). IRF items carry no NBT, except the Hot Pebble, which stores its cool-down timer and the uses of the pebble it was heated from.

**Config** (`impactsfood-common.toml`): knife durabilities (64 / 250), `cutting.byproducts` true, `boiling.heatUpTicks` 200, `boiling.bucketBoils` 8, `boiling.evaporationTicks` 2400, `soup.maxBowls` 6, `spit.speed` 1.25, `spit.turnSpeed` 1.5, `cooking.styles` true, `cooking.overcooking` true (Burnt Food and Mush), `rawFood.*` (see Raw food risk), and toggles `overhaul.cutting`, `overhaul.boiling`, `overhaul.spit`, `overhaul.baking`, `overhaul.stews`.

## Achievements

IRF has its own advancement tab, "Realistic Food", with 26 achievements in five branches: prep, boiling, cooking styles, spit and baking. Each one marks a milestone in learning the mod. **Goals** mark big steps; **challenges** are long grinds and give 50 XP.

| Achievement | How to get it | Unlocks after | Type |
| --- | --- | --- | --- |
| **Sharp Thinking** (tab root, Flint Knife icon) | Craft a knife | — | task |
| Cut It Out | Cut raw meat into cuts or pieces | Sharp Thinking | task |
| Something Fishy | Fillet a fish | Sharp Thinking | task |
| Bone Appetit | Get a bone, feather or bone meal from cutting | Cut It Out | task |
| Raw Deal | Get Hunger from eating raw food | Sharp Thinking | task |
| Hot Water | Bring a cauldron to a boil | Sharp Thinking | task |
| Hard Boiled | Boil an egg | Hot Water | task |
| Why was this necessary? | Get Boiled Bread | Hot Water | task (hidden) |
| Mush Ado About Nothing | Make Mush | Hot Water | task (hidden) |
| Camp Cook | Boil something in a bucket on a campfire | Hot Water | task |
| Stone Soup | Boil water with a Hot Pebble (only shown with IRC installed) | Camp Cook | task |
| Souper | Ladle a bowl of soup | Hot Water | task |
| Watered Down | Make Watery Soup | Souper | task (hidden) |
| Master Chef | Make every soup at least once | Souper | challenge |
| Toast of the Town | Toast a cooked food on a campfire | Sharp Thinking | task |
| Jerky Move | Dry cooked meat into jerky in a furnace | Toast of the Town | task |
| Well Done | Make Burnt Food | Toast of the Town | task (hidden) |
| Three Ways | Own the toasted, dried and boiled versions of the same food | Jerky Move | goal |
| **Day in and Day out** | Eat the same boiled or toasted food 100 times (for example, 100 Fire-Roasted Beef) | Three Ways | challenge |
| Taste Tester | Eat 25 different toasted, dried or boiled foods | Three Ways | challenge |
| Spit Take | Mount a spit on a campfire | Sharp Thinking | task |
| Round and Round | Turn a spit 20 times | Spit Take | task |
| Use Your Loaf | Bake bread from Loaf Dough | Sharp Thinking | task |
| Dough Re Mi | Make a Dough Bun, a Flatbread and a Dumpling | Use Your Loaf | goal |
| Rise and Grind | Grind wheat into Flour | Use Your Loaf | task |
| Well Fed | Earn every other Realistic Food achievement | Master Chef, Day in and Day out, Taste Tester | challenge |

**Implementation**

- All advancements come from datagen. Vanilla triggers cover most of them (`inventory_changed`, `consume_item`, `recipe_crafted`, `placed_block`).
- Shared triggers from Impact's Lib: `impactslib:tool_crafted` (cutting), `impactslib:hold_action` (spit turns), `impactslib:count` (counters).
- **Day in and Day out:** every time a player eats a food in `#impactsfood:boiled_foods` or `#impactsfood:toasted_foods`, Lib's per-player counter adds 1 for that exact item. The trigger fires when any one item reaches 100. Counts are saved with the player and survive death (copied in `PlayerEvent.Clone`). Taste Tester reads the same counter for how many different styled foods have a count of 1 or more.
- IRF triggers: `impactsfood:cauldron_boiled`, `impactsfood:soup_ladled` (with the soup ID, for Master Chef and Watered Down), `impactsfood:raw_food_sickness`.
- **Stone Soup** is gated with `forge:conditions` `mod_loaded impactscrafting`, so it doesn't exist without IRC.
- Hidden achievements stay invisible in the tab until earned, so the jokes land.

## Technical notes, milestones and handoff

**Technical**

- **Build:** ForgeGradle 6, Forge 47.x, Parchment mappings, Java 17. Impact's Lib is a required `mods.toml` dependency with a version range, and an `implementation fg.deobf("com.impact:impactslib:<version>")` dependency from Lib's Maven in Gradle. JEI 15.x is optional.
- **Packages:** `com.impact.realisticfood` → registry, item (KnifeItem, SpitItem, HotPebbleItem, SoupItem, StyledFoodItem), food (the FoodVariants table and style rules), block (CampfireBucketBlock and StoneBoilingPotBlock + block entities), boiling (cauldron heat tracking, soup batches, the Lib boiling provider), spit (campfire capability, renderer), recipe (BoilingRecipe, SoupRecipe), event (raw food risk, cutting byproducts), compat (IRC and IRV recipe conditions, JEI), debug (log categories), config, datagen.
- **Cauldron state:** vanilla cauldrons have no block entity, so heat, warm-up time and soup contents live in a chunk capability keyed by block position, synced to nearby clients for particles. The same capability answers Lib's `isBoiling`. Don't change the vanilla cauldron's blockstates.
- **Recipe conditions:** datagen writes both versions of each IRC-affected recipe (see Working alone and together).
- **Config:** `impactsfood-server.toml` holds gameplay values (boil and warm-up times, evaporation, raw-food risk, spit speed). `impactsfood-common.toml` holds anything a recipe condition reads, plus debug.\*, because recipe conditions load before a world's server config.
- **JEI:** categories for Cutting (through Lib's tool crafting), Boiling, Soups and Spit roasting, plus an info page on the three cooking styles.
- **Tests:** GameTests for cutting byproducts, cauldron boiling, a soup batch, a wrong soup becoming Watery Soup, the bucket's 8-boil limit, spit speed, the three styles of one food, Burnt Food and Mush, raw food risk, and the handler pass-through (an unknown item used on a boiling cauldron is left for the next handler). They run in every family test setup that includes IRF (Lib's Family compatibility and testing).

**Configuration** (Lib's framework; files under `config/impactslib/` and `serverconfig/impactslib/`, see Lib's Configuration). Every number in this spec is a value in `impactsfood-server.toml`, including: knife durability (flint 64, iron 250); cut yields and byproduct chances per input (`id=value` lists); cooking times for pieces; cauldron warm-up and cool-down ticks (200/200) and per-recipe boil times; evaporation interval (2400); soup wait (400); servings per batch (6 in a cauldron, 2 in a bucket) and servings per water level (2); Watery Soup hunger (2); campfire bucket boils (8) and warm-up (100); stone-boiling time and Hot Pebble cool-down (30 seconds each); spit slots (6), speed (1.25×) and turn bonus (1.5×); raw-food Hunger chance (30%) and length (15 seconds), with per-food overrides; the hunger, saturation and eating-time multipliers of every cooking style; Burnt Food's burn time (400); Mush's food values; and Day in and Day out's count (100). `impactsfood-common.toml` switches each feature off on its own, along with its recipes (cutting, cauldron boiling, soups, eating from the pot, campfire bucket, stone boiling, spit, raw-food risk, each cooking style, baking), plus debug.\*. `impactsfood-client.toml` holds boiling particles and sounds.

**Debug logging.** IRF logs through Lib's `ImpactsLog` (see Lib's Debug logging). It's on by default (`debug.enabled = true` in `impactsfood-common.toml`), and every line starts with `[Impact/food/<category>]`. The categories:

- `startup`: version, Lib version, which Impact mods and SRF were found, which recipe sets are active (alone or IRC versions), tag sizes.
- `compat`: SRF detection and the cauldron pass-through check.
- `cutting`: each cut (input, outputs, knife, uses left) and byproducts given or dropped.
- `heat`: a cauldron, campfire bucket or stone-boiling pot starting to heat, boiling, or cooling off (position, heat source).
- `boiling`: each item boiled (input, result, ticks). With `debug.verbose`, every item passed on to the next handler.
- `soup`: ingredients added, the batch matched (soup ID) or turned into Watery Soup, each serving bowled or eaten from the pot and servings left, bottles refused.
- `evaporation`: water levels lost, cauldrons boiling dry, items dropped out.
- `campfire_bucket` and `stone_boiling`: pots placed, boils used, Hot Pebbles heated and cooled, buckets returned (uses left, boiled or not).
- `spit`: items added and removed, turns, finished items.
- `raw`: raw food eaten, the risk roll and whether Hunger was given.
- `styles`: the style each cook produced (cooked, boiled, dried, toasted) and Burnt Food or Mush results.
- `counters`: Day in and Day out progress every 10 eats of an item, and the grant at 100.

**Milestones** (build Impact's Lib first, then these)

**Before milestone 1:** Impact's Lib is built and published to mavenLocal.

1. **Skeleton:** mod loads with Lib as a dependency; config; IRF's debug categories are registered; IRC detection works.
2. **Knives and cutting:** knives, every cutting recipe, byproducts, cooked versions of every piece.
3. **Cauldron boiling:** heat sources, warm-up, single-item boiling, evaporation, the Lib boiling provider, the handler pass-through, and the SRF washing check.
4. **Cooking any food:** the FoodVariants table, every styled item, layered models, all smelting, smoking, campfire and boiling recipes, Burnt Food, Mush, advancements.
5. **Soups:** batches, ladling, Watery Soup, new soups, grid stews off, the soups tag, refusing bottles at a soup.
6. **Bucket on a campfire:** placing, the 8-boil limit, getting the bucket back.
7. **Spit roasting:** capability, renderer, turning.
8. **Raw food risk.**
9. **Baking:** Flour, Dough (with Lib's water bottle ingredient), Loaf Dough, Dough Bun, Flatbread, Dumpling, cake, cookies, pie.
10. **IRC integration:** recipe switching, stone boiling with Hot Pebbles and stone-boiling pots, the water-filled event, tier and timing checks.
11. **IRV, IRI and SRF checks:** Purified Water from a boiling pot, the food tags IRV reads, byproducts with IRI's locked slots, the decompiled-SRF cauldron review in `docs/srf-notes.md`, and the family test setups.
12. **Polish:** JEI, tooltips, all 26 achievements, lang, sounds and particles.

**Acceptance criteria**

- [ ] IRF runs with no IRC installed, and every recipe uses vanilla materials.
- [ ] With IRC installed, knife and spit recipes switch to IRC parts, the iron knife needs the Stone Table, and IRF crafts are timed.
- [ ] Cutting raw beef gives 2 cuts, and sometimes a bone; each cut cooks in 100 ticks and restores 4 hunger.
- [ ] Raw beef in a furnace or on a campfire gives vanilla cooked beef; boiled, it gives Boiled Beef.
- [ ] Cooked beef on a campfire gives Fire-Roasted Beef; in a furnace, Beef Jerky.
- [ ] Bread in a furnace gives Dry Bread (eaten in 16 ticks), on a campfire Toasted Bread (6 hunger), and boiled, Boiled Bread, which grants "Why was this necessary?".
- [ ] Boiled food won't cook in a furnace and won't go on a campfire; boiling it again gives Mush.
- [ ] Toasted or dried food cooked again gives Burnt Food.
- [ ] Eating the same boiled or toasted food 100 times grants "Day in and Day out", and the count survives logging out and dying.
- [ ] A water cauldron over a campfire boils after 10 seconds; an egg dropped in becomes a Boiled Egg.
- [ ] 3 bones in a boiling cauldron make 2 servings of Bone Broth; random ingredients make Watery Soup.
- [ ] A finished soup in a cauldron or campfire bucket can be eaten with an empty hand or put in any bowl, and IRV counts a serving from the pot exactly like a bowl.
- [ ] A bucket on a campfire boils 8 times, then returns an empty bucket with no wear.
- [ ] A spit holds 6 items, cooks raw food and toasts cooked food, and cooks faster after turning.
- [ ] Raw beef gives Hunger about 30% of the time.
- [ ] SRF's cauldron ore washing and IRV's bottle filling still work with IRF installed: items IRF doesn't use pass through its cauldron handler.
- [ ] With IRV, a glass bottle filled from a boiling cauldron gives Purified Water, a bottle can't be filled from a soup, and Dough accepts Purified Water.
- [ ] A stone-boiling pot gives back the same Wooden Bucket, with its uses and other mods' tags, minus 1 use per Hot Pebble.
- [ ] Cutting byproducts that don't fit drop at the player's feet, with or without IRI. Nothing is deleted.
- [ ] Every number above can be changed in IRF's config files under Lib's folder, and each feature can be switched off without breaking the others.
- [ ] IRF passes every family test setup that includes it, and a dedicated server starts in each.
- [ ] With default settings, every event under Debug logging writes its line. With `debug.enabled = false`, IRF logs only warnings and errors.

**Open questions**

- None. Decided Nov 14, 2026: any bowl works for soups, and soups can also be eaten straight from a cauldron or campfire bucket with an empty hand; buckets on a campfire never wear down; boiled water as a product belongs to IRV.

**Handoff:** build Impact's Lib first (see its spec) and publish it to mavenLocal, then build IRF in its own repo. Export this doc as Markdown and save it as SPEC.md there. Debug logging is on by default: when something misbehaves, read `logs/impacts-debug.log` first, and add log lines with every new feature. The decompiled SRF copy is reference only (see Lib's checklist). For IRF, list every SRF cauldron interaction in `docs/srf-notes.md` and add a GameTest that SRF's washing still works.

## Implementation changes

Changes made during implementation, following the handoff rule "follow the API and note the change".

- **Platforms (decided Nov 14, 2026):** 1.20.1 Forge first, then NeoForge 1.20.1, then Forge and NeoForge up to 1.21. No Fabric. The mod is split into a `common` module (vanilla code) and a `forge` module, and a `neoforge` module gets added at port time.
- **Saved data:** wherever this spec says "capability", the data is stored with vanilla NBT (mixins on the owning entity or block entity, or `SavedData` for world data) in common code. NeoForge replaces capabilities in 1.20.5, so this keeps the later ports small. Behavior is unchanged: saved, synced and copied exactly as the spec describes.
- **Config files (milestone 1):** the spec lists knife durabilities, boiling, soup and spit numbers under `impactsfood-common.toml` in one place and under `impactsfood-server.toml` in another. They follow the family rule instead: every gameplay number is in the server file, and the common file holds only the feature switches (`overhaul.*`, `rawFood.enabled`, `cooking.styles`, `cooking.overcooking`, and `cooking.dried`, `cooking.toasted` and `cooking.boiled` for each style), which recipe conditions read, plus `debug.*`. The spec's key names are kept (`cutting.byproducts`, `boiling.heatUpTicks`, `boiling.bucketBoils`, `boiling.evaporationTicks`, `soup.maxBowls`, `spit.speed`, `spit.turnSpeed`, `rawFood.chance`, `rawFood.durationTicks`). Keys the spec doesn't name are `knives.flintDurability`, `knives.ironDurability`, `cooking.<style>.hungerBonus`, `cooking.<style>.saturationMultiplier`, `cooking.<style>.eatTicks` and similar, each listed with its comment in the file. Forge sends the server file to clients, so food values match on both sides.
- **Feature switches in recipes:** one condition, `impactsfood:feature_enabled` with a `feature` field (`cutting`, `boiling`, `soups`, `baking`, `stews`, ...), gates every switchable recipe.
- **Recipe numbers from config (milestone 2):** recipes keep the spec's numbers in their JSON, and the config changes them at runtime: `cutting.yieldOverrides` and `cutting.byproductChanceOverrides` (`input=value` lists) through Lib's tool-crafting overrides, and `pieces.furnaceTicks` (100), `pieces.smokerTicks` (50) and `pieces.campfireTicks` (300) through a mixin on vanilla's cooking recipes that looks up Food's own recipe IDs.
- **Pieces (milestone 2):** a piece restores `pieces.foodShare` (0.5) of its whole item's hunger, rounded down so two pieces never give more than the whole, and at least 1. The saturation modifier stays the same, so the saturation a piece gives halves with its hunger. Pieces keep the whole item's effects (raw chicken pieces keep raw chicken's Hunger chance) and its meat flag. Potato Slices aren't eaten raw: half a potato's 1 hunger rounds to nothing.
- **Seeds (milestone 2):** with cutting on, a melon slice or a pumpkin plus a knife gives 1 or 4 seeds, and vanilla's grid seed recipes are turned off. Food writes them under their vanilla IDs gated by `forge:not` of its cutting switch, so turning cutting off brings them back.
- **Knives (milestone 2):** they attack at speed 2 (a sword is 1.6), take 1 damage per hit, and are repaired with flint or iron ingots. They're in Food's creative tab, along with Lib's grinder items (Food ships their recipes).
- **Cauldron state (milestone 3):** the spec's "chunk capability" is a per-dimension `SavedData` (`impactsfood_cauldron_pots`), keyed by position and holding only cauldrons that are heating, boiling or holding items. Vanilla cauldrons don't tick, so Food watches their block changes through `BlockStateBase` (a water cauldron filled or refilled, a heat source changing under one, a water cauldron emptied or broken) and ticks its pots from the dimension tick. The vanilla cauldron's blocks and blockstates are unchanged.
- **Taking items (milestone 3):** a cauldron takes items, by hand or dropped in, only while it is hot (over a heat source, or still boiling), so items are never stuck in a cold cauldron. It boils up to `boiling.cauldronItemSlots` (8, a number the spec doesn't give) single items at once. Boiled items pop up and over the rim, marked so the pot doesn't take them straight back. An emptied or broken cauldron drops its unboiled items.
- **Pass-through (milestone 3):** Food's click handler runs inside the vanilla cauldron's `use`, after Forge's `RightClickBlock` handlers, and never cancels an event. It acts only on items a boiling recipe takes in a hot cauldron; everything else returns PASS untouched (GameTest `clicksaddonlyitemsfoodboils`, which also uses SRF's raw gold ore when SRF is installed).
- **Boiling particles (milestone 3):** about once a second, the server tells players who can see a boiling pot where its surface is, on Food's `impactsfood:main` channel. Each client draws bubbles, steam and a simmering sound on its own, following `boiling.particles` and `boiling.sounds` in its client file.
- **Finished food values (milestone 3):** each of Food's finished foods has `foods.<name>.hunger` and `foods.<name>.saturation` in the server file, starting with the Boiled Egg (4 hunger; the spec gives no saturation, so it uses 0.6 like vanilla's cooked meats).
- **Styled food art (milestone 4):** each styled item's model has the spec's two layers, its base food's texture plus a shared overlay (cracks, toast marks or a wet shine), but the client trims the overlay to the base texture's shape as the item atlas loads (sprite source `impactsfood:styled_overlays`). Overlays then never show outside thin shapes like berries or kelp, Food ships no copies of vanilla textures, and resource packs' food textures get matching overlays.
- **Style values (milestone 4):** a style's saturation multiplier applies to the base food's total saturation (hunger times modifier times 2), so toasted food's extra hunger point doesn't add saturation on top of the multiplier. Boiling makes harmful effects less likely by `cooking.boiledNegativeEffectChance` (0.5); helpful effects keep their chance. The spec gives boil times for raw food only (meat and fish 300, bread 100, everything else 200); boiling food that is already cooked takes 200 ticks (bread 100). Every style time is a config value (`cooking.dryTicks` 200, `cooking.smokerDryTicks` 100, `cooking.toastTicks` 300, and `cooking.rawFurnaceTicks`, `cooking.rawSmokerTicks` and `cooking.rawCampfireTicks` for the new raw foods and the egg).
- **New foods' values (milestone 4):** the spec gives hunger only, so saturation follows vanilla's closest foods: Cooked Tropical Fish 4 hunger at 0.6, Cooked Rotten Flesh 3 at 0.3 with a 10% chance of Hunger for 30 seconds (vanilla rotten flesh's length), Fried Egg 3 at 0.6. Burnt Food's and Mush's food values moved under `foods.burntFood` and `foods.mush` with the other foods; their fuel and compost values stay in `burntFood.*` and `mush.*`. Compost chances apply at every server start, so a world's values take effect.
- **Overcooking (milestone 4):** dry heat (furnace, smoker or campfire) burns `#impactsfood:dried_foods`, `#impactsfood:toasted_foods` and the Fried Egg; a furnace or smoker also burns dried kelp. Boiling turns `#impactsfood:styled_foods`, both eggs and Burnt Food into Mush. `#impactsfood:finished_foods` holds the styled foods, both eggs, Burnt Food and Mush (baking adds its own).
- **Tag inheritance (milestone 4):** styled foods join every item tag their base food is in, except Food's own, as item tags are built at each data load. The startup log counts the additions.
- **How a pot picks a soup (milestone 5):** the spec doesn't say how a pot tells soup ingredients from single items, or which soup wins when one recipe holds another (rabbit stew holds hearty stew's ingredients; suspicious stew holds mushroom stew's). Once nothing has gone in for `soup.settleTicks` (40 ticks), the pot makes the soup that uses the most of its ingredients, in as many sets as it holds (a cauldron's most is `soup.maxBowls`). Leftovers stay. Items with a boiling recipe of their own still boil on their own if no soup takes them. Soup ingredients with no boiling recipe that nothing used for `soup.cookTicks` of boiling become Watery Soup. More sets of the same soup can join while it cooks. A pot holds up to `soup.cauldronIngredientSlots` (12) soup ingredients, or `soup.bucketIngredientSlots` (4) in a bucket; the spec gives no number.
- **Serving (milestone 5):** any bowl in `#impactsfood:bowls` (vanilla's bowl; packs can add more) ladles a serving. Eating from the pot runs the soup's own finishing (food, effects, stats and vanilla's `consume_item` trigger, which vanilla's `Player.eat` fires), then the loader's finished-using event through Lib's `postItemUseFinished`, so a suspicious stew's effect applies too. A milk bucket put in a pot leaves its bucket behind.
- **Bottles at a soup (milestone 5):** refused both in Forge's `RightClickBlock` at HIGH priority (so no other mod fills one) and in the cauldron's own click (so vanilla doesn't either), while the pot holds soup ingredients or a soup.
- **Grid stews (milestone 5):** vanilla's mushroom stew, beetroot soup, both rabbit stews and suspicious stew recipes are written under their own IDs, gated by `forge:not` of soups and `overhaul.stews` both on, so turning either off brings them back. Porridge's recipe needs Flour and comes with baking.
- **Soup values (milestone 5):** the new soups use vanilla's stew saturation (0.6); Watery Soup uses 0.1. Bone Broth's Regeneration I lasts 5 seconds. They live under `foods.<soup>` with the other foods, so `soup.waterySoupHunger` became `foods.waterySoup.hunger`.
- **Campfire bucket (milestone 6):** the block sits in the space above the campfire, but its model and shape reach 9 pixels down so the bucket stands in the flames on the logs. It keeps the bucket that was set down (with other mods' NBT) and returns that bucket (as a water bucket, or an empty one once its 8 boils are used). Each item boiled on its own uses a boil, and every 2 soup servings use one, as a cauldron's levels do. A bucket whose water runs out stops boiling at once and pops out what's still in it. Like the stone-boiling pot, taking back a bucket whose water boiled posts Lib's water-filled event with `boiled = true`. Placing it is handled in Forge's `RightClickBlock` (Lib's shared interactions table gives a lit campfire's "sneak + water bucket" input to Food), so the water isn't poured; only vanilla's own water bucket works (Crafting's Wooden Bucket would burn).
- **Spit (milestone 7):** the spec's campfire capability is an `impactsfood:spit` NBT entry on vanilla's campfire block entity, added by a mixin. It is saved with the campfire, sent to clients in the campfire's update tag, and cooked by the campfire's own cooking tick, so it pauses while the campfire is out. Vanilla's campfire renderer draws it (a client mixin), so the campfire keeps its own renderer and no other mod's is replaced. Food goes on the spit first and onto the campfire's own 4 slots once the spit is full. "Turning ... cooks at 1.5×" is read as 1.5 times the campfire's speed in place of the spit's usual 1.25 for `spit.turnTicks`; each turn rolls the spit half a turn on screen and fires Lib's `impactslib:hold_action` trigger with the action `impactsfood:spit_turn`. The standalone Spit is three sticks in a row, because Salty's Realistic Forging's `two_sticks` recipe takes two (see `docs/srf-notes.md`). The Spit burns like a stick (100 ticks).
- **Raw food risk (milestone 8):** applied in Forge's `LivingEntityUseItemEvent.Finish` as the spec says. `#impactsfood:raw_risky` holds raw beef, porkchop, mutton, rabbit, cod and salmon and their raw cuts, pieces and fillets, but not raw chicken or raw chicken pieces: those keep vanilla's own 30% Hunger (a raw chicken piece carries raw chicken's effect), so they aren't rolled twice.
