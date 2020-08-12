package test.app

import cats.effect._
import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import DBDriver.XA

object Main extends IOApp {
  def run(args: List[String]) = args match {
    case Nil => SetupServer.stream[IO].compile.drain.as(ExitCode.Success)
    case _ =>
      (args(0), args(1)) match {
        case ("dev", "initialize") => DB.initialize(DBDriver.development).sequence.as(ExitCode.Success)
        case ("prod", "initialize") => DB.initialize(DBDriver.production).sequence.as(ExitCode.Success)
        case (_, _) => SetupServer.stream[IO].compile.drain.as(ExitCode.Success)
      }
  }
}
