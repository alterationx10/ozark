package com.alterationx10.ozark

import scala.compiletime.*
import scala.deriving.*
import scala.quoted.*

import zio.*

trait AutoLayer[A] {

  def layer(using
      p: Mirror.ProductOf[A]
  ): ZLayer[MacroHelpers.R[p.MirroredElemTypes], Nothing, A]

}

object AutoLayer {

  def layer[A](using l: AutoLayer[A])(using
      p: Mirror.ProductOf[A]
  ) = l.layer

  inline given derived[A](using m: Mirror.Of[A]): AutoLayer[A] = {

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

        new AutoLayer[A] {

          override def layer(using
              p: Mirror.ProductOf[A]
          ): ZLayer[MacroHelpers.R[p.MirroredElemTypes], Nothing, A] = ZLayer {
            flattened
              .asInstanceOf[
                ZIO[MacroHelpers.R[p.MirroredElemTypes], Nothing, List[Any]]
              ]
              .map(deps => p.fromProduct(Tuple.fromArray(deps.toArray)))
          }

        }
      }
    }
  }

}
