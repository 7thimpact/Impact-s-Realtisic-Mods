---
name: never-nesting
description: Coding convention for keeping functions flat and readable by limiting indentation depth. Use whenever writing, editing, reviewing, or refactoring code in any language, especially functions with nested if/else, loops, or try/catch blocks. Covers extraction (pull blocks into their own functions) and inversion (guard clauses and early returns).
---

# Never Nesting

Every inner block (`if`, `for`, `while`, `try`, callback) adds a level of indentation. The deeper the indentation, the more context the reader must hold in their head to understand a single line. Aim to keep functions flat, with the main logic at the lowest indentation level.

This skill is model-agnostic and applies to any language with blocks and branching.

## The core idea

- Nesting depth is a cost. Each extra level makes code harder to read, test, and change.
- Do not just reformat deeply nested code. Restructure it so the depth is not needed.
- Two techniques do most of the work: **inversion** and **extraction**.

Target: **no more than two levels of indentation inside a function body** (one is better). Treat three or more as a signal to refactor.

## Technique 1: Inversion (guard clauses and early returns)

Flip the condition and handle the unhappy path first. Return, throw, `continue`, or `break` immediately. The happy path then continues down the function at the same indentation level instead of being pushed inward.

```
// Bad: the happy path is buried three levels deep
function processOrder(order) {
  if (order) {
    if (order.isPaid) {
      if (order.items.length > 0) {
        shipItems(order.items);
        sendReceipt(order.customer);
      } else {
        throw new Error("Order has no items");
      }
    } else {
      throw new Error("Order is not paid");
    }
  } else {
    throw new Error("Order is missing");
  }
}

// Good: failures exit early, the happy path stays flat
function processOrder(order) {
  if (!order) throw new Error("Order is missing");
  if (!order.isPaid) throw new Error("Order is not paid");
  if (order.items.length === 0) throw new Error("Order has no items");

  shipItems(order.items);
  sendReceipt(order.customer);
}
```

Guidelines:
- Put all validation and precondition checks at the top of the function.
- Exit immediately on the unhappy path. Do not wrap the rest of the function in an `else`.
- Remove any `else` that follows a `return`, `throw`, `continue`, or `break`. It is redundant.
- Inside loops, use `continue` to skip uninteresting items instead of wrapping the loop body in an `if`.

```
// Bad
for (const user of users) {
  if (user.isActive) {
    if (user.email) {
      sendNewsletter(user.email);
    }
  }
}

// Good
for (const user of users) {
  if (!user.isActive) continue;
  if (!user.email) continue;

  sendNewsletter(user.email);
}
```

## Technique 2: Extraction

Pull a nested block out into its own well-named function. The outer function gets shorter and flatter, and the extracted piece can be understood and tested on its own.

```
// Bad: loop, condition, and inner work all nested together
function generateReport(departments) {
  const lines = [];
  for (const department of departments) {
    if (department.isActive) {
      for (const employee of department.employees) {
        if (employee.salaryDollars > 0) {
          lines.push(`${employee.name}: ${employee.salaryDollars}`);
        }
      }
    }
  }
  return lines;
}

// Good: each function does one thing at one level
function generateReport(departments) {
  return departments
    .filter(department => department.isActive)
    .flatMap(formatDepartmentLines);
}

function formatDepartmentLines(department) {
  return department.employees
    .filter(employee => employee.salaryDollars > 0)
    .map(employee => `${employee.name}: ${employee.salaryDollars}`);
}
```

Guidelines:
- Extract the body of a loop when it contains its own branching.
- Extract each `try` body or `catch` handler when it grows beyond a few lines.
- Name the extracted function for *what* it does, not *where* it came from.
- Extract when a block needs a comment to explain it. The function name replaces the comment.
- Do not extract into a vague grab-bag (`helpers`, `utils`). Give the function a logical home in the relevant class or module.

## Combine both techniques

Use inversion first to flatten the shape, then extraction to shorten what remains. Together they usually take a function from four or five levels down to one or two.

## Other tools for reducing depth

- Combine related conditions with `&&` / `||` only when the result is still easy to read. Do not trade nesting for an unreadable one-liner.
- Use language features that remove branching: `filter`, `map`, `flatMap`, pattern matching, optional chaining, null coalescing, default arguments.
- Replace long `if/else if` chains with a lookup table, `switch`, or polymorphism when the branches only differ by value or behavior.
- Handle errors where it is most natural. A single `try` around a small call is flatter than a `try` wrapping a whole function.

## When some nesting is fine

- One level of nesting for a simple loop or a single condition is normal.
- Do not create tiny functions or contorted conditions purely to hit a number. If flattening makes the code harder to follow, keep the clearer version.
- Structural nesting that mirrors the data (for example, building a nested JSON or HTML tree) is fine.

## Working in existing codebases

- **New code** follows these rules.
- **Edited code**: apply them to the functions you touch. Do not refactor unrelated code just to flatten it.
- **Behavior must not change.** Inversion and extraction are refactors. Preserve the exact conditions, ordering, side effects, and error behavior. Run the existing tests if there are any.
- If a project style guide conflicts with this one, follow the project guide and mention the conflict.

## Before you finish: checklist

- [ ] Validation and error cases are handled first, with early returns or `continue`.
- [ ] No `else` after a `return`, `throw`, `continue`, or `break`.
- [ ] The happy path reads straight down at the lowest indentation level.
- [ ] No function body goes deeper than two levels of indentation (or there is a clear reason it does).
- [ ] Nested blocks with their own logic have been extracted into named functions.
- [ ] Extracted functions have specific names and live in a logical home, not a `Utils` module.
- [ ] Behavior is unchanged by the refactor.
