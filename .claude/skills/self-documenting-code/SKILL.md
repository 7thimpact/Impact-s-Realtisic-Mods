---
name: self-documenting-code
description: Write or refactor code so names and structure explain it without comments. Use when writing, reviewing, or cleaning up code for readability.
---

# Self-Documenting Code

Make the code explain *what* it does. Use comments only for *why*. Refactoring must never change behavior.

## Rules

1. **Names state intent.** Functions are verb phrases (`calculate_late_fee`). Booleans start with `is_`, `has_`, `can_`. No abbreviations or single letters, except tiny loop indices.
2. **No magic values.** Replace bare numbers and strings with named constants or enums.
3. **Name conditions.** Put complex expressions into an explaining variable or a small predicate function.
4. **One job per function.** Keep functions short (about 20 lines) and at one level of abstraction.
5. **Guard clauses.** Return early instead of nesting `if` blocks deeply.
6. **Make types explicit.** Use type hints, enums, and dataclasses/structs so invalid states are hard to express.
7. **Put units in names** when ambiguous: `timeout_seconds`, `price_cents`.
8. **Comments explain why only:** business rules, workarounds, non-obvious tradeoffs, links to specs or tickets. Delete comments that restate the code. Keep docstrings on public APIs.
9. **Stay in scope.** Don't rename public APIs, change behavior, or restyle unrelated code unless asked. Follow the language's existing naming conventions.

## Example

Before:

```python
def calc(d, r):
    # d = days overdue, r = daily rate
    if d > 0:
        return d * r
    return 0
```

After:

```python
LATE_FEE_PER_DAY = 0.50

def calculate_late_fee(days_overdue: int) -> float:
    is_overdue = days_overdue > 0
    if not is_overdue:
        return 0.0
    return days_overdue * LATE_FEE_PER_DAY
```

## Scale and correctness

1. **One source of truth.** Define each constant, enum, and schema once (e.g. `constants.py`) and import it everywhere. Never retype the same value.
2. **Reuse first.** Search the codebase for an existing helper before writing one. Match its naming and style.
3. **Verify references.** Every function, class, constant, and import you use must exist. If you can't confirm a name is defined, say so or ask. Never invent one.
4. **Large data.** Keep big tables, lookups, and fixtures in data files or config, not inline. Name columns and fields by meaning and unit (`order_total_cents`). Document schema and shape in one place.
5. **Dense constructs.** Name regexes, SQL, and formulas (`EMAIL_PATTERN`, `ACTIVE_USERS_QUERY`). For math, algorithms, concurrency, or performance tricks, add a short *why* comment or cite the source.
6. **Consistent terms.** One word per concept across files (not `user`/`customer`/`client` for the same thing).

## Before finishing

- Can each function be understood from its name and signature alone?
- Any remaining magic values or unexplained conditions?
- Any comment that says *what* instead of *why*?
- Is every referenced name defined or imported, and every constant defined once?
- Does the code behave exactly as before (same inputs, same outputs)?
