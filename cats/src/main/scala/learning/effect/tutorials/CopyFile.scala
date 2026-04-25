package learning.effect.tutorials

import java.io.File
import cats.effect.IO
import cats.effect.IOApp

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
  */
object CopyFile extends IOApp.Simple:
  def run: IO[Unit] = ???

  /** copies file from origin to destination. When run, all side-effects will be
    * actually executed and the IO instance will return the bytes copied in a
    * Long (note that IO is parameterized by the return type)
    */
  def copy(origin: File, destination: File): IO[Long] = ???
