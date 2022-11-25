package com.alterationx10.ozark

import zio.*
import sttp.tapir.*
import sttp.tapir.server.*

import scala.compiletime.*
import scala.deriving.*
import scala.quoted.*

@Routed
case class OtherController() derives Controller {

  val healthRoute: ServerEndpoint[Any, Task] =
    endpoint
      .in("healthz")
      .serverLogicSuccess(_ => ZIO.unit)

}

@Routed
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

  val stuff = Routed.gatherControllers("com.alterationx10.ozark").length

  private final val program = for {
    // routes      <- ZIO.foreach(Routed.gatherControllers("server.controllers"))(_.routesZIO)
    aController <- ZIO.service[AController]
    _           <- aController.someMethod
    _           <- Console.printLine(s"Aggregated ${stuff} controllers")
  } yield ExitCode.success

  override final def run: ZIO[Environment, Throwable, ExitCode] =
    program
      .provide(
        ZLayer.succeed("42"),
        ZLayer.succeed(42),
        Controller.autoLayer[AController]
      )

}

object Example extends OzarkServer
