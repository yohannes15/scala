package example

import java.io.StringWriter
import java.io.PrintWriter

/* 
In Scala 2, methods can be defined inside classes, traits, objects, case classes, and case objects. But it gets better: 
In Scala 3 they can also be defined outside any of those constructs; we say that they are “top-level” definitions, 
since they are not nested in another definition. In short, methods can now be defined anywhere.

def methodName(param1: Type1, param2: Type2): ReturnType =
    body goes here

*/

def sum(a: Int, b: Int): Int = a + b
def concatenate(s1: String, s2: String): String = s1 + s2

def getStackTraceAsString(t: Throwable): String =
  val sw = StringWriter()
  t.printStackTrace(new PrintWriter(sw))
  sw.toString

// default values

def makeConnection(url: String, timeout: Int = 5000): Unit = 
    println(s"url=$url, timeout=$timeout")

// extension keyword declares that youre about to define one or more extension methods on the parameter
// thats put in parentheses

extension (i: Int)
    def makeStringWithZeros(extraZeros: Int): String = i.toString() + ("0" * extraZeros)


def testExtension(): Unit = 
    println(1.makeStringWithZeros(5))
    println(10.makeStringWithZeros(5))
