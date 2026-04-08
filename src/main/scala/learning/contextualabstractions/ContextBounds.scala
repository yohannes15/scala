package learning.contextualabstractions
import scala.math.*

/* 
In many cases, the name of a context parameter doesn't have to be
mentioned explicitly, since it is only used by the compiler in
synthesized arguments for other context parameters. 

In that case you don't have to define a parameter name, and can just
provide the parameter type. For example, consider a method maxElement
that returns the maximum value in a collection.

The method maxElement takes a context parameter of type Ord[A] 
only to pass it on as an argument to the method max. Note that the 
method max takes a context parameter of type Ord[A], like the method
maxElement. 

Note that in practice we would use the existing method max on List, 
but we made up this example for illustration purpose
*/

/** Defines how to compare values of type `A` */
trait Ord[A]:
  def greaterThan(a1: A, a2: A): Boolean

// `given` provides the canonical Ord[Int] and Ord[String].
// The compiler uses these automatically whenever a `using Ord[_]`
// parameter is required — no need to pass them at call sites.
//
// `with` is used here because Ord[A] is a trait — it has no concrete
// value yet, so we must implement its abstract method from scratch.
// `with` opens an inline anonymous class body, equivalent to:
//
//   given intOrd: Ord[Int] = new Ord[Int]:
//     def greaterThan(a1: Int, a2: Int): Boolean = a1 > a2
//
// Compare to ContextParameters.scala where `= config` is used instead:
//   given Config = config
// That works because Config is a concrete case class value that already
// exists — no methods need to be implemented, just point at the value.
//
// Rule of thumb:
//   `with { ... }` — type is a trait/abstract class, implement it here
//   `= value`      — type is concrete, wrap an existing value
given intOrd: Ord[Int] with
  def greaterThan(a1: Int, a2: Int): Boolean = a1 > a2

given stringOrd: Ord[String] with
  def greaterThan(a1: String, a2: String): Boolean = a1 > a2

/** Returns the maximum of two values */
// `using ord: Ord[A]` — named context parameter; we need the name
// because we explicitly pass it on to max inside the body
def max[A](a1: A, a2: A)(using ord: Ord[A]): A =
  if ord.greaterThan(a1, a2) then a1 else a2

// Version 1: explicit — `ord` is named so we can forward it manually.
// This is verbose because we're just forwarding `ord` that the compiler
// could thread through for us automatically.
def maxElementUgly[A](as: List[A])(using ord: Ord[A]): A =
  as.reduceLeft(max(_, _)(using ord))

/* 
Since `ord` is a context parameter in the method max, the compiler 
can supply it for us in the implementation of maxElement, when we call 
the method max

Note that, because we don't need to explicitly pass it to the method max,
we can leave out its name in the definition of the method maxElement. 
This is an anonymous context parameter.
 */

// Version 2: anonymous context parameter — no name needed because
// we never refer to `ord` directly; the compiler forwards it to max.
def maxElementBetter[A](as: List[A])(using Ord[A]): A =
  as.reduceLeft(max(_, _))

/***************************************************************
Context bounds
****************************************************************

Given the above background, a context bound is a shorthand syntax 
for expressing the pattern of:
    - "a context parameter applied to a type parameter."

Using a context bound, the maxElement method can be written succinct
as below. 

A bound like `: Ord` on a type parameter A of a method or class 
indicates a context parameter with type Ord[A]. Under the hood, 
the compiler transforms this syntax into `def maxElementUgly`.

So all three definitions are equivalent — each is just a more
concise way of writing the same thing:

    maxElementUgly[A](as)(using ord: Ord[A])   // named, explicit
    maxElementBetter[A](as)(using Ord[A])       // anonymous using
    maxElement[A: Ord](as)                      // context bound (shortest)
 */

// Version 3: context bound — [A: Ord] is the shortest form.
// The compiler rewrites [A: Ord] to (using Ord[A]) automatically.
def maxElement[A: Ord](as: List[A]): A =
  as.reduceLeft(max(_, _))

def contextBoundsExample() =
  // The compiler finds `intOrd` and `stringOrd` automatically
  println(maxElement(List(3, 1, 4, 1, 5, 9, 2)))        // 9
  println(maxElement(List("banana", "apple", "cherry"))) // cherry

  // All three versions produce the same result
  val ints = List(10, 3, 7)
  println(maxElementUgly(ints))   // 10 — named, explicit forwarding
  println(maxElementBetter(ints)) // 10 — anonymous using
  println(maxElement(ints))       // 10 — context bound shorthand

/* 
Typing Shorthands / Shortcuts (SUPERNOTE)

[A]                     // plain type parameter — unconstrained
[A: Ord]                // context bound — shorthand for (using Ord[A])
[A <: Animal]           // upper bound — A must be a subtype of Animal
[A >: Dog]              // lower bound — A must be a supertype of Dog
[+A]                    // covariant
[-A]                    // contravariant
// can also combine them
[A <: Animal: Ord]      // A must be a subtype of Animal AND have an Ord[A] in context


The way to read [A: Ord] out loud is:
  "A, with a context bound of Ord"
which the compiler rewrites to:
  "A, with an implicit using Ord[A] parameter"
*/
