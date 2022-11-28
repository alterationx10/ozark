package com.alterationx10.ozark

import scala.compiletime.*
import scala.deriving.*
import scala.quoted.*

import zio.*

object MacroHelpers {

  type IType[T <: Tuple] = Tuple.Fold[T, Any, [x, y] =>> x & y]
  type UType[T <: Tuple] = Tuple.Fold[T, Any, [x, y] =>> x | y]

  inline def summonServices[T <: Tuple]: List[URIO[?, ?]] = {
    inline erasedValue[T] match {
      case _: EmptyTuple => Nil
      case _: (t *: ts)  =>
        ZIO.service[t] :: summonServices[ts]
    }
  }
}
