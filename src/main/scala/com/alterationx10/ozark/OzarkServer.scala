package com.alterationx10.ozark

import zio.*
import sttp.tapir.*
import sttp.tapir.server.*

case class AController(thing: String, stuff: Int) derives Controller {


  val somethingElse: String = "not a route"

  val healthRoute: ServerEndpoint[Any, Task] =
    endpoint
      .in("health")
      .serverLogicSuccess(_ => ZIO.unit)

  def someMethod: Task[Unit] = for {
    _ <- Console.printLine(s"I have ${this.routes.length} routes")
  } yield ()
}

trait OzarkServer extends ZIOAppDefault {

  final val program: ZIO[AController, Throwable, ExitCode] = for {
    aController <- ZIO.service[AController]
    _           <- aController.someMethod
  } yield ExitCode.success

  override final def run: ZIO[Environment, Throwable, ExitCode] =
    program
      .provide(
        ZLayer.succeed("42"),
        ZLayer.succeed(42),
        Controller.autoLayer[AController]
      )

}

object Example extends OzarkServer {}
