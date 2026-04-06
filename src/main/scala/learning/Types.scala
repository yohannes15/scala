package learning

/* 
Statically-typed programming languages offer a number of benefits:

    - Helping to provide strong IDE support
    - Eliminating many classes of potential errors at compile time
    - Assisting in refactoring
    - Providing strong documentation that cannot be outdated since 
      it is type checked

Scala’s ability to automatically infer types is one feature that makes 
it feel like a dynamically typed language.

************************************************
*************************************************
Generics
----------------------
- Generic classes (or traits) take a type as a parameter within [...]
- The scala convention is to use a single letter (like `A`) to name 
  those type parameters.
- The type can then be used inside the class as needed for method
  instance parameters, or on return types

This implementation of a Stack class takes any type as a parameter. 
The beauty of generics is that you can now create a Stack[Int], 
Stack[String], and so on, allowing you to reuse your implementation
of a Stack for arbitrary element types.
*/

class Stack[A]: // here we declare the type parameter A

    private var elements: List[A] = Nil
    // Here we refer to the type parameter A
    def push(x: A): Unit =
        elements = elements.prepended(x)
    
    def peek: A = elements.head
    def pop(): A = 
        val currentTop = peek
        elements = elements.tail
        currentTop

def genericsExample() = 
    val stack = Stack[Int]
    stack.push(1)
    stack.push(5)
    println(s"popped element ${stack.pop()} from stack")
    println(s"popped element ${stack.pop()} from stack")

/************************************************
**************************************************
Intersection Types (Scala 3 only)
------------------------------------
Used on types, the `&` operator creates a so called intersection type. 
The type `A & B` represents values that are both of the type `A` and of 
the type `B` at the same time. 

For instance, the following example uses the intersection type

    `Resettable & Growable[String]`

The members of an intersection type A & B are all the members of 
A and all the members of B. Therefore, as shown, Resettable & 
Growable[String] has member methods `reset` and `add`.

Intersection types can be useful to describe requirements structurally. 
That is, in our example f, we directly express that we are happy with 
any value for x as long as it’s a subtype of both Resettable and Growable. 
We did not have to create a nominal helper trait Both[A] like the following:

*/

trait Resettable:
    def reset(): Unit

trait Growable[A]:
    def add(a: A): Unit

trait Both[A] extends Resettable, Growable[A]

def goodf(x: Resettable & Growable[String]): Unit =
    x.reset()
    x.add("first")

def badf(x: Both[String]): Unit =
    x.reset()
    x.add("first")

/* 
DIFFERENCE B/N TWO ALTERNATIVES
-----------------------------------------
There is an important difference between the two alternatives of defining f: 
While both allow f to be called with instances of Both, only the former allows 
passing instances that are subtypes of Resettable and Growable[String], 
but not of Both[String].

Concrete illustration:
  - `NominalBoth` extends the named trait `Both[String]`, so it works with both `f` and `badf`.
  - `StructuralOnly` mixes `Resettable` and `Growable[String]` in one class but does *not* extend
    `Both[String]`. It is still a `Resettable & Growable[String]`, so `f` accepts it; `badf` does not,
    because the parameter type requires the *nominal* type `Both[String]`, not just the same methods.

Note that & is commutative: A & B is the same type as B & A.
 */

/** values are subtypes of `Both[String]`. */
final class NominalBoth extends Both[String]:
    private var items: List[String] = Nil
    def reset(): Unit = items = Nil
    def add(a: String): Unit = items = items :+ a
    override def toString: String = s"NominalBoth($items)"

/** Mixes the same two capabilities without extending `Both` — structural match, nominal mismatch. */
final class StructuralOnly extends Resettable, Growable[String]:
    private var items: List[String] = Nil
    def reset(): Unit = items = Nil
    def add(a: String): Unit = items = items :+ a
    override def toString: String = s"StructuralOnly($items)"

def intersectionTypesExample(): Unit =
    val fromNominal = NominalBoth()
    val fromStructural = StructuralOnly()

    goodf(fromNominal)
    goodf(fromStructural)
    println(s"after f: $fromNominal, $fromStructural")

    badf(fromNominal)
    // badf(fromStructural)  // does not compile: StructuralOnly is not a subtype of Both[String]

    println("intersectionTypesExample: f accepts any Resettable & Growable[String]; badf only Both[String]")

/************************************************************
*************************************************************
Union Types

The | operator creates a so-called union type. The type A | B
represents values that are either of the type A or of the type B.

In the following example, the help method accepts a parameter 
named id of the union type `Username | Email`, that can be 
either a Username or a Email:

As shown, union types can be used to represent alternatives 
of several different types, without requiring those types to
be part of a custom-crafted class hierarchy, or requiring 
explicit wrapping.

Union is also commutative: A | B is the same type as B | A.
************************************************************
************************************************************/

case class Username(name: String)
case class Email(email: String)

def help(id: Username | Email): Unit =
  id match
    case Username(name) => println(s"You have a name $name")
    case Email(email) => println(s"You have a email $email")
    // case 1.0 => ???   // ERROR: this line won’t compile

def unionTypeExample() =
    val email = Email("test@email.com")
    val username = Username("YB")
    help(email)
    help(username)
    /*
        help("hi")   // error: Found: ("hi" : String) Required: Username | Email
    
    You’ll also get an error if you attempt to add a case 
    to the match expression that doesn’t match the Username 
    or Email types
      */

/* 
Without union types, it would require pre-planning of class hiearchy:
    trait UsernameOrEmail
    case class Username(name: String) extends UsernameOrEmail
    case class Email(name: String) extends UsernameOrEmail
    def help(id: UsernameOrEmail) = ...

Pre-planning does not scale very well since, for example, requirements 
of API users might not be foreseeable. Additionally, cluttering the 
type hierarchy with marker traits like UsernameOrPassword also makes
the code more difficult to read.

Inference of Union Types
--------------------------------
The compiler assigns a union type to an expression only if such a type
is explicilty given. For instance given these values:
 */

val name = Username("Eve")
val email = Email("eve@email.com")

val ac = if true then name else email
/*
The type of ac is Object, which is a supertype of Username and Password, 
but not the least supertype, Password | Username */

val bc: Email | Username = if true then name else email
// If you want the least supertype you have to give it explicitly, as is done for bc.

/**********************************************************************
***********************************************************************
Algebraic Data Types - Part 2 
------------------------------------
The enum concept is general enough to also support ADTs and their
generalized versions (GADTs). Below is an example that shows how an
`Option` type can be repressented as an ADT.

Example creates 

- an Option enum with a covariant type parameter `T` consisting of 
  two cases, `Some` and `None`
- `Some` is parameterized with the a value parameter x: T. This is
  shorthand for writing a `case` class that extends Option
- `None` is not parameterized, treated as a normal `enum` value

**********************************************************************
**********************************************************************/

// +T = covariant: Option[String] is a subtype of Option[Any]
enum Option[+T]: 
    // Shorthand for: case class Some(x: T) extends Option[T]
    case Some(x: T)
    // Singleton with no fields. Inferred as: case object None extends Option[Nothing]
    // Option[Nothing] is a subtype of Option[T] for any T, due to covariance
    case None

    // Pattern match on `this` is exhaustive — the compiler knows all cases
    def isDefined: Boolean = this match
        case None => false
        case Some(_) => true

// companion
object Option:
    // Smart constructor: wraps non-null values in Some, maps null to None
    // T >: Null constrains T to be a nullable (reference) type
    def apply[T >: Null](x: T): Option[T] =
        if (x == null) None else Some(x)

/* long hand version extends is optional 
enum Option[+T]:
    case Some(x: T) extends Option[T]
    case None extends Option[Nothing]
*/

def optionAsEnumExample() = 
    val res1: Option[String] = Option.Some("Hello")
    val res2: Option[Nothing] = Option.None
    println(res1)
    println(res2)

/* 
Enumerations and ADTs share the same syntactic construct, so they can be 
seen simply as two ends of a spectrum, and it’s perfectly possible to 
construct hybrids. For instance, the code below gives an implementation 
of Color, either with three enum values or with a parameterized case 
that takes an RGB value:
 */

// The enum constructor declares a shared `rgb` field every case must supply.
// This is a hybrid: three fixed singleton cases + one dynamic ADT case.
enum ADTColor(val rgb: Int):
    // Singleton cases: no runtime fields, each hardcodes its rgb value
    case Red   extends ADTColor(0xFF0000)
    case Green extends ADTColor(0x00FF00)
    case Blue  extends ADTColor(0x0000FF)
    // ADT case: accepts a runtime argument and forwards it as the rgb value
    case Mix(mix: Int) extends ADTColor(mix)


def enumExampleMore() = 
    println(ADTColor.Red.rgb)           // 16711680 — fixed at compile time
    println(ADTColor.Mix(0xFF0000).rgb) // 16711680 — supplied at runtime

    /*
    Recursive Enumerations
    --------------------------------------------- 
    Enumerations can be recursive
    */

    // Peano encoding of natural numbers: integers built purely from structure
    // Zero is the base case; Succ wraps another Nat, adding 1 each time
    enum Nat:
        case Zero          // represents 0
        case Succ(n: Nat)  // represents n + 1; e.g. Succ(Succ(Zero)) == 2

    // The same recursive pattern applied to a generic collection
    // Nil terminates the list; Cons prepends one element to an existing List[A]
    enum List[+A]:
        case Nil                           // empty list — base case
        case Cons(head: A, tail: List[A])  // e.g. Cons(1, Cons(2, Cons(3, Nil))) == [1,2,3]

    import Nat.*
    val zero  = Zero                    // 0
    val one   = Succ(Zero)              // 1
    val two   = Succ(Succ(Zero))        // 2
    val three = Succ(Succ(Succ(Zero)))  // 3
    println(zero)   // Zero
    println(one)    // Succ(Zero)
    println(two)    // Succ(Succ(Zero))
    println(three)  // Succ(Succ(Succ(Zero)))

    import List.*
    val empty  = Nil                             // []
    val nums   = Cons(1, Cons(2, Cons(3, Nil)))  // [1, 2, 3]
    val strs   = Cons("a", Cons("b", Nil))       // ["a", "b"]
    println(empty)  // Nil
    println(nums)   // Cons(1,Cons(2,Cons(3,Nil)))
    println(strs)   // Cons(a,Cons(b,Nil))

    // pattern matching works exhaustively across all recursive cases
    def natToInt(n: Nat): Int = n match
        case Zero    => 0
        case Succ(n) => 1 + natToInt(n)

    def listHead[A](l: List[A]): Option[A] = l match
        case Nil        => Option.None
        case Cons(h, _) => Option.Some(h)

    println(natToInt(three))    // 3
    println(listHead(nums))     // Some(1)
    println(listHead(empty))    // None
