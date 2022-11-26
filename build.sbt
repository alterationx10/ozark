ThisBuild / scalaVersion := "3.2.1"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.alterationx10.ozark"

ThisBuild / scalacOptions ++= Seq(
  "-Yretain-trees"
)

lazy val ozark = (project in file("ozark"))
  .settings(
    name := "ozark",
    libraryDependencies ++= Dependencies.server,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    fork := true
  )

lazy val example = (project in file("example"))
  .settings(
    fork := true
  )
  .dependsOn(ozark)
