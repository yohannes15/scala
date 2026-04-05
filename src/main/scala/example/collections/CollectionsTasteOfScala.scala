package example.collections

// There are both immutable and mutable collections.
// This file shows common operations on immutable `List`. Lists are persistent:
// operations return a new List rather than mutating the original.

////////////////////////////
////////// LIST ////////////
////////////////////////////

// List in Scala is an immutable, linked-list class.
// Create a simple list (immutable).

def listCollection(): Unit =
    val simpleList = List(1, 2, 3)

    // Range methods — convenient ways to build lists from ranges.
    val rangeToFive = (1 to 5).toList             // List(1, 2, 3, 4, 5)
    val rangeByTwo  = (1 to 10 by 2).toList       // List(1, 3, 5, 7, 9)
    val untilFive    = (1 until 5).toList          // List(1, 2, 3, 4)
    val range1to5    = List.range(1, 5)            // List(1, 2, 3, 4)
    val rangeStep3   = List.range(1, 10, 3)        // List(1, 4, 7)

    // List Methods — none of these mutate `sampleList`; they return new Lists.
    val sampleList = List(10, 20, 30, 40, 10)    // example list

    sampleList.drop(2)           // List(30, 40, 10)     // drops first 2 elements
    sampleList.dropWhile(_ < 25) // List(30, 40, 10)     // drops while condition true
    sampleList.filter(_ < 25)    // List(10, 20, 10)     // keeps elements matching predicate
    sampleList.slice(2, 4)       // List(30, 40)         // elements in index range [2,4)
    sampleList.tail              // List(20, 30, 40, 10) // all but the head
    sampleList.take(3)           // List(10, 20, 30)     // first 3 elements
    sampleList.takeWhile(_ < 30) // List(10, 20)         // take while predicate true

    // flatten — collapse nested lists into a single list
    val nested = List(List(1,2), List(3,4))
    nested.flatten               // List(1, 2, 3, 4)

    // map, flatMap — transform and flatten/transform respectively
    val nums = List("one", "two")
    nums.map(_.toUpperCase)      // List("ONE", "TWO")
    nums.flatMap(_.toUpperCase)  // List('O', 'N', 'E', 'T', 'W', 'O')

    // Reductions and folds
    val firstTen = (1 to 10).toList
    firstTen.reduceLeft(_ + _)   // 55
    firstTen.foldLeft(100)(_ + _)// 155 (100 is a "seed" / initial accumulator)

/////////////////////////////////////////////////////
///////////////// Reductions & Folds //////////////////
/////////////////////////////////////////////////////

// Provide a shared demo function showing reduce/fold steps.
def foldReduceDemo(): Unit =
  val nums = List(1, 2, 3, 4)
  println(s"nums: $nums")

  println("\nreduceLeft steps:")
  val red = nums.reduceLeft { (a, b) =>
    println(s"$a + $b = ${a + b}")
    a + b
  }
  println(s"reduceLeft result: $red")

  println("\nfoldLeft steps (seed = 0):")
  val fld = nums.foldLeft(0) { (acc, x) =>
    println(s"acc=$acc + x=$x -> ${acc + x}")
    acc + x
  }
  println(s"foldLeft result: $fld")

  println("\nfoldLeft with different accumulator type (sum of string lengths):")
  val words = List("a", "bb", "ccc")
  val lenSum = words.foldLeft(0) { (acc, s) =>
    println(s"acc=$acc + len(${s})=${s.length} -> ${acc + s.length}")
    acc + s.length
  }
  println(s"length sum result: $lenSum")

  println("\nempty reduceOption (safe alternative to reduce on empty lists):")
  val empty = List.empty[Int]
  println(s"empty.reduceOption(_ + _): ${empty.reduceOption(_ + _)}")


//////////////////////////////////
/////////////// TUPLES ///////////
//////////////////////////////////

// Tuple is a type that lets you easily put a collection of different types in the same container
// nice for times when you want to put a collection of heterogeneous types in a little collection-like structure.
case class Persona(name: String)

def tupleExample(): Unit =
    val t = (11, "Eleven", Persona("Eleven"))
    println(t(0))
    println(t(1))
    println(t(2))

    val (num, str, person) = t
    
