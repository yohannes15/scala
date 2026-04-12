package capstone.receipt

import geny.Generator
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import scala.math.BigDecimal.RoundingMode

sealed trait AppError
case class LineError(msg: String = "error processing line") extends AppError
case class FileError(msg: String = "error reading file") extends AppError

enum TaxCode(val rate: BigDecimal):
  case EXEMPT extends TaxCode(BigDecimal("0.00"))
  case STANDARD extends TaxCode(BigDecimal("0.1"))
  case REDUCED extends TaxCode(BigDecimal("0.05"))

case class Summary(
  ok: Int = 0,
  bad: Int = 0,
  subtotals: Map[TaxCode, BigDecimal] =
    TaxCode.values.
    map(tc => tc -> BigDecimal("0.0")).toMap,
):
  def totalTax: BigDecimal =
    subtotals.foldLeft(BigDecimal("0.0")){
      case (acc, (tc, price)) => acc + (tc.rate * price)
    }.setScale(2, RoundingMode.HALF_UP)

  def grandTotal: BigDecimal =
    (
      subtotals.values
      .foldLeft(BigDecimal("0.0"))((acc, price) => acc + price) 
      + this.totalTax
    ).setScale(2, RoundingMode.HALF_UP)

  override def toString: String =
    "==================== Summary ==================\n" +
    (
      // Line summary
      Seq(
        ("Valid lines", ok.toString),
        ("Invalid lines", bad.toString),
      ) ++
      // Subtotal price summary
      TaxCode.values.toSeq
      .map(
        tc => (s"Subtotal $tc", subtotals(tc).toString)
      ) ++
      // Totals summary
      Seq(
        ("Total tax", totalTax.toString),
        ("Grand total", grandTotal.toString),
      )
    )
    // Pad to a general large value (good enough)
    .map((label, value) =>s"${label.padTo(25, ' ')}  ->  $value")
    .mkString("\n")


case class Receipt(description: String, taxCode: TaxCode, price: BigDecimal):
  def tax: BigDecimal = 
    price * taxCode.rate

/** Capstone 1 — boss project (see `capstone/README.md` — Boss project).
  *
  * Pass the sample ''filename'' only (e.g. `receipt-good.txt`); resolved under
  * `capstone/samples/`. Run from repo root:
  * `sbt "capstone/runMain capstone.receipt.ReceiptApp receipt-good.txt"`
  */
@main def ReceiptApp(textFile: String): Unit =
  println("================== ReceiptApp =================")
  getFileStream(textFile) match
    case Left(err) => println(s"error: $err.msg")
    // Generator is lazy: effects (println) only run when the stream is traversed — use foreach, not map alone.
    case Right(g) => 
      println(parseReceipt(g))

def parseReceipt(stream: Generator[String]): Summary =
  stream.zipWithIndex.foldLeft(Summary()) { case (acc, (line, index)) =>
    parseLine(line, index + 1) match
      case Right(receipt) =>
        acc.copy(
          ok = acc.ok + 1,
          subtotals = acc.subtotals + (receipt.taxCode -> (acc.subtotals(receipt.taxCode) + receipt.price)),
        )
      case Left(err) =>
        println(err.msg.toString)
        acc.copy(bad = acc.bad + 1)
  }

def parseLine(line: String, line_no: Int): Either[LineError, Receipt] =
  // `split` takes a regex; `|` is special — escape for a literal pipe.
  val lineTokens = line.split("\\|")
  if lineTokens.length != 3 then
    Left(
      LineError(
        s"Line: $line_no: Expected format {description}|{taxCode}|{price}., Got: `$line`"
      )
    )
  else
    val parsings = 
      for
        description <- parseDescription(lineTokens(0))
        taxCode <- parseTaxCode(lineTokens(1))
        price <- parsePrice(lineTokens(2))
      yield Receipt(description, taxCode, price)
    parsings.left.map(
      e => LineError(s"Line: $line_no: ${e.msg}")
    )

def parseTaxCode(taxCode: String): Either[LineError, TaxCode] =
  TaxCode.values
    .find(tc => tc.toString == taxCode.trim().toUpperCase)
    .toRight(LineError(s"Unable to parse tax code: Got: `$taxCode`"))

def parseDescription(description: String): Either[LineError, String] =
  val desc = description.trim
  if desc.isBlank then
    Left(LineError("description can't be blank"))
  else if desc.length > 40 then
    Left(LineError("description can't be more than 40 characters"))
  else Right(desc)

def parsePrice(price: String): Either[LineError, BigDecimal] =
  Try { BigDecimal(price.trim()) } match
    case Success(value) => 
      if value < 0 then Left(LineError(s"Price needs to be >= 0. Got $value"))
      else Right(value.setScale(2, RoundingMode.HALF_UP))
    case Failure(exception) => 
      Left(LineError(s"Unable to parse price. Got: `$price`"))

def getFileStream(textFile: String): Either[FileError, Generator[String]] =
  val samplesDir = os.pwd / "capstone" / "samples"
  // return a generator
  if !os.exists(samplesDir / textFile) then
    Left(FileError(s"Non existent file: $textFile in path $samplesDir"))
  else if !textFile.endsWith(".txt") then
    Left(
      FileError(s"File `$textFile` not accepted, only `txt` files are allowed.")
    )
  else
    Right(
      os.read.lines
        .stream(samplesDir / textFile)
        .map(_.trim)
        .dropWhile(x => x.isEmpty())
    )
