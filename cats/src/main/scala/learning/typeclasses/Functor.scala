package learning.typeclasses

object LearningFunctor:
  /** Functor --------------------
    *   - A type class that abstracts over type constructors that can be map'ed
    *     over
    *   - A `Functor` is anything you can map over while staying "inside" the
    *     same wrapper: turn F[A] into F[B] using A => B, without unpacking the
    *     whole structure by hand.
    *
    * E.g: List, Option, Either[E, *], Future, etc are all functors b/c they
    * have a sensible map
    *   - Functor[F] means: “for this F, we know how to map over the inner A.”
    *
    * Quick Note
    *   - F is a type constructor: it’s not a full type until you fill in what’s
    *     inside.
    *   - F[_] in Scala means: “some F that takes one type parameter.”
    *   - Mental model: read F[A] as “a value of type A in some context F”
    *     (missing value, list of possibilities, async, errors on the left,
    *     etc.). Functor is “apply a pure function through that context.”
    *   - Look at `TypeConstructor.scala` for more.
    *
    * A `Functor` instance must obey 2 laws: --------------------------------
    *   - Composition
    *
    * Given `fa: F[A]`, `f: A => B`, and `g: B => C`, you can transform the
    * inner value in two equivalent ways:
    *
    * (1) Two maps: apply `f` inside `F`, then `g` — `fa.map(f).map(g)` →
    * `F[C]`.
    *
    * (2) One map with the composed function — `fa.map(f.andThen(g))` → `F[C]`.
    *
    * In Scala, `f.andThen(g)` is `x => g(f(x))` (do `f` first, then `g`).
    *
    * The law requires these to be equal:
    *
    * `fa.map(f).map(g) == fa.map(f.andThen(g))`
    *
    * So “map twice” must match “map once with the combined step.” Intuitively,
    * `map` must not smuggle in extra behavior between the two steps;
    * practically, implementations may fuse consecutive maps without changing
    * meaning (`map(f).map(g)` → `map(f.andThen(g))`).
    *
    *   - Composition Example:
    *     {{{
    * List(1).map(_ + 1).map(_ * 2)  // List(4)
    * List(1).map(x => (x + 1) * 2)  // List(4)
    *     }}}
    *     Same as `map(_.+ 1).andThen(_ * 2)` composed in one map.
    *   - Identity
    *
    * Mapping with the identity function is a no-op:
    *
    * `fa.map(x => x) == fa`
    *
    * Together with composition, these laws pin down what “mapping” is allowed
    * to mean for a lawful functor.
    *
    * Another way of viewing a `Functor[F]` is that `F` allows the lifting of a
    * pure function `A => B` into the effectful function `F[A] => F[B]`. We can
    * see this in `lift` below in the trait.
    *
    * Functors for effect management -------------------------------- The `F` in
    * `Functor[F[_]]` is often referred to as an "effect" or "computational
    * context". Different effects will abstract away different behaviours with
    * respect to fundamental functions like `map`. For instance `Option`'s
    * effect abstracts away potentially missing values, where `map` applies the
    * function only in the `Some` case but otherwise threads the `None` through.
    *
    * Taking this view, we can view `Functor` as the ability to work with a
    * single effect — we can apply a pure function to a single effectful value
    * without needing to "leave" the effect. This matches the mental model
    * above: `F` is the context (optional value, list, async, …) and `map`
    * applies an `A => B` through that context without unpacking to a bare `A`
    * first. The quotes on "leave" mean you remain inside the same structure
    * (e.g. still `Option`, still `Future`); you are not duplicating
    * `map`-by-hand logic, even though you stay in the effect.
    */
  trait Functor[F[_]]:
    def map[A, B](fa: F[A])(f: A => B): F[B]

    def lift[A, B](f: A => B): F[A] => F[B] =
      fa => map(fa)(f)

  // Example implementation for Option
  given functorForOption: Functor[Option] = new Functor[Option]:
    def map[A, B](fa: Option[A])(f: A => B): Option[B] = fa match
      case None    => None
      case Some(a) => Some(f(a))

/** Functors compose -------------------------------------- If you've ever found
  * yourself working with nested data types such as `Option[List[A]]` or
  * `List[Either[String, Future[A]]]` and tried to `map` over it, you've most
  * likely found yourself doing something like `_.map(_.map(_.map(f)))`. As it
  * turns out, Functors compose, which means if `F` and `G` have `Functor`
  * instances, then so does `F[G[_]]`.
  *
  * Such composition can be achieved via the `Functor#compose` method.
  */
@main def FunctorsComposeExample() =
  import cats.Functor
  import cats.syntax.all._

  val listOption: List[Option[Int]] = List(Some(1), None, Some(2))
  Functor[List].compose[Option].map(listOption)(_ + 1)
  // res1: List[Option[Int]] = List(Some(value = 2), None, Some(value = 3))

/** After `FunctorsComposeExample`: `Functor[List].compose[Option].map(...)`
  * still uses the raw `List[Option[A]]` value — no extra wrapper. That breaks
  * down when code is generic in `F` and expects a `Functor[F]`: when `F` is
  * “list on the outside, option on the inside”, i.e. `F[A]` is
  * `List[Option[A]]`, Scala has no one standard name for that `F`, so you
  * either introduce an alias + `given Functor[...] = compose` (as in `foo`
  * below) or hand the instance in with `using` — the “pass the composed
  * instance / local implicit” situation.
  *
  * The second half uses `cats.data.Nested` instead: a dedicated type so the
  * functor instance is found like any other, traded off against
  * wrapping/unwrapping noise at the value level.
  */
@main def FunctorsWithNestedExample() =
  import cats.Functor
  import cats.syntax.all._

  // Continues `FunctorsComposeExample`: same `List[Option[Int]]`, but now a generic helper
  // needs one `Functor[F]` — illustrating why composed functors need a `given` (or `Nested`).
  val listOption: List[Option[Int]] = List(Some(1), None, Some(2))

  // Generic helper: if `F` is a functor, map every inner `A` to `()` and keep the same `F`-shape.
  def needsFunctor[F[_]: Functor, A](fa: F[A]): F[Unit] =
    Functor[F].map(fa)(_ => ())

  // `needsFunctor` only sees “one functor `F`”. The value `listOption` has type `List[Option[Int]]`,
  // but the compiler can split that in two ways:
  //
  //   • Wrong split: `F = List`, `A = Option[Int]`. Then `map` treats each list cell as the `A`;
  //     `_ => ()` maps `Option[Int] => Unit`, so you get `List[Unit]` and the `Option` layer is gone.
  //
  //   • Right split: `F = ListOption` with `ListOption[A] = List[Option[A]]`, so `A = Int`. The
  //     composed functor’s `map` changes each `Int` inside `Some` and leaves `None` alone →
  //     `List[Option[Unit]]`.
  //
  // We define the alias `ListOption`, supply `Functor[ListOption]` with `compose` (Cats will not
  // derive that for the alias by itself), and write `needsFunctor[ListOption, Int](listOption)` so
  // inference picks the composed functor instead of plain `List`.
  def foo: List[Option[Unit]] = {
    type ListOption[A] = List[Option[A]]
    given Functor[ListOption] = Functor[List].compose[Option]
    needsFunctor[ListOption, Int](listOption)
  }

  // As in the scaladoc above: avoid alias + `given` by boxing in `Nested` (extra wrap/unwrap).
  import cats.data.Nested
  import cats.syntax.all._

  val nested: Nested[List, Option, Int] = Nested(listOption)
  // Nested(value = List(Some(value = 1), None, Some(value = 2)))
  println(nested.map(_ + 1))
  // Nested(value = List(Some(value = 2), None, Some(value = 3)))
