package learning.effect.concepts

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import cats.effect.{IO, IOApp}
import cats.effect.std.Console
import cats.Monad
import cats.syntax.all.*


/** Effects
  * ----------------
  * An effect is a description of an action (or actions) that will be taken when evaluation 
  * happens. One very common sort of effect is IO.
  
  * In `effectsExample` below, `printer` and `printAndRead` are both effects, they describe
  * an action (in the case of printAndRead, actions plural). In short, such functions are
  * called "effectful". `Foo` is an effectful function. 
  * 
  * This is very different from saying that `Foo` is a function which performs effects, in
  * the same way that the `printer` effect is very distinct from actually printing. This is
  * illustrated neatly in effectfulExamples when we place printer 3 times.

  * When this code is evaluated, the text "Hello, World" is printed ZERO times, since printer is
  * just a descriptive value; it doesn't do anything on its own. Cats Effect is all about making
  * it possible to express effects (descriptions) as values.
  * 
  * Notably, `Future` cannot do this, as seen in futureIsNotReallyAnEffectPerSay example below . 
  * It will print "Future is not an effect" exactly once, meaning that printer does not represent
  * a description of an action (aka an effect), but the results of that action (which was already
  * acted upon outside of our control).
  */
object LearningEffects extends IOApp.Simple:
  def run: IO[Unit] =
    futureIsNotReallyAnEffectPerSay()
    println("************************")
    effectsExample().flatMap(name => IO.println(s"entered name: $name")) >>
    effectfulFunction[IO]("Hello, from effectfulFunction") >> IO.println("done")

  def effectsExample() = 
    val printer: IO[Unit] = IO.println("Hello, World")
    val printAndRead: IO[String] = IO.print("Enter your name: ") >> IO.readLine
    def foo(str: String): IO[String] = IO.pure("test")

    // the text "Hello, World" is printed ZERO times here, printer is an effect (descriptive value)
    // println(printer) // IO(...)
    // println(printer) // IO(...)
    // println(printer) // IO(...)

    printAndRead

  def futureIsNotReallyAnEffectPerSay() = 
    val printer: Future[Unit] = Future(println("Future is not an effect")) // prints it
    println(printer) // Future(<not completed>)

  /**
    * In advanced usage of Cats Effect, it is also common to use effect types (other than IO)
    * -----------------------------------------------------------------------------------------
    * effectfulFunction is as its name states an effectful function. 
    * The following are the effects:
        - printer is an effect 
        - printer >> printer >> printer
        - (printer >> printer >> printer).as(str)). 
        
    * The effect type here is F, which might be IO, but it also might be something more 
    * interesting! The caller of `effectfulFunction` is free to choose the effect at the
    * call site, for example by writing something like effectfulFunction[IO]("Hello, World"), 
    * which in turn would return an IO[String]. Much of the Cats Effect ecosystem's 
    * composability is achieved using this technique under the surface.
    */
  def effectfulFunction[F[_]: Monad: Console](str: String): F[String] = {
    val printer: F[Unit] = Console[F].println(str)
    (printer >> printer >> printer).as(str)
  }
