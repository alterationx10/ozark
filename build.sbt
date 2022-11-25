ThisBuild / scalaVersion := "3.2.1"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.alterationx10.ozark"

lazy val ozark = (project in file("ozark"))
  .settings(
    name := "ozark",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"      % "2.0.4",
      "dev.zio" %% "zio-test" % "2.0.4" % Test
    ) ++ Dependencies.server,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

lazy val example = (project in file("example"))
  .dependsOn(ozark)
