// When writing code in an OOP style, your two main tools for data encapsulation are traits and classes.

package example

// Traits

// Scala traits can be used as simple interfaces, but they can also contain abstract and concrete methods and fields, 
// and they can have parameters, just like classes. They provide a great way for you to organize behaviors into small,
// modular units. Later, when you want to create concrete implementations of attributes and behaviors, classes and
// objects can extend traits, mixing in as many traits as needed to achieve the desired behavior.
trait Speaker:
    def speak(): String  // no body = abstract; subclasses must implement

trait TailWagger:
    def startTail(): Unit = println("tail is wagging")  // Unit = like void, no useful return
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
    override def startRunning(): Unit = println("Yeah ... I don't run")  // override = replace trait's default
    override def stopRunning(): Unit = println("No need to stop")


def exampleTraitClasses() =
    val d = Dog("Rover")  // "new" is optional for regular classes too
    println(d.speak())

    val c = Cat("Morris")
    println(c.speak())
    c.startRunning()
    c.stopRunning()


// Scala classes are used in OOP style. Constructor params with var become mutable fields.
// (Use val for immutable, or no prefix if param is only used during construction.)

class Person(var firstName: String, var lastName: String):
    def printFullName() = println(s"$firstName $lastName")

/////////////////////////////////////////////////
///////// ADTs & FP DOMAIN MODELING /////////////
/////////////////////////////////////////////////

// Algebraic Data Types (ADTs) => define the data / a way of structuring data (https://rockthejvm.com/articles/algebraic-data-types-in-scala)
// Traits for functionality on the data

// ADTs are commonly used in Scala. Simply put, an algebraic data type is any data that uses the Product or Sum pattern.
// They’re widely used in Scala mostly to how well they work with pattern matching and how easy it is to use them to 
// Key benefit: ADTs make illegal states impossible to represent.

// ADTs allow us to construct complex data types by combining simpler ones.
