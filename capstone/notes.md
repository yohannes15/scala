# Capstone notes — Capstone 1 (complete)

Scratch notes while building **MiniCli** (credit band) and **ReceiptApp** (receipt lines + tax **`Summary`**): **`Either`**, regex `split`, empty-line edge case, **`foldLeft`** over a stream, **`Either.left.map`** for line-number prefixes. See **`capstone/README.md`** for the checklist and run commands.

## Input parsing

### Regex `[,\\s]+` (Scala string passed to `split`)

- Splits on **one or more** commas and/or **whitespace** (space, tab, …).
- Examples that yield **two tokens**: `"720 50000"`, `"720,50000"`, `"720 , 50000"`.

### Pieces

| Piece | Meaning |
| --- | --- |
| `[` … `]` | One character from the set inside. |
| `,` | Literal comma. |
| `\\s` in a Scala string | Becomes `\s` in the regex → whitespace. |
| `+` | One or more of the preceding group (here, the character class). |
| `\\` in the string | Escaping so the compiler passes `\` through to the regex engine. |

## Empty lines

- After `trim`, an empty line is `""`.
- `"".split("[,\\s]+")` yields **`Array("")`**: length **1**, not 0 — one **empty** token. That is why a generic “wrong number of fields” message felt wrong for “user typed nothing.”
- Handle **`line.isEmpty`** before `split` if you want a clear “no input” error.

## Smart constructor (FP-friendly, not exercise answers)

- Keep **`CreditInfo`** as **plain data** (fields); put **range / domain** checks in the **companion object** as something like **`from(...): Either[InvalidInput, CreditInfo]`** so invalid combos never need to exist as bare `CreditInfo` values.
- Optional: **`private`** constructor on the `case class` so only the companion can call the real constructor after checks (see Scala 3 Book / docs on **private case class constructors**).
- **You** wire `for` / `flatMap` to call **`CreditInfo.from(cs, in)`** instead of **`CreditInfo(cs, in)`** once parsing has produced raw numbers.

## `private case class` vs `case class … private (…)`

| | `private case class A(…)` | `case class B private (…)` |
| --- | --- | --- |
| **Hides** | The **type** `A` outside the enclosing scope | **Construction** (`apply` / ctor) from outside the allowed region |
| **Typical use** | Whole type is an implementation detail | Public type, creation only via companion / factory |

```scala
object Outer {
  private case class Secret(n: Int) // name Secret not visible outside Outer
}

case class Credit private (cs: Int, income: Int) // Credit is public; Credit(1, 2) may be illegal outside companion
object Credit { def trusted(cs: Int, income: Int): Credit = new Credit(cs, income) }
```

Syntax: **`private case class`** (class hidden) vs **`case class Name private (...)`** — `private` **after** the name, before `(`.

---

## `foldLeft` (reduce a stream to one summary)

**Idea:** You have **many** items (e.g. lines from a file) and want **one** result (counts, subtotals, tax, …). **`foldLeft`** walks the collection **in order** from the **left**, carrying a **running accumulator** and updating it for each element.

### Shape (Scala)

```scala
collection.foldLeft(initialAccumulator) { (acc, element) =>
  // compute nextAccumulator from acc and element
  ???
}
```

- **`initialAccumulator`:** your “empty summary” before any line (e.g. zeros, empty subtotals).
- **`(acc, element) => ...`:** one step — given **summary so far** and **next line**, return the **new** summary.
- **Result:** final accumulator after the last element — **no need to store all lines** in memory (streaming-friendly if the collection is lazy / iterator-based).

### Compare

| Method | Result |
| --- | --- |
| **`map`** | Same *count* of elements, each transformed. |
| **`foreach`** | **`Unit`** — only side effects; no returned summary. |
| **`foldLeft`** | **One** value — the folded summary. |

### Tiny example (sum)

```scala
List(1, 2, 3).foldLeft(0)((acc, n) => acc + n) // 6
// acc=0, n=1 -> 1; acc=1, n=2 -> 3; acc=3, n=3 -> 6
```

### Receipt / streaming angle

- **Accumulator** can be a small **`case class`** (valid line count, invalid count, subtotals per `TaxCode`, running tax, …) — **fixed size**, not a `List` of every line.
- Each step: **`parseLine(...)` → `Either`** → on **`Right(receipt)`** add price/tax into the right buckets; on **`Left`** bump invalid count (and optionally print).
- Same logic can be written with a **`var`** and **`foreach`** (still one pass); **`foldLeft`** avoids mutating the summary in **your** code by returning a **new** accumulator each step (immutable style).

### Name

**“Left”** = fold **from the left** (first element first). For commutative sums the direction rarely matters; for ordered or non-commutative combines, use **`foldLeft`** unless you know you need **`foldRight`**.

---

## `Either`: `map` vs `.left.map` (and “left projection”)

**Right-biased `Either`:** `either.map(f)` applies **`f`** only to **`Right`**; a **`Left`** is left unchanged ( **`f`** is not run).

**Left side:** `either.left.map(f)` applies **`f`** only to **`Left`**; a **`Right`** is unchanged. That is the usual way to tweak **error values** (e.g. prefix `"Line 5: "` to a message) without touching the success branch.

**“Left projection”** is the *idea*: “view this `Either` so **`map`** affects **`Left`** instead of **`Right`.”** In older Scala tutorials the API was often spelled **`leftProjection`**; in current Scala you use **`either.left.map(...)`** — same concept, modern name.

| Goal | Typical call |
| --- | --- |
| Transform **`Right`** | `either.map(...)` |
| Transform **`Left`** | `either.left.map(...)` |
| Swap sides, then use normal **`map`** | `either.swap.map(...).swap` |

Tiny example:

```scala
val e: Either[String, Int] = Left("oops")
e.map(s => s + "!")           // Left("oops") — still the same Left
e.left.map(s => s"err: $s")   // Left("err: oops")
```
