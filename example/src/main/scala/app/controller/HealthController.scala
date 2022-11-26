package app.controller

import com.alterationx10.ozark.Controller
import sttp.tapir.*
import sttp.tapir.server.*
import zio.*

final case class HealthController() derives Controller {

  val healthRoute: ServerEndpoint[Any, Task] =
    endpoint
      .in("healthz")
      .serverLogicSuccess(_ => ZIO.unit)

}
