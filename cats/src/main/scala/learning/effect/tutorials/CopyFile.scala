package learning.effect.tutorials

import java.io.{
  File, FileInputStream, FileOutputStream, InputStream, OutputStream
}
import cats.syntax.all.*
import cats.effect.{IO, Resource, Sync}
import cats.effect.IOApp
import cats.effect.ExitCode

/** Copying files - basic concepts, resource handling and cancelation
  * -------------------------------------------------------------------- Our
  * goal is to create a program that copies files. We need:
  *
  * 1) a function that carries out such a task, 2) a program that can be invoked
  * from shell and uses that function.
  *
  * First of all we must code the function that copies the content from a file
  * to another file. The function takes the source and destination files as
  * parameters. But this is functional programming! So invoking the function
  * shall not copy anything, instead it will return an `IO` instance that
  * encapsulates all the side effects involved:
  *
  *   - opening/closing files
  *   - reading/writing content
  *
  * This way purity is kept. Only when that IO instance is evaluated all those
  * side-effectful actions will be run. In our implementation the IO instance
  * will return the amount of bytes copied upon execution, but this is just a
  * design decision. Of course errors can occur, but the IO instance will carry
  * the error raised.
  *
  * IOApp is a kind of 'functional' equivalent to Scala's `App`, where instead
  * of coding an effectful main method we code a pure run function. When
  * executing the class a main method defined in IOApp will call the run
  * function we have coded. Any interruption (like pressing Ctrl-c) will be
  * treated as a cancelation of the running IO.
  *
  * When coding IOApp, instead of a main function we have a run function, which
  * creates the IO instance that forms the program. In our case, our run method
  * can look like this:
  *
  * {{{
  *    sbt 'cats/runMain learning.effect.tutorials.CopyFile cats/src/main/scala/learning/effect/tutorials/origin.txt
  *    cats/src/main/scala/learning/effect/tutorials/dest.txt'
  * }}}
  */
object CopyFile extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    for
      // Heed how run args are verified. As IO implements MonadError we can
      // at any moment call to IO.raiseWhen or IO.raiseError to interrupt a
      // sequence of IO operations.
      _ <- IO.raiseWhen(
        args.length < 2
      )(new IllegalArgumentException("Need origin and destination files"))
      orig = new File(args(0))
      dest = new File(args(1))
      count <- copyPoly[IO](orig, dest)
      _ <- IO.println(
        s"$count bytes copied from ${orig.getPath} to ${dest.getPath}"
      )
    yield ExitCode.Success

  /** Opening a stream is considered a side-effectful action, so we have to
    * encapsulate those actions on their own IO instances. We can just embed the
    * actions by calling IO(action), but when dealing with input/output actions,
    * it is advised to be used IO.blocking(action) instead.This helps CE to plan
    * how to assign threads to actions.
    *
    * We will use a Resource to orderely create/use/release resources.
    */
  def inputStream(f: File): Resource[IO, FileInputStream] =
    Resource.make(IO.blocking(new FileInputStream(f))) {
      inStream => IO.blocking(inStream.close()).handleErrorWith(_ => IO.unit)
    }

  def outputStream(f: File): Resource[IO, FileOutputStream] =
    Resource.make(IO.blocking(new FileOutputStream(f))) {
      outStream => IO.blocking(outStream.close()).handleErrorWith(_ => IO.unit)
    }

  /** We want to ensure that streams are closed once we are done using them, no
    * matter what. That is why we use `Resource` in both inputStream and
    * outputStream functions, each one returning one `Resource` that
    * encapsulates the actions for opening and then closing each stream.
    *
    * `inputOuputStreams` encapsulates both resources in a single Resource
    * instance that will be available once the creation of both streams has been
    * successful, and only in that case. Resources can be combined in
    * for-comprehensions. Note also when releasing resources we must also take
    * care of any possible error during the release itself, eg with
    * .handleErrorWith. In this case we just ignore, but normally it should be
    * at least logged. Often you will see `.attempt.void` - this is used to get
    * the same `swallow and ignore errors` behavior
    */
  def inputOutputStreams(
      in: File,
      out: File
  ): Resource[IO, (InputStream, OutputStream)] =
    for
      inStream <- inputStream(in)
      outStream <- outputStream(out)
    yield (inStream, outStream)

  /** Optionally, we could have used `Resource.fromAutoCloseable` to define our
    * resources. This creates Resource instances over objects that implement the
    * java.lang.AutoCloseable interface without having to define how the
    * resource is released.
    *
    * This code is simpler but we would not have control over what would happen
    * if close operation throws an exception. Also it could be that we want to
    * be aware when closing operations are being run, for example using logs. In
    * contrast, using Resource.make allows to easily control the actions of the
    * release phase
    */
  def inputStreamFromAC(f: File): Resource[IO, FileInputStream] =
    Resource.fromAutoCloseable(IO.blocking(new FileInputStream(f)))

  /** copies file from origin to destination. When run, all side-effects will be
    * actually executed and the IO instance will return the bytes copied in a
    * Long (note that IO is parameterized by the return type)
    */
  def copy(origin: File, destination: File): IO[Long] =
    val streamResources = inputOutputStreams(origin, destination)
    streamResources.use {
      // We can use transferPoly or transfer (showcasing using IO vs Sync/Async)
      case (in, out) => transferPoly(in, out, new Array[Byte](1024 * 10), 0)
    }

  /** If you are familiar with cats-effect's `bracket` you may wonder why we use
    * `Resource` elsewhere — `Resource` is built from bracket-like guarantees.
    *
    * Bracket has three stages, each an `IO`: acquire, use, release. **Once
    * `acquire` completes successfully**, `release` runs when `use` is done,
    * whether `use` finished normally, failed, or was cancelled. So “release
    * always runs” is true **after** a successful acquire and **around** `use`;
    * it is not invoked for a resource you only half-opened inside a failing
    * `acquire`.
    *
    * Here, `acquire` is a **single** `IO` that opens both streams (`.tupled`
    * combines `inIO` and `outIO` into one effect). If the input opens but the
    * output throws **before** that combined `acquire` succeeds, the bracket
    * never reaches “acquired”: **`release` is not called**, and the input
    * stream can leak. The remedy is nested brackets (or `Resource` /
    * `flatMap`), so each stream has its own acquire/release pair. A one-shot
    * `bracket` on a tupled pair is still fine when that failure mode is
    * acceptable; for several lifetimes, `Resource` is usually clearer.
    */
  def copyBracketVersion(origin: File, destination: File): IO[Long] =
    val inIO: IO[InputStream] = IO.blocking(new FileInputStream(origin))
    val outIO: IO[OutputStream] = IO.blocking(new FileOutputStream(destination))

    (inIO, outIO) // Stage 1: Getting resources
      .tupled // From (IO[InputStream], IO[OutputStream]) to IO[(InputStream, OutputStream)]
      .bracket {
        case (in, out) =>
          transfer(
            in,
            out,
            new Array[Byte](1024 * 10),
            0L
          ) // Stage 2: Using resources (for copying data, in this case)
      } {
        case (in, out) => // Stage 3: Freeing resources
          (IO(in.close()), IO(out.close()))
            .tupled // From (IO[Unit], IO[Unit]) to IO[(Unit, Unit)]
            .void.handleErrorWith(_ => IO.unit)
      }

  /** This will do the real work of actually copying the data, once the streams
    * are obtained. When they are not needed anymore, whatever the outcome of
    * transfer (success/failure) both streams will be closed. If any of the
    * streams could not be obtained, then transfer will not be run.
    *
    * Even better, because of Resource semantics, if there is any problem
    * opening the input file then the output file will not be opened. On the
    * other hand, if there is any issue opening the output file, then the input
    * stream will be closed.
    *
    * Steps are read data from the input stream into a buffer, and the write the
    * buffer contents into output stream. At same time, loop keeps a counter of
    * the bytes transfer for return. Observe that both input and output actions
    * are created by invoking IO.blocking.
    *
    * IO, being a monad, we can sequence our new IO instances using a for-comp
    * to create another IO. The for-comp loops as long as the call to read()
    * does not return a negative value that would signal that the end of the
    * stream has reached.
    *
    * >> is a Cats operator to sequence two operations where the ouput of the
    * first is not needed by the second. it is equivalent to
    * `first.flatMap(_ => second)`. It means that after each write operation we
    * recursively all transfer again, but as IO is stack safe we are not
    * concerned about stack overflow issues. At each iteration we increase the
    * counter acc with the amount of bytes read at that iteration
    */
  def transfer(
      origin: InputStream,
      destination: OutputStream,
      buffer: Array[Byte],
      acc: Long
  ): IO[Long] =
    for
      amount <- IO.blocking(origin.read(buffer, 0, buffer.length))
      count <-
        (
          if (amount > -1) then
            (
              IO.blocking(destination.write(buffer, 0, amount)) >>
                transfer(origin, destination, buffer, acc + amount)
          )
          // end of read stream reached (by java.io.InputStream contract)
          else IO.pure(acc)
        )
    yield count

  /** Cancelation
    *
    * IO instances execution can be canceled! Cancelation is a powerful but but
    * non-trivial CE feature. It shouldn't be ignored. In CE, some IO instances
    * can be canceled (e.g. by other IO instances running concurrently) meaning
    * that their evaluation will be aborted. If programmer is careful, an
    * alternative IO task will be run under cancelation, for example to deal
    * with potential cleaning up activities.
    *
    * Resource makes dealing with cancelation an easy task. If the IO inside a
    * Resource.use is canceled, the release section of that resource is run. In
    * our example this means the input/output streams will be properly closed.
    * Also, CE does not cancel code inside `IO.blocking` instances. In the case
    * of our transfer function this means the execution would be interrupted
    * only between two calls to IO.blocking. If we want the execution of an IO
    * instance to be interrupted when canceled, without waiting for it to
    * finish, we must instantiate it using `IO.interruptible`!
    *
    * It can be argued that using IO(java.nio.file.Files.copy(...)) would get an
    * IO with the same characterstics of purity as our function, but there is a
    * difference in that our IO is safely cancelable. So the user can stop the
    * running code at any time and our code will deal with safe resource release
    * (streams closing) even under circumstances (like Ctrl-c). The same will
    * apply if the copy function is run from other modules that require its
    * functionality. If the IO returned by this function is cancelled while
    * being run, resources will be properly released.
    *
    * Polymorphic cats-effect code & Sync/Async
    * ------------------------------------------------------------------------
    * There is an important characterstic of IO that we should be aware of. IO
    * is able to suspend side-effects async thanks to the existence of an
    * instance of Async[IO]. Because Async extends Sync, IO can also suspend
    * side-effects synchronously. On top of that Async extends typeclasses such
    * as MonadCancel, Concurrent or Temporal, which bring the possibility to
    * cancel an IO instance, to run serveral IO instances concurrently, to
    * timeout an execution, to force the execution to wait (sleep), etc ...
    *
    * Could we have coded our copy file functions in terms of some F[_]: Sync
    * and F[_]: Async instead of IO? Yes! below is an example of how we would
    * define a polymorphic version of our transfer function with this apporach,
    * just by replacing any use of IO by calls to the delay and pure methods the
    * Sync[F] functions.
    *
    * Only in our main function we could set IO as the final F for our program.
    * To do so, of course, a Sync[IO] instance must be in scope, but that
    * instance is brought transparently by IOApp, so we don't need to be
    * concerned about it (falling to IO only in the run method is common).
    * Polymorphic code is less restrictive, as functions are not tied to IO but
    * are applicable to any F[] as long as there is an instance of the type
    * class required (Sync, Async ...) in scope. The type class to use depends
    * on requirements of the code
    */
  def transferPoly[F[_]: Sync](
      origin: InputStream,
      destination: OutputStream,
      buffer: Array[Byte],
      acc: Long
  ): F[Long] =
    for
      amount <- Sync[F].blocking(origin.read(buffer, 0, buffer.length))
      count <-
        (
          if (amount > -1) then
            Sync[F].blocking(destination.write(buffer, 0, amount)) >>
              transferPoly(origin, destination, buffer, acc + amount)
          // end of stream reached
          else Sync[F].pure(acc)
        )
    yield count

  // Implementing the polymorphic versions of copy functions below
  def inputStreamPoly[F[_]: Sync](f: File): Resource[F, FileInputStream] =
    Resource.make(Sync[F].blocking(new FileInputStream(f))) {
      inStream =>
        Sync[F].blocking(inStream.close()).handleErrorWith(_ => Sync[F].unit)
    }
  def outputStreamPoly[F[_]: Sync](f: File): Resource[F, FileOutputStream] =
    Resource.make(Sync[F].blocking(new FileOutputStream(f))) {
      outStream =>
        Sync[F].blocking(outStream.close()).handleErrorWith(_ => Sync[F].unit)
    }
  def inputOutputStreamsPoly[F[_]: Sync](
      in: File,
      out: File
  ): Resource[F, (InputStream, OutputStream)] =
    for
      inStream <- inputStreamPoly(in)
      outStream <- outputStreamPoly(out)
    yield (inStream, outStream)

  def copyPoly[F[_]: Sync](origin: File, destination: File): F[Long] =
    inputOutputStreamsPoly(origin, destination).use {
      case (in, out) => transferPoly(in, out, new Array[Byte](1024 * 10), 0L)
    }

/** TODO: Exercises: improving our small IO program
  *
  * To finalize we propose you some exercises that will help you to keep
  * improving your IO-kungfu:
  *
  *   1. Modify the IOApp so it shows an error and abort the execution if the
  *      origin and destination files are the same, the origin file cannot be
  *      open for reading, or the destination file cannot be opened for writing.
  *      Also, if the destination file already exists, the program should ask
  *      for confirmation before overwriting that file.
  *   2. Test safe cancelation, checking that the streams are indeed being
  *      properly closed. You can do that just by interrupting the program
  *      execution pressing Ctrl-c. To make sure you have the time to interrupt
  *      the program, introduce a delay of a few seconds in the transfer
  *      function (see IO.sleep). And to ensure that the release functionality
  *      in the Resources is run you can add some log message there (see
  *      IO.println).
  *   3. Create a new program able to copy folders. If the origin folder has
  *      subfolders, then their contents must be recursively copied too. Of
  *      course the copying must be safely cancelable at any moment.
  */
