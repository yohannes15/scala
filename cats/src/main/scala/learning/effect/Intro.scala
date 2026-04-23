package learning.effect

import cats.effect.IOApp
import cats.effect.IO
import scala.concurrent.duration._


/** To create a new Cats Effect app
 * ----------------------------------
 * Applications written in this style have full access to timers, 
 * multithreading, and all of the bells and whistles that you would
 * expect from a full application. 
 */
object HelloWorld extends IOApp.Simple:
  val run = IO.println("Hello, World")

/**
  * Here's a very silly version of FizzBuzz which runs four concurrent lightweight
  * threads, or fibers, one of which counts up an Int value once per second, while
  * the others poll that value for changes and print in response.
  * 
  * We will learn more about constructs like `start` and `*>` in later pages, but 
  * for now notice how easy it is to compose together concurrent programs based on
  * simple building blocks. Additionally, note the reuse of `wait` and `poll` program. 
  * 
  * Because we're describing our program as a value (an `IO`), we can reuse that value
  * as part of many different programs, and it will continue to behave the same 
  * regardless of this duplication.
  */
object StupidFizzBuzz extends IOApp.Simple:
  val run =
    for {
      ctr <- IO.ref(0)

      wait = IO.sleep(1.second)
      poll = wait *> ctr.get

      _ <- poll.flatMap(IO.println(_)).foreverM.start
      _ <- poll.map(_ % 3 == 0).ifM(IO.println("fizz"), IO.unit).foreverM.start
      _ <- poll.map(_ % 5 == 0).ifM(IO.println("buzz"), IO.unit).foreverM.start

      _ <- (wait *> ctr.update(_ + 1)).foreverM.void
    } yield ()

/**
  * Use the REPL to understand IO / effects a little easier in a simpler context.
  * The unsafeRunSync() function is not meant to be used within a normal application. 
  * As the name suggests, its implementation is unsafe in several ways, but it is 
  * very useful for REPL-based experimentation and sometimes useful for testing.
  */

def scalaReplExampleToCopyAndPlayWith() =
  import cats.effect.unsafe.implicits._

  /* PART 1 */
  val program = IO.println("Hello")
  //   val program: cats.effect.IO[Unit] = Blocking(
  //    hint = Blocking,
  //    thunk = cats.effect.std.ConsoleCompanionCrossPlatform$SyncConsole$$Lambda$7208/0x000000d8022c0870@25080f9a,
  //    event = cats.effect.tracing.TracingEvent$StackTrace
  //  )
  program.unsafeRunSync()
  // HELLO


  /* PART 2 */
  lazy val loop: IO[Unit] = IO.println("loop until cancel..") >> IO.sleep(4.seconds) >> loop
  val cancel = loop.unsafeRunCancelable() // type cancel() in the REPL to stop it
  /* 
  unsafeRunCancelable() 
  -----------------------------
  is another variant to launch a task, that allows a long-running task to be cancelled.
  This can be useful to clean up long or infinite tasks that have been spawned from the REPL.

  import cats.effect.unsafe.implicits._
  import cats.effect.IO

  lazy val loop: IO[Unit] = IO.println("loop until cancel..") >> IO.sleep(2.seconds) >> loop
  val cancel = loop.unsafeRunCancelable()

  unsafeRunCancelable starts the loop task running, but also emits an invocable handle value
  which when invoked (ie cancel()) cancels the loop. This can be useful when running in 
  SBT with the console command, where by default terminating the REPL doesn't terminate 
  the process, and thus the task will remain executing in the background.
  */


/******************************* TESTING ************************************
 * The easiest way to write unit tests which use Cats Effect is with MUnit:

      libraryDependencies += "org.typelevel" %% "munit-cats-effect" % "2.2.0" % Test

 * With this dependency, you can now write unit tests which directly return 
 * IO programs without being forced to run them using one of the unsafe functions.
 * tests  run more efficiently (since MUnit can run them in parallel).
 * 
 * MUnit + cats-effect tests live under `cats/src/test/scala/` 
 * (e.g. [[ExampleSuite]]). 
 * 
*/
