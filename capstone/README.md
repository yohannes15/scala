# Capstone 1 — before Cats

This is the **first capstone** in this repo: finish it before **[Phase B — Cats → Cats Effect](../README.md)** (see main `README.md`).

**Everything for this capstone lives under the `capstone/` folder** (this file, samples, and Scala sources).

**Goal:** Build **habit**, **`Either`**, and **small programs** so Cats feels like a new library, not a second first language.

**Time box:** About **1–2 weeks** at an easy pace (adjust freely).

---

## Layout (`capstone/`)

| Path | Purpose |
| --- | --- |
| **`README.md`** | This checklist (steps 1–4 + boss project) |
| **`notes.md`** | Scratch notes for the capstone |
| **`src/main/scala/capstone/`** | Code — `capstone.mini` (**MiniCli** — done), `capstone.receipt` (**ReceiptApp** — boss: **in progress**, see § Boss project) |
| **`samples/`** | Example `.txt` files for the boss receipt parser |

---

## Why this exists

Cats assumes you’re comfortable with **generic types**, **`map` / `flatMap` intuition**, and **errors as values** (`Option`, `Either`). This capstone is the **playground** before **type classes** and **`IO`**.

---

## 1. Tiny CLI (Phase A capstone)

**Implemented:** [`MiniCli.scala`](src/main/scala/capstone/mini/MiniCli.scala) — fake **credit band**: **FICO-style score** + **income (USD)** → **`Approved`** or **`Declined`** with a **reason** string.

| Piece | What you built |
| --- | --- |
| **Input** | One line with **two** numbers, separated by **space or comma** (regex split `[,\\s]+`). **`@main`** takes an optional **single string** argument; if omitted / empty, **`stdin`** is read after a prompt. |
| **Model** | **`CreditInfo`** (`creditScore`, `income`) with a **private** constructor; only **`CreditInfo.apply`** can build values after validation. |
| **Errors** | **`Either[InvalidInput, CreditInfo]`** — not `String` on the left; **`InvalidInput`** carries the message. Parsing and validation short-circuit on first **`Left`**. |
| **Validation** | Score in **250–900**; income **≥ 0**. |
| **Decision** | Sealed-style **`Decision`**: **`Approved`** / **`Declined`**, each with **`name`** + **`reason`**. Rules use **`CREDIT_MIN_THRESHOLD`** (450) and **`INCOME_MIN_THRESHOLD`** (7500): decline if score or income is below the corresponding threshold; otherwise approve. |
| **UX** | **`Left`**: print error message. **`Right`**: echo score/income, then print decision **`name`**: **`reason`**. |

Checklist (this CLI):

- [x] Package **`capstone.mini`** — **`MiniCli.scala`** (not `Basics.scala` “hello”).
- [x] One **`@main`**: **stdin** (prompt) **or** one **string** argument (default `""` → interactive).
- [x] Parse into a **`case class`**; failures as **`Either`**; clear **`println`** for left/right.
- [x] Theme: **credit band** (score + income → approved / declined with reason).

---

## 2. Reps that feel like games

- [ ] **[Advent of Code](https://adventofcode.com/)** — any year, **days 1–3** only. Parse in Scala; use **`Either`** when input can be junk.
- [ ] **[Exercism — Scala](https://exercism.org/tracks/scala)** — **3–5** easy exercises; prefer **`Option`** / lists.

*(External sites — no subfolder required; add notes under `capstone/notes/` if you want, optional.)*

---

## 3. Brush-up (only if rusty)

- [ ] Skim **`Either`** in the [Scala 3 Book — functional error handling](https://docs.scala-lang.org/scala3/book/fp-functional-error-handling.html) (and `Option` there).
- [ ] **`for`-comprehension** chaining **two** `Either` steps (parse → validate).

---

## 4. Stop — you’re ready for Cats when…

- [ ] You can **explain** in one sentence why **`flatMap`** on `Either` short-circuits on first **Left**.
- [ ] You’ve **finished** one small CLI or AoC mini you’d show without apologizing.

---

## Then what?

Main roadmap: **[README.md](../README.md)** → **Phase B — Cats → Cats Effect**.

---

## If you get stuck

- Smaller scope: **half** a CLI (validation only, print `Either` with `println`).
- Skip Exercism if AoC is more fun — **one** track is enough.

---

## Boss project — Receipt line parser + tax summary

**Name:** **Receipt line parser + tax summary**

**Why it tests you:** Real parsing, **line-level errors**, **ADTs**, **`Either`**, **aggregation**, one **file + `@main`** — same *shape* as config/API validation at work. This repo uses **Scala Toolkit** (`os`, streaming) on the classpath — still **no Cats**.

### Implementation status — [`ReceiptApp.scala`](src/main/scala/capstone/receipt/ReceiptApp.scala)

| Done | Not done (per spec above) |
| --- | --- |
| **`@main`** with filename argument; reads lines from **`capstone/samples/<file>`** (filename only — not an arbitrary filesystem path). | README asked for **path as first argument**; you can later accept `java.nio.file.Paths.get(args(0))` or document “filename under `samples/`” as the contract. |
| **`enum TaxCode`**, **`Receipt`**, **`Either`** parsing: `\|`-separated fields, description length ≤ 40, non-negative **`BigDecimal`** (scale **2** after parse). | **Empty description** after trim should fail — currently **`parseDescription`** allows `""` if length ≤ 40. |
| Per-line success: prints line number prefix + **`processReceipt`**. Per-line failure: prints line number + message. **Intentional:** scan the **whole file** and report bad lines as you go — **no fail-fast** (you already know that pattern). | **Tax summary**: subtotals by code, **total tax**, **grand total** with fixed rates (**EXEMPT 0%**, **REDUCED 5%**, **STANDARD 10%**) — **not implemented yet**. |
| Uses **`os.read.lines.stream`** + **`Generator`** (lazy traversal). | Optional later: **non-zero exit** if any line failed (stretch). |
| Sample files: [`receipt-good.txt`](samples/receipt-good.txt), [`receipt-bad.txt`](samples/receipt-bad.txt). | Error lines don’t use the exact shape **`Line N: …`** everywhere (you prefix with **`print(s"$line_no: ")`** — close). |

### Input

- Path to a **text file** as the **first program argument** (or stdin — pick one and document in code).
- Each **non-empty** line looks like `description|taxCode|price` (rules in the table below).
- Empty lines: skip.

| Field | Rules |
| --- | --- |
| `description` | Non-empty after trim; max **40** characters. |
| `taxCode` | **`EXEMPT`**, **`STANDARD`**, or **`REDUCED`** (`enum` or sealed type). |
| `price` | Non-negative decimal (`BigDecimal` or `Double` — pick one). **Two decimal places** in input (e.g. `12.99` ok) — state your rule in a comment. |

### Output (success)

- Summary: line count, **subtotal by `taxCode`**, **total tax**, **grand total**, using e.g. **EXEMPT 0%**, **REDUCED 5%**, **STANDARD 10%** (define **one** tax rule in comments and stick to it).

### Output (failure)

- Invalid lines: report **line number** (1-based) and **reason** (e.g. `Line 4: expected '|' separator`). Do **not** silently skip bad lines.

**Error handling (this repo):** Keep going for **every** line — report each invalid line with its number and reason (same as the “report all bad lines” style). *Skipping* fail-fast is fine here; you can still use **`Either`** per line without stopping the whole file.

### Non-goals

- No HTTP, DB, Cats, or heavy CLI frameworks — `args(0)` is enough.

### Done when

`sbt "runMain capstone.receipt.ReceiptApp path/to/receipt.txt"` works on a **good** and a **bad** sample (see [`samples/`](samples/)).

### Sample output — success (**current** `ReceiptApp`)

With [`samples/receipt-good.txt`](samples/receipt-good.txt) (three valid lines), from the **repo root**:

```bash
sbt "runMain capstone.receipt.ReceiptApp receipt-good.txt"
```

Example console output (sbt noise omitted):

```text
==================== ReceiptApp =================
1: description: Coffee beans. taxcode: STANDARD. price: 4.50
2: description: Annual fee. taxcode: EXEMPT. price: 0.00
3: description: Snack. taxcode: REDUCED. price: 1.25
```

### Sample output — success (**target**, tax summary not implemented yet)

When **aggregation** matches the **Output (success)** bullet list above, you might print something like the following (rates fixed in code/comments: **EXEMPT 0%**, **STANDARD 10%**, **REDUCED 5%**; line prices from `receipt-good.txt`).

| | |
| --- | --- |
| Lines read | 3 |
| Subtotal **STANDARD** | 4.50 |
| Subtotal **EXEMPT** | 0.00 |
| Subtotal **REDUCED** | 1.25 |
| **Total tax** | 0.5125 (example: 4.50×10% + 0×0% + 1.25×5%) |
| **Grand total** (sum of prices + total tax) | **6.2625** |

Exact rounding policy (**`BigDecimal`** scale / `RoundingMode`) should appear in a **comment** next to your tax math — the table is the shape the README expects, not a golden file yet.

---

## Run

From the **repository root** (this repo uses a **single** sbt project; `capstone/` sources are on the compile path — see root `build.sbt`).

**MiniCli (credit band — implemented):**

```bash
# Interactive: prompt, then type e.g. 500 10000
sbt "runMain capstone.mini.MiniCli"

# One argument (non-interactive):
sbt 'runMain capstone.mini.MiniCli "500 10000"'
```

**Receipt boss (`ReceiptApp`) — in progress** (line parsing works; tax totals still to do). Pass **filename only**; file is resolved under **`capstone/samples/`**:

```bash
sbt "runMain capstone.receipt.ReceiptApp receipt-good.txt"
sbt "runMain capstone.receipt.ReceiptApp receipt-bad.txt"
```

*Keep it fun; Cats will still be there when this capstone is done.*
