package com.alterationx10.ozark

import zio.*
import sttp.tapir.*
import sttp.tapir.server.*
import scala.annotation.StaticAnnotation
import scala.compiletime.*
import scala.deriving.Mirror
import scala.deriving.Mirror.ProductOf
import scala.reflect.ClassTag
import scala.quoted.*

case class AController() derives Controller {

  val somethingElse: String = "not a route"

  val healthRoute: ServerEndpoint[Any, Task] =
    endpoint
      .in("health")
      .serverLogicSuccess(_ => ZIO.unit)

  def someMethod: Task[Unit] = for {
    _ <- Console.printLine(s"I have ${this.routes.length} routes")
  } yield ()
}

object OzarkServer extends ZIOAppDefault {

  val program: ZIO[AController, Throwable, ExitCode] = for {
    aController <- ZIO.service[AController]
    _           <- aController.someMethod
  } yield ExitCode.success

  override def run: ZIO[Environment, Throwable, ExitCode] =
    program
      .provide(
        AController.derived$Controller.autoLayer
      )

}
