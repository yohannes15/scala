package learning.effect.datatypes

import cats.effect.{IO, Ref, IOApp}
import cats.syntax.all.*

/**
 * Ref[IO, A] is a purely functional mutable reference.
 * It provides thread-safe, atomic operations on a value in a concurrent environment.
 * 
 * Think of it as a "Safe Box":
 * 1. Only one fiber can open and update the box at a time.
 * 2. It prevents "Race Conditions" where two threads overwrite each other.
 * 3. It is mandatory when using `parTraverse` or other concurrent operations to share state.
 * 
 * --- Notes from Typelevel Docs ---
 * - Implementation: A purely functional wrapper over `java.util.concurrent.atomic.AtomicReference`.
 * - Constraint: It is non-blocking and lightweight.
 * - Warning: NEVER use Ref to store mutable data structures (like a mutable.Map). 
 *   It relies on object reference equality for its atomic updates.
 * - Initialization: A Ref is always initialized with a value (unlike Deferred).
 * 
 * --- Abstract Definition ---
 * abstract class Ref[F[_], A] {
 *   def get: F[A]
 *   def set(a: A): F[Unit]
 *   def update(f: A => A): F[Unit]
 *   def modify[B](f: A => (A, B)): F[B]
 *   def updateAndGet(f: A => A): F[A]
 *   def getAndUpdate(f: A => A): F[A]
 * }
 */
object RefExample extends IOApp.Simple:

  def run: IO[Unit] =
    for
      // Creation: Must be created inside an effect (IO)
      counter <- Ref.of[IO, Int](0)

      // Parallel updates: 1000 fibers incrementing the same counter
      _ <- (1 to 1000).toList.parTraverse(_ => counter.update(_ + 1))

      // .updateAndGet: Update the value and return the NEW value in one atomic step
      newValue <- counter.updateAndGet(_ + 1)
      _ <- IO.println(s"Counter reached: $newValue")

      // .modify: The most powerful method. 
      // Update the state AND return a different value (e.g. return a boolean if successful)
      wasEven <- counter.modify { current =>
        val next = current + 1
        (next, current % 2 == 0) // Returns (NewState, ResultValue)
      }
      _ <- IO.println(s"Was the previous value even? $wasEven")

      // Map Aggregation: Useful for collecting statistics
      stats <- Ref.of[IO, Map[String, Int]](Map.empty)
      
      // Merge maps using |+| (Semigroup combine)
      _ <- stats.update(_ |+| Map("errors" -> 5, "warnings" -> 2))
      _ <- stats.update(_ |+| Map("errors" -> 3, "info" -> 10))
      
      finalStats <- stats.get
      _ <- IO.println(s"Final Stats: $finalStats") 
      // Result: Map("errors" -> 8, "warnings" -> 2, "info" -> 10)
    yield ()

/**
 * Key Methods:
 * - .get: Read the current value.
 * - .set: Replace the value completely.
 * - .update: Apply a function to the current value (e.g. counter.update(_ + 1)).
 * - .modify: Update the value AND return a result (e.g. state change + response).
 */
