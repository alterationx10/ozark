package com.alterationx10.ozark

import scala.compiletime.*
import scala.deriving.*
import scala.quoted.*

import zio.*

object MacroHelpers {

  type R[Args] =
    Args match {
      case EmptyTuple      => Any
      case h *: EmptyTuple => h
      case h *: tail       => h & R[tail]
    }

  inline def summonServices[T <: Tuple]: List[URIO[?, ?]] = {
    inline erasedValue[T] match {
      case _: EmptyTuple => Nil
      case _: (t *: ts)  =>
        ZIO.service[t] :: summonServices[ts]
    }
  }
}
