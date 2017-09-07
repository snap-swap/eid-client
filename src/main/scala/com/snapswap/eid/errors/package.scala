package com.snapswap.eid

import akka.http.scaladsl.model.StatusCode

import scala.util.control.NoStackTrace


package object errors {
  def exceptionDetails(ex: Throwable): String =
    s"${ex.getClass.getSimpleName}: ${ex.getMessage}"
}


trait EidException extends NoStackTrace {
  def details: String

  override def getMessage: String =
    details
}


case class EidHttpRequestError(details: String) extends EidException

case class EidHttpResponseError(statusCode: StatusCode,
                                reason: String) extends EidException {
  override def details: String =
    s"$statusCode: $reason"
}

case class InternalEidError(details: String) extends EidException

case class EidMalformedResponse(details: String) extends EidException