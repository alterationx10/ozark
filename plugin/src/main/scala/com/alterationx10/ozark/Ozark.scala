package com.alterationx10.ozark

import dotty.tools.dotc.plugins.StandardPlugin
import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.transform.Pickler
import dotty.tools.dotc.transform.Staging
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.ast.tpd
import scala.quoted.Quotes
import dotty.tools.dotc.quoted.QuotesCache
import scala.quoted.runtime.impl.QuotesImpl
import dotty.tools.dotc.transform.FirstTransform
import dotty.tools.dotc.parsing.Parser
import dotty.tools.dotc.typer.Typer
import dotty.tools.dotc.typer.TyperPhase
import dotty.tools.dotc.transform.Erasure

class Ozark extends StandardPlugin {

  override def name: String = "OZARK"

  override def description: String = "Plugin to help generate new ozark methods"

  override def init(options: List[String]): List[PluginPhase] = List(
    new OzarkPhase()
  )

}

class OzarkPhase extends PluginPhase {

  override def phaseName: String = "OZARk"
  override val runsAfter         = Set(Pickler.name)
  override val runsBefore         = Set(Erasure.name)

  override def transformTypeDef(
      tree: tpd.TypeDef
  )(using ctx: Context): tpd.Tree = {

    implicit val fresh          = ctx.fresh
    fresh.setTree(tree)
    QuotesCache.init(fresh)
    implicit val quotes: Quotes = QuotesImpl.apply() // picks up fresh
    import quotes.reflect.*

    if (
      tree.isClassDef &&
      tree.symbol.flags.is(
        dotty.tools.dotc.core.Flags.Case
      )
    ) {

      val newSymbol: quotes.reflect.Symbol = Symbol.newMethod(
        Symbol.spliceOwner,
        "greet",
        MethodType(List("msg"))(                         // parameter list
          _ =>
            List( // types of the parameters
              TypeRepr.typeConstructorOf(classOf[String])
            ),
          _ => TypeRepr.typeConstructorOf(classOf[Unit]) // return type
        )
      )

      val classSymbol: quotes.reflect.Symbol =
        tree.tpe.asInstanceOf[quotes.reflect.TypeRef].classSymbol.get

      val greetMethodDef = DefDef(
        newSymbol,
        { case List(List(msg: Term)) =>
          Some('{ println("hello") }.asTerm.changeOwner(newSymbol))
        }
      ).changeOwner(classSymbol)

      val classDef = tree.asInstanceOf[ClassDef]
      val cd       = ClassDef.copy(classDef)(
        name = classDef.name,
        constr = classDef.constructor,
        parents = classDef.parents,
        selfOpt = classDef.self,
        body = classDef.body :+ greetMethodDef
      )

      println(s"I ran for class ${classDef.name}")
      cd.asInstanceOf[dotty.tools.dotc.ast.tpd.Tree]
    } else {
      tree
    }

  }
}
