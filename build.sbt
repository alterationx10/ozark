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
//    autoCompilerPlugins                    := true,
//    addCompilerPlugin("com.alterationx10.ozark" %% "plugin" % "0.1.0-SNAPSHOT")
  )

lazy val example = (project in file("example"))
  .settings(
    fork := true
  )
  .dependsOn(ozark)

lazy val plugin = (project in file("plugin"))
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" %% "scala3-compiler" % "3.2.1"
    )
  )

//addCommandAlias(
//  "mac",
//  "plugin/publishLocal;ozark/clean;ozark/publishLocal;example/compile"
//)
