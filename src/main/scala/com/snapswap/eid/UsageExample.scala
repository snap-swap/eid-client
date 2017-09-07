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

  val client = new EidRetrievalAkkaHttpClient(
    token = "",
    "etrust-sandbox.electronicid.eu",
    system.log
  )

  val videoId = ""

  try {
    val result = Await.result(client.obtainVideoInfo(videoId), Duration.Inf)
    println(s"\n\n$result\n\n")
  } finally {
    system.terminate()
  }
}
