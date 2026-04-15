package learning.typeclasses

/** Small runnable sketch: See `README.md` in this package for the longer story (type classes vs subtyping).*/

// trait Monoid[A]:
//     def empty: A // the identity element
//     def combine(x: A, y: A): A // the associative binary operation


import cats.Monoid

// Simple Pair class
final case class Pair[A, B](first: A, second: B)

object Pair:

  /** If `Monoid[A]` and `Monoid[B]` exist, build `Monoid[Pair[A,B]]` pointwise (tuple monoid). */
  given monoidPair[A: Monoid, B: Monoid]: Monoid[Pair[A, B]] =
    new Monoid[Pair[A, B]]:
      def empty: Pair[A, B] = Pair(Monoid[A].empty, Monoid[B].empty)
      def combine(x: Pair[A, B], y: Pair[A, B]): Pair[A, B] =
        Pair(Monoid[A].combine(x.first, y.first), Monoid[B].combine(x.second, y.second))

/** Fold with the monoid identity as the `foldRight` seed; `A` names the implicit `Monoid[A]` (same as `using m: Monoid[A]`). */
def combineAll[A: Monoid](list: List[A])(using A: Monoid[A]): A =
  list.foldRight(A.empty)(A.combine)

/** this can be further synatic sugarized to below, but see note about cost for implementer in README. **/
def combineAll2[A : Monoid](list: List[A]): A =
  list.foldRight(Monoid[A].empty)(Monoid[A].combine)

@main def catsTypeClassesExample() =
  // Uses Cats’ `Monoid[Int]` / `Monoid[String]` and the derived `Pair` instance above.
  println(combineAll2(List(Pair(1, "hello"), Pair(2, " "), Pair(3, "world"))))
  // Pair(first = 6, second = "hello world")
