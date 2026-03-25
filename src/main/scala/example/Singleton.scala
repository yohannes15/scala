package example

// In Scala, the object keyword creates a Singleton object. Put another way, 
// an object defines a class that has exactly one instance.

// Use cases

/////////////////////////////////
//// 1) UTILITY METHODS /////////
/////////////////////////////////

// because obj is a singleton, its methods can be accessed like static methods

object StringUtils:
    def isNullOrEmpty(s: String): Boolean = s == null || s.trim().isEmpty()
    def leftTrim(s: String): String = s.replaceAll("^\\s+", "")
    def rightTrim(s: String): String = s.replaceAll("\\s+$", "")


def singeltonExample(): Unit =
    val x = null
    println(s"$x is ${if StringUtils.isNullOrEmpty(x) then "Empty" else "Not Empty"}")

    val y = "Yohan"
    println(s"$y is ${if StringUtils.isNullOrEmpty(y) then "Empty" else "Not Empty"}")

///////////////////////////////////
//// 2) Companion Objects /////////
///////////////////////////////////

/* 
 Companion objects and companion classes:

 - A companion object is an `object` with the same name as a `class` in the same file.
 - The class and its companion object can access each other's private members.
 - Use a companion object to hold factory methods, constants, or utilities that are
   related to the class but don't belong to any particular instance.

 This example shows:
 1) `Circle` (a class) calling `calculateArea` which is defined as a private method
    in the companion `object Circle`.
 2) An `import Circle.*` inside the class which brings the companion's members into
    the class scope so they can be referenced without qualifying with `Circle.`.
    (The import is optional here because companions already have access to each
     other's private members; the import simply allows unqualified use.)
*/

import scala.math.*

// The class represents an instance of a circle with a radius.
// It uses the companion's private calculateArea method to compute the area.
class Circle(radius: Double):
    // Bring companion members into scope so we can call `calculateArea` without
    // qualifying it as `Circle.calculateArea`. This is a convenience, not a
    // requirement for companion access.
    import Circle.*
    def area: Double = calculateArea(radius)

// Companion object for `Circle`.
// Holds helper/utility code related to Circle instances. Its `calculateArea`
// is private to the companion pair but accessible from the `Circle` class.
object Circle:
    // Private helper used by the class above. Marked private because it's an
    // implementation detail that shouldn't be exposed outside the companion pair.
    private def calculateArea(radius: Double): Double =
        Pi * pow(radius, 2.0)

def companionExample(): Unit =
    val circle = Circle(5.0)
    circle.area

/////////////////////////////////////
//// 3) Modules from Traits /////////
/////////////////////////////////////

// Traits in Scala are like interfaces that may also provide default implementations.
// They define behavior that multiple concrete implementations can share.
//
// Define small traits that provide single responsibilities (good modular design).
trait AddService:
    def add(a: Int, b: Int) = a + b

trait MultiplyService:
    def multiply(a: Int, b: Int) = a * b

// An `object` can extend one or more traits and provide a single shared implementation.
// Because `object` is a singleton, `MathService` becomes a module exposing add/multiply.
// This is a common pattern for grouping related services or utilities.
object MathService extends AddService, MultiplyService

// Example usage: import the module's members to call them unqualified.
def modulesFromTraits(): Unit = 
    // `import MathService.*` brings add/multiply into local scope for convenience.
    import MathService.* 
    println(add(1,1))       // prints 2
    println(multiply(2,2))  // prints 4
