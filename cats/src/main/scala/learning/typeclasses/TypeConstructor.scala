package learning.typeclasses

/** ******************************************************************************
  * Type constructors ----------------- Values vs types vs "things that take
  * types"
  *   - A value has a type: 42 has type `Int`
  *   - *Proper types* are complete by themselves: `Int`, `String`, `Boolean`.
  *   - A type constructor is not a full type until you give it another type, it
  *     has a "hole" to fill.
  *
  * A *proper type* (or *fully applied type*) is something a value can have:
  * `Int`, `String`, `Option[Int]`, `List[Boolean]`.
  *
  * A *type constructor* is a type-level "template" with a hole: you must supply
  * type arguments to get a proper type. `Option` and `List` are type
  * constructors: `Option` by itself is not the type of any runtime value — you
  * write `Option[Int]`, `Option[String]`, etc.
  *
  *   - `Int`, `String` — proper types (no hole).
  *   - `Option`, `List` — unary type constructors (one hole): `* -> *` in docs.
  *   - `Either` — binary type constructor (two holes): `* -> * -> *`.
  *
  * `F[_]` in Cats means: "`F` is a unary type constructor" (one hole). Then
  * `F[A]` and `F[B]` are proper types.
  *
  * `F` vs `f` in `Functor` (do not conflate these):
  *   - `F` names the *type constructor* (`Option`, `List`, …): it describes the
  *     shape of a type, not a runtime function from `A` to `B`. So `F` is *not*
  *     a value-level `A => B`.
  *   - `Functor`'s `map` has the form
  *     `def map[A, B](fa: F[A])(f: A => B): F[B]`. Here `f: A => B` is* an
  *     ordinary Scala function: you pass it to `map` to turn each inner `A`
  *     into a `B` while staying inside `F`. That is the function you *map*
  *     with.
  *
  * In short: `F` types the wrapper; `f` is what you run on the contents when
  * you map.
  *
  * Kinds (optional vocabulary): `Int` has kind `*`; `Option` has kind `* -> *`.
  * Saying "F has kind `* -> *`" is the same as "`F` is a unary type
  * constructor."
  * ******************************************************************************
  */

@main def typeConstructorExample() =
  // Proper types: these are complete types you ascribe to values.
  val n: Int = 42
  val s: String = "hi"
  val maybe: Option[Int] = Some(7)
  val nums: List[Int] = List(1, 2, 3)
  println(s"Values with proper types: n=$n, s=$s, maybe=$maybe, nums=$nums")

  // Unary type constructor: `Option` — same constructor, different fillings.
  val a: Option[Int] = Some(1)
  val b: Option[String] = None
  println(s"Option[Int] and Option[String]: a=$a, b=$b")

  // Binary type constructor: must supply both type args for a proper type, e.g. Either[E, A].
  val ok: Either[String, Int] = Right(10)
  val err: Either[String, Int] = Left("oops")
  println(s"Either[String, Int]: ok=$ok, err=$err")

  // Type alias: name for a partially applied `Either` (left fixed, right varies).
  type StringOr[A] = Either[String, A]
  val viaAlias: StringOr[Boolean] = Right(true)
  println(s"StringOr[Boolean] (alias for Either[String, Boolean]): $viaAlias")

  // Nested: outer and inner constructors composed — not what `F[_]` alone denotes;
  // here you have `List` of `Option` of `Int`.
  val layered: List[Option[Int]] = List(Some(1), None, Some(2))
  println(s"List[Option[Int]]: $layered")

  // Higher-kinded example: accept any unary constructor `F` and any inner type `A`.
  def identityInF[F[_], A](fa: F[A]): F[A] = fa
  println(s"identityInF(Some(3)): ${identityInF(Some(3))}")
  println(s"identityInF(List(1,2)): ${identityInF(List(1, 2))}")
