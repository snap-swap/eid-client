package com.snapswap.eid

import com.snapswap.eid.domain.{EidDocument, EidRequestParams, EidVideoVerificationResult}
import com.snapswap.eid.errors.InternalEidError
import com.snapswap.eid.utils.json.extractor.SimpleJsonExtractor
import spray.json._

object VideoInfoParser extends SimpleJsonExtractor with DefaultJsonProtocol {

  def parse(json: JsValue): EidVideoVerificationResult =
    (json ~> "error").to[String] match {
      case Some(error) =>
        val details = (json ~> "message").to[String].map(msg => s"$error: $msg").getOrElse(error)
        throw InternalEidError(details)
      case _ =>
        doParse(json)
    }


  private def doParse(json: JsValue): EidVideoVerificationResult = {
    val requestParams = EidRequestParams(
      requestedAt = (json -> "request" -> "date").to[Long],
      minSimilarityLevel = (json -> "request" -> "minSimilarityLevel").to[String]
    )

    val doc = json -> "document"
    val subj = doc -> "subject"

    val idDocument = EidDocument.apply(
      docType = (doc -> "type").to[String],
      issuer = (doc -> "issuer").to[String],
      expiresAt = (doc -> "expiryDate").to[String],
      primaryName = (subj -> "primaryName").to[String],
      secondaryName = (subj -> "secondaryName").to[String],
      birthDate = (subj -> "birthDate").to[String],
      sex = (subj ~> "sex").to[String],
      nationality = (subj ~> "nationality").to[String],
      personalNumber = (subj ~> "personalNumber").to[String],
      documentNumber = (doc -> "documentNumber").to[String]
    )

    EidVideoVerificationResult(
      id = (json -> "id").to[String],
      process = (json -> "process").to[String],
      status = (json -> "status").to[String],
      tenantId = (json -> "tenantId").to[String],
      request = requestParams,
      completedAt = (json -> "completion" -> "date").to[Long],
      document = idDocument,
      faceSimilarityLevel = (json -> "biometrics" -> "face" -> "similarityLevel").to[String]
    )
  }
}

