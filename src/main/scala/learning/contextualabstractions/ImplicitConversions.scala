package learning.contextualabstractions


/************************************************************************
Implicit Conversions
---------------------
- Powerful scala feature that allows user to supply an argument
  of one type as if it were another type, to avoid boilerplate.
- Note that in Scala 2, implicit conversions were also used to 
  provide additional members to closed classes

        Scala2 Implicit Classes 
        https://docs.scala-lang.org/overviews/core/implicit-classes.html
  
In Scala 3, we recommend to address this use-case by defining `extension`
methods instead of implicit conversions (although the standard library
still relies on implicit conversions for historical reasons).

Consider a method `findUserById` that takes a param of type Long. In 
scala, it is possible to call the method with an arg of type `Int` instead
of the expected type Long, because the argument will be implicity converted
into the type Long.

This code does not fail to compile with an error like “type mismatch: 
expected Long, found Int” because there is an implicit conversion that 
converts the argument id to a value of type Long.
************************************************************************/
case class User(name: String)

def findUserById(id: Long): Option[User] =
    id match
        case x if x >= 1 => Some(User("test"))
        case _ => None

def implictConversionExample() = 
    val id: Int = 42
    println(findUserById(id)) // Some(User("test"))
    val id2: Int = 0
    println(findUserById(id2)) // None

/* 
Defining an Implicit Conversion
-------------------------------------------------------
In Scala 2, an implicit conversion from type S to type T is defined 
by 
- an implicit class T that takes a single constructor parameter of type S, 
  an implicit value of function type S => T, or 
- an implicit method convertible to a value of that type.

See the section “Beware the Power of Implicit Conversions” below 
for an explanation of the clause import scala.language.implicitConversions
at the beginning.
*/

// Scala 2 
import scala.language.implicitConversions
// defines an implicit conversion from Int to Long
implicit def int2long(x: Int): Long = x.toLong

/*
In Scala 3, an implicit conversion from type `S` to type `T` is defined
by a `given` instance of type `scala.Conversion[S, T]`. For compatibility
with Scala 2, it can also be defined by an implicit method.
*/

// Scala 3
// Option 1
given int2long: Conversion[Int, Long] with
  def apply(x: Int): Long = x.toLong

// Option 2: anonymous
// given Conversion[Int, Long] with
//   def apply(x: Int): Long = x.toLong

// Option 3: Using an alias, this can be expressed more concisely.
// given Conversion[Long, Int] = (x: Long) => x.toInt

/***************************************************************
Implicit conversions are applied in two situations:
-------------------------------------------------------
1. If an expression e is of type S, and S does not conform to the 
   expression’s expected type T. a conversion `c` is searched for, 
   which is applicable  to e and whose result type conforms to T. 
   In our example above, when we pass  the argument id of type Int 
   to the method findUserById, the implicit conversion int2long(id) 
   is inserted.
2. In a selection `e.m` with `e` of type `S`, if the selector `m` 
   does not denote a member of `S` (to support Scala-2-style 
   extension methods). a conversion `c` is searched for, which is
   applicable to e and whose result contains a member named `m`.
   
   An example is to compare two strings "foo" < "bar". In this case, 
   String has no member <, so the implicit conversion below is
   inserted. `scala.Predef` is automatically imported into all
   Scala programs.
            -> `Predef.augmentString("foo") < "bar"` 
            
How Are Implicit Conversions Brought Into Scope?
-------------------------------------------------------
When the compiler searches for applicable conversions:

    first, it looks into the current lexical scope
        - implicit conversions defined in the current scope or the outer scopes
        - imported implicit conversions
        - implicit conversions imported by a wildcard import (Scala 2 only)
    then, it looks into the companion objects associated with the argument type 
    S or the expected type T. The companion objects associated with a type X are:
        - the companion object X itself
        - the companion objects associated with any of X’s inherited types
        - the companion objects associated with any type argument in X
        - if X is an inner class, the outer objects in which it is embedded

For instance, consider an implicit conversion `fromStringToUser` defined in 
an object Conversions. The following imports would bring the conversion into
scope

    import Conversions.fromStringToUser
    import Conversions._

In the introductory example, the conversion from Int to Long does not require
an import because it is defined in the object Int, which is the companion 
object of the type Int.

Further reading: 
    Where does Scala look for implicits? (on Stackoverflow).
    https://stackoverflow.com/a/5598107

*/
// Scala 2

// import scala.language.implicitConversions

// object Conversions {
//   implicit def fromStringToUser(name: String): User = User(name)
// }

// Scala 3
object Conversions:
  given fromStringToUser: Conversion[String, User] = 
    (name: String) => User(name)

/*
Beware the Power of Implicit Conversions
-------------------------------------------------------
Because implicit conversions can have pitfalls if used indiscriminately
the compiler warns in two situations:

    - when compiling a Scala 2 style implicit conversion definition.
    - at the call site where a given instance of scala.Conversion is 
      inserted as a conversion.

To turn off the warnings take either of these actions:

    - Import `scala.language.implicitConversions` into the scope of the
      implicit conversion definition
    - Invoke the compiler with -language:implicitConversions
*/
