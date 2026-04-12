# Capstone 1 — before Cats

**Status: complete** — [`MiniCli`](src/main/scala/capstone/mini/MiniCli.scala) (§1) and [`ReceiptApp`](src/main/scala/capstone/receipt/ReceiptApp.scala) (boss) are implemented. Optional items below (e.g. AoC reps) remain optional.

This is the **first capstone** in this repo: do it before **[Phase B — Cats → Cats Effect](../README.md)** (see main `README.md`).

**Everything for this capstone lives under the `capstone/` folder** (this file, samples, and Scala sources).

**Goal:** Build **habit**, **Either**, and **small programs** so Cats feels like a new library, not a second first language.

**Time box:** About **1–2 weeks** at an easy pace (adjust freely).

---

## Layout (`capstone/`)


| Path | Purpose |
| --- | --- |
| **`README.md`** | This checklist (steps 1–4 + boss project) |
| **`notes.md`** | Scratch notes (parsing, `Either`, `foldLeft`, …) |
| **`src/main/scala/capstone/`** | **`capstone.mini`** — **MiniCli** · **`capstone.receipt`** — **ReceiptApp** (both **done**) |
| **`samples/`** | Example `.txt` files for **ReceiptApp** (`receipt-good.txt`, `receipt-bad.txt`) |


---

## Why this exists

Cats assumes you’re comfortable with **generic types**, **map / flatMap** intuition, and **errors as values** (`Option`, `Either`). This capstone is the **playground** before **type classes** and **IO**.

---

## 1. Tiny CLI (Phase A capstone)

**Implemented:** [`MiniCli.scala`](src/main/scala/capstone/mini/MiniCli.scala) — fake **credit band**: **FICO-style score** + **income (USD)** → **Approved** or **Declined** with a **reason** string.

| Piece | What you built |
| --- | --- |
| **Input** | One line with **two** numbers, separated by **space or comma** (regex split `[,\\s]+`). **`@main`** takes an optional **single string** argument; if omitted / empty, **stdin** is read after a prompt. |
| **Model** | **`CreditInfo`** (`creditScore`, `income`) with a **private** constructor; only **`CreditInfo.apply`** can build values after validation. |
| **Errors** | **`Either[InvalidInput, CreditInfo]`** — not `String` on the left; **`InvalidInput`** carries the message. Parsing short-circuits on first **`Left`**. |
| **Validation** | Score in **250–900**; income **≥ 0**. |
| **Decision** | Sealed-style **`Decision`**: **Approved** / **Declined**, each with **name** + **reason**. Thresholds **450** (credit) and **7500** (income). |
| **UX** | **Left:** print error. **Right:** echo fields, then print decision **name** and **reason**. |

Checklist (this CLI) — **done:**

- [x] Package **`capstone.mini`** — **`MiniCli.scala`** (not `Basics.scala` “hello”).
- [x] One **`@main`**: **stdin** (prompt) **or** one **string** argument (default `""` → interactive).
- [x] Parse into a **`case class`**; failures as **`Either`**; clear **`println`** for left/right.
- [x] Theme: **credit band** (score + income → approved / declined with reason).

---

## 2. Reps that feel like games

- [ ] **[Advent of Code](https://adventofcode.com/)** — any year, **days 1–3** (or similar). Parse in Scala; use **`Either`** when input can be junk. Each puzzle includes **example I/O** in the text — you can practice **without** creating an account; a login is only for personalized input / leaderboard.

*(No subfolder required; add notes under `capstone/notes/` if you want, optional.)*

---

## 3. Brush-up (only if rusty)

- [ ] Skim **`Either`** in the [Scala 3 Book — functional error handling](https://docs.scala-lang.org/scala3/book/fp-functional-error-handling.html) (and **`Option`** there).
- [ ] **`for`-comprehension** chaining **two** **`Either`** steps (parse → validate).

---

## 4. Stop — you’re ready for Cats when…

- [x] You can **explain** in one sentence why **`flatMap`** on **`Either`** short-circuits on first **Left**.
- [x] You’ve **finished** this capstone’s **MiniCli** + **ReceiptApp** (and optional reps below if you want more practice).

---

## Then what?

Main roadmap: **[README.md](../README.md)** → **Phase B — Cats → Cats Effect**.

---

## If you get stuck

- Smaller scope: **half** a CLI (validation only, print `Either` with `println`).
- Prefer **this repo** (`learning/`, capstone) first; add **AoC** when you want puzzle variety.

---

## Boss project — Receipt line parser + tax summary

**Name:** **Receipt line parser + tax summary**

**Why it tests you:** Real parsing, **line-level errors**, **ADTs**, **`Either`**, **aggregation**, one **file + `@main`** — same *shape* as config/API validation at work. This repo uses **Scala Toolkit** (`os`, streaming) on the classpath — still **no Cats**.

### Implementation — [`ReceiptApp.scala`](src/main/scala/capstone/receipt/ReceiptApp.scala) — **done**

| Feature | Notes |
| --- | --- |
| **Input** | Filename under **`capstone/samples/`**; lines streamed with **`os.read.lines`** + **`Generator`**. |
| **Parsing** | Three fields per line separated by **`|`**; **`enum TaxCode`** with **`BigDecimal`** rates (`"0.1"`, `"0.05"`, `"0.00"`); **`Either`** per field; **`parsePrice`** uses **`Try(BigDecimal(...)).setScale(2, HALF_UP)`**. |
| **Line errors** | **Line numbers** via **`parsings.left.map(...)`** so helpers stay simple. Full file scan: invalid lines **`println`**’d, summary still built. |
| **Summary** | **`foldLeft`** into **`Summary`**: valid/invalid counts, **subtotals per `TaxCode`**, **`totalTax`**, **`grandTotal`** (scaled **2** places). **`Summary.toString`** prepends a banner; no I/O inside **`toString`**. |

**Price scale:** After parsing the price string to **`BigDecimal`**, values are normalized to **two decimal places** (`parsePrice`).

**Boss checklist — done:** [x] parse + validate + **`TaxCode`** · [x] **`Summary`** (counts, subtotals, tax, grand total) · [x] line numbers on errors · [x] full-file scan (no fail-fast) · [x] samples **`receipt-good.txt`** / **`receipt-bad.txt`**.

### Input

- **One required argument:** the **filename** of a file under **`capstone/samples/`** (e.g. `receipt-good.txt`). This is **not** a general filesystem path: the program resolves **`os.pwd / "capstone" / "samples" / <argument>`** so sample data stays **in the repo** and the CLI stays simple. **`stdin` is not used** for `ReceiptApp` (interactive input is for **MiniCli** only).
- Each **non-empty** line looks like `description|taxCode|price` (rules in the table below).
- Empty lines: skip.


| Field         | Rules                                                                                                                                         |
| ------------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| `description` | Non-empty after trim; max **40** characters.                                                                                                  |
| `taxCode` | **`EXEMPT`**, **`STANDARD`**, or **`REDUCED`** (`enum` or sealed type). |
| `price`       | Non-negative decimal (`BigDecimal` or `Double` — pick one). **Two decimal places** in input (e.g. `12.99` ok) — state your rule in a comment. |


### Output (success)

- Summary: line count, **subtotal by `taxCode`**, **total tax**, **grand total**, using e.g. **EXEMPT 0%**, **REDUCED 5%**, **STANDARD 10%** (define **one** tax rule in comments and stick to it).

### Output (failure)

- Invalid lines: report **line number** (1-based) and **reason** (e.g. `Line 4: expected '|' separator`). Do **not** silently skip bad lines.

**Error handling (this repo):** Keep going for **every** line — report each invalid line with its number and reason (same as the “report all bad lines” style). *Skipping* fail-fast is fine here; you can still use **`Either`** per line without stopping the whole file.

### Non-goals

- No HTTP, DB, Cats, or heavy CLI frameworks — `args(0)` is enough.

### Done when

From the **repo root**, `sbt "capstone/runMain capstone.receipt.ReceiptApp receipt-good.txt"` and the same with **`receipt-bad.txt`** work (files live in [`samples/`](samples/); pass **basename only**).

### Sample output — [`receipt-good.txt`](samples/receipt-good.txt)

From the **repo root**:

```bash
sbt "capstone/runMain capstone.receipt.ReceiptApp receipt-good.txt"
```

Example (sbt `[info]` lines omitted):

```text
================== ReceiptApp =================
==================== Summary ==================
Valid lines                ->  3
Invalid lines              ->  0
Subtotal EXEMPT            ->  0.00
Subtotal STANDARD          ->  4.50
Subtotal REDUCED           ->  1.25
Total tax                  ->  0.51
Grand total                ->  6.26
```

Rates in code: **EXEMPT 0%**, **STANDARD 10%**, **REDUCED 5%**. **Total tax** and **grand total** use **`RoundingMode.HALF_UP`** at scale **2** (so totals may differ slightly from unrounded intermediate sums).

### Sample output — [`receipt-bad.txt`](samples/receipt-bad.txt)

Expect **error lines** printed as they are parsed, then a **Summary** with **`Invalid lines > 0`**. Run:

```bash
sbt "capstone/runMain capstone.receipt.ReceiptApp receipt-bad.txt"
```

---

## Run

From the **repository root** (capstone is its **own** sbt subproject — not aggregated into `scala-tutorial`; use `sbt capstone/compile` to build it alone — see root `build.sbt`).

**MiniCli (credit band — implemented):**

```bash
# Interactive: prompt, then type e.g. 500 10000
sbt "capstone/runMain capstone.mini.MiniCli"

# One argument (non-interactive):
sbt 'capstone/runMain capstone.mini.MiniCli "500 10000"'
```

**Receipt boss (`ReceiptApp`) — implemented.** Pass **filename only** (resolved under **`capstone/samples/`**):

```bash
sbt "capstone/runMain capstone.receipt.ReceiptApp receipt-good.txt"
sbt "capstone/runMain capstone.receipt.ReceiptApp receipt-bad.txt"
```

*Keep it fun; Cats will still be there when you open Phase B.*
