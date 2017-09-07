package com.snapswap.eid.utils.json.extractor.errors


import spray.json.JsValue

import scala.reflect.{ClassTag, classTag}
import scala.util.control.NoStackTrace


case class JsonExtractingError(name: String, json: Option[JsValue]) extends NoStackTrace {
  private val jsn: String = json.map { j =>
    s" from: ${JsonAsStringFormatter.trimmedBlock(j, 1000)}"
  }.getOrElse(", EMPTY json was given")

  override def getMessage: String =
    s"""\nCan't extract node "$name"$jsn"""
}

case class JsonUnmarshallingError[C: ClassTag](json: JsValue, details: Option[String]) extends NoStackTrace {
  private val className = classTag[C].runtimeClass.getSimpleName

  override def getMessage: String =
    s"\nCan't unmarshall into $className ${details.getOrElse("")}${JsonAsStringFormatter.trimmedBlock(json, 1000)}"
}

object JsonUnmarshallingError {
  def apply[C: ClassTag](json: JsValue): JsonUnmarshallingError[C] =
    new JsonUnmarshallingError[C](json, None)

  def apply[C: ClassTag](json: JsValue, ex: Throwable): JsonUnmarshallingError[C] =
    new JsonUnmarshallingError[C](json, Some(s"(caused by ${ex.getClass.getSimpleName}: ${ex.getMessage})"))
}

private[errors] object JsonAsStringFormatter {
  def trimmedBlock(json: JsValue, trimTo: Int): String = {
    val str = json.prettyPrint
    val trimmed =
      if (str.length <= trimTo)
        str
      else
        s"${str.take(trimTo)}\n<... MORE JSON ...>"

    s"""\n${"-" * 100}\n$trimmed\n${"-" * 100}\n"""
  }
}