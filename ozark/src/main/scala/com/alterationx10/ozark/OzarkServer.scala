package com.alterationx10.ozark

import zio.*
import sttp.tapir.*
import sttp.tapir.server.*

import scala.compiletime.*
import scala.deriving.*
import scala.quoted.*
import zio.http.Server

@Routed
case class OtherController() derives Controller, AutoLayer {

  val healthRoute: ServerEndpoint[Any, Task] =
    endpoint
      .in("healthz")
      .serverLogicSuccess(_ => ZIO.unit)

}

@Routed
case class AController(thing: String) derives Controller, AutoLayer {

  val somethingElse: String = "not a route"

  val healthRoute: ServerEndpoint[Any, Task] =
    endpoint
      .in("health")
      .out(stringBody)
      .serverLogicSuccess(_ => ZIO.succeed(s"$thing"))

}

case class AppRouter(a: AController) derives Router, AutoLayer

trait OzarkServer[R] extends ZIOAppDefault {}

object Example extends OzarkServer[AppRouter] {

  val program: ZIO[AController & Server, Nothing, ExitCode] =
    summon[Router[AppRouter]].program

  val deps: ZLayer[Any, Throwable, AController & Server] =
    ZLayer.make[AController & Server](
      Server.default,
      AutoLayer.layer[AController],
      ZLayer.succeed("hello macro")
    )

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] =
    program.provide(deps)

}
