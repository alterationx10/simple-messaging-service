Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion := "3.3.0"
ThisBuild / organization := "works.scala"
ThisBuild / scalacOptions ++= Seq()
ThisBuild / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

lazy val root = project
  .in(file("."))
  .settings(
    name := "simple-messaging-service"
  )
  .aggregate(server, cli)

lazy val server = project
  .in(file("server"))
  .settings(
    name := "sms-server",
    libraryDependencies ++= Dependencies.server,
    fork := true
  )

lazy val cli = project
  .in(file("cli"))
  .settings(
    name := "sms-cli",
    fork := true,
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-cli"      % "0.5.0",
      "dev.zio" %% "zio-http-cli" % Dependencies.Versions.zioHttp
    )
  )
  .dependsOn(server)

lazy val docs = project
  .in(file(".site-docs"))
  .dependsOn(server)
  .settings(
    mdocOut := file("./website/docs")
  )
  .enablePlugins(MdocPlugin)

addCommandAlias("fmt", "all root/scalafmtSbt root/scalafmtAll")
addCommandAlias("fmtCheck", "all root/scalafmtSbtCheck root/scalafmtCheckAll")
