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
 */
object RefExample extends IOApp.Simple:

  def run: IO[Unit] =
    for
      // Creation: Must be created inside an effect (IO)
      counter <- Ref.of[IO, Int](0)

      // Parallel updates: 1000 fibers incrementing the same counter
      _ <- (1 to 1000).toList.parTraverse(_ => counter.update(_ + 1))

      // Accessing: .get retrieves the current value
      finalValue <- counter.get
      _ <- IO.println(s"Counter reached: $finalValue") // Should be exactly 1000

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
