package capstone.mini

/** Capstone 1 — tiny CLI (see `capstone/README.md` §1).
  *
  * Replace the body with your project fake “credit band” (score + income →
  * Approved / Declined with reason). Run: `sbt "runMain capstone.mini.MiniCli"`
  */

sealed trait AppError
case class InvalidInput(msg: String) extends AppError

/** Only built through [[CreditInfo.apply]] after validation (private ctor). */
case class CreditInfo private (creditScore: Int, income: Double)

sealed trait Decision:
  def name: String
  def reason: String

case class Approved(name: String = "Approved", reason: String = "NA")
    extends Decision
case class Declined(name: String = "Declined", reason: String = "NA")
    extends Decision

object CreditInfo:
  /** Smart constructor. Inside the companion, `CreditInfo(cs, in)` would call
    * *this* `apply` again (recursive `Either`). After validation, build the
    * value with `new` so you get a plain [[CreditInfo]], not a nested `Either`.
    */
  def apply(
      creditScore: Int,
      income: Double
  ): Either[InvalidInput, CreditInfo] =
    for
      cs <- validateScore(creditScore)
      in <- validateIncome(income)
    yield new CreditInfo(cs, in)

  def validateScore(creditScore: Int): Either[InvalidInput, Int] =
    if (creditScore <= 900 && creditScore >= 250) then Right(creditScore)
    else
      Left(InvalidInput("FICO scores need to be between 250 and 900 inclusive"))

  def validateIncome(income: Double): Either[InvalidInput, Double] =
    if (income >= 0) then Right(income)
    else Left(InvalidInput("Negative income not allowed"))

  /* In `match`, you can’t write `case i < 10000` — `<` isn’t a pattern. Bind a name
   * (`case i`) and add a boolean guard: `case i if i < 10000 =>`. */
  def makeDecision(creditInfo: CreditInfo): Decision =
    if creditInfo.income < 10000 then
      Declined(reason = "Income below threshold")
    else Approved(reason = "Testing for now")

@main def MiniCli(args: String*): Unit =
  println("Input your credit_score and income in $ (separated by space or ,)")
  val creditInfo = parseCreditInfo()
  creditInfo match
    case Left(err) =>
      println(s"Got error: ${err.msg}")
    case Right(ci @ CreditInfo(credit, income)) =>
      println(s"credit score: $credit, your income: ${'$'}$income")
      val decision = CreditInfo.makeDecision(ci)
      println(s"${decision.name}: ${decision.reason}")

def parseCreditInfo(): Either[InvalidInput, CreditInfo] =
  val line = scala.io.StdIn.readLine().trim
  // Empty line → `"".split(...)` becomes `Array("")` (length 1),
  // so handle explicitly for a clearer error.
  if line.isEmpty then
    Left(InvalidInput("No input; enter credit score and income (two values)."))
  else
    val tokens: Array[String] = line.split("[,\\s]+")
    if tokens.length != 2 then
      Left(
        InvalidInput(
          s"invalid input. found ${tokens.length} inputs. Only 2 allowed"
        )
      )
    else
      val credit_score = tokens(0).toIntOption match
        case Some(value) => Right(value)
        case _           => Left(InvalidInput("Unable to parse credit_score"))

      val income = tokens(1).toDoubleOption match
        case Some(value) => Right(value)
        case _           => Left(InvalidInput("Unable to parse income"))

      for
        cs <- credit_score
        in <- income
        info <- CreditInfo(cs, in)
      yield info
