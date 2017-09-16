enablePlugins(JavaAppPackaging)

name := "eid-client"

version := "0.0.3"

organization := "com.snapswap"

scalaVersion := "2.11.8"

scalacOptions := Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xfuture",
  "-Ywarn-unused-import",
  "-encoding",
  "UTF-8")

libraryDependencies ++= {
  val akkaHttpV = "10.0.7"
  Seq(
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
    "org.scalatest" %% "scalatest" % "3.0.1" % Test,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % Test
  )
}

assemblyJarName in assembly := "eid-client.jar"
