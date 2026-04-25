package learning.effect.concepts

import cats.effect.{IO, IOApp}

/** Side-Effects -------------------- When running a piece of code causes
  * changes outside of just returning a value, we generally say that code "has
  * side-effects". More intuitively, code where you care:
  *
  *   - whether it runs more than once and/or
  *   - when it runs
  *
  * almost always has side-effects. The classic example of this is
  * System.out.println. The `double` function in example below takes an Int and
  * returns that same Int added to itself... and it prints "something" to
  * standard out. This is what is meant by the "side" in "side-effect":
  * something else is being done "on the side". The same thing could be said
  * about:
  *
  *   - logging
  *   - changing the value of a var
  *   - making a network call, etc...
  *
  * Critically, a side-effect is not the same thing as an effect. **********
  *
  * An effect is a description of some action, where the action may perform
  * side-effects when executed. The fact that effects are just descriptions of
  * actions is what makes them much safer and more controllable. When a piece of
  * code contains a side-effect, that action just happens. You can't make it
  * evaluate in parallel, or on a different thread pool, or on a schedule, or
  * make it retry if it fails.
  *
  * Since an effect is just a description of what actions to take, it can freely
  * change the semantics of how it eventually executes to meet the needs of your
  * specific use-case.
  *
  * In Cats Effect, code containing side-effects should always be wrapped in one
  * of the "special" constructors. In particular:
  *
  * A) Synchronous (`return`s or `throw`s)
  *   - IO(...) or IO.delay(...)
  *   - IO.blocking(...)
  *   - IO.interruptible(...)
  *   - IO.interruptibleMany(...)
  * B) Asynchronous (invokes a `callback`)
  *   - IO.async or IO.async_
  *
  * When side-effecting code is wrapped in one of these constructors, the code
  * itself still contains side-effects, but outside the lexical scope of the
  * constructor we can reason about the whole thing (e.g. including the IO(...))
  * as an effect, rather than as a side-effect.
  *
  * For example, we can wrap the System.out.println side-effecting code to
  * convert it into an effect value in example wrappingSideEffects.
  *
  * Being strict about this rule of thumb and always wrapping your
  * side-effecting logic in effect constructors unlocks all of the power and
  * composability of functional programming. This also makes it possible for
  * Cats Effect to do a more effective job of scheduling and optimizing your
  * application, since it can make more aggressive assumptions about when to
  * evaluate pieces of code in ways that better utilize CPU and cache resources.
  */

object LearningSideEffects extends IOApp.Simple:
  def run: IO[Unit] =
    println(double(3))
    wrappingSideEffects()

  def double(x: Int): Int =
    System.out.println("Hello, World. I am a side effect")
    x + x

  def wrappingSideEffects() =
    IO(System.out.println("Hello, World. I am a side effect but wrapped"))
