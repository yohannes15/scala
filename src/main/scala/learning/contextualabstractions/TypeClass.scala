package learning.contextualabstractions

/* 
Type Classes
---------------------------------------------------------------
A `type class` is an abstract, parameterized type that lets you
add new behavior to any closed data type without using sub-typing

Type classes were originally developed in Haskell as a disciplined
alternative to ad-hoc polymorphism. Similar thing in Java: 
`java.util.Comparator[T]`

Paper: https://infoscience.epfl.ch/record/150280/files/TypeClasses.pdf

Type classes are traits with one or more parameters whose 
implementations are provided as given instances in Scala 3 
or implicit values in Scala 2.

Use Cases
------------------
1. Expressing how a type you don't own - std / 3rd party lib -
   conforms to such behaviour
2. Expressing such a behaviour for multiple types w/o involving
   sub-typing relationships between those types

For example, `Show` is a well-known type class in Haskell, 
and the following code shows one way to implement it in Scala. 
If you imagine that Scala classes don’t have a toString method, 
you can define a Show type class to add this behavior to any 
type that you want to be able to convert to a custom string.

Creating a Type class
---------------------
Step 1:
    declare a parameterized trait that has one or more abstract methods. 
    Notice that this approach is close to the usual object-oriented 
    approach, where you would typically define a trait Show below

    There are a few important things to point out:

    - Type-classes like Showable take a type parameter A to say which type
      we provide the implementation of show for; in contrast, classic traits
      like Show do not.
    - To add the show functionality to a certain type A, the classic trait
      requires that A extends Show, while for type-classes we require to have 
      an implementation of Showable[A].
    - In Scala 3, to allow the same method calling syntax in Showable
      that mimics the one of Show, we define Showable.show as an 
      extension method.
*/

// OOP version
trait Show:
    def show: String

// a type class
trait Showable[A]:
    extension (a: A) def show: String

/* 
Step 2:
    Implement Concrete Instances. 
    
    Determine what classes in your app Showable should work for and then 
    implement behaviour for them. 
    
    For instance, to implement Showable for this Person class. you’ll 
    define a single canonical value of type Showable[Person], ie an 
    instance of Showable for the type Person, as the following code 
    example demonstrates:
*/

case class Person(firstName: String, lastName: String)

given Showable[Person] with
    extension (p: Person) def show: String = 
        s"${p.firstName} ${p.lastName}"

/*
Writing Methods that use a type class
--------------------------------------
NOTE

`a.show` looks like a regular method call but A has no `show` 
of its own. The compiler resolves it via the extension method 
defined inside Showable[A]:

    trait Showable[A]:
        extension (a: A) def show: String   // what gets called

So the context bound and extension method work together:
    [A: Showable]  — ensures a Showable[A] instance is in context
    a.show         — the compiler finds `show` as an extension from 
                     that Showable[A] & desugars it to: s.show(a)

Without [A: Showable], writing `a.show` would be a compile error
because A has no `show` method of its own.

    [A: Showable] is context bound syntax. It desugars to:
    
    def showAll[A](as: List[A])(using s: Showable[A]): Unit =
        as.foreach(a => println(a.show))
*/
def showAll[A: Showable](as: List[A]): Unit =
    as.foreach(a => println(a.show))

/* type class with multiple methods */
trait HasLegs[A]:
    extension (a: A)
        def walk(): Unit
        def run(): Unit

def typeClassExample() =
    val person = Person("Lionel", "Messi")
    println(person.show)
    showAll(
        List(Person("Jane", "Doe"), 
        Person("Mary", "Jane"))
    )

