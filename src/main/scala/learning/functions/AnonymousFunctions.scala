package learning.functions

/** **********************************************************
  * *********************************************************** Anonymous
  * Functions / Lambda --------------------------------
  *   - a block of code that's passed as an argument to a higher-order function.
  *   - a function definition that is not bound to an identifier — it has no
  *     name.
  *
  * The => symbol acts as a transformer: it converts the parameter list on the
  * left into a result using the expression on the right.
  * ***********************************************************
  * **********************************************************
  */

val ints = List(1, 2, 3)

val doubledInts = ints.map(_ * 2) // shorthand: _ stands for each element
val doubledInts2 = ints.map((i: Int) => i * 2) // explicit parameter and type

def anonymousExample() =
  // full form: explicit type
  ints.foreach((i: Int) => println(i))
  // inferred type
  ints.foreach(i => println(i))
  // `_` when the argument is used exactly once in the body
  ints.foreach(println(_))
  // method reference: no argument needed when the function body is a single call
  ints.foreach(println)
