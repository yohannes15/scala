package learning.typeclasses

/*
Monoid extends the power of `Semigroup` by providing an additional empty/identity value.

    trait Semigroup[A] {
        def combine(x: A, y: A): A
    }

    trait Monoid[A] extends Semigroup[A] {
        def empty: A
    }

This `empty` value should be an identity for the `combine` operation, which means the
following equalities hold for any choices of `x`.

    combine(x, empty) = combine(empty, x) = x

Many types that form a `Semigroup` also form a `Monoid`, such as `Int` (with 0) and
Strings (with "").

In the `Semigroup` section we had trouble writing a generic `combineAll` function
because we had nothing to give if the list was empty. With `Monoid` we can return
`empty` — see `combineAll` in `Basics.scala`, or use `Monoid.combineAll` from Cats.

Cats defines the `Monoid` type class in cats-kernel. The cats package object defines type
aliases to the Monoid from cats-kernel, so that you can simply import cats.Monoid.
 */

import cats.{Monoid, Semigroup}
import cats.syntax.all._

@main def monoidExample() =
  // an implentation of a Int monoid (available in the cats Monoid implementations)

  // given intAdditionMonoid: Monoid[Int] = new Monoid[Int] {
  //     def empty: Int = 0
  //     def combine(x: Int, y: Int): Int = x + y
  // }

  println(
    s"Monoid is associative:" +
      s" ${Monoid[Int].combine(1, Monoid[Int].empty) == Monoid[Int].combine(Monoid[Int].empty, 1)}"
  )
  /* -------------- */
  optionMonoidExample()

  import cats.syntax.all._

  println(Monoid.combineAll(List(1, 2, 3))) // 6
  println(Monoid.combineAll(List("hello", " ", "world"))) // "hello world"
  println(
    Monoid.combineAll(
      List(Map('a' -> 1), Map('a' -> 2, 'b' -> 3), Map('b' -> 4, 'c' -> 5))
    )
  ) // Map('b' -> 7, 'c' -> 5, 'a' -> 3)
  println(
    Monoid.combineAll(List(Set(1, 2), Set(2, 3, 4, 5)))
  ) // Set(5, 1, 2, 3, 4)

/*
The Option monoid
---------------------------
There are some types that can form a `Semigroup` but not a `Monoid`. For example, the
following `NonEmptyList` type forms a semigroup through ++, but has no corresponding
identity element to form a monoid.

NonEmptyList is a specialized data type that has at least one element. Otherwise it
behaves like a normal List.
 */

final case class NonEmptyList[A](head: A, tail: List[A]):
  def ++(other: NonEmptyList[A]): NonEmptyList[A] =
    NonEmptyList(head, tail ++ other.toList)

  def toList: List[A] = head :: tail

object NonEmptyList:
  // Semigroup from Cats is a trait with one abstract method:
  //   def combine(x: A, y: A): A
  //
  // SAM (Single Abstract Method): Scala 3 can treat a *function value* with the same
  // shape — (NonEmptyList[A], NonEmptyList[A]) => NonEmptyList[A] — as that trait by
  // using the function as the implementation of `combine`. So the name `combine` is not
  // “inferred” from `++`; the compiler matches the *single abstract method* to your
  // two-argument function (parameter order matches `combine`'s arguments).
  //
  // Shorthand: `_ ++ _` is `(x, y) => x ++ y`, i.e. binary concatenation — same as the
  // body below. Uncomment the one-liner to use SAM style instead of an anonymous class:

  // given nonEmptyListSemigroup[A]: Semigroup[NonEmptyList[A]] = _ ++ _

  given nonEmptyListSemigroup[A]: Semigroup[NonEmptyList[A]] =
    new Semigroup[NonEmptyList[A]] {
      def combine(x: NonEmptyList[A], y: NonEmptyList[A]): NonEmptyList[A] =
        x ++ y
    }

/*
How then can we collapse a `List[NonEmptyList[A]]`? For such types that only have a `Semigroup`
we can lift into `Option` to get a `Monoid`. So: “for every A that we know how to combine as a
semigroup, we can build a monoid on Option[A].”

This is the Monoid for Option: for any Semigroup[A], there is a Monoid[Option[A]].
This lifting and combining of Semigroups into Option is provided by Cats as Semigroup.combineAllOption.
Instead of doing map(nel => Option(nel)) manually in example below

 */
given optionMonoid[A: Semigroup]: Monoid[Option[A]] = new Monoid[Option[A]] {
  def empty: Option[A] = None
  def combine(x: Option[A], y: Option[A]): Option[A] = x match
    case None     => y
    case Some(xv) =>
      y match
        case None     => x
        case Some(yv) => Some(xv |+| yv)
}

def optionMonoidExample() =
  val list = List(
    NonEmptyList(1, List(2, 3)),
    NonEmptyList(4, List(5, 6))
  )
  // lifting to Option manually
  println(
    s"Example combineAll after lifting to " +
      s"Option because NonEmptyList doesn't have an " +
      s"identity: ${Monoid.combineAll(list.map(nel => Option(nel)))}"
  )

  // provided by Cats: needs only Semigroup[NonEmptyList[Int]], not a (nonexistent) Monoid
  println(Semigroup.combineAllOption(list))
