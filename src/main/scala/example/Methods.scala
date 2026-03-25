package example

import java.io.StringWriter
import java.io.PrintWriter

// Scala classes, case classes, traits, enums, and objects can all contain methods.

// def methodName(param1: Type1, param2: Type2): ReturnType =
    // body goes here

def sum(a: Int, b: Int): Int = a + b
def concatenate(s1: String, s2: String): String = s1 + s2

def getStackTraceAsString(t: Throwable): String =
  val sw = StringWriter()
  t.printStackTrace(new PrintWriter(sw))
  sw.toString

// default values

def makeConnection(url: String, timeout: Int = 5000): Unit = 
    println(s"url=$url, timeout=$timeout")

// extension keyword declards that youre about to defined one or more extension methods on the parameter
// thats put in parantheses

extension (i: Int)
    def makeStringWithZeros(extraZeros: Int): String = i.toString() + ("0" * extraZeros)


def testExtension(): Unit = 
    println(1.makeStringWithZeros(5))
    println(10.makeStringWithZeros(5))
