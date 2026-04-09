package capstone.mini

/** Capstone 1 — tiny CLI (see `capstone/README.md` §1).
  *
  * Replace the body with your project
  * fake “credit band” (score + income → Approved / Declined with reason).
  * Run: `sbt "runMain capstone.mini.MiniCli"`
  */
  
case class CreditInfo(creditScore: Int, income: Double)

@main def MiniCli(args: String*): Unit =
  println("Input your credit_score and income in $ (separated by space or ,)")
  val tokens = scala.io.StdIn.readLine().trim.split("[,\\s]+")
  val creditInfo = parseCreditInfo(info = tokens)
  creditInfo match
    case Some(CreditInfo(credit, income)) =>
      println(s"Your credit score $credit, your income $income")
    case None =>
      println("Could not parse credit info")
  
  
def parseCreditInfo(info: Array[String]): Option[CreditInfo] = 
  if info.length != 2 then
    println(s"invalid input. found ${info.length} inputs. Only 2 allowed")
    None
  else
    val credit_score = info(0).toIntOption.getOrElse(200)
    val income = info(1).toDoubleOption.getOrElse(0.0)
    Some(CreditInfo(creditScore=credit_score, income=income))

