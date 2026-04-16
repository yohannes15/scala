# [Cats](https://typelevel.org/cats/) — tutorial layout

This tree holds **topic-based modules** for learning Cats in this repo.

- [Cats](https://typelevel.org/cats/) provides FP abstractions for Scala: **kernel** type classes, **core** syntax and instances, binary compatible and modular.
- Runs on **JVM**, **Scala Native**, and **Scala.js**.
- **Scala 2.12:** add `scalacOptions += "-Ypartial-unification"` (SI-2712). **Scala 2.13+** has partial unification on by default (this repo uses **Scala 3**).

## How to install

```scala
libraryDependencies += "org.typelevel" %% "cats-core" % "2.13.0"
```

## Installation options (overview)

| Module | Role |
| ------ | ---- |
| `cats-kernel` | Small set of basic type classes (pulled in by `cats-core`). |
| `cats-core` | Most type classes and syntax — **start here**. |
| `cats-laws` / `cats-testkit` | Law checking and tests for your instances. |
| `cats-free` | Free applicative/monad, etc. |
| `algebra` | Extra algebraic structures (often read **after** core Monoid/Semigroup comfort). |
| `alleycats-core` | Instances that are **not** lawful — use **later**. |
| Ecosystem (separate artifacts) | `cats-effect`, `cats-mtl`, `mouse`, `kittens`, `cats-tagless`, `cats-collections`, … |

## Progress & goal (Phase B — Cats core)

**Aim:** finish the **spine** (concepts + tiny `@main` examples here) **before** a larger service-style program; then move to **Cats Effect** per the [root `README.md`](../../../../../README.md).

| Status | Topic |
| ------ | ----- |
| Covered | [Type classes (concepts)](typeclasses/README.md), **Semigroup**, **Monoid**, **Functor**, **type constructors**, light **Nested** |
| Next | **Applicative** → **Monad** → **Foldable** / **Traverse**; data types **`Validated`** (+ **`Either`** patterns) |
| Later | Deep **Nested**, monad transformers, **Alleycats** / **algebra** extras |

**Suggested doc order:** [Cats home](https://typelevel.org/cats/) → [Type classes](https://typelevel.org/cats/typeclasses.html) → individual pages in **spine order** (not alphabetical). [Imports](https://typelevel.org/cats/imports.html) when needed.

## What’s in this directory

| Path | Purpose |
| ---- | ------- |
| [`typeclasses/`](typeclasses/README.md) | Runnable `*.scala` notes: Semigroup, Monoid, Functor, type constructors, … |
| [`datatypes/`](datatypes/Nested.scala) | Data-type experiments (e.g. Nested); grow as you add topics |

## Run examples (subproject `cats`)

Use the **fully qualified** `@main` name:

```bash
sbt "cats/runMain learning.typeclasses.semiGroupExample"
sbt "cats/runMain learning.typeclasses.monoidExample"
sbt "cats/runMain learning.typeclasses.catsTypeClassesExample"
sbt "cats/runMain learning.typeclasses.typeConstructorExample"
sbt "cats/runMain learning.typeclasses.FunctorsComposeExample"
```

List `@main` defs with search or Metals; the pattern is `learning.typeclasses.<Name>`.
