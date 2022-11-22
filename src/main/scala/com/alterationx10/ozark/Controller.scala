package com.alterationx10.ozark

import zio.*
import sttp.tapir.*
import sttp.tapir.server.*

import scala.compiletime.*
import scala.deriving.*
import scala.quoted.*

trait Controller[A] {
  def autoLayer[R, E, B >: A]: ZLayer[R, E, B]
  extension (a: A) {
    def routes: List[ServerEndpoint[Any, Task]]
  }
}

object Controller {

  private inline def summonServices[T <: Tuple]: List[URIO[?, ?]] = {
    inline erasedValue[T] match {
      case _: EmptyTuple => Nil
      case _: (t *: ts)  =>
        ZIO.service[t] :: summonServices[ts]
    }
  }

  private inline def gatherRoutes[A](a: A): List[ServerEndpoint[Any, Task]] = ${
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
      summonServices[m.MirroredElemTypes]

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
          override def autoLayer[R, E, B >: A]: ZLayer[R, E, B] = ZLayer {
            flattened
              .asInstanceOf[ZIO[R, E, List[Any]]]
              .map(deps => p.fromProduct(Tuple.fromArray(deps.toArray)))
          }
          extension (a: A) {
            override def routes: List[ServerEndpoint[Any, Task]]  =
              gatherRoutes[A](a)
          }
        }
      }
    }
  }
}
