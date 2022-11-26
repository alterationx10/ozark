package com.alterationx10.ozark

import zio.*
import sttp.tapir.*
import sttp.tapir.server.*

import scala.compiletime.*
import scala.deriving.*
import scala.quoted.*

trait Controller[A] {

  extension (a: A) {
    def routes: List[ServerEndpoint[Any, Task]]
  }

  val routesZIO: ZIO[A, Nothing, List[ServerEndpoint[Any, Task]]]

  inline def layerZIO(using p: Mirror.ProductOf[A])(using
      l: AutoLayer[A]
  ): ZLayer[MacroHelpers.R[p.MirroredElemTypes], Nothing, A] =
    l.layer

}

object Controller {

  inline def gatherRoutes[A](a: A): List[ServerEndpoint[Any, Task]] = ${
    gatherRoutesImpl[A]('a)
  }

  private def gatherRoutesImpl[A: Type](a: Expr[A])(using
      Quotes
  ): Expr[List[ServerEndpoint[Any, Task]]] = {
    import quotes.reflect.*

    println(s"/**")
    val controllerRep = TypeRepr.of[A]
    println(s"* Controller: ${controllerRep.typeSymbol.name}")
    val fields        = TypeTree.of[A].symbol.declaredFields
    val fieldTypes    = fields.map(controllerRep.memberType)
    val fieldsT       = fields.zip(fieldTypes)
    println(s"* Found the following fields:")
    fieldsT.foreach { case (f, t) =>
      println(s"*\t$f: ${t.typeSymbol.name}")
    }
    // TODO this is only filtering on ServerEndpoint; needs to also filter on ServerEndpoint type arguments
    val desired       = TypeRepr.of[ServerEndpoint[_, _]]
    val filtered      = fieldsT.filter {
      case (f, t) if (t.typeSymbol.name == desired.typeSymbol.name) => true
      case _                                                        => false
    }
    println(s"* Collecting the following fields:")
    filtered.foreach { case (f, t) =>
      println(s"*\t$f: ${t.typeSymbol.name}")
    }
    val results       = filtered.map { case (f, t) =>
      Select(a.asTerm, f).asExprOf[ServerEndpoint[Any, Task]]
    }
    println(s"**/")
    Expr.ofList(results)
  }

  inline given derived[A <: Product](using m: Mirror.Of[A]): Controller[A] = {

    lazy val constructorServices: List[URIO[?, ?]] =
      MacroHelpers.summonServices[m.MirroredElemTypes]

    inline m match {
      case _: Mirror.SumOf[A]     =>
        error("Auto derivation is not supported for Sum types")
      case p: Mirror.ProductOf[A] => {

        val init: ZIO[?, Nothing, List[Any]] =
          ZIO.succeed(List.empty[Any])

        val flattened: ZIO[?, Nothing, List[Any]] =
          constructorServices
            .map(_.asInstanceOf[URIO[Any, Any]])
            .foldLeft(init)((l, z) => l.flatMap(_l => z.map(_z => _l :+ _z)))

        new Controller[A] {

          extension (a: A) {
            def routes: List[ServerEndpoint[Any, Task]] = gatherRoutes[A](a)
          }

          override val routesZIO
              : ZIO[A, Nothing, List[ServerEndpoint[Any, Task]]] =
            ZIO.service[A].map(a => a.routes)

        }
      }
    }
  }
}
