package com.alterationx10.ozark

import zio.*
import sttp.tapir.*
import sttp.tapir.server.*

import scala.compiletime.*
import scala.deriving.*
import scala.quoted.*
import zio.http.Server
import scala.deriving.Mirror.ProductOf
import com.alterationx10.ozark.MacroHelpers.R

trait OzarkServer[R] extends ZIOAppDefault {

  inline def program(using
      p: Mirror.ProductOf[R]
  ): ZIO[MacroHelpers.R[p.MirroredElemTypes] & Server, Nothing, ExitCode] = {
    summonInline[Router[R]].program
  }

}
