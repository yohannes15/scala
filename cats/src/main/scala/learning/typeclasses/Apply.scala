package learning.typeclasses

import cats.Functor
import cats.Apply

object LearningApply:

  /** A closely related type class is [[Apply]], which is identical to
    * [[Applicative]] except for the `pure` method. Indeed in Cats,
    * `Applicative` is a subclass of `Apply` with the addition of `pure` (and
    * default `map` from `ap` + `pure`).
    *
    * The laws for `Apply` are the laws of `Applicative` that do not mention
    * `pure`. For `Apply` alone, associativity (of combining / `product`) is the
    * main structural law; left/right identity laws that use `pure(())` belong
    * to `Applicative`.
    *
    * One motivation for `Apply` is that some types have `Apply` instances but
    * not `Applicative`. Consider `pure` for `Map[K, A]`: given a value of type
    * `A`, you would need to choose some key `K` to store it under, with no
    * information from `a` alone — so no sensible `pure` exists in general.
    *
    * However, given existing `Map[K, A]` and `Map[K, B]` (or `Map[K, A => B]`),
    * you can pair or apply values for the *same* keys. Hence `Map[K, ·]` has an
    * `Apply` instance but (typically) no `Applicative`.
    */
  trait Apply[F[_]] extends Functor[F]:
    def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]

  trait Applicative[F[_]] extends Apply[F]:
    def pure[A](a: A): F[A]
    def map[A, B](fa: F[A])(f: A => B): F[B] = ap(pure(f))(fa)

/** Cats provides [[cats.Apply]] for `Map[K, *]` (in Scala 3:
  * `[X] =>> Map[K, X]`): combine values that share the same key. There is no
  * [[cats.Applicative]] for `Map` — you cannot implement `pure` without
  * inventing keys.
  */
@main def learningApplyExample(): Unit =
  val names: Map[Int, String] = Map(1 -> "Ada", 2 -> "Bob")
  val years: Map[Int, Int] =
    Map(1 -> 1815, 3 -> 1950) // key `2` missing on this side

  val M = Apply[[X] =>> Map[Int, X]]
  // `map2` keeps only keys present in *both* maps, then zips the values.
  val intro: Map[Int, String] = M.map2(names, years)((n, y) => s"$n ($y)")

  println(intro) // Map(1 -> "Ada (1815)") — no entry for 2 or 3 alone
