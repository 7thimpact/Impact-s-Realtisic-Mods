---
name: clean-naming-coding
description: General coding conventions for readable, maintainable code. Use whenever writing, editing, reviewing, or refactoring code in any language. Covers naming (no single-letter variables, no abbreviations, no types in names, units in names) and code organization (no Utils/Helper grab-bags).
---

# Clean Naming and Organization for Code

Apply these rules to all code you write or modify, in any language. They are written to be model-agnostic: any LLM or human should be able to follow them without special tooling.

The goal: code is read and edited far more often than it is written. Names should carry meaning so the reader never has to guess.

## 1. Avoid single-letter variables

Single-letter names are leftovers from mathematical shorthand. They carry no context and are hard to search for and edit.

```
// Bad
for (let i = 0; i < n; i++) { total += p[i].c * q; }

// Good
for (let productIndex = 0; productIndex < productCount; productIndex++) {
  totalCost += products[productIndex].unitCost * quantity;
}
```

- Name loop variables for what they iterate: `rowIndex`, `customer`, `line`.
- Lambdas and comprehensions get real names too: `orders.filter(order => order.isPaid)`, not `o => o.p`.
- Only exception: a language-mandated placeholder such as `_` for an intentionally unused value.

## 2. Never abbreviate

Modern IDEs autocomplete and screens are large. Abbreviations save keystrokes nobody needs to save, and they hide intent.

| Bad | Good |
|---|---|
| `btn`, `cfg`, `msg` | `button`, `configuration`, `message` |
| `idx`, `cnt`, `tmp` | `index`, `count`, `temporaryFile` (or a more specific name) |
| `usr`, `addr`, `req`, `res` | `user`, `address`, `request`, `response` |
| `calcAvgPrc` | `calculateAveragePrice` |

- Spell out every word. Choose a longer, clearer name over a shorter, cryptic one.
- Established acronyms that are themselves the proper term (`HTTP`, `URL`, `JSON`, `SQL`, `UUID`) are fine. Shortened *words* are not.
- Treat acronyms as words in casing where the language convention calls for it (`HttpClient`, `parseJsonResponse`).

## 3. Don't include types in names

Do not encode the type in a variable name (Hungarian notation). Statically typed languages, type hints, and IDEs already show the type. In dynamically typed code, a good name still describes meaning, not storage.

```
// Bad
const nameString = "Ada";
const userList = getUsers();
let isActiveBool = true;
const strFirstName = "Ada";

// Good
const name = "Ada";
const users = getUsers();
let isActive = true;
const firstName = "Ada";
```

A plural noun (`users`) already says "collection." Do not add `List`, `Array`, `Map`, or `Dict` unless the data structure itself is the meaningful distinction (for example, `usersById` is good because it explains the key).

## 4. Put units in variable names

When a value has a unit, include it. This removes ambiguity and prevents unit-conversion bugs.

```
// Bad
const delay = 30;
const timeout = 5000;
const size = 2048;

// Good
const delaySeconds = 30;
const timeoutMilliseconds = 5000;
const fileSizeBytes = 2048;
```

Applies to time (`Seconds`, `Milliseconds`, `Minutes`), size (`Bytes`, `Kilobytes`), distance (`Meters`, `Pixels`), money (`Dollars`, `Cents`), angles (`Degrees`, `Radians`), rates (`RequestsPerSecond`), and percentages (`Percent` vs. a `0.0–1.0` `Fraction`).

If the language has a dedicated duration or quantity type (for example `Duration`, `TimeSpan`), prefer that over a raw number, and then the unit is not needed in the name.

## 5. Don't add types to type names

Do not mark a class or interface with its kind. No `I` prefix for interfaces, no `Base` or `Abstract` prefixes or suffixes, no `Impl` suffix. Instead, name the thing for what it is or does.

```
// Bad
interface IUserRepository {}
class BaseHandler {}
abstract class AbstractShape {}
class UserRepositoryImpl implements IUserRepository {}

// Good
interface UserRepository {}
class RequestHandler {}
class Shape {}
class PostgresUserRepository implements UserRepository {}
```

- Name the abstraction cleanly (`UserRepository`), and give implementations descriptive names that say how they differ (`PostgresUserRepository`, `InMemoryUserRepository`).
- If you reach for `Base`, ask what the class actually represents and name that.
- If a language convention strongly requires a marker (for example, a framework that mandates a suffix), follow the framework and note why in a comment.

## 6. Avoid `Utils` / `Helper` classes

A `Utils`, `Helpers`, `Common`, `Misc`, or `Tools` module is a dumping ground for unrelated functions. It hides where behavior belongs and grows without bound.

```
// Bad
class StringUtils { static capitalize(...) {} static parseDate(...) {} static slugify(...) {} }

// Good
// Behavior lives with the thing it operates on.
class Slug { static fromTitle(title) {} }
class DateParser { parse(text) {} }
class Name { capitalized() {} }
```

- Put each function in the class or module that owns the data or concept it works on.
- If no home exists, that is a signal a concept is missing. Create a small, specifically named module (`Slug`, `RetryPolicy`, `PriceFormatter`) instead of a generic one.
- Small, cohesive modules named for one responsibility are fine. A module named for *vagueness* is not.

## Working in existing codebases

- **New code** follows all rules above.
- **Edited code**: apply the rules to the lines you touch. Do not rename unrelated code or rewrite whole files just to satisfy naming.
- **Public APIs, database columns, wire formats, and third-party interfaces**: do not rename them silently. If they violate these rules, leave them and mention it, or propose a migration separately.
- **Local convention conflicts**: if the project has a documented style guide that contradicts a rule here, follow the project guide and mention the conflict.

## Before you finish: checklist

- [ ] No single-letter variables (except a language-mandated `_`).
- [ ] No abbreviated words anywhere (variables, functions, classes, files).
- [ ] No type names baked into variable names.
- [ ] Every number with a unit has the unit in its name (or uses a dedicated unit type).
- [ ] No `I`, `Base`, `Abstract`, or `Impl` decorations on type names.
- [ ] No new `Utils`, `Helpers`, or `Common` modules; every function lives with its concept.
- [ ] Names read clearly to someone seeing the code for the first time.
