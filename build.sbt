name := "eid-client"

version := "0.1"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  val akkaV = "2.4.18"
  val akkaHttpV = "10.0.7"

  Seq(
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % "test"
  )
}