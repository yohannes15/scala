package example

import scala.io.StdIn.readLine

@main
def hello(name: String): Unit =
  println(s"Scala ${name} dev container is ready.")
  helloInteractive()


def helloInteractive() =
  println("Please enter your name")
  // Learning Interactive Input
  val name = readLine()
  // Control Structures (if then else if then else)
  if name.length > 4 then
    println("You have a long name")
  else
    println("You have a short name")
  // String interpolation
  println(s"Hello, $name!")

  ////////////////////////////////////////////////
  //////////////////// Map //////////////////////
  /////////////////////////////////////////////// 
  val allowedChoices = Map(
    "m" -> "Male",
    "f" -> "Female",
    "na" -> "Prefer not to say"
  )
  println(s"What is your gender? allowed choices: ${allowedChoices.keys}")
  val gender = readLine()

  if allowedChoices.contains(gender) then
    println(s"Thanks saving $name as gender ${allowedChoices(gender)}")
  else
    println(s"Invalid choice. Re run program")
    

  /////////////////////////////////////////////////////
  //////////////////// For Loops //////////////////////
  /////////////////////////////////////////////////////
  val ints = List(1, 2, 3, 4, 5)
  for
    i <- ints
    // guarding allowed
    if i > 2
  do
    println(i)

  for
    // You can use multiple generators and guards. 
    // This loop iterates over the numbers 1 to 3, and for each number it also iterates over 
    // the characters a to c. However, it also has two guards, so the only time the print 
    // statement is called is when i has the value 2 and j is the character b:
    i <- 1 to 3
    j <- 'a' to 'c'
    if i == 2
    if j == 'b'
  do
    println(s"i = $i, j = $j")

  // yield
  // When you use the yield keyword instead of do, you create for expressions 
  // which are used to calculate and yield results.

  val doubles = for i <- ints yield i * 2
  // val doubles = for (i <- ints) yield i * 2
  // val doubles = for (i <- ints) yield (i * 2)
  // val doubles = for { i <- ints } yield (i * 2)
  println(doubles) // List(2, 4, 6, 8, 10)

  val names = List("chris", "ed", "mark")
  val capNames = for name <- names yield name.capitalize
  println(capNames) // List("Chris", "Ed", "Mark")

  val fruits = List("apple", "banana", "lime", "orange")
  val fruitsLengths = for 
    f <- fruits
    if f.length > 4
  yield
    s"$f with length ${f.length}"
  
  print(fruitsLengths) // List(apple with length 5, banana with length 6, orange with length 6)

  /////////////////////////////////////////////////////
  //////////////////// Match //..//////////////////////
  /////////////////////////////////////////////////////
