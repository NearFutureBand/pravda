package pravda.cli.docs

import akka.actor.ActorSystem
import cats.implicits._
import pravda.cli.languages.impl.IoLanguageImpl
import pravda.cli.programs
import pravda.cmdopt.CommandLine.{HelpNeeded, Ok, ParseError}
import pravda.cmdopt.printers.ConsolePrinter

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.sys.process.stderr

object GenDocs extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  lazy val io = new IoLanguageImpl()
  lazy val genDocs = new programs.GenDocs(io)

  lazy val consolePrinter = new ConsolePrinter[GenDocsConfig]

  val eventuallyExitCode = GenDocsArgsParser.parse(args.toList, GenDocsConfig()) match {
    case Ok(config: GenDocsConfig) => genDocs(config).map(_ => 0)
    case HelpNeeded(cli) =>
      Future {
        print(consolePrinter.printCL(GenDocsArgsParser))
        0
      }
    case ParseError(msg) =>
      Future {
        stderr.println(msg)
        stderr.print(consolePrinter.printCL(GenDocsArgsParser))
        1 // every non zero exit code says about error
      }
  }

  val exitCode = Await.result(
    awaitable = for (exitCode <- eventuallyExitCode; _ <- system.terminate()) yield exitCode,
    atMost = 5.minutes
  )

  sys.exit(exitCode)
}
