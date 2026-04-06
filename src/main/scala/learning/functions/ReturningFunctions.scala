package learning.functions

/************************************************************
************************************************************
Creating a Method That Returns a Function
-----------------------------------------------------------
Writing a method that returns a function is similar to everything you've seen.
For example, imagine that you want to write a greet method that returns a function.

Once again we start with a problem statement:

    `I want to create a greet method that returns a function. That function will take
     a string parameter and print it using println. To simplify this first example,
     greet won't take any input parameters; it will just build a function and return it.`

Steps:

1. It is a method:

    `def greet()`

2. It will return a function that (a) takes a String parameter and (b) prints it:

    `def greet(): String => Unit = ???`

3. Now you just need a method body:

    def greet(): String => Unit =
        (name: String) => println(s"Hello, $name")

4. We can also pass in a greeting to make it more useful:

    def greet(theGreeting: String): String => Unit =
        (name: String) => println(s"$theGreeting, $name")

* Notice that returning a function from a method is no different than
  returning a String or Int value.
************************************************************
************************************************************/

def greet(theGreeting: String): String => Unit =
    (name: String) => println(s"$theGreeting, $name")

val sayHello2: String => Unit = greet("Hello")  // type is String => Unit
val sayCiao = greet("Ciao")
val sayHola = greet("Hola")

def methodReturningFuncExample() =
    sayHello2("Joe")      // Hello, Joe
    sayCiao("Isabella")   // Ciao, Isabella
    sayHola("Carlos")     // Hola, Carlos

/*
A More Real-World Example
---------------------------------------------------------
Very useful when your method returns one of many possible functions, like a factory that
returns custom-built functions.

Imagine you want to write a method that returns functions that greet people in different
languages — limited to English or French — depending on a parameter that's passed into the method.

Steps:

1. You want to create a method that:
    (a) takes a "desired language" as an input
    (b) returns a function as its result

2. Because that function prints a string that it's given, you know it has type String => Unit.

3. Because you know the possible functions you'll return take a string and print it,
   you can write two anonymous functions for the English and French languages.
*/

def createGreetingFunction(desiredLanguage: String): String => Unit =
    val englishGreeting = (name: String) => println(s"Hello, $name")
    val frenchGreeting  = (name: String) => println(s"Bonjour, $name")
    desiredLanguage match
        case "english" => englishGreeting
        case "french"  => frenchGreeting

def methodReturningFuncExample2() =
    val greetInFrench  = createGreetingFunction("french")
    greetInFrench("Jonathan")   // Bonjour, Jonathan
    val greetInEnglish = createGreetingFunction("english")
    greetInEnglish("Joe")       // Hello, Joe

/*************************************************************
*****************SUMMARY*************************************
*************************************************************

This was a long chapter, so let's review the key points covered.

A higher-order function (HOF) is often defined as a function that takes
other functions as input parameters or returns a function as its value.

In Scala this is possible because functions are first-class values.

Moving through the sections, first you saw:
    - You can write anonymous functions as small code fragments
    - You can pass them into the dozens of HOFs on the collections classes (map, filter, etc.)
    - With these small code fragments and powerful HOFs, you create a lot of functionality
      with just a little code

After looking at anonymous functions and HOFs, you saw:
    - Function variables are simply anonymous functions that have been bound to a variable

After seeing how to be a consumer of HOFs, you then saw how to be a creator of HOFs:
    - How to write methods that take functions as input parameters
    - How to return a function from a method

A beneficial side effect of this chapter is that you saw many examples of how to declare
type signatures for functions. The same syntax is used to define function parameters,
anonymous functions, and function variables — and it also makes it easier to read the
Scaladoc for higher-order functions like map, filter, and others.

*************************************************************/
