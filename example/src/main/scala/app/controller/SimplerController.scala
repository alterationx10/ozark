package app.controller

import com.alterationx10.ozark.Controller
import sttp.tapir.*
import sttp.tapir.server.*
import zio.*

final case class SimplerController(msg: String) derives Controller {

  val simpleRoute: ServerEndpoint[Any, Task] =
    endpoint
      .in("")
      .out(stringBody)
      .serverLogicSuccess(_ => ZIO.succeed(s"$msg"))

}
