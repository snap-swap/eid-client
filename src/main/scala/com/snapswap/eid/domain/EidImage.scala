package com.snapswap.eid.domain

import akka.http.scaladsl.model.{MediaType, MediaTypes}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.snapswap.eid.utils.base64._

case class EidImage(data: Source[ByteString, Any], classifier: String, typ: MediaType)

object EidImage {
  def front(dataBase64: String): EidImage =
    EidImage(base64StrToBytes(dataBase64), "front", MediaTypes.`image/jpeg`)

  def back(dataBase64: String): EidImage =
    EidImage(base64StrToBytes(dataBase64), "back", MediaTypes.`image/jpeg`)

  def face(dataBase64: String): EidImage =
    EidImage(base64StrToBytes(dataBase64), "face", MediaTypes.`image/jpeg`)
}
