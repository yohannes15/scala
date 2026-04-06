package learning.functions

/************************************************************
************************************************************
Function Variables
--------------------------------
An anonymous function (function literal) can be assigned to a variable
to create a function variable.

    val variableName = (parameter: Type) => body

The REPL shows the inferred type:
    val double: Int => Int = ...
meaning it takes a single Int and returns an Int.

Once you have a function variable you can treat it like any other value
— pass it to methods, store it in collections, etc.
************************************************************
************************************************************/

val doubleFunc = (i: Int) => i * 2
val triple     = (i: Int) => i * 3

def functionVariableExample() =
    val x = doubleFunc(2)   // 4

    // pass a function variable into map
    val doubled = List(1, 2, 3).map(doubleFunc)  // List(2, 4, 6)

    // a List whose elements are functions of type Int => Int
    val functionList: List[Int => Int] = List(doubleFunc, triple)

    // a Map from String keys to function values of type Int => Int
    val functionMap: Map[String, Int => Int] = Map(
        "2x" -> doubleFunc,
        "3x" -> triple
    )

    println(x)            // 4
    println(doubled)      // List(2, 4, 6)
    println(functionList) // List(<function1>, <function1>)
    println(functionMap)  // Map(2x -> <function1>, 3x -> <function1>)
