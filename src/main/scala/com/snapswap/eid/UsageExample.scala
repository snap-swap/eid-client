package com.snapswap.eid

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object UsageExample extends App {
  implicit val system = ActorSystem()
  system.eventStream.setLogLevel(Logging.DebugLevel)
  implicit val ctx = system.dispatcher
  implicit val mat = ActorMaterializer()

  val client = new EidRetrievalAkkaHttpClient("")

  val result = Await.result(client.obtainVideoInfo(""), Duration.Inf)

  println(result)


}
