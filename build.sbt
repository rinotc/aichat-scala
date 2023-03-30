ThisBuild / version := "0.1"

ThisBuild / scalaVersion := "3.2.2"

lazy val root = (project in file("."))
  .enablePlugins(PackPlugin)
  .settings(
    name := "aichat-scala",
    libraryDependencies ++= Seq(
      "io.cequence"        %% "openai-scala-client"        % "0.3.1",
      "io.cequence"        %% "openai-scala-client-stream" % "0.3.1",
      "org.wvlet.airframe" %% "airframe-launcher"          % "23.3.0",
      "ch.qos.logback"      % "logback-classic"            % "1.4.6"
    )
  )
