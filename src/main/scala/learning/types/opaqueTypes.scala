package learning.types

// Opaque Types

// `Opaque type` aliases provide type abstraction without any overhead.
// In Scala 2, similar result can be achieved using value classes

/* 
Abstraction Overhead
- Lets assume we want to define a module that offers arithmetic on numbers
  which are represented by their logarithm. This can be useful to improve
  precision when the numerical values involved tend to be very large, or
  close to zero
- Its important to distinguish "regular" double values from numbers stored
  as their logarithm. We introduce Logarithm class
- While the class Logarithm offers a nice abstraction for Double values 
  that are stored in this particular logarithmic form, it imposes severe 
  performance overhead: For every single mathematical operation, we need 
  to extract the underlying value and then wrap it again in a new instance of Logarithm.
 */

class LogarithmUgly(protected val underlying: Double):
  def toDouble: Double = math.exp(underlying)
  def + (that: LogarithmUgly): LogarithmUgly = 
    // here we use the apply method on the companion
    LogarithmUgly(this.toDouble + that.toDouble)
  def * (that: LogarithmUgly): LogarithmUgly = 
    new LogarithmUgly(this.underlying + that.underlying)

object LogarithmUgly:
  def apply(d: Double): LogarithmUgly = new LogarithmUgly(math.log(d))

def opaqueTypescala2abstractionOverhead() = 
  val a = LogarithmUgly(2.0)
  val b = LogarithmUgly(3.0)

  println((a * b).toDouble) // 6.0
  println((a + b).toDouble) // 4.999

/* 
Lets us consider another approach to implement the same library. This time instead
of defining Logarithm as a class, we define it using a `type alias`. First we
define an abstract interface of our module
 */

trait Logarithms:

  type Logarithm

  // operations on Logarithm
  def add(x: Logarithm, y: Logarithm): Logarithm
  def mul(x: Logarithm, y: Logarithm): Logarithm

  // functions to convert between Double and Logarithm
  def make(d: Double): Logarithm
  def extract(x: Logarithm): Double

  // extension methods to use `add` and `mul` as "methods" on Logarithm
  extension (x: Logarithm)
    def toDouble: Double = extract(x)
    def + (y: Logarithm): Logarithm = add(x, y)
    def * (y: Logarithm): Logarithm = mul(x, y)

// Now, implement this abstract interface by saying type Logarithm is equal to Double:
object LogarithmsImpl extends Logarithms:

  // allows us to implement the various methods.
  type Logarithm = Double

  // operations on Logarithm
 
  def add(x: Logarithm, y: Logarithm): Logarithm = 
    make(x.toDouble + y.toDouble)
  def mul(x: Logarithm, y: Logarithm): Logarithm = 
    x + y

  // functions to convert between Double and Logarithm
  def make(d: Double): Logarithm = 
    math.log(d)
  def extract(x: Logarithm): Double = 
    math.exp(x)

/* 
Above has Leaky Abstractions
--------------------------
However, this abstraction is slightly leaky. We have to make sure to only ever 
program against the abstract interface Logarithms and never directly use LogarithmsImpl. 

Directly using LogarithmsImpl would make the equality `Logarithm = Double` visible 
for the user, who might accidentally use a `Double` where a logarithmic double is expected.
Look at `leakyAbstractionscala2Example` below.

Having to separate the module into an abstract interface and implementation can be useful,
but is also a lot of effort, just to hide the implementation detail of Logarithm. 
Programming against the abstract module Logarithms can be very tedious and often requires
 the use of advanced features like path-dependent types, as in the following example:

def someComputation(L: Logarithms)(init: L.Logarithm): L.Logarithm = ...

Boxing Overhead

Type abstractions, such as type Logarithm erase to their bound (which is Any in our case). 
That is, although we do not need to manually wrap and unwrap the Double value, 
there will be still some boxing overhead related to boxing the primitive type Double..
 */

def leakyAbstractionscala2Example() = 
    import LogarithmsImpl.*
    val l: Logarithm = make(1.0)
    val d: Double = l // type checks AND leaks the equality!
    println(s"${d == l} leaky abstraction")

/**********************************************************************
***********************************************************************
Opaque Types
----------------------------------
- `Opaque type` aliases provide type abstraction without any overhead.
- Instead of manually splitting our `Logarithms` component into an
  abstract part and into a concrete implementation, we simply use
  `opaque` types in Scala 3 to achieve a similar effect
- The fact that Logarithm is the same as Double is only known in the
  scope where Logarithm is defined -> `Logarithms`. The type equality
  Logarithm = Double can be used to implement the methods 
  (like * and toDouble).
- However, outside of the module the type `Logarithm` is completely 
  encapsulated, or “opaque.” To users of Logarithm it is not possible
  to discover that Logarithm is actually implemented as a Double
- Even though we abstracted over Logarithm, the abstraction comes for 
  free: Since there is only one implementation, at runtime there will 
  be no boxing overhead for primitive types like Double.
- They integrate very well with the Extension methods
**********************************************************************
**********************************************************************/

object Logarithms:
    /*
    `opaque` means: inside this object, Logarithm IS a Double (we can use all Double ops).
    Outside this object, Logarithm is an entirely separate type — callers cannot see
    or exploit the Double underneath. No boxing overhead because there is no wrapper.
    */
    opaque type Logarithm = Double

    /* --- What is `apply`? ---
    In Scala, `apply` is a special method name. When you write:
    
        SomeObject(args)
    
    the compiler desugars it to:
    
        SomeObject.apply(args)
    
    So `object Logarithm` with a `def apply(d: Double)` means that writing
    `Logarithm(2.0)` is exactly the same as `Logarithm.apply(2.0)`.
    This is the standard Scala pattern for providing a constructor-like syntax
    on an object (companion object factory method).
    
    Why is it defined here (inside `Logarithms`) rather than at the top level?
    Because this is the only place where `Logarithm = Double` is visible.
    apply needs to take a raw Double and produce a Logarithm — that conversion
    (math.log returns Double btw) is only expressible where the type equality is in scope.
    */
    object Logarithm:
        // Takes a regular Double, stores its natural log as the Logarithm value.
        // math.log(2.0) ≈ 0.693 — that raw Double is what gets stored.
        def apply(d: Double): Logarithm = math.log(d)

    // Extension methods add operations onto Logarithm values outside their definition.
    // Inside here, Logarithm is still known to be Double, so math.exp(x) works.
    extension (x: Logarithm)
        // math.exp reverses math.log: exp(log(d)) == d
        def toDouble: Double = math.exp(x)
        // Addition in log-space: exp(a) + exp(b), then re-wrap with log via apply
        def + (y: Logarithm): Logarithm = Logarithm(math.exp(x) + math.exp(y))
        // Multiplication in log-space: log(a*b) == log(a) + log(b), so just add the raw Doubles
        def * (y: Logarithm): Logarithm = x + y


def opaqueTypeExample() =
    import Logarithms.*

    // Logarithm(2.0) desugars to Logarithm.apply(2.0)
    // apply calls math.log(2.0) ≈ 0.693 and returns that as a Logarithm
    val log2 = Logarithm(2.0)  // internally stores math.log(2.0) ≈ 0.693
    val log3 = Logarithm(3.0)  // internally stores math.log(3.0) ≈ 1.099

    // * uses the rule log(a*b) = log(a) + log(b): adds the raw Doubles (0.693 + 1.099 = 1.792)
    // toDouble calls math.exp(1.792) ≈ 6.0
    println((log2 * log3).toDouble) // 6.0

    // + converts back: exp(0.693) + exp(1.099) = 2.0 + 3.0 = 5.0, then re-wraps with log
    // toDouble calls math.exp(log(5.0)) ≈ 5.0
    println((log2 + log3).toDouble) // 4.999... (floating point rounding of 5.0)

    // Outside Logarithms, the compiler sees Logarithm and Double as different types.
    // val d: Double = log2  // ERROR: Found Logarithm, required Double
    // This is the whole point of opaque types — the abstraction is enforced by the type system.
