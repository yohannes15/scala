package learning.functions

/** **********************************************************
  * *********************************************************** Partial
  * Functions --------------------------------
  *   - A function that may not be defined for all values of its argument type.
  *   - Implements the `PartialFunction[A, B]` trait where A is the input type
  *     and B is the result type.
  *   - Uses `case` expressions instead of a regular function body.
  *
  * Key methods: isDefinedAt(x) — returns true if the function handles x
  * applyOrElse(x, default) — applies the function or falls back to the default
  * orElse(other) — chains two partial functions; tries `other` for inputs not
  * handled by the first
  * ***********************************************************
  * **********************************************************
  */

// Only defined for odd integers
val doubledOdds: PartialFunction[Int, Int] = {
  case i if i % 2 == 1 => i * 2
}

def partialFunctionExample() =
  println(doubledOdds.isDefinedAt(3)) // true  — 3 is odd
  println(doubledOdds.isDefinedAt(4)) // false — 4 is even

  // calling with an undefined input throws MatchError
  try doubledOdds(4)
  catch case me: scala.MatchError => println(me.toString())

  // collect applies the partial function and skips undefined inputs
  val res = List(1, 2, 3).collect { case i if i % 2 == 1 => i * 2 }
  println(res) // List(2, 6)

  // applyOrElse: apply the function or fall back to the default
  println(
    doubledOdds.applyOrElse(4, _ + 1)
  ) // 5 (4 + 1, since 4 is not in domain)

  // orElse: compose two partial functions
  // the second handles values the first doesn't
  val incrementedEvens: PartialFunction[Int, Int] = {
    case i if i % 2 == 0 => i + 1
  }

  val combined = doubledOdds.orElse(incrementedEvens)
  val res2 = List(1, 2, 3, 4, 5).collect(combined)
  println(res2) // List(2, 3, 6, 5, 10)
