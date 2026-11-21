# Impact's Realistic Vitality — Mod Spec

Nov 14, 2026 · @Supa

## Overview

Impact's Realistic Vitality (IRV) adds three survival needs on top of health and hunger: **thirst**, **temperature** and **air**. They feed into each other: heat makes you thirsty, thirst and hunger together decide how far you can heal, and the Nether is both hot and smoky.

**Target platform**

- Minecraft 1.20.1, Forge (Java 17), like the other Impact mods.
- Mod ID: `impactsvitality`, package `com.impact.realisticvitality`. Display name: Impact's Realistic Vitality.
- **Requires Impact's Lib** (`impactslib`) as a separate download.
- **Optional:** [Impact's Realistic Crafting](https://claude.ai/artifact/SaBQus4NifLnCSbvWYGWWL) (IRC), [Impact's Realistic Food](https://claude.ai/artifact/H85Ku1ujPuD8QUSVhTsUAF) (IRF), [Impact's Realistic Inventory](https://claude.ai/artifact/Euz9kvYYXiDVHnaD9xRjkB) (IRI), JEI, and Salty's Realistic Forging (SRF). None of them is needed, and IRV never imports their classes.

**Inspirations** (ideas only; no code or assets are copied):

- **Thirst:** *Thirst Was Taken 2*. A thirst bar that works like hunger, water that has to be cleaned before drinking, and drinks that hydrate.
- **Air:** *Thin Air*. Air gets harder to breathe up high, deep down, and in the Nether and End, with breathing gear to cope.
- **Temperature:** *Cold Sweat*. A world temperature from biome, height, time and nearby blocks, and a body temperature that drifts toward it, slowed by clothing.

**Goals**

1. Each need is simple to read on the HUD and works like hunger: a bar or gauge, clear thresholds, and clear effects.
2. The needs connect: heat drains water, thirst limits healing, extreme places stack several needs at once.
3. Every need has an answer you can build or craft, and the answers use the other Impact mods' items when they're installed.

**Non-goals**

- **No air circulation.** Air depends only on height, dimension and nearby lava. A furnace, fire or campfire in a house never makes it hard to breathe.
- No diseases or wounds. No body-part damage. No seasons (temperature uses biome, time and weather only).

## Thirst

Thirst works like hunger: a bar of 20 points (10 droplets) with a hidden **Hydration** value that works like saturation and drains first.

**What drains it**

- The same actions that drain hunger (sprinting, jumping, attacking, mining, taking damage) add thirst exhaustion at the same rates. There's also a small constant drain: 1 point every 5 minutes at rest.
- **Heat:** Hot body temperature multiplies thirst drain by 1.5, and Very Hot by 2 (see Temperature).
- Unclean water can make you **Parched**, a new effect that drains thirst like the Hunger effect drains food.

**Low and empty**

- At 6 points (3 droplets) or less, you can't sprint, just like low hunger.
- At 0, you take dehydration damage on vanilla's starvation rules: down to 10 health on Easy, 1 on Normal, and death on Hard.

**Health cap: hunger and thirst share healing**

Natural regeneration depends on **both** bars. The fuller bar has to be at 18 or more (9 icons) for any regeneration, as vanilla requires of hunger. Then the emptier bar decides how far your health can fill:

| Fuller bar | Emptier bar | Health regenerates up to |
| --- | --- | --- |
| 18–20 | 18–20 | Full (20 health) |
| 18–20 | 7–17 | Three-quarters (15 health) |
| 18–20 | 0–6 (3 icons or fewer) | Half (10 health) |
| Below 18 | Any | No natural regeneration |

For example: 3 bars of hunger (6) with full water heals you to half. Full hunger with empty water also heals to half. Full on both heals to full. The limits are fractions of max health, so they scale with mods that change max health.

- Regeneration only stops at the limit. It never takes away health you already have.
- Vanilla's fast regeneration (from saturation) only happens when both bars are full and both saturation and Hydration are above 0. The exhaustion it costs is split evenly between hunger and thirst.
- **Only natural regeneration is limited.** Potions, golden apples, beacons, Regeneration and other healing work as in vanilla.

**Water and drinks**

| Drink | Thirst | Hydration | Notes |
| --- | --- | --- | --- |
| Unclean Water (any bottle filled from a river, lake, rain or cauldron) | 4 | 1 | 30% chance of Parched for 30 seconds |
| Salt Water (filled from an ocean or beach biome) | −2 | 0 | Makes thirst worse. Boil it to get Purified Water |
| Purified Water | 6 | 4 | Unclean or salt water, boiled or smelted (below) |
| Cold Water | 6 | 4 | Purified Water + ice. Also cools you (see Temperature) |
| Milk | 6 | 3 | Clears Parched, as it clears all effects |
| Any potion | 3 | 1 |  |
| Honey bottle | 2 | 0 |  |
| Bowl soups and stews | 4 | 2 | Also warm you |
| Melon slice | 2 | 1 | Also cools you |
| Sweet berries, glow berries, apples, carrots, beetroot | 1 | 0 |  |
| Dry foods (bread, cookies, dried kelp; with IRF, every dried food) | −1 | 0 | Can't be eaten while Hot (see Temperature) |

- **Purifying water:** smelt any water bottle in a furnace, smoker or campfire to get Purified Water. With IRF installed, a glass bottle filled from a boiling cauldron, campfire bucket or stone-boiling pot gives Purified Water: IRV asks Lib's `isBoiling`, which IRF answers.
- **Drinking from a source:** sneak-right-click water with an empty main hand to take a sip: 1 point of thirst, and the Unclean Water Parched chance.
- **Bottles pick up their purity.** Every bottle of water picks up its purity when filled (separate items: unclean\_water, salt\_water, purified\_water, cold\_water). IRV handles the glass bottle on water blocks itself and replaces the glass-bottle entry in vanilla's `CauldronInteraction.WATER` map, so filling never gives a vanilla water bottle.
- **Nothing that needs a water bottle breaks.** Purified Water brews like a water bottle: IRV registers Forge brewing recipes (`IBrewingRecipe`) for every vanilla brew that starts from one (Awkward, Mundane and Thick Potions, and splash and lingering water bottles). Any IRV water turns dirt into mud and pours into a cauldron like a water bottle. IRV adds its water items to Lib's `#impactslib:water_bottles`, so recipes using Lib's `impactslib:water_bottle` ingredient (like IRF's Dough) accept them. A vanilla water bottle from anywhere else (a villager, a loot chest, creative) still works, and drinks as Unclean Water.

**Water gear by stage.** There's something new for water at each of five stages, from a wooden cup to a canteen that refills itself. With IRC, iron and copper items need the Makeshift Stone Table, and gold and diamond items need the Crafting Table.

| Stage | Item | What it does | Recipe (alone) | Recipe (with IRC) |
| --- | --- | --- | --- | --- |
| 1. Early | Sipping by hand | Sneak-right-click water with an empty hand: 1 thirst, unclean | — | — |
| 1. Early | Wooden Cup | Holds 1 drink. Set a filled cup on a campfire to purify it (300 ticks) | Bowl + stick | Bowl + Trimmed Stick |
| 2. Mid-early | Waterskin | Holds 3 drinks | 3 leather + string | 2 Leather Panels + Twine + (Bone Needle) |
| 2. Mid-early | Rain Barrel (block) | Collects clean rainwater under open sky: 1 drink per minute of rain, up to 16. Fill cups, bottles and containers from it | Barrel + 3 planks | Wooden Bucket + 2 Wooden Panels (the funnel) |
| 2. Mid-early | Smelting bottles | Smelt any water bottle into Purified Water (needs glass bottles) | — | — |
| 3. Mid | Canteen | Holds 5 drinks | 3 iron ingots + leather | 3 Iron Plates + Leather Strip + Iron Rivets |
| 3. Mid | Water Filter (block) | Pour in unclean or salt water (a bucket is 4 drinks). It drips out purified water, 1 drink every 30 seconds. Uses Charcoal Filters, the same item the Respirator takes | Barrel + Charcoal Filter + iron bars | Wooden Bucket + Charcoal Filter + Iron Bars |
| 4. Mid-late | Insulated Flask | Holds 8 drinks, and drinks keep their temperature effect: Cold Water stays cooling and soups stay warming | Canteen + 2 gold ingots + 2 wool | Canteen + 2 gold ingots + Wool Lining material (2 wool) + Twine + (Bone Needle) |
| 4. Mid-late | Copper Still (block) | Automatic: with a heat source under it, turns water from hoppers or buckets into purified water, 1 bucket per minute. Outputs to hoppers | Cauldron + 4 copper ingots + glass | Cauldron + 4 copper ingots + glass |
| 5. Late | Tidebound Canteen | Holds 8 drinks that refill themselves with purified water: 1 every 20 seconds, or every 5 seconds in rain or water | Canteen + Heart of the Sea + 4 prismarine shards | Same |
| 5. Late | Hydration Pack | Holds 24 drinks, and a keybind (default V) lets you sip without holding it. Worn in the back slot when Lib reports one (IRI installed), otherwise in the chest slot | Canteen + 5 leather + 2 diamonds | Canteen + 4 Leather Panels + 2 diamonds + Twine + (Bone Needle) |

**With IRC, you can drink from a Water-filled Wooden Bucket** (`impactscrafting:wooden_water_bucket`): hold use while aiming at the air. A full bucket holds 4 drinks, counted in IRV's own `impactsvitality:drinks` tag, and drinking doesn't cost bucket uses. Its purity comes from Lib's water-filled event: when IRC fills the bucket, or IRF hands it back from a stone-boiling pot, IRV stamps `impactsvitality:purity` (purified if it boiled, salt in ocean and beach biomes, otherwise unclean). After the last drink, or when you pour it over yourself to cool off (sneak + use at the air), IRV swaps it for IRC's empty `impactscrafting:wooden_bucket`, found by registry ID, keeping only its uses tag.

Containers remember their water's purity. Filling one with a different purity mixes down to the worst (one sip of salt water spoils a canteen). Rain Barrel water counts as purified. Sneak-use a container to pour it over yourself and cool off (see Temperature).

## Temperature

There are two temperatures. **Ambient** is how hot or cold the world is where you stand, shown in °F (config `display.units`, °F or °C). **Body temperature** runs from −100 to +100, with 0 as comfortable. It drifts toward what the ambient temperature calls for, and clothing slows the drift and widens your comfort range.

**Ambient temperature** (all values configurable)

| Factor | Effect |
| --- | --- |
| Biome | Base = 30 + 40 × the biome's vanilla temperature. Plains (0.8) is 62°F, desert (2.0) 110°F, snowy plains (0.0) 30°F |
| Dimension | Nether +40°F on top of its biomes. End is a flat 20°F |
| Height | Above Y 100, −1°F per 4 blocks. Underground (no sky above, below Y 50), it drifts toward a steady 55°F; below Y 0, it rises toward 75°F |
| Time of day | Night −15°F; in deserts and badlands −35°F, so desert nights get cold |
| Weather | Rain −10°F, snow −15°F (only with open sky above) |
| Sun | +10°F at midday under open sky |
| Nearby heat (within 5 blocks; Lib's `#impactslib:heat_sources`) | Lava +25, fire or lit campfire +15, lit furnace, smoker or blast furnace +10, magma block +8. Closer counts more; the total from blocks is capped at +40 |
| Nearby cold (within 5 blocks; Lib's new `#impactslib:cold_sources`) | Blue ice −20, packed ice −15, ice −10, powder snow −10, snow block −5. Capped at −40 |
| Water | Standing in water counts as 20°F colder, and leaves you **Wet** for 30 seconds afterward (also from rain or pouring water on yourself) |
| Shelter | No sky above you removes the sun, rain and snow effects |

**Body temperature**

- The comfortable ambient range is 50–80°F. Inside it, body temperature drifts back to 0. Outside it, body temperature drifts toward ±100, faster the further outside you are (about 1 point per second per 10°F beyond the range).
- **Insulation** widens the comfortable range: each point of warmth lowers the cold edge by 3°F, and each point of cooling raises the hot edge by 3°F.
- Being Wet cools you, and drinking Cold Water or eating a melon slice drops body temperature by 15 right away. A bowl of soup or stew raises it by 15.

| State | Body temperature | Effects |
| --- | --- | --- |
| Freezing | ≤ −100 | 1 freezing damage every 3 seconds, the frost screen overlay from powder snow, Slowness II |
| Very Cold | −100 to −75 | Slowness II, can't sprint |
| Cold | −75 to −50 | Slowness I, mining 15% slower |
| Chilly | −50 to −25 | No effect, just a warning on the gauge |
| Comfortable | −25 to +25 | None |
| Warm | +25 to +50 | No effect, just a warning on the gauge |
| Hot | +50 to +75 | Thirst drains 1.5×. **You can't eat dry foods** |
| Very Hot | +75 to +100 | Thirst drains 2×, Weakness I, still no dry foods |
| Overheating | ≥ +100 | 1 heat damage every 3 seconds, Nausea, still no dry foods |

**Too hot for dry food:** while Hot or worse, right-clicking a food in `#impactsvitality:dry_foods` does nothing, and the action bar says "Too hot to eat anything dry." Without IRF the tag holds bread, cookies and dried kelp. With IRF it also holds every dried food (jerky, Dry Bread, crisps, chips), Dough Buns and Flatbread: the tag includes `#impactsfood:dried_foods` and those two IRF items as `"required": false` entries, so it loads fine without IRF.

**Insulation**

| Source | Warmth | Cooling |
| --- | --- | --- |
| Leather armor | 2 per piece (chestplate 3) | 0 |
| Iron armor | −1 per piece | −1 per piece. Metal conducts, so it makes both heat and cold worse |
| Gold, diamond and netherite armor | 0 | 0 |
| Chainmail armor | 0 | 1 per piece (it breathes) |
| Wool Lining (for cold) | +2 | 0 |
| Fur Lining (rabbit hide, for bitter cold) | +3 | 0 |
| Leather Lining (for heat) | 0 | +2 |
| Straw Hat (helmet) | 0 | +4, but only in the sun |
| Wet | 0 | +6 |

Negative insulation narrows the comfortable range: a full set of iron armor (−4 each way) moves the cold edge up to 62°F and the hot edge down to 68°F. With SRF installed, its iron armor counts as iron.

- **Linings** are sewn into any armor piece, and there are two ways to do it:
  - **With IRC:** in any crafting grid, armor piece + lining material + Twine + (Bone Needle). This is IRV's own recipe type, `impactsvitality:armor_lining`, gated by `forge:mod_loaded impactscrafting`. The result is a copy of the armor piece with all its NBT (damage, enchantments, trims, name) plus the lining. The needle stays in the grid and takes 1 damage through Lib's `ToolCrafting.damageTools`, as with all IRC sewing.
  - **Without IRC:** at an anvil, with the armor piece in the left slot and the lining material in the right, for 1 XP level. IRV handles this in `AnvilUpdateEvent`, which also copies the armor's NBT.
  - Lining materials: 3 wool (Wool Lining), 4 rabbit hide (Fur Lining), or 3 leather (Leather Lining).
- The lining is stored as NBT on the armor (`impactsvitality:lining`) and shown in its tooltip. One lining per piece; a new lining replaces the old one, and the old material isn't returned.
- Straw Hat: 5 wheat in a helmet shape. With IRC: 5 wheat + Twine.
- Armor materials are read from tags (`#impactsvitality:warm_armor`, `#impactsvitality:cooling_armor`, `#impactsvitality:conductive_armor`), so modded armor can be added. conductive\_armor holds iron armor by default.

**Thermometer:** glass + iron ingot + redstone (with IRC: glass + Iron Plate + redstone, at the Makeshift Stone Table). Held in either hand or carried in your inventory, it adds the ambient temperature readout to the HUD.

## Air

Air is only a problem in four kinds of places: very high up, very deep down, the Nether and End, and right next to lava. Everywhere else it's fine. **There's no air circulation.** Air quality comes only from your height, your dimension and nearby lava, so a furnace, fire, campfire or torch in a sealed house never affects breathing.

**Air quality**

| Where | Quality | Air bubbles lost |
| --- | --- | --- |
| Overworld, Y −40 to 160 | Good | None; bubbles refill at vanilla's rate |
| Overworld, Y 160 to 220 | Thin | 1 bubble every 6 seconds (empty in about 1 minute) |
| Overworld, above Y 220 | Very Thin | 1 bubble every 3 seconds |
| Overworld, below Y −40 | Thin (stale deep air) | 1 bubble every 6 seconds |
| The End | Thin | 1 bubble every 6 seconds |
| The Nether | Smoky | 1 bubble every 4 seconds |
| Within 3 blocks of lava (any dimension) | Smoky | 1 bubble every 4 seconds |
| Right next to lava (within 1 block) | Very Smoky | 1 bubble every 2 seconds |

- It reuses vanilla's air supply and bubble row, so it shows above the hunger bar as it does underwater. Being underwater still uses vanilla rules.
- When the bubbles run out, you take 2 damage every 2 seconds, like drowning. The damage types are `impactsvitality:thin_air` and `impactsvitality:fumes`, with their own death messages ("... ran out of air on the mountaintop", "... choked on lava fumes").
- Lava means lava blocks (source or flowing) and lava cauldrons. Fire, magma blocks and campfires don't make fumes.
- **Water Breathing** and **Conduit Power** protect against bad air too (config `air.waterBreathingProtects`, default true). **Respiration** slows bubble loss by a third per level.
- Heights are configurable, and other dimensions can be given a quality through a datapack file (`data/impactsvitality/air_quality/<dimension>.json`).

**Breathing gear by stage.** Each stage has an answer for thin air and one for smoke. With IRC, iron and copper items need the Makeshift Stone Table, and gold, diamond and shulker items need the Crafting Table.

| Stage | Item | What it does | Recipe (alone) | Recipe (with IRC) |
| --- | --- | --- | --- | --- |
| 1. Early | Cloth Mask (helmet slot) | Use it on water to dampen it. A damp mask halves smoky air loss for 2 minutes, then dries out | 2 wool + string | 2 wool + Twine + (Bone Needle) |
| 1. Early | Air Bladder | Fill it by using it in Good air. In Thin air, use it to get back 4 bubbles. 3 uses per fill | Leather + string | Leather Panel + Twine |
| 2. Mid-early | Bottled Air | Use a glass bottle in Good air to bottle it (not in the Nether, the End or Thin air). Drinking it gives back 5 bubbles. Stacks to 16 | Glass bottle | Glass bottle |
| 2. Mid-early | Respirator (helmet slot) + Charcoal Filter | With a filter, Smoky and Very Smoky air count as Good. A filter lasts 10 minutes of smoky air | Respirator: 2 leather + glass pane + string. Filter: charcoal + wool + string | Respirator: Leather Panel + glass pane + Twine + (Bone Needle). Filter: charcoal + wool + Leather Strip |
| 3. Mid | Air Tank | Carried anywhere in your inventory with a Respirator on: supplies air in Thin air for 3 minutes before your bubbles start dropping. Refills by itself in Good air (full in 30 seconds) | 5 iron ingots + glass | 4 Iron Plates + Iron Hinge (the valve) + Iron Rivets → loose tank, then hammer |
| 4. Mid-late | Rebreather (helmet slot) | Respirator upgrade: filters last twice as long, any Air Tank lasts twice as long, and it gives 2 armor | Respirator + 2 gold ingots + Air Bladder | Respirator + 2 gold ingots + Air Bladder + Iron Rivets |
| 4. Mid-late | Air Compressor (block) | Refills an Air Tank in 5 seconds, even in Thin air, burning furnace fuel. Lets you set up camp high up or in the End | 4 copper ingots + piston + furnace | 4 copper ingots + Redstone Mechanism + Stone Casing |
| 5. Late | Netherite Rebreather | Upgraded from the Rebreather at a smithing table with a netherite ingot and an upgrade template. Never needs a filter: smoky air never affects you. Fire-resistant | Smithing upgrade | Smithing upgrade |
| 5. Late | Shulker Air Tank | Holds 15 minutes of air and refills in Good air in 30 seconds | Air Tank + 2 shulker shells | Air Tank + 2 shulker shells |

Water Breathing, Conduit Power and Respiration still help, as described above.

With IRI installed, an Air Tank or Shulker Air Tank can go in the back slot instead of a pack, so going high or into the End means choosing between air and storage. IRV adds both tanks and the Hydration Pack to Lib's `#impactslib:back_slot_items` and reads the slot through Lib's `BackSlot.get`, never through IRI's classes. Without IRI, the back slot is always empty and tanks work from anywhere in the inventory, as described above.

## How it all connects

**Between the three needs**

| Cause | Effect |
| --- | --- |
| Hot body temperature | Thirst drains 1.5× (Hot) or 2× (Very Hot), and dry foods can't be eaten |
| Eating dry food | Costs 1 thirst |
| Low thirst or low hunger | Lowers the health cap: one bar empty and the other full lets health regenerate only to half |
| Being Wet (water, rain, pouring a drink on yourself) | Cools you, so it fights heat and the thirst that comes with it |
| Standing near lava | Hard to breathe (Smoky air) and hot (+25°F), so thirst drains faster too |
| The Nether | Hot and Smoky everywhere: you need cooling, water and a filtered Respirator |
| The End and high mountains | Cold and Thin: you need warm clothing and an Air Tank |
| Deep caves below Y −40 | Warm and Thin, and often near lava |

**Other Impact mods** (connected through Impact's Lib and recipe conditions, never direct code references)

| Mod | How IRV fits |
| --- | --- |
| Impact's Lib | Uses Lib's `#impactslib:heat_sources` for warmth and adds a matching `#impactslib:cold_sources` tag to Lib. Asks Lib's `isHeated`, `isBoiling` and `BackSlot`, listens to the water-filled event, adds its water items to `#impactslib:water_bottles`, and places its HUD rows through Lib's HUD layout. Uses Lib's per-player counters and shared achievement triggers, and registers itself so "Why am I here?" stays hidden |
| IRF | A bottle filled from a boiling cauldron, campfire bucket or stone-boiling pot is Purified Water (Lib's `isBoiling`, answered by IRF). IRF soups (`#impactsfood:soups`) hydrate (6 thirst) and warm you (+15). Boiled foods (`#impactsfood:boiled_foods`) give +2 thirst. Toasted foods (`#impactsfood:toasted_foods`) warm you slightly (+5). Every IRF dried food (`#impactsfood:dried_foods`) is a dry food: −1 thirst, and it can't be eaten while Hot. All of these are optional tag references, so IRV loads without IRF |
| IRC | Container, lining, thermometer and breathing-gear recipes switch to IRC parts (Leather Panels, Twine, Bone Needle, Iron Plates through `#impactscrafting:iron_plates`). Iron items need the Makeshift Stone Table. IRV ships an `impactscrafting:hammering` recipe for its loose Air Tank, gated by `forge:mod_loaded impactscrafting`. You can drink from a Water-filled Wooden Bucket (see Drinks). Linings are sewn with the Bone Needle through IRV's `armor_lining` recipe. Rabbit hide for Fur Lining also drops from IRF's knife cutting |
| IRI | Drinking needs the container in your active hand slot, and a Waterskin takes up a precious pocket. Air Tanks and the Hydration Pack can go in the back slot through Lib. Wearing a Frame Pack (`impactsinventory:frame_pack`, an optional entry in IRV's `#impactsvitality:heavy_packs`) adds 10% to thirst drain from exertion. IRV's HUD lines up with IRI's hotbar through Lib's layout |
| SRF | Claude Code reads the decompiled SRF source for hot blocks and items (forges, heated metal) and adds them to `#impactslib:heat_sources` as `"required": false` entries. IRV's cauldron handling only touches glass bottles and IRV's own containers, so SRF's ore washing is unaffected |

## Achievements

IRV's advancement tab, "Realistic Vitality", has 22 achievements in three branches, one for each need. **Goals** mark big steps; **challenges** give 50 XP.

| Achievement | How to get it | Unlocks after | Type |
| --- | --- | --- | --- |
| **Stay Hydrated** (tab root, Waterskin icon) | Drink any water | — | task |
| Something in the Water | Get Parched from unclean water | Stay Hydrated | task (hidden) |
| Worth Its Salt | Drink salt water | Stay Hydrated | task (hidden) |
| Clean Living | Drink Purified Water | Stay Hydrated | task |
| Skin in the Game | Craft a Waterskin | Clean Living | task |
| Half Measures | Have your health stop regenerating at the limit because one bar is low | Stay Hydrated | task |
| Well Watered | Drink 500 times | Skin in the Game | challenge |
| Cold Feet | Become Cold | Stay Hydrated | task |
| Albert and Einstein | Catch on fire while Cold | Cold Feet | task (hidden) |
| Bundle Up | Wear 4 armor pieces with Wool or Fur Linings | Cold Feet | task |
| Taking the Temperature | Craft a Thermometer | Cold Feet | task |
| Dry Spell | Try to eat a dry food while Hot | Stay Hydrated | task (hidden) |
| It's getting hot in here! (so hot!) | Reach Overheating, the dangerous end of hot | Dry Spell | task |
| Hat Trick | Wear a Straw Hat in a desert at noon | Dry Spell | task |
| Breathtaking | Lose air from Thin air on a mountain | Stay Hydrated | task |
| Is it worth it? | Go down near bedrock (Y −58 or lower) with no air support: no Respirator, Rebreather, Air Tank, Bottled Air or breathing effect | Breathtaking | task |
| Up in Smoke | Lose air from lava fumes | Breathtaking | task |
| Masked Up | Wear a Respirator with a filter in the Nether | Up in Smoke | goal |
| It feels like… nothing | Enter the End | Breathtaking | task |
| Into Thin Air | Use an Air Tank in the End | It feels like… nothing | goal |
| Peak Condition | Stand above Y 300 with full thirst, full air and a Comfortable body temperature | Into Thin Air | challenge |
| In Balance | Spend a whole Minecraft day (20 minutes) with thirst above 6, air never empty, and body temperature never past Cold or Hot | Bundle Up | challenge |

**Implementation:** datagen advancements. IRV triggers: `impactsvitality:drank` (with drink type and purity), `impactsvitality:temperature_state`, `impactsvitality:air_lost` (with cause: thin or fumes), `impactsvitality:health_capped`, `impactsvitality:dry_food_refused`, `impactsvitality:gear_used`. It feels like… nothing uses vanilla's changed\_dimension trigger, and Albert and Einstein checks for being on fire while Cold. Well Watered and In Balance use Lib's per-player counters (In Balance resets whenever a condition breaks).

## Items, HUD, config and handoff

**Items and effects**

| Group | IDs |
| --- | --- |
| Water | `unclean_water`, `salt_water`, `purified_water`, `cold_water` |
| Water gear | `wooden_cup`, `waterskin`, `canteen`, `insulated_flask`, `tidebound_canteen`, `hydration_pack` (purity and drinks stored in NBT; stack 1) |
| Water blocks | `rain_barrel`, `water_filter`, `copper_still` |
| Breathing gear | `cloth_mask`, `air_bladder`, `bottled_air`, `respirator`, `charcoal_filter`, `air_tank` (plus `loose_air_tank`, only obtainable with IRC), `rebreather`, `netherite_rebreather`, `shulker_air_tank` |
| Breathing blocks | `air_compressor` |
| Clothing | `straw_hat`. Linings are NBT on existing armor, not items |
| Tools | `thermometer` |
| Effect | `parched` |
| Damage types | `dehydration`, `heatstroke`, `thin_air`, `fumes` (cold damage uses vanilla's `freeze`) |
| Tags | `dry_foods`, `drinks`, `warm_armor`, `cooling_armor`, `conductive_armor`, plus Lib's `heat_sources` and new `cold_sources` |

**HUD**

- **Thirst:** a row of 10 droplets directly above the hunger row, mirroring it. Droplets shake when thirst is at 6 or less, and turn green while Parched.
- **Air:** vanilla's bubble row, shown above the thirst row whenever air isn't full (not only underwater), with a small cloud icon at its end: gray for Thin, darker gray for Very Thin, orange for Smoky.
- **Body temperature:** a round gauge centered just above the hotbar, between the health and hunger columns. It's blue when cold, white when comfortable and red when hot, with a needle that moves. It pulses at Cold/Hot and worse.
- **Ambient readout** (°F or °C) appears under the gauge only while you carry a Thermometer.
- IRV never hard-codes positions. The thirst and air rows stack on the right through Lib's HUD layout (Forge's right-side height), and the gauge centers on the hotbar bounds Lib publishes, so everything lines up over vanilla's hotbar or IRI's two-slot one.

**Config** (every number from this spec): `impactsvitality-server.toml` (per world, synced to clients) holds thirst.\* (drain rates, Parched chance, sip size), healthCap.enabled true, healthCap.capAllHealing false, temperature.\* (biome formula, height, time, weather, block values, comfort range, drift speed), air.\* (height bands, drain rates, lava radius) and air.waterBreathingProtects true. `impactsvitality-client.toml` holds display.units F and HUD options. `impactsvitality-common.toml` holds the need toggles thirst.enabled, temperature.enabled and air.enabled, plus debug.\*. Each need can be turned off on its own, and its gear recipes with it, so the toggles are read by recipe conditions and must be COMMON. The server sends its toggles to clients at login, so the HUD matches.

**Extended configuration** (Lib's framework; files under `config/impactslib/` and `serverconfig/impactslib/`, see Lib's Configuration). Beyond the keys above, `impactsvitality-server.toml` holds per-item drink and food values (`drinks.overrides`, as `item=thirst,hydration`), purity rules (the salt-water biome list, whether rain counts as clean), per-biome and per-dimension temperature overrides, block heat and cold values (`blockTemps`, as `block=value`), each armor tag's insulation, lining values (wool, fur, leather), per-dimension air rules and height bands, every water and breathing gear item's capacity, refill rate, durability and filter life, the Rain Barrel, Water Filter and Copper Still rates, and dehydration, freezing, overheating and air damage. `impactsvitality-common.toml` also switches off single pieces (the health cap, the dry-food rule, sipping, linings, each gear item) along with their recipes. `impactsvitality-client.toml` holds HUD positions and offsets, gauge size, droplet shake, sounds and units.

**Technical**

- **Build:** ForgeGradle 6, Forge 47.x, Parchment mappings, Mixin, Java 17. Impact's Lib is a required `mods.toml` dependency (`implementation fg.deobf("com.impact:impactslib:<version>")`). JEI 15.x is optional.
- **Packages:** `com.impact.realisticvitality` → registry, thirst (ThirstData, drinks, purity, sipping, bottle filling, brewing recipes), temperature (AmbientCalculator, BodyTemperature, insulation, linings, ArmorLiningRecipe, the anvil handler), air (AirQuality, breathing gear), health (the shared cap), client (HUD overlays through Lib's layout), network, compat (IRC, IRF and IRI conditions, Lib listeners, JEI), debug (log categories), config, datagen.
- **Player data:** one capability holds thirst, hydration, thirst exhaustion, body temperature, the Wet timer and gear state. It's saved, copied on respawn when keepInventory-style rules apply, and synced to the client every time a value changes.
- **Performance:** ambient temperature and air quality are recalculated once a second per player, not every tick. The heat, cold and lava scans (5- and 3-block radius) use cached block-tag lookups and Lib's `isHeated`.
- **Health cap:** a mixin in FoodData.tick checks the cap before vanilla's natural regeneration heals, and splits the regeneration exhaustion between hunger and thirst.
- **Dry foods:** LivingEntityUseItemEvent.Start cancels eating while Hot. Thirst values from foods are applied in LivingEntityUseItemEvent.Finish.
- **Water bottles:** glass-bottle filling is IRV's own handler on water blocks plus the glass-bottle entry of `CauldronInteraction.WATER`. Brewing uses `BrewingRecipeRegistry.addRecipe` with `IBrewingRecipe`. Dirt-to-mud and cauldron pouring live on IRV's water items. Wooden Bucket purity comes from a listener on Lib's water-filled event.
- **Peaceful mode:** thirst doesn't drain, like hunger. Temperature and air still work, but never deal damage.
- **Tests:** GameTests for thirst drain and refill, purification by smelting, the health cap at full/empty, the dry-food refusal while Hot, ambient temperature next to lava and ice, air loss at Y 230 and next to lava, a filtered Respirator in Smoky air, an Awkward Potion brewed from Purified Water, an enchanted iron chestplate keeping its enchantments when lined, and the Wooden Bucket's drink count. They run in every family test setup that includes IRV.

**Debug logging.** IRV logs through Lib's `ImpactsLog` (see Lib's Debug logging). It's on by default (`debug.enabled = true` in `impactsvitality-common.toml`), and every line starts with `[Impact/vitality/<category>]`. Per-second systems log only changes. The categories:

- `startup`: version, Lib version, which Impact mods and SRF were found, need toggles, tag sizes, brewing recipes registered.
- `compat`: SRF heat entries resolved or missing, and which Lib providers answered (boiling, back slot).
- `thirst`: thirst crossing each 2-point step, dehydration damage, Parched given or cleared.
- `drink`: each drink (item, purity, thirst and hydration gained), sips from a source, bottles filled (source, purity, whether Lib said boiling), Wooden Bucket drinks left.
- `purity`: purity stamped on a Wooden Bucket from the water-filled event, and smelting conversions.
- `health`: natural regeneration stopped by the cap (hunger, thirst, the cap reached).
- `temperature`: body state changes (Comfortable to Warm, and so on). With `debug.verbose`, each second's ambient breakdown (biome, height, time, weather, sun, blocks, water, armor).
- `food`: dry food refused while Hot, and food thirst and warmth applied.
- `air`: air quality band changes, bubble drain starting and stopping, air damage.
- `gear`: tank refills and empties, filters wearing out, masks drying, linings sewn or refused, back-slot items found.

**Milestones**

**Before milestone 1:** Impact's Lib is built and published to mavenLocal.

1. **Skeleton:** mod loads with Lib; the server, client and common configs; IRV's debug categories; player capability.
2. **Thirst:** the bar, drain, HUD through Lib's layout, dehydration damage.
3. **Drinks and water gear:** water purity items, bottle filling on water blocks and cauldrons, brewing and mud with IRV water, sipping, smelting, all five stages (cup, Waterskin, Rain Barrel, Canteen, Water Filter, Insulated Flask, Copper Still, Tidebound Canteen, Hydration Pack).
4. **Health cap.**
5. **Ambient temperature:** biome, dimension, height, time, weather, sun, blocks, water.
6. **Body temperature:** drift, states, effects, the gauge, the dry-food rule.
7. **Insulation:** armor tags (iron makes both worse), linings by Bone Needle (the armor\_lining recipe) or anvil, Straw Hat, Thermometer.
8. **Air:** quality bands, lava fumes, bubble drain, damage types.
9. **Breathing gear:** all five stages (Cloth Mask, Air Bladder, Bottled Air, Respirator and filters, Air Tank, Rebreather, Air Compressor, Netherite Rebreather, Shulker Air Tank).
10. **Integration:** IRF purification through Lib's `isBoiling` and the IRF food tags, IRC recipes and the hammering recipe, the Wooden Bucket (water-filled event, drinks), IRI's back slot through Lib, SRF heat entries from the decompiled source, and the family test setups.
11. **Achievements.**
12. **Polish:** JEI info pages, tooltips, lang, sounds (panting when thirsty, shivering, coughing in fumes).

**Acceptance criteria**

- [ ] Thirst drains with exertion, twice as fast when Very Hot, and dehydration damage follows vanilla's difficulty rules.
- [ ] With 3 bars of hunger and full water, health regenerates to 10 and stops. Full hunger with empty water also stops at 10. Full on both regenerates to 20. A healing potion still heals past the limit.
- [ ] A river bottle gives Unclean Water, an ocean bottle gives Salt Water, and smelting either gives Purified Water.
- [ ] Purified Water brews into an Awkward Potion, turns dirt into mud, and works in IRF's Dough.
- [ ] A Rain Barrel fills in the rain under open sky; a Water Filter turns unclean water into purified water; a Tidebound Canteen refills on its own.
- [ ] Standing in a desert at noon makes you Hot, and bread can't be eaten until you cool down.
- [ ] A full set of iron armor narrows the comfortable range to 62–68°F.
- [ ] With IRC, a Wool Lining is sewn on with the Bone Needle; without IRC, at an anvil. Either way, an enchanted, damaged chestplate keeps its enchantments and damage.
- [ ] A lit furnace in a sealed room warms it, but never affects air.
- [ ] At Y 230 air drains; within 1 block of lava it drains faster; a Respirator with a filter stops the lava drain; a Netherite Rebreather never needs a filter.
- [ ] Going below Y −58 with no air gear grants "Is it worth it?"; entering the End grants "It feels like… nothing".
- [ ] With IRF, a glass bottle filled from a boiling cauldron gives Purified Water, and from an unheated one, Unclean Water. With IRC, gear recipes use IRC parts. With IRI, air tanks and the Hydration Pack fit in the back slot.
- [ ] With IRC, a Water-filled Wooden Bucket gives 4 drinks of the right purity, then becomes an empty Wooden Bucket with the same uses.
- [ ] Each need can be turned off in config without breaking the others.
- [ ] A dedicated server starts with IRV alone and with every Impact mod installed, and IRV passes every family test setup that includes it, SRF included.
- [ ] With default settings, every event under Debug logging writes its line. With `debug.enabled = false`, IRV logs only warnings and errors.

**Open questions**

- None right now. Decided on Nov 14, 2026: no seasons; iron armor makes heat and cold worse; the healing limit applies to natural regeneration only; linings are sewn with the Bone Needle (IRC) or at an anvil (without IRC).
- From the family review: Purified Water does everything a vanilla water bottle does (brewing, mud, cauldrons, recipes); Wooden Bucket purity comes from Lib's water-filled event; linings copy the armor's NBT.

**Handoff:** build after Impact's Lib (published to mavenLocal). Export as Markdown and save it as SPEC.md in the IRV repo. Debug logging is on by default: read `logs/impacts-debug.log` first when something misbehaves, and add log lines with every new feature. The decompiled SRF copy is reference only (see Lib's checklist). For IRV, list SRF's hot blocks and items in `docs/srf-notes.md` and add them as optional heat sources.

## Implementation changes

Changes made during implementation, following the handoff rule "follow the API and note the change".

- **Platforms (decided Nov 14, 2026):** 1.20.1 Forge first, then NeoForge 1.20.1, then Forge and NeoForge up to 1.21. No Fabric. The mod is split into a `common` module (vanilla code) and a `forge` module, and a `neoforge` module gets added at port time.
- **Saved data:** wherever this spec says "capability", the data is stored with vanilla NBT (mixins on the owning entity or block entity, or `SavedData` for world data) in common code. NeoForge replaces capabilities in 1.20.5, so this keeps the later ports small. Behavior is unchanged: saved, synced and copied exactly as the spec describes.
- **Config placement:** the common file holds the need switches (`thirst.enabled`, `temperature.enabled`, `air.enabled`), the piece switches `pieces.sipping`, `pieces.linings` and `pieces.dryFoodRule`, and one switch per gear item under `gear.` (`gear.canteen`, `gear.respirator`, ...). Recipes read them through IRV's `impactsvitality:feature_enabled` condition, which names a need by its config key, a piece as `sipping`, `linings` or `dry_food_rule`, and gear by its item ID. A piece or gear item is off whenever its need is. The health cap stays in the server file (`healthCap.enabled`), as the Config paragraph lists it there, because it has no recipe.
- **Player data on death:** thirst, Hydration, body temperature, the Wet timer and air reset on death like hunger, even with keepInventory, and are kept when returning from the End. Copying them with keepInventory would respawn a player who died of thirst at 0 thirst.
- **Sync:** the player's values go to their client at login and then whenever a value the HUD shows changes (checked at the end of each player tick), on IRV's `impactsvitality:main` channel. The server's need switches go with the login message.
- **Thirst exhaustion:** a mixin on `Player.causeFoodExhaustion` adds the same exhaustion to thirst, so every action vanilla charges food for costs thirst at the same rate (times `thirst.exhaustionMultiplier`, default 1). The Hunger effect drains food through the same method but isn't an action, so it is marked and costs no thirst. Natural regeneration's exhaustion is not charged here; milestone 4 splits it.
- **Resting drain:** the 1 point every 5 minutes is paid as one point's worth of exhaustion (`thirst.exhaustionPerPoint`, default 4, as hunger), so like any other drain it takes Hydration first.
- **Peaceful:** thirst never drops and refills 1 point every 10 ticks, as vanilla refills hunger on Peaceful.
- **Damage types:** all four are generated with their vanilla tags. Dehydration is like starving (`bypasses_armor`, `bypasses_effects`); heatstroke bypasses armor and uses the burning hurt sound; thin air and fumes are like drowning (`bypasses_armor`, `no_impact`, drowning sound). Each has a death message and a "while fighting" variant.
- **Drink table:** the Drinks table lives in `drinks.values` as `item=thirst,hydration` or `#tag=thirst,hydration` entries, checked in order (first match wins); `drinks.overrides` wins over it. Bowl soups and stews are `#impactsvitality:soups`; sweet berries, glow berries, apples, carrots and beetroot are `#impactsvitality:juicy_foods`. Both are IRV tags added for the table.
- **Sipping:** sips have a cooldown, `thirst.sipCooldownTicks` (default 20), so holding the button doesn't drink a lake in a second. A sip is unclean everywhere, as the spec says, even from the ocean. When there's no block behind the water within reach only the client sees the click, so it sends `impactsvitality:main`'s sip message and the server checks everything again.
- **Bottle filling:** cauldron water is never salty (unclean, or purified while Lib says it boils). Dispensers fill glass bottles from water with its purity too; Vitality wraps vanilla's dispenser behavior, so honey from beehives still works. While thirst is off, bottles fill as in vanilla.
- **Brewing:** one `IBrewingRecipe` covers every brew: Purified and Cold Water give whatever vanilla (or another mod) brews from a water bottle with the same ingredient. Unclean and Salt Water don't brew.
- **Water bottle models:** the four bottles reuse vanilla's potion bottle and liquid textures, tinted per water, so the mod ships no copy of vanilla art.
- **Container NBT:** a container saves `impactsvitality:drinks`, `impactsvitality:purity` (`purified`, `unclean` or `salt`), `impactsvitality:contents` (`water`, `cold_water` or `soup`) and, for a soup, `impactsvitality:soup` (the soup's item ID). Emptying removes only these keys.
- **Filling containers:** using a container on water fills it to the top; at a water cauldron, each level is one drink. Bottles pour into a container in any crafting grid through IRV's `impactsvitality:container_filling` recipe (one drink per bottle, glass bottles back, never past the capacity). The spec doesn't say how Cold Water or soup gets into the Insulated Flask, so the same recipe does it: the flask keeps Cold Water cold and takes bowls of soup (bowls back); any other container turns Cold Water into plain Purified Water and refuses soup. A soup never mixes with water or another soup. Drinking soup from the flask feeds and hydrates you as the bowl would.
- **Pouring over yourself:** each sneak-use pours one drink.
- **Tidebound Canteen:** its refills are purified water, mixed with what's already inside like any other filling, so salt water stays salt until it's drunk.
- **Hydration Pack:** without a back slot it's worn in the chest slot (it's `Equipable` there); with one (IRI) it goes in the back slot through `#impactslib:back_slot_items`. The V key drinks one drink from the worn pack, with the same 32-tick wait as drinking from a held container.
- **Wooden Cup on a campfire:** two campfire recipes (unclean and salt) whose ingredient and result carry the cup's NBT. Their time comes from `containers.cupPurifyingTicks` through a small mixin on cooking recipes.
- **Lib addition:** `ResourceIds.item(id)` looks an item up by ID, empty for unknown IDs.
- **Water blocks:** a bucket is 4 drinks (`waterBlocks.bucketDrinks`) and a vanilla water bucket's water counts as unclean. Each block's tank holds 16 drinks per side (`waterBlocks.*Drinks`). The Rain Barrel shows water in its top and gives a comparator signal; bottles and containers fill from it. The Water Filter takes water from buckets, bottles and containers; a Charcoal Filter goes in by using it on the filter and comes out with a sneak-use and an empty hand; each filtered drink uses 10 of the filter's 600 life (`waterBlocks.waterFilterWearPerDrink`, a new number: 60 drinks per filter), and a worn-out filter crumbles. The Copper Still has four slots: water buckets and bottles, and glass bottles, go in from above or the sides; the emptied buckets and bottles and the bottles of purified water come out below. It works only while Lib's `isHeated` says the block under it is hot, shows `lit` while working, and gives a comparator signal.
- **Charcoal Filter switch:** the Charcoal Filter serves both the Respirator and the Water Filter, so its recipe stays on while either thirst or air is on (and `gear.charcoalFilter` is).
- **Health cap:** the mixin injects into `FoodData.tick` just after vanilla pays the tick's exhaustion (where it reads the naturalRegeneration rule) and runs regeneration and starvation itself. Starvation is unchanged. The cap applies while `healthCap.enabled` and thirst are both on; otherwise vanilla's regeneration runs. With `healthCap.capAllHealing`, Forge's heal event caps every heal at the same limit (the fuller-bar rule applies only to natural regeneration). The thresholds and shares are `healthCap.fullBarAtOrAbove` (18), `lowBarAtOrBelow` (6), `middleShare` (0.75) and `lowShare` (0.5).
- **Ambient temperature details:** the per-dimension rules are two lists, `temperature.dimensionOffsets` (`minecraft:the_nether=40`) and `temperature.dimensionFixed` (`minecraft:the_end=20`); per-biome overrides are `temperature.biomeOverrides`. Night and sun follow the sun smoothly: the night drop is full at midnight and the sun bonus full at noon (and none through rain). Underground, with no sky above, the surface temperature gives way to the cave's over `temperature.undergroundBlendBlocks` (20) blocks below Y 50; the cave's own temperature is 55°F down to Y 0 and rises to 75°F at Y −64 (`deepestY`). A block's warmth counts fully right next to you and falls off to 1/5 at the edge of the 5-block radius. Heat sources not in `blockTemps` give `unlistedHeatSource` (10°F) and cold sources `unlistedColdSource` (5°F).
- **Heat sources:** IRV adds furnaces, smokers, blast furnaces and lava cauldrons to Lib's `#impactslib:heat_sources` for its heat table. Everything that asks Lib's `isHeated` sees them, so with IRV installed a lit furnace under an IRF cauldron heats it too.
- **Wet:** you're Wet while in water, rain or a bubble column, and for `temperature.wetTicks` (600) after.
- **Body temperature details:** inside the comfortable range body temperature returns to 0 at `temperature.recoveryPerSecond` (2 points a second, a new number). The band edges are `temperature.warmAt` (25), `hotAt` (50) and `veryHotAt` (75), mirrored below zero; Freezing and Overheating are at ±100. Status effects are refreshed each second for 50 ticks, without particles. Mining is 15% slower at Cold and colder (`temperature.coldMiningSpeed`), and sprinting stops at Very Cold and colder, both on the client too so they match. Freezing deals vanilla's `freeze` damage and Overheating `impactsvitality:heatstroke`, every `temperature.damageIntervalSeconds` (3). IRV draws the frost overlay itself from vanilla's powder-snow texture, without touching vanilla's freezing, so vanilla's own freeze damage and slowness don't stack on top.
- **Heat and thirst:** the Hot and Very Hot multipliers apply to all thirst exhaustion (actions, the resting drain, Parched and the health cap's share).
- **Food temperatures:** `temperature.foodTemperatures` lists what a drink or food changes body temperature by (`impactsvitality:cold_water=-15`, `minecraft:melon_slice=-15`, `#impactsvitality:soups=15`). A container counts as what it holds, so Cold Water or soup from the Insulated Flask still cools or warms.
- **Gauge placement:** the gauge sits centered on Lib's hotbar bounds, just above the experience bar, and moves up above the experience level number while the player has levels.
- **Testing command:** `/impacts vitality show <player>` prints a player's thirst, Hydration, body and ambient temperature and Wet timer; `/impacts vitality set <players> thirst|hydration|body <value>` sets them. Lib gained `ImpactsCommands.addSubcommand` for this.
- **Insulation details:** the values live in `insulation.*` (warm armor 2 a piece and 3 for the chestplate, cooling armor 1, conductive armor −1 each way, linings 2/3/2, the Straw Hat 4 in the sun, Wet 6). The Straw Hat counts only under open sky in daytime without rain. It's a helmet of IRV's own straw armor material with no armor points, so it shows on the head.
- **Anvil linings:** offered only while Crafting isn't installed (with Crafting, linings are sewn with the Bone Needle) and `pieces.linings` and temperature are on. Any `ArmorItem` takes one; the armor is copied with all its NBT, and a name typed into the anvil applies too. The cost is `insulation.anvilLiningLevels` (1) and the material count `insulation.*LiningMaterials` (3 wool, 4 rabbit hide, 3 leather).
- **Thermometer readout:** the readout under the gauge is drawn at `display.readoutScale` (0.5 by default, client config) so it fits between the health and hunger rows.
- **Air details:** a dimension's air comes from data files `data/<namespace>/air_quality/<name>.json` holding `{"dimension": "...", "quality": "good|thin|very_thin|smoky|very_smoky"}`; IRV ships the Nether (smoky) and the End (thin), and the file name doesn't matter. The height bands apply to the dimensions in `air.heightBandDimensions` (the Overworld). When several things apply (the dimension, height, lava), the air that takes bubbles fastest wins. A mixin holds back vanilla's out-of-water refill (`LivingEntity.increaseAirSupply`) while the player is in bad air they aren't protected from; the client does the same from the synced air quality, so the bubble row doesn't flicker. Respiration slows bubble loss by `air.respirationSlowingPerLevel` (a third) per level, so Respiration III stops it. Creative players never lose air.
- **Cloud icon:** the cloud sits where an eleventh bubble would, at the left end of vanilla's bubble row (Lib gained `HudLayout.lastRightRowTop` to find that row).
- **Breathing gear details:** a Respirator or Rebreather takes its Charcoal Filter in any crafting grid through IRV's `impactsvitality:mask_filter` recipe (the old filter comes back); the filter is saved on the mask as `impactsvitality:filter` and loses 1 life per second of smoky air it filters (every other second in a Rebreather), crumbling when worn out. A tank's air is `impactsvitality:air` (ticks; a new tank is full); with a Respirator, Rebreather or Netherite Rebreather on, the first tank with air in the back slot or the inventory supplies thin air (20 ticks a second, 10 with a Rebreather) and every carried tank refills in good air, all once a second so the items aren't re-sent every tick. A Cloth Mask's dampness is `impactsvitality:damp` (ticks) and dries only while worn. The Air Bladder's uses are `impactsvitality:air_uses`. Bottled Air comes from using a glass bottle in good air when it isn't aimed at water. The masks are helmets of IRV's own armor materials (the Rebreather gives 2 armor, the Netherite Rebreather 3 with 3 toughness). The numbers are `breathing.*`.
- **Air Compressor:** use a tank on it to put it in, any furnace fuel to feed it (through Lib's new `LoaderPlatform.burnTicks`, so every mod's fuels count), and an empty hand to take the tank out. Fuel burns only while a tank is filling.
- **Filling from Food's pots:** Food's campfire bucket isn't a fluid or a vanilla cauldron, so Lib gained water vessels (`WaterVessels`): Food registers the bucket as one, and IRV fills a glass bottle or a container from any vessel (one bottle's worth, one of the bucket's boils), purified while Lib's `isBoiling` says it boils, otherwise unclean. Food still refuses bottles at a soup. The stone-boiling pot arrives with Crafting.
- **Food's tables:** `drinks.values` gains `#impactsfood:soups=6,2` (before the vanilla soups' 4,2) and `#impactsfood:boiled_foods=2,0`; `temperature.foodTemperatures` gains `#impactsfood:toasted_foods=5`. `#impactsvitality:soups` includes `#impactsfood:soups` as an optional entry, so Food's soups warm you and fit the Insulated Flask. The spec gives no Hydration for Food's soups; they give 2, like the vanilla ones.
- **Frame Pack:** `#impactsvitality:heavy_packs` holds `impactsinventory:frame_pack` as an optional entry; a pack from that tag in Lib's back slot multiplies thirst from exertion by `thirst.heavyPackMultiplier` (1.1).
- **Triggers:** `drank`, `temperature_state`, `air_lost` and `gear_used` are one kind of trigger: fired with named facts, and a criterion lists the facts it needs. `drank` carries `water`, `purity` and `parched`; `temperature_state` fires once a second with `state`, `cold_or_worse`, `hot_or_worse`, `on_fire`, `thirst_full` and `air_full`; `air_lost` fires per bubble with `cause` (`thin` or `fumes`) and `without_gear`; `gear_used` carries `gear` (`air_tank`, `filtered_mask`, `warm_linings`, `straw_hat_in_desert_noon`). Places (a mountain at Y 160, near bedrock at Y −58, Y 300, the Nether, the End) are vanilla player location predicates in the advancement files, so datapacks can change them. "Well Watered" and "In Balance" count with Lib counters up to `counters.wellWateredDrinks` (500) and `counters.inBalanceSeconds` (1200), then fire their own triggers. "Skin in the Game" and "Taking the Temperature" are granted when the item is in the inventory, so they work with Crafting's recipes too. A drink is anything drunk (the drinking animation), sips included; "Hat Trick" counts the hour around noon.
- **Sounds:** `impactsvitality:player.pant`, `player.shiver` and `player.cough` play vanilla sound events (breath, freeze hurt, drowning hurt) until a resource pack gives them their own, like Food's sounds. They're played on the client for the player alone, so the client config's `display.needSounds` can turn them off: panting every 5 seconds at 6 thirst or less, shivering every 4 seconds at Cold or colder, a cough each time smoke takes a bubble.
- **JEI:** Vitality's recipes show in vanilla's categories; the rest is explained on JEI info pages.
