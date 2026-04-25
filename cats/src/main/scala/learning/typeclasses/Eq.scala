package learning.typeclasses

import cats.kernel.Eq

/** Eq is an alternative to the standard Java equals method. It is defined by
  * the single method eqv. In Scala it's possible to compare any two values
  * using == (which desugars to Java equals). This is because `equals` type
  * signature uses Any (Java's Object) to compare two values. This means that we
  * can compare two completely unrelated types without getting a compiler error.
  *
  * The Scala compiler may warn us in some cases, but not all, which can lead to
  * some weird bugs. For example this code will raise a warning at compile time
  * in scala2. Scala3 won't let this compile.
  *
  * E.g "Hello" == 42 or 42 == "Hello" // warning: comparing values of types Int
  * and String using `==` will always yield false
  *
  * Scala shouldn't let us compare two types that can never be equal.
  *
  * As you can probably see in the type signature of `eqv`, it is impossible to
  * compare two values of different types, eliminating these types of bugs.
  *
  * The `Eq` syntax package also offers some handy symbolic operators:
  *
  * final class EqOps[A: Eq](lhs: A) { def ===(rhs: A): Boolean = Eq[A].eqv(lhs,
  * rhs) def =!=(rhs: A): Boolean = Eq[A].neqv(lhs, rhs) def eqv(rhs: A):
  * Boolean = Eq[A].eqv(lhs, rhs) def neqv(rhs: A): Boolean = Eq[A].neqv(lhs,
  * rhs) }
  *
  * Implementing `Eq` instances yourself for every data type might seem like a
  * huge drawback compared to only slight gains of typesafety. Fortunately, we
  * have two great options:
  *
  *   1. Use inbuilt helper functions
  *   2. Use library called `kittens` -> automatic type class derivation for
  *      Cats including `Eq`
  */
def eqv[A](x: A, y: A): Boolean = ???

@main def eqExamples() =
  import cats.syntax.all.*
  println(s"1 === 1 is ${1 === 1}") // true
  println(s"Hello =!= World is ${"Hello" =!= "World"}") // true

  case class Foo(a: Int, b: String)
  // Eq.fromUniversalEquals defers to ==.
  // case class have reasonable `equals` implementations
  given eqFoo: Eq[Foo] = Eq.fromUniversalEquals

  println(
    s"== equality check for case class Foo: ${Foo(10, "") === Foo(10, "")}"
  )

/*
extra resources: https://github.com/typelevel/kittens
 */
