package app

import controller.*
import com.alterationx10.ozark.Router
import com.alterationx10.ozark.OzarkServer
import zio.*

import zio.http.Server
import com.alterationx10.ozark.AutoLayer

// We make a case class that has the "Controllers" we want to inject as dependencies
case class Routes(health: HealthController, simple: SimplerController)
    derives Router

object App extends OzarkServer[Routes] {

  val deps = ZLayer.make[Server & HealthController & SimplerController](
    Server.default,
    AutoLayer.layer[HealthController],
    AutoLayer.layer[SimplerController],
    ZLayer.succeed("Hello from ozark!")
  )

  // this.program has everything set up for the web server. WIll just need to provide the dependencies needed
  override def run: ZIO[Any, Any, Any] =
    program.provide(deps)

}
