package learning.effect.datatypes

import java.io.File
import java.nio.charset.StandardCharsets
import cats.effect.{IO, IOApp, Resource}
import cats.syntax.all.*

// Bare shape of `Resource` (educational sketch — not this file’s real API;
trait ResourceImpl[F[_], A]:
  // needs a constraint like 'MonadCancel' in real cats-effect-3
  def use[B](f: A => F[B]): F[B]

object ResourceImpl:
  def make[F[_], A](acquire: F[A])(release: A => F[Unit]): ResourceImpl[F, A] =
    ???
  def eval[F[_], A](fa: F[A]): ResourceImpl[F, A] = ???

/** Resource is a datatype in cats effect useful for allocating and releasing a
  * resource. Forms a `MonadError` on the resource type when the effect type has
  * a `Bracket` instance. The acquiring and releasing of Resource's section of
  * the tutorials provides some additional context and examples regarding
  * Resource as well.
  *
  * A common pattern is to acquire a resource (file/socket), perform some action
  * and then run a finalizer (eg closing the file), regardless of the outcome of
  * the action. Like we discussed this methodlogy called `bracket` in CE3 is
  * provided in the `MonadCancel#bracket`.
  *
  * However composing this quickly becomes unwieldy.
  *
  * {{{
  *     val concat: IO[Unit] = IO.bracket(openFile("file1")) { file1 =>
  *       IO.bracket(openFile("file2")) { file2 =>
  *         IO.bracket(openFile("file3")) { file3 =>
  *           for {
  *             bytes1 <- read(file1)
  *             bytes2 <- read(file2)
  *             _ <- write(file3, bytes1 ++ bytes2)
  *           } yield ()
  *         }(file3 => close(file3))
  *       }(file2 => close(file2))
  *     }(file1 => close(file1))
  * }}}
  *
  * and it also couples the logic to acquire the resource with the logic to use
  * the resource.
  *
  * Resource[F[_], A] is a solution to this which encapsulates the logic to
  * acquire and finalize a resource of type A and forms a Monad in A so that we
  * can construct composite resources without the deep nesting of bracket.
  *
  * The simplest way to construct a Resource is with Resource#make and the
  * simplest way to consume a resource is wtih Resource#use. Arbitrary actions
  * can also be lifted to resources with Resource#eval.
  */
trait ResourceI[F[_], A]:
  // needs a constraint like 'MonadCancel' in real cats-effect-3
  def use[B](f: A => F[B]): F[B]

object ResourceI:
  def make[F[_], A](acquire: F[A])(release: A => F[Unit]): ResourceI[F, A] =
    ???
  def eval[F[_], A](fa: F[A]): ResourceI[F, A] = ???

object LearningResource extends IOApp.Simple:
  import cats.effect.{IO, IOApp, Resource}
  import cats.syntax.all.*

  // In-memory stand-in so the concat demo runs without real paths on disk.
  private val demoContents = scala.collection.mutable.Map(
    "file1" -> "hello".getBytes(StandardCharsets.UTF_8),
    "file2" -> " world".getBytes(StandardCharsets.UTF_8),
    "file3" -> Array.emptyByteArray
  )

  def openFile(name: String): IO[File] =
    IO(new File(name))

  def close(file: File): IO[Unit] =
    IO.println(s"releasing ${file.getName}")

  /** Read bytes of an already-opened handle (the `use` side of the lifecycle).
    */
  def readBytes(file: File): IO[Array[Byte]] =
    IO(demoContents.getOrElse(file.getName, Array.emptyByteArray))

  def write(file: File, bytes: Array[Byte]): IO[Unit] =
    IO { demoContents(file.getName) = bytes }

  /** `Resource` for a path: acquire = open, release = close. Not the same as
    * `readBytes`.
    */
  def fileResource(name: String): Resource[IO, File] =
    Resource.make(openFile(name))(f => close(f))

  def concatExampleWithResource: IO[Unit] =
    val threeFiles: Resource[IO, (File, File, File)] =
      for
        in1 <- fileResource("file1")
        in2 <- fileResource("file2")
        out <- fileResource("file3")
      yield (in1, in2, out)

    threeFiles.use { case (file1, file2, file3) =>
      for
        bytes1 <- readBytes(file1)
        bytes2 <- readBytes(file2)
        _ <- write(file3, bytes1 ++ bytes2)
      yield ()
    } >> IO.println {
      s"file3 after concat: ${String(demoContents("file3"), StandardCharsets.UTF_8)}"
    }

  /** You can see the last f1.use reads files twice but only releases once while
    * others release everytime. Resource is acquired everytime use is invoked
    */
  def resourceUseSemanticsDemo: IO[Unit] =
    val f1: Resource[IO, File] = fileResource("file1")
    f1.use(_ => IO.unit) >> f1.use(_ => IO.unit) >> f1.use { f =>
      readBytes(f).void >> readBytes(f).void
    }

  def run: IO[Unit] = concatExampleWithResource >>
    resourceUseSemanticsDemo

end LearningResource

/* Resource (CE3) — short reminders
 * - Resources are released in reverse order to the acquire.
 * - acquire/release are non-interruptible; hence safe in the face of cancelation
 * - Outer `Resource` is still released if something inner fails.
 * - finalization happens as soon as the use block finishes.
 * - When `use` finishes, the handle is invalid: do not `r.use(IO.pure).flatMap(readBytes)`
 *   (file is already closed by the time readBytes is called)
 * - This means that the resource is acquired every time use is invoked. So
 *   file.use(read) >> file.use(read) opens the file twice whereas
 *   file.use { file => read(file) >> read(file) } will only open it once.
 */
