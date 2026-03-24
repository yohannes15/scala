// When writing code in an OOP style, your two main tools for data encapsulation are traits and classes.

package example

// Traits

// Scala traits can be used as simple interfaces, but they can also contain abstract and concrete methods and fields,
// and they can have parameters, just like classes. They provide a great way for you to organize behaviors into small,
// modular units. Later, when you want to create concrete implementations of attributes and behaviors, classes and
// objects can extend traits, mixing in as many traits as needed to achieve the desired behavior.
trait Speaker:
  def speak(): String // no body = abstract; subclasses must implement

trait TailWagger:
  def startTail(): Unit = println(
    "tail is wagging"
  ) // Unit = like void, no useful return
  def stopTail(): Unit = println("tail is stopped")

trait Runner:
  def startRunning(): Unit = println("I'm running")
  def stopRunning(): Unit = print("Stopped running")

// Given those traits, here’s a Dog class that extends all of those traits while providing a behavior
// for the abstract speak method:
// "extends A, B, C" = mix in multiple traits (unlike Java, you can mix many)

class Dog(name: String) extends Speaker, TailWagger, Runner:
  def speak(): String = s"$name Woofed!"

class Cat(name: String) extends Speaker, TailWagger, Runner:
  def speak(): String = s"$name Meowed!"
  override def startRunning(): Unit = println(
    "Yeah ... I don't run"
  ) // override = replace trait's default
  override def stopRunning(): Unit = println("No need to stop")

def exampleTraitClasses() =
  val d = Dog("Rover") // "new" is optional for regular classes too
  println(d.speak())

  val c = Cat("Morris")
  println(c.speak())
  c.startRunning()
  c.stopRunning()

// Scala classes are used in OOP style. Constructor params with var become mutable fields.
// (Use val for immutable, or no prefix if param is only used during construction.)

/*
class Person(var firstName: String, var lastName: String):
    def printFullName() = println(s"$firstName $lastName")
 */

/////////////////////////////////////////////////
///////// ADTs & FP DOMAIN MODELING /////////////
/////////////////////////////////////////////////

// Algebraic Data Types (ADTs) => define the data / a way of structuring data (https://rockthejvm.com/articles/algebraic-data-types-in-scala)
// Traits for functionality on the data

// ADTs provide no functionality, only data

// ADTs are commonly used in Scala. Simply put, an algebraic data type is any data that uses the Product or Sum pattern.
// They’re widely used in Scala mostly to how well they work with pattern matching and how easy it is to use them to
// Key benefit: ADTs make illegal states impossible to represent.

// ADTs allow us to construct complex data types by combining simpler ones.

////////////////////////////////////////////////
//////// SUM / ENUMERATION TYPES ///////////////
////////////////////////////////////////////////

// Sum type enumerates all the possible instances of a type; used when data can be represented with d/f choices
// XOR or exclusive OR relationship
// E.g pizza has three main attribues: Crust Size, Crust Type, Toppings
// They are concisely modeled with enumerations, which are sum types that only contain singleton values

enum CrustSize:
  case Small, Medium, Large

enum CrustType:
  case Thin, Thick, Regular

enum Topping:
  case Cheese, Pepperoni, BlackOlives, GreenOlives, Onions

import CrustSize.*

def sumTypeExample(): Unit =
  val currentCrustSize = Small

  currentCrustSize match
    case Small  => println("Small crust size")
    case Medium => println("Medium crust size")
    case Large  => println("Large crust size")

// the word sealed forces us to define all possible extensions of the trait in the same file
sealed trait Weather

// Why did we use a set of case objects? The answer is straightforward. We don’t need to have
// more than one instance of each extension of Weather. Indeed, there is nothing that distinguishes
// two instances of the Sunny type. So, we use object types that are translated by the language as idiomatic singletons.

// Moreover, using a case object instead of a simple object gives us a set of useful features,
// such as the unapply method, which lets our objects to work very smoothly with pattern matching,
// the free implementation of the methods equals, hashcode, toString, and the extension from Serializable.

case object Sunny extends Weather
case object Windy extends Weather
case object Rainy extends Weather
case object Cloudy extends Weather
case object Foggy extends Weather

// type Weather = Sunny + Windy + Rainy + Cloudy + Foggy (Sum Type)

def sumTypeExample2(w: Weather): String = w match
  case Sunny  => "Oh, it's such a beautiful sunny day :D"
  case Cloudy => "It's cloudy, but at least it's not raining :|"
  case Rainy  => "I am very sad. It's raining outside :("

// [warn] [...] match may not be exhaustive.
// [warn] It would fail on the following inputs: Foggy, Windy
// [warn]     def feeling(w: Weather): String = w match {
// [warn]                                       ^

////////////////////////////////////////////////
//////// PRODUCT TYPES /////////////////////////
////////////////////////////////////////////////

// Product Type is an ADT that only has one shape, ex Singleton object, represented in Scala by a `case object`
// or an immutable structure with accessible fields, represented by `case class`
// Associated with the AND operator

/*
A case class has func of a class and also has additional features baked in that make them useful for FP
When compiler sees the case keyword in front of a class it has these effects and benefits:
    A) constructor parameters are public val fields by default, so fields are immutable and
       accessor methods are generated for each param
    B) `unapply` method is generated, which lets you use case classes in more ways in `match` expressions
    C) `copy` method is generated in the class. allows creation of updated copies of the obj w/o changing the original obj
    D) `equals` and `hashCode` methods are generated to implement equality
    E) default `toString` method generated
 */

case class Person(name: String, vocation: String)

def productType(): Unit =
  val p = Person("Yohannes Berhane", "Engineer")

  // a good default toString method
  // Person = Person("Yohannes Berhane", "Engineer")

  // can access its fields, which are immutable
  println(p.name) // "Yohannes Berhane"
  // p.name = "Joe"         // error: can’t reassign a val field

  // when you need to make a change, use the `copy` method
  // to “update as you copy”
  val p2 = p.copy(name = "Elton John")
  println(p2) // : Person = Person(Elton John, Engineer)

// Imagine having to model a request to our forecast service
case class ForecastRequest(val latitude: Double, val longitude: Double)

/*
In the language of types, we can write the constructor as (Long, Long) => ForecastRequest. In other words,
the number of possible values of ForecastRequest is precisely the cartesian product of the possible values
for the latitude property AND all the possible values for the longitude property:

    type ForecastRequest = Long x Long (Product Type)
 */


///////////////////////////////////////////////////////////////////////
//////// HYBRID TYPES / Sum of Product types //////////////////////////
///////////////////////////////////////////////////////////////////////

sealed trait ForecastResponse // ForecastResponse is a Sum type because it is an Ok OR a Ko

case class Ok(weather: Weather) extends ForecastResponse

// The Ko type is a Product type because it has an error AND a description.
case class Ko(error: String, description: String) extends ForecastResponse

/*
val weatherReporter: Behavior[ForecastResponse] =
  Behaviors.receive { (context, message) =>
    message match {
      case Ok(weather: Weather) =>
        context.log.info(s"Today the weather is $weather")
      case Ko(e, d) =>
        context.log.info(s"I don't know what's the weather like, $d")
    }
    Behaviors.same
  }
*/

////////////////////////////////////////////////////
////////////// SUMMARY / NOTES /////////////////////
////////////////////////////////////////////////////

/* 
case classes are also known as products
sealed traits (or sealed abstract classes) are also known as coproducts
case objects and Int, Double, String (etc) are known as values
 */

// Sum type (coproduct) can only be one of its values
// Weather (coproduct) = `Sunny` XOR `Windy` XOR `Rainy` XOR ...

// Product contains every type that is composed of 
// Ko product = String x String

/* 
We can define the complexity of a data type as the number of values that can exist. 
Data types should have the least amount of complexity they need to model the information they carry.
 */

// Example
// Imagine we have to model a data structure that holds mutually exclusive configurations. 
// For the sake of simplicity, let this configuration be three Boolean values:

case class ProductTypeExampleConfig(a: Boolean, b: Boolean, c: Boolean)
// Above product type has a complexity of 8

sealed trait SumTypeExampleConfig
case object A extends SumTypeExampleConfig
case object B extends SumTypeExampleConfig
case object C extends SumTypeExampleConfig

/* 
The Sum type Config has the same semantic as its Product type counterpart, plus it has a smaller complexity,
and it does not allow 5 invalid states to exist. Also, as we said, the lesser values a type admits, 
the easier the tests associated with it will be. Less is better :)
 */

// Improving our Ko product type to avoid invalid states and limit the number of values type can admit
// error: String, is replaced with Sum type that enumerates the possible types of available errors:
sealed trait Error
case object NotFound extends Error
case object Unauthorized extends Error
case object BadRequest extends Error
case object InternalError extends Error
// And so on...
case class ImprovedKo(error: Error, description: String) extends ForecastResponse
