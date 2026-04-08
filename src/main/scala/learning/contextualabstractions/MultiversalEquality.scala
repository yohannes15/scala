package learning.contextualabstractions

/*

Previously, scala had universal equality. Two values of any
types could be compared with each other using == and !=. This
came from the fact that == and != are implemented in terms of
Java's `equals` method, which can compare values of any two
reference types

Universal equality is convenient, but its also dangerouds as
it undermines type safety. For instance, let’s assume that
after some refactoring, you’re left with an erroneous program
where a value y has type S instead of the correct type T:

    val x = ...   // of type T
    val y = ...   // of type S, but should be T
    x == y        // typechecks, will always yield false
    // Run time error is raised

Type-safe language can do better and multiversal equality is
that. It is an opt-in way to make universal equality safer. It
uses the binary type class `CanEqual` to indicate values of two
given types can be compared with each other
 */

/*
Allowing comparison of class instances
---------------------------------------
By default, in scala3 you can still create an equality comparsion
like below.

In scala3, you can disable such comparisons:
    a) importing `scala.language.strictEquality`
    b) using the `-language:strictEquality` compiler flag

 */

def defaultComparsionExample() =
  case class Cat(name: String)
  case class Dog(name: String)
  val c = Cat("clem")
  val d = Dog("Buddy")
  println(d == c) // false but it compiles

def wontCompileComparisonExample() =
  import scala.language.strictEquality
  case class Cat(name: String)
  case class Dog(name: String)
  val rover = Dog("Rover")
  val fido = Dog("Fido")
  println("Compiler won't allow comparison between rover and fido")
  // println(rover == fido)
  // compiler error message:
  // Values of types Dog and Dog cannot be compared with == or !=

/*
Enabling Comparisons
---------------------------------------
There are 2 ways to enable this comparison using Scala 3 `CanEqual`
type class.

    1) derive the `CanEqual` class
    b) using the `-language:strictEquality` compiler flag

 */
def derivedCanEqualExample() =
// Option 1: derive (simple way)
  case class Cat(name: String) derives CanEqual
  // Option 2:
  case class Dog(name: String)
  given CanEqual[Dog, Dog] = CanEqual.derived
  val rover = Dog("Rover")
  val rover2 = Dog("Rover")    
  val fido = Dog("Fido")
  println(rover == fido) // false
  println(rover == rover2) // true

/* 
A more real world example
---------------------------------------
Imagine you have an online bookstore and want to allow or disallow
the comparison of (physical), (printed) books, and (audiobooks). 
With Scala 3 you start by enabling multiversal equality as shown 
in the previous example:
*/

// [1] add this import, or this command line flag: -language:strictEquality
import scala.language.strictEquality

// [2] create your class hierarchy / domain objects
trait Book:
    def author: String
    def title: String
    def year: Int

case class PrintedBook(author: String, title: String, year: Int, pages: Int) extends Book:
    override def equals(that: Any): Boolean = that match
        case a: AudioBook =>
            this.author == a.author 
            && this.title == a.title
            && this.year == a.year
        case p: PrintedBook =>
            this.author == p.author 
            && this.title == p.title 
            && this.pages == p.pages
        case _ =>
            false

case class AudioBook(author: String, title: String, year: Int, lengthInMinutes: Int) extends Book:
    // override to allow AudioBook to be compared to PrintedBook
    override def equals(that: Any): Boolean = that match
        case a: AudioBook =>
            this.author == a.author 
            && this.title == a.title
            && this.year == a.year
            && this.lengthInMinutes == a.lengthInMinutes
        case p: PrintedBook =>
            this.author == p.author && this.title == p.title
        case _ =>
            false

def realWorldExample() = 

    // [3] create type class instances to define the allowed comparisons
    given CanEqual[PrintedBook, PrintedBook] = CanEqual.derived // allow `PrintedBook == PrintedBook`
    given CanEqual[AudioBook, AudioBook] = CanEqual.derived     // allow `AudioBook == AudioBook`

    // [4a] Comparing two printed books works as desired
    val p1 = PrintedBook("1984", "George Orwell", 1961, 328)
    val p2 = PrintedBook("1984", "George Orwell", 1961, 328)
    println(s"$p1 and $p2 are equal: ${p1 == p2}") // true

    // [4b] you can’t compare a printed book and an audiobook
    val pBook = PrintedBook("1984", "George Orwell", 1961, 328)
    val aBook = AudioBook("1984", "George Orwell", 2006, 682)
    // println(pBook == aBook)   // compiler error 
    // Values of types PrintedBook and AudioBook cannot be compared with == or !=
    
    // NOTE: This is how multiversal equality catches illegal type comparisons at compile time.

    /* 
    Enabling “PrintedBook == AudioBook” 
    ******************************************
    That works as desired, but in some situations you may want to allow the
    comparison of physical books to audiobooks. When you want this, create 
    these two additional equality comparisons:
    */

    // allow `PrintedBook == AudioBook`, and `AudioBook == PrintedBook`
    given CanEqual[PrintedBook, AudioBook] = CanEqual.derived
    given CanEqual[AudioBook, PrintedBook] = CanEqual.derived

    // Now you can compare physical books to audiobooks without a compiler error:
    println(aBook == pBook)   // true (works because of `equals` in `AudioBook`)
    println(pBook == aBook)   // true (works because of `equals` in `PrintedBook`)
    
    /* 
    Implement “equals” to make them really work
    ******************************************
    While these comparisons are now allowed, they will mostly be useless w/o a custom
    equals method. Compiler won't know how to make these comparisons. Therefore, 
    the solution is to override the equals methods for each class. For instance, 
    we override the equals method for AudioBook in the def above.
     */
    
// More info: https://docs.scala-lang.org/scala3/reference/contextual/multiversal-equality.html
