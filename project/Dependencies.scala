import sbt._

object Dependencies {

  object Versions {
    val zio = "2.0.2"
    val zioConfig = "3.0.1"
    val zioJson = "0.3.0-RC10"
    val zioLogging = "2.0.1"
    val tapir = "1.2.1"
    val sttp = "3.7.2"
  }

  val server: Seq[ModuleID] = Seq(
    "dev.zio" %% "zio-metrics-connectors" % Versions.zio,
    "com.softwaremill.sttp.tapir" %% "tapir-zio" % Versions.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % Versions.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-sttp-client" % Versions.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-json-zio" % Versions.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % Versions.tapir
  )

}