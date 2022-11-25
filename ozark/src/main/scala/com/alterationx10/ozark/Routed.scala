package com.alterationx10.ozark

import com.alterationx10.ozark.Controller
import scala.quoted.*
import scala.compiletime.*
import scala.annotation.StaticAnnotation
import zio.*
import sttp.tapir.server.ServerEndpoint

final class Routed extends StaticAnnotation

object Routed {

  inline def gatherControllers(
      inline packageName: String
  ) = ${ gatherControllersImpl('packageName) }

  def gatherControllersImpl(
      packageNameExpr: Expr[String]
  )(using q: Quotes) = {
    import quotes.reflect.*

    val packageName: String = packageNameExpr.valueOrAbort

    val packageSymbol: Symbol = Symbol.requiredPackage(packageName)

    val routed = packageSymbol.declarations
      .collect {
        case controller
            if controller.isClassDef && controller.hasAnnotation(
              TypeRepr.of[Routed].typeSymbol
            ) => {
          Some(controller.typeRef.asType)
        }
        case _ => None
      }
      .flatten
      .flatMap {
        case '[t] => {
          Expr.summon[Controller[t]]
        }
      }

    Expr.ofList(routed)
  }

}
