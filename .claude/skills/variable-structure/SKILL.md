---
name: variable-structure
description: Organize variables as nested objects with dotted paths like player.customization.accessories. Use when designing data models, game state, config, or any code with grouped variables.
---

# Variable Structure

Group related data into nested objects so every value has one predictable path: `entity.group.field`. Language-agnostic.

## Rules

1. **Path = entity.group.field.** Level 1 is the entity (`player`), level 2 is a category (`customization`), level 3 is the value (`accessories`). Result: `player.customization.accessories`.
2. **Max 3 levels.** If you need a 4th, that value becomes its own named object or variable with its own short path (e.g. `accessory.color`, not `player.customization.accessories.hat.color`).
3. **Real nested objects.** Each level is an object, struct, or class reached with dot access. Don't use string keys or flat names like `player_customization_accessories`.
4. **Group by meaning.** Siblings must share a category. `player.customization.health` is wrong; `player.stats.health` is right.
5. **Naming by role.**
   - Entities and groups are nouns.
   - Singular for one thing (`player`), plural for a collection (`accessories`).
   - Booleans start with `is_`, `has_`, `can_`.
   - Include units when ambiguous (`cooldown_seconds`).
6. **Collections.** A plural leaf holds items of one type. For lookup by key, name it `<things>_by_<key>` (`accessories_by_id`).
7. **Casing follows the language** (snake_case, camelCase, PascalCase) and stays consistent across the whole project.
8. **Define once.** Declare each structure in one place and reference it by path everywhere else. Never duplicate a field in two groups.
9. **Verify every path.** A path you use must exist in the definition. If unsure, check the definition or ask; never invent a path. To rename, change the definition and update every reference.
10. **Set defaults.** Every field gets an explicit default (empty collection, `0`, `false`, named constant) so no path is accidentally null or undefined.
11. **Aliases are named by meaning.** A local shortcut is fine (`accessories = player.customization.accessories`), but never use cryptic names like `a` or `tmp`.

## Example

```
player
├── identity
│   ├── name
│   └── id
├── customization
│   ├── accessories    (list of Accessory)
│   ├── skin_tone
│   └── hair_style
└── stats
    ├── health
    └── level
```

Reference: `player.customization.accessories`

Too deep: `player.customization.accessories.hat.color`
Fix: give `Accessory` its own definition and use `accessory.color`.

## Before finishing

- Is every path 3 levels or fewer?
- Does each value live in the group that matches its meaning?
- Is every referenced path defined, and every field defined once?
- Does every field have a default?
- Do names follow the language's casing, plural/singular, and boolean rules?
