package com.alterationx10.ozark

import scala.compiletime.*
import scala.deriving.*
import scala.quoted.*

import zio.*

object MacroHelpers {

  type IType[Tple] =
    Tple match {
      case EmptyTuple      => Any
      case h *: EmptyTuple => h
      case h *: tail       => h & IType[tail]
    }

  type UType[Tple] =
    Tple match {
      case EmptyTuple      => Any
      case h *: EmptyTuple => h
      case h *: tail       => h | UType[tail]
    }

  type R[Args] = IType[Args]

  inline def summonServices[T <: Tuple]: List[URIO[?, ?]] = {
    inline erasedValue[T] match {
      case _: EmptyTuple => Nil
      case _: (t *: ts)  =>
        ZIO.service[t] :: summonServices[ts]
    }
  }
}
