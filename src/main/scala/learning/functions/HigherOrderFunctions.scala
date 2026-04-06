package learning.functions

/************************************************************
************************************************************
Higher-Order Functions (HOFs)
--------------------------------------------------------------------
A higher-order function (HOF) is often defined as a function that:
    (a) takes other functions as input parameters, or
    (b) returns a function as a result.

In Scala, HOFs are possible because functions are first-class values.
This applies to both methods and functions due to Scala's eta-expansion.

-------------------------------------------------------------------
Understanding `filter`'s Scaladoc
-------------------------------------------------------------------
Here's the filter definition in the List[A] class:

  `def filter(p: A => Boolean): List[A]`
  // uses the predicate p to create and return the List[A]

This states that filter is a method that takes a function parameter named p.
By convention, p stands for a predicate: a function that takes one or more
arguments and returns a Boolean value, which is either true or false.

Returns a List[A], where A is the type held in the list; if you call filter
on a List[Int], A is the type Int.

`p: A => Boolean`
    -> the function must take a value of type A and return a Boolean

So if your list is a List[Int], you can replace the type parameter A with Int,
and read that signature like this:
    `p: Int => Boolean`

Because isEven has this type — it transforms an input Int into a Boolean —
it can be used with filter.

-------------------------------------------------------------------
Writing methods that take function parameters
-------------------------------------------------------------------
- f is the name of the function input parameter.
- The type signature of f specifies the type of the functions this method will accept.
- The () portion of f's signature (on the left side of the => symbol): f takes no input parameters.
- The Unit portion of the signature (on the right side of the => symbol): f should not return a meaningful result.
- In the body of sayHello (on the right side of the = symbol),
  the f() statement invokes the function that was passed in.
************************************************************
************************************************************/

def sayHello(f: () => Unit): Unit = f()

/*
Now that we've defined sayHello, let's create a function to match f's signature so we can test it.
The following function takes no input parameters and returns nothing, so it matches f's type signature:
*/

def helloJoe(): Unit     = println("Hello, Joe")
def bonjourJulien(): Unit = println("Bonjour, Julien")

def hofExample() =
    // sayHello can take any function that matches f's signature
    sayHello(helloJoe)       // prints "Hello, Joe"
    sayHello(bonjourJulien)  // prints "Bonjour, Julien"

/*
-------------------------------------------------------------------
General syntax for defining function input parameters in HOFs
-------------------------------------------------------------------
Because functional programming is like creating and combining a series of algebraic equations,
it's common to think about types a lot when designing functions and applications.
You might say that you "think in types."

    `variableName: (parameterTypes ...) => returnType`

To demonstrate more type signature examples, here's a function that takes
a String parameter and returns an Int:

    `f: String => Int`

Examples: stringLength, checkSum

    `f: (Int, Int) => Int`

Examples: any function that takes two input Ints and returns an Int:
    1. def add(a: Int, b: Int): Int      = a + b
    2. def subtract(a: Int, b: Int): Int = a - b
    3. def multiply(a: Int, b: Int): Int = a * b

-------------------------------------------------------------------
Taking a function parameter along with other parameters
-------------------------------------------------------------------
For HOFs to be really useful, they also need some data to work on. For a class like List,
its map method already has data to work on: the data in the List. But for a standalone HOF
that doesn't have its own data, it should also accept data as other input parameters.

For instance, here's a method named executeNTimes that has two input parameters: a function and an Int:
*/

def executeNTimes(f: () => Unit, n: Int): Unit =
    for i <- 1 to n do f()

def helloWorld(): Unit = println("Hello, World!")

def hofExample2() =
    // executes helloWorld three times
    executeNTimes(helloWorld, 3)

/*
Your methods can continue to get as complicated as necessary.
For example, this method takes a function of type (Int, Int) => Int, along with two input parameters.

Because the sum and multiply methods match that type signature,
they can be passed into executeAndPrint along with two Int values:
*/

def executeAndPrint(f: (Int, Int) => Int, i: Int, j: Int): Unit =
    println(f(i, j))

def sum(x: Int, y: Int)      = x + y
def multiply(x: Int, y: Int) = x * y

def hofExample3() =
    executeAndPrint(sum, 3, 11)      // prints 14
    executeAndPrint(multiply, 3, 9)  // prints 27

/*
A great thing about learning Scala's function type signatures is that the syntax you use
to define function input parameters is the same syntax you use to write function literals.

    type signature:   (Int, Int) => Int
    input parameters: (a, b)
    body:             a + b
*/

// function that calculates the sum of two integers — type matches f in executeAndPrint
val f: (Int, Int) => Int = (a, b) => a + b

/************************************************************
************************************************************
Writing your own map method
---------------------------
Imagine for a moment that the List class doesn't have its own map method.
A good first step when creating functions is to accurately state the problem.
Focusing only on a List[Int], you state:

    => I want to write a `map` method that can be used to apply a function to
       each element in a List[Int] that it's given, returning the transformed
       elements as a new list.

Steps:

1. You want to accept a function as a parameter. That function should
   transform an Int into some type A:

    `def map(f: (Int) => A)`

2. The syntax for using a type parameter requires declaring it in square
   brackets [] before the parameter list:

    `def map[A](f: (Int) => A)`

3. map should also accept a List[Int]:

    `def map[A](f: (Int) => A, xs: List[Int])`

4. map returns a transformed List containing elements of type A:

    `def map[A](f: (Int) => A, xs: List[Int]): List[A] = ???`

5. Apply body — applies the function to every element to produce a new list:

    def map[A](f: (Int) => A, xs: List[Int]): List[A] =
        for x <- xs yield f(x)

6. As a bonus, notice the for expression doesn't depend on the inner type
   being Int. Replace Int in the type signature with type B:

    def map[A, B](f: (B) => A, xs: List[B]): List[A] =
        for x <- xs yield f(x)
************************************************************
************************************************************/

def map[A, B](f: (B) => A, xs: List[B]): List[A] =
    for x <- xs yield f(x)

def timesTwo(i: Int): Int  = i * 2
def strlen(s: String): Int = s.length

def customMapMethodExample() =
    println(map(timesTwo, List(1, 2, 3)))          // List(2, 4, 6)
    println(map(strlen, List("a", "bb", "ccc")))   // List(1, 2, 3)
