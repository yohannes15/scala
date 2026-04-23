package learning.effect

import cats.effect.IO
import munit.CatsEffectSuite

/** MUnit + cats-effect: tests return `IO` — no `unsafeRunSync` in tests. 
 * Run with: `sbt cats/test`
*/
class ExampleSuite extends CatsEffectSuite:

  test("make sure IO computes the right result") {
    IO.pure(1).map(_ + 2).flatMap { result =>
      IO(assertEquals(result, 3))
    }
  }
