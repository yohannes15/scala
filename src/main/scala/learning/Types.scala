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

def f(x: Resettable & Growable[String]): Unit =
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

    f(fromNominal)
    f(fromStructural)
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

val a = if true then name else email
/*
The type of a is Object, which is a supertype of Username and Password, 
but not the least supertype, Password | Username */

val b: Email | Username = if true then name else email
// If you want the least supertype you have to give it explicitly, as is done for b.
