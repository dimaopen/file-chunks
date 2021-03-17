name := "file-chunks"

version := "0.0.1"

scalaVersion := "2.13.4"

lazy val vAkka = "2.6.10"
lazy val vAkkaHttp = "10.2.2"
lazy val vScalaTest = "3.0.8"
lazy val vTypeConfig = "1.3.0"
lazy val vTypeLogging = "3.9.2"
lazy val vLogback = "1.2.3"
lazy val vCommonCodec = "1.15"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % vAkka,
  "com.typesafe.akka" %% "akka-stream" % vAkka,
  "com.typesafe.akka" %% "akka-http" % vAkkaHttp,
  "com.typesafe.akka" %% "akka-http-spray-json" % vAkkaHttp,
  "commons-codec" % "commons-codec" % vCommonCodec,
  "ch.qos.logback" % "logback-classic" % vLogback,
  "com.typesafe" % "config" % vTypeConfig,
  "com.typesafe.scala-logging" %% "scala-logging" % vTypeLogging,
  "org.scalatest" %% "scalatest" % vScalaTest % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % vAkka % Test,
  "com.typesafe.akka" %% "akka-testkit" % vAkka % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % vAkkaHttp % Test,
)

enablePlugins(JavaAppPackaging)

dockerExposedPorts := Seq(8080)