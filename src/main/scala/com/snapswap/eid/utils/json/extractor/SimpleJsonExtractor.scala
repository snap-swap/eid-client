package com.snapswap.eid.utils.json.extractor

import errors.{JsonExtractingError, JsonUnmarshallingError}
import spray.json._

import scala.annotation.tailrec
import scala.reflect.ClassTag
import scala.util.control.NonFatal


trait SimpleJsonExtractor {
  @tailrec
  private def find(json: Option[JsValue], name: String): Option[JsValue] = json match {
    case None =>
      None
    case Some(JsNull) =>
      None
    case Some(JsArray(j)) =>
      find(j.headOption, name)
    case Some(JsObject(f)) =>
      find(f, name)
    case j@Some(_) =>
      j
  }

  private def get(json: Option[JsValue], name: String): JsValue =
    find(json, name).getOrElse(throw JsonExtractingError(name, json))

  private def find(jMap: Map[String, JsValue], name: String): Option[JsValue] = jMap.get(name) match {
    case Some(JsNull) =>
      None
    case result@Some(_) =>
      result
    case None if jMap.isEmpty =>
      None
    case _ =>
      val obj = jMap.filter {
        case (_, JsObject(f)) if f.nonEmpty =>
          true
        case _ =>
          false
      }
      find(obj.values.toList, name)
  }

  @tailrec
  private def find(jList: List[JsValue], name: String): Option[JsValue] = jList match {
    case j :: jl =>
      find(Some(j), name) match {
        case Some(JsNull) =>
          None
        case result@Some(_) =>
          result
        case None =>
          find(jl, name)
      }
    case _ =>
      None
  }


  implicit class OptJsValueLifter(json: Option[JsValue]) {
    def ~>(name: String): Option[JsValue] =
      find(json, name)

    def ->(name: String): JsValue =
      get(json, name)

    def to[T: ClassTag](implicit formatter: JsonReader[T]): Option[T] =
      json.map { j => j.to[T] }
  }

  implicit class JsValueLifter(json: JsValue) {
    def ~>(name: String): Option[JsValue] =
      find(Some(json), name)

    def ->(name: String): JsValue =
      get(Some(json), name)

    def to[T: ClassTag](implicit formatter: JsonReader[T]): T =
      try {
        json.convertTo[T]
      } catch {
        case ex: JsonUnmarshallingError[_] =>
          throw ex
        case NonFatal(ex) =>
          throw JsonUnmarshallingError[T](json, ex)
      }
  }

}

object SimpleJsonExtractor extends SimpleJsonExtractor