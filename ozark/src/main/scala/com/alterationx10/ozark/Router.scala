package com.alterationx10.ozark

import scala.compiletime.*
import scala.deriving.*
import scala.quoted.*
import zio.*
import com.alterationx10.ozark.Controller
import com.alterationx10.ozark.MacroHelpers.R
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zio.http.Server

trait Router[R] {

  inline def program(using
      p: Mirror.ProductOf[R]
  ): ZIO[MacroHelpers.R[p.MirroredElemTypes] & Server, Nothing, ExitCode] = {

    val app = for {
      routes <- ZIO
                  .foreach(Router.summonRoutes[p.MirroredElemTypes])(
                    _.asInstanceOf[ZIO[MacroHelpers.R[
                      p.MirroredElemTypes
                    ], Nothing, List[ServerEndpoint[Any, Task]]]]
                  )
                  .map(_.flatten)
      _      <- Server.serve(
                  ZioHttpInterpreter().toHttp(routes)
                )
    } yield ExitCode.success

    app

  }

}

object Router {

  inline def summonRoutes[T <: Tuple]
      : List[ZIO[?, Nothing, List[ServerEndpoint[Any, Task]]]] = {
    inline erasedValue[T] match {
      case _: EmptyTuple => Nil
      case _: (t *: ts)  => {
        summonInline[Controller[t]].routesZIO :: summonRoutes[ts]
      }
    }
  }

  inline given derived[A <: Product](using m: Mirror.Of[A]): Router[A] = {
    inline m match {
      case _: Mirror.SumOf[A]     =>
        error("Auto derivation is not supported for Sum types")
      case p: Mirror.ProductOf[A] => {
        new Router[A] {}
      }
    }
  }

}
