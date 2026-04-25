package learning.datatypes

import cats.{Functor, Id}
import cats.syntax.all.*

/** At first glance `Const` seems like a strange data type — it has two type
  * parameters, yet only stores a value of the first. What possible use is it?
  * As it turns out, it does have uses, and they illustrate how type-level
  * structure lines up with everyday patterns.
  *
  * The `Const` data type is the data counterpart of the `const` function:
  *
  * `def const[A, B](a: A)(b: => B): A = a`
  *
  * `case class Const[A, B](getConst: A)`
  *
  * There are two type parameters, `A` and `B`, but only one piece of data: an
  * `A` in `getConst`. The `B` is not a field — it is part of the type only.
  * Part 3 (below) explains how that second name gets used when we recover `get`
  * from `modifyF`.
  *
  * Is `Const` only useful for lenses? ----------------------------------- No.
  * Phantom types and `Const` show up in many places (e.g. functor composition,
  * `traverse`/`Foldable` derivations, optics libraries, profunctors). This file
  * uses **lenses** as a concrete story: first `modifyF` unifies `modify` /
  * `Option` / `List`, then `Const` explains how to recover `get` from `modifyF`
  * alone.
  *
  * Why lenses? ----------- Types that contain other types are everywhere. In
  * OOP you might use getters and setters; in FP a common abstraction is a
  * **lens**: a small API for focusing on a part `A` of a whole `S` (`get` /
  * `set`, and derived updates).
  *
  * Two reasons lenses are popular:
  *   1. Lenses **compose** (focus on nested parts by composing smaller lenses).
  *   2. You can define **uniform** updates: pure `modify`, or effectful ones
  *      like `Option` / `List` / arbitrary `Functor F` via `modifyF`.
  */
object LearningConst:
  // const and Const: function and data type
  def const[A, B](a: A)(b: => B): A = a
  case class Const[A, B](getConst: A)

  object part1:
    /** Part 1 — `get` and `set` are primitive; everything else is derived.
      *
      * Part 2 — `modifyOption` and `modifyList` differ only in the outer type
      * constructor (`Option` vs `List`). Both have `map`, i.e. they are
      * **Functors**. So one general API is `modifyF` for any `F` with
      * `Functor`.
      */
    trait Lens[S, A]:
      def get(s: S): A
      def set(s: S, a: A): S
      def modify(s: S)(f: A => A): S =
        set(s, f(get(s)))
      // Effectful modifications: the update might fail (`Option`) or
      // branch to many values (`List`).
      def modifyOption(s: S)(f: A => Option[A]): Option[S] =
        f(get(s)).map(a => set(s, a))
      def modifyList(s: S)(f: A => List[A]): List[S] =
        f(get(s)).map(a => set(s, a))
      // `modifyOption` and `modifyList` share the same shape: only `map`
      // is needed — that is exactly `Functor`.
      def modifyF[F[_]: Functor](s: S)(f: A => F[A]): F[S] =
        f(get(s)).map(a => set(s, a))

  object part2:
    /** Take `modifyF` as the one abstract update; recover the rest.
      *
      *   - `modify` uses `Id`: no extra effect, so `F[A]` is just `A`.
      *   - `set` is `modify` with a function that ignores the old `A`.
      *   - `get` stays primitive: you cannot derive it from `set`/`modify`
      *     alone in a sound way.
      *
      * `modifyF` must stay abstract: if you defined it only from `set`, you
      * would circle back through methods that already depend on `modifyF`.
      */
    trait Lens[S, A]:
      def modifyF[F[_]: Functor](s: S)(f: A => F[A]): F[S]
      def modify(s: S)(f: A => A): S = modifyF[Id](s)(f)
      def set(s: S, a: A): S = modify(s)(_ => a)
      def get(s: S): A

  /** Part 3 — Can we define `get` from `modifyF`?
    *
    * **Why name `B` in `Const[A, B]`?** The only runtime field is the `A` in
    * `getConst`, so a `Const[A, B]` value is "really" just that `A`. If you
    * swap `B` for another type, you do not add or remove data — you only change
    * a static type. That is the usual sense of a *phantom* type parameter.
    *
    * **Why have two type parameters at all?** `modifyF` is polymorphic in a
    * type constructor `F[_]` (e.g. `List`, `Option`). Those functors use that
    * parameter to describe the inner type. `Const` is written with two
    * parameters so you can form `F[_] = Const[A, *]`: the stored value stays an
    * `A`, while the second parameter can still appear in types like
    * `Const[A, S]` when the surrounding `modifyF` must talk about the whole
    * structure `S` (see `F[S]` on the return side of the lens).
    *
    * **Trick for `get`:** choose `F` so that "mapping" does not change the
    * stored `A`. That is `Const[A, *]`: for any `X`, `Const[A, X]` stores an
    * `A`. The `Functor` must map `Const[A, X]` → `Const[A, Y]` without using
    * `f: X => Y` to change the stored `A` — so `map` keeps `getConst` as-is.
    * Then `modifyF[Const[A, *]]` puts the current focused `A` inside `Const`,
    * and `getConst` reads it back out.
    *
    * You need a `Functor` instance for `Const[X, *]` (first parameter `X`
    * fixed).
    */
  object part3:
    given constFunctor[X]: Functor[Const[X, *]] = new Functor[Const[X, *]]:
      // `Const[X, A]` only holds `X`; the `A => B` is unused at the value level.
      def map[A, B](fa: Const[X, A])(f: A => B): Const[X, B] =
        Const(fa.getConst)

    /** Here only `modifyF` is implemented; `set`, `modify`, and `get` are
      * derived. In particular, `get` uses `modifyF[Const[A, *]]` as above.
      */
    trait Lens[S, A]:
      def modifyF[F[_]: Functor](s: S)(f: A => F[A]): F[S]

      def set(s: S, a: A): S = modify(s)(_ => a)

      def modify(s: S)(f: A => A): S = modifyF[Id](s)(f)

      /* `Const(a)` stores the current focused `A`; `map` does not change it,
       * so `getConst` is that `A`. */
      def get(s: S): A =
        val storedValue = modifyF[Const[A, *]](s)(a => Const(a))
        storedValue.getConst

  // -------------------------------------------------------------------------
  // Examples: one small domain model and a lens `Person` -> `Int` (age).
  // -------------------------------------------------------------------------

  final case class Person(name: String, age: Int)

  /** Part 1: implement `get` and `set`; all other `Lens` methods use them. */
  val personAgePart1: part1.Lens[Person, Int] = new part1.Lens[Person, Int]:
    def get(p: Person): Int = p.age
    def set(p: Person, a: Int): Person = p.copy(age = a)

  /** Part 2: implement `modifyF` and `get`; `set` and `modify` are derived. */
  val personAgePart2: part2.Lens[Person, Int] = new part2.Lens[Person, Int]:
    def get(p: Person): Int = p.age
    def modifyF[F[_]: Functor](s: Person)(f: Int => F[Int]): F[Person] =
      f(s.age).map(newAge => s.copy(age = newAge))

  /** Part 3: implement only `modifyF`; `get` is derived via `Const`. */
  val personAgePart3: part3.Lens[Person, Int] = new part3.Lens[Person, Int]:
    def modifyF[F[_]: Functor](s: Person)(f: Int => F[Int]): F[Person] =
      f(s.age).map(newAge => s.copy(age = newAge))

  def constAndPhantomDemo(): Unit =
    import part3.given
    val ignored = const(42) {
      println("this line is not evaluated (by-name `b`)")
      0
    }
    println(s"const ignores the second arg: $ignored")
    val c: Const[String, Int] = Const("stored")
    val c2: Const[String, Boolean] = Functor[Const[String, *]].map(c)(_ => true)
    println(
      s"Const Functor map ignores the function for the inner type: ${c2.getConst}"
    )

  def part1Examples(): Unit =
    val p = Person("Alice", 30)
    println(s"part1 get: ${personAgePart1.get(p)}")
    println(s"part1 set: ${personAgePart1.set(p, 31)}")
    println(s"part1 modify (+1): ${personAgePart1.modify(p)(_ + 1)}")
    println(
      s"part1 modifyOption (bump if even): ${personAgePart1
          .modifyOption(p)(a => if a % 2 == 0 then Some(a + 1) else None)}"
    )
    println(
      s"part1 modifyOption (always None): ${personAgePart1
          .modifyOption(p)(_ => None)}"
    )
    println(
      s"part1 modifyList: ${personAgePart1.modifyList(p)(a => List(a, a + 1))}"
    )
    println(
      s"part1 modifyF (List): ${personAgePart1
          .modifyF[List](p)(a => List(a, a + 1))}"
    )

  def part2Examples(): Unit =
    val p = Person("Bob", 40)
    println(s"part2 get (still primitive): ${personAgePart2.get(p)}")
    println(s"part2 set (from modifyF + Id): ${personAgePart2.set(p, 41)}")
    println(s"part2 modify (*2): ${personAgePart2.modify(p)(_ * 2)}")
    println(
      s"part2 modifyF Option: ${personAgePart2
          .modifyF[Option](p)(a => Some(a + 1))}"
    )
    println(
      s"part2 modifyF List: ${personAgePart2
          .modifyF[List](p)(a => List(a, a + 1))}"
    )

  def part3Examples(): Unit =
    val p = Person("Carol", 25)
    println(
      s"part3 get (derived via Const + modifyF): ${personAgePart3.get(p)}"
    )
    println(s"part3 set: ${personAgePart3.set(p, 26)}")
    println(s"part3 modify: ${personAgePart3.modify(p)(_ + 10)}")
    // Same `modifyF` behavior as part 1 / 2 for non-Const functors
    println(
      s"part3 modifyF Option: ${personAgePart3
          .modifyF[Option](p)(a => if a < 30 then Some(a) else None)}"
    )
    // Agreement: part2 and part3 only differ in how `get` is defined; for the same
    // `modifyF` implementation, `get` should match.
    assert(personAgePart2.get(p) == personAgePart3.get(p))
    assert(personAgePart1.get(p) == personAgePart3.get(p))
    println("part3: get agrees with part1 and part2 for the same Person.")

@main def constExamples(): Unit =
  import LearningConst.*
  println("=== const + Const[_,_] Functor ===")
  constAndPhantomDemo()
  println()
  println("=== part1: get/set + modifyF family ===")
  part1Examples()
  println()
  println("=== part2: modifyF + get; set/modify derived ===")
  part2Examples()
  println()
  println("=== part3: only modifyF; get via Const ===")
  part3Examples()
