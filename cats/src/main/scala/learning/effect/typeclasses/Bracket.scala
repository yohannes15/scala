package learning.effect.typeclasses

/** Bracket is an extension of MonadError exposing the bracket operation, a
  * generalized abstracted pattern of safe resource acquisition and release in
  * the face of errors or interruption.
  *
  * Important note, throwing in `release` function is undefined since the
  * behaviour is left to the concrete implementations (ex cats-effect
  * Bracket[IO], Monix Bracket[Task] or ZIO)
  *
  * Mental model in cats-effect 2 vs 3, and why this file still matters — the
  * *pattern* is unchanged (acquire, use, release, failures, and where supported
  * cancellation).
  *
  * In cats-effect 3 the library exposes that under **`MonadCancel`** and
  * `bracket` / `bracketCase` (etc.) on `F`, not a public type class literally
  * called `Bracket` the way older docs did. You learn the shape here; in real
  * code you use `MonadCancel` (often `MonadCancel[F, Throwable]` /
  * `MonadCancelThrow` in signatures) and concrete `IO` / `Resource` rather than
  * `import cats.effect.Bracket` on CE3.
  *
  * Pairs with `learning.effect.datatypes.Resource`: same discipline, but
  * `Resource` is the composable data structure; `bracket` is the primitive
  * guarantee underneath. Official CE3 `Resource` guide:
  * https://typelevel.org/cats-effect/docs/std/resource
  */

import cats.MonadError
import cats.effect.IO
sealed trait ExitCase[+E]

trait Bracket[F[_], E] extends MonadError[F, E]:

  def bracketCase[A, B](acquire: F[A])(use: A => F[B])(release: (
      A,
      ExitCase[E]
  ) => F[Unit]): F[B]

  // simpler version, doesn't distingush between exit conditions
  def bracket[A, B](acquire: F[A])(use: A => F[B])(release: A => F[Unit]): F[B]

/**   - The shape of `bracket` is the FP version of "try a thing, then always
  *     clean up": `acquire` runs first, then `use` runs with the acquired
  *     value. The `release` action runs when that scope ends, including when
  *     `use` fails with `E` (because `F` is also a `MonadError`) or when the
  *     effect supports cancellation. That is why `bracketCase` takes
  *     `ExitCase[E]`: the releaser can branch on "finished normally", "failed
  *     with an error of type E", or "canceled" — exact constructors depend on
  *     the real library; they are not filled in in this file on purpose.
  *   - A typical default implementation in real code sets `bracket` to call
  *     `bracketCase` and ignores the exit case in the simple
  *     `release: A => F[Unit]`, e.g. by passing `(a, _) => release0(a)`.
  *   - In **cats-effect 3** you will mostly see the idea under
  *     **`MonadCancel`** (not a separate `Bracket` type class name at the use
  *     site). In application code, `IO` and other `F`s expose
  *     `acquire.bracket(use)(release)` (or the `bracketFull` / `bracketCase`
  *     variants when you need the exit case).
  *     `learning.effect.datatypes.Resource` in this repo is the next step: many
  *     resources composed from a single underlying `bracket` pattern.
  */
object BracketIOExample extends cats.effect.IOApp.Simple:

  def run: IO[Unit] =
    program

  // Concrete effect: IO's bracket is the library implementation of the same idea as this file's
  // abstract Bracket. Run with: sbt "cats/runMain learning.effect.typeclasses.BracketIOExample"
  val program: IO[Unit] =
    IO.println("acquire (e.g. open a handle)").bracket { _ =>
      IO.println(
        "use: happy path (and errors/cancel are still followed by release)"
      )
    } { _ =>
      IO.println(
        "release: must not assume the same exit reason as `bracketCase` would expose"
      )
    }
