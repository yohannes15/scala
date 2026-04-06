package learning.functions

/************************************************************
************************************************************
Eta-Expansion
--------------------------------
`map` is defined to accept a function value:
    def map[B](f: A => B): List[B]

Yet you can pass a *method* into map and it still works. This is
eta-expansion: the compiler automatically converts a method reference
into an equivalent anonymous function value.

    times10       →  (x: Int) => times10(x)
    isLessThan    →  (x: Int, y: Int) => isLessThan(x, y)

Difference between methods and functions
-------------------------------------------------------------
- A function IS an object (an instance of a FunctionN class) with
  its own methods (e.g. .apply, .andThen, .compose).
- A method is NOT a value on its own; it can only be invoked via
  method application: foo(arg1, arg2, ...).
- Eta-expansion bridges the gap by wrapping a method in a function object.

When does automatic eta-expansion happen?
-------------------------------------------------------------
Scala 2: only when the expected type is a function type.
Scala 3: anywhere a method reference appears as a value — no explicit
         `_` or `(_, _)` annotation required.

Manual eta-expansion is still valid and sometimes clearer:
    isLessThan(_, _)           // placeholder syntax
    (x, y) => isLessThan(x, y) // explicit lambda
************************************************************
************************************************************/

def times10(i: Int)                        = i * 10
def isLessThan(x: Int, y: Int): Boolean    = x < y
def isGreaterThan(x: Int, y: Int): Boolean = x > y
def isEqualTo(x: Int, y: Int): Boolean     = x == y

def etaExpansionExample() =
    // Automatic eta-expansion: times10 becomes x => times10(x)
    val res = List(1, 2, 3).map(times10)   // List(10, 20, 30)
    println(res)

    // Multiple methods eta-expanded into a List of functions
    val comparators: List[(Int, Int) => Boolean] = List(isLessThan, isGreaterThan, isEqualTo)
    val results = comparators.map(f => f(3, 5))  // List(true, false, false)
    println(results)

    // Manual eta-expansion — identical behaviour, just explicit
    val methodsA = List(isLessThan(_, _))           // placeholder syntax
    val methodsB = List((x, y) => isLessThan(x, y)) // explicit lambda

    println(methodsA.map(f => f(3, 5)))  // List(true)
    println(methodsB.map(f => f(3, 5)))  // List(true)
