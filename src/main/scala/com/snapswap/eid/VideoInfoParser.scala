package com.snapswap.eid

import com.snapswap.eid.domain.{EidDocument, EidRequestParams, EidVideoVerificationResult}
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
      minSimilarityLevel = ((json -> "request") ~> "minSimilarityLevel").toNonEmpty[String]
    )

    val doc = json -> "document"
    val subj = doc -> "subject"
    val frontImageBase64 = (doc ~> "front").toNonEmpty[String]
    val scanImageBase64 = (doc ~> "scan").toNonEmpty[String]
    require(frontImageBase64.isDefined || scanImageBase64.isDefined, "Either 'front' or 'scan' field must contain Base64-encoded JPEG photo of the document")
    val frontBase64: String = frontImageBase64.orElse(scanImageBase64).get

    val idDocument = EidDocument.apply(
      docType = (doc -> "type").to[String],
      issuer = (doc -> "issuer").to[String],
      expiresAt = (doc ~> "expiryDate").toNonEmpty[String],
      primaryName = (subj -> "primaryName").to[String],
      secondaryName = (subj -> "secondaryName").to[String],
      birthDate = (subj -> "birthDate").to[String],
      sex = (subj ~> "sex").toNonEmpty[String],
      nationality = (subj ~> "nationality").toNonEmpty[String],
      personalNumber = (subj ~> "personalNumber").toNonEmpty[String],
      documentNumber = (doc ~> "documentNumber").toNonEmpty[String],
      passportNumber = (doc ~> "passportNumber").toNonEmpty[String],
      frontBase64 = frontBase64,
      backBase64 = (doc ~> "back").toNonEmpty[String]
    )

    EidVideoVerificationResult(
      id = (json -> "id").to[String],
      process = (json -> "process").to[String],
      status = (json -> "status").to[String],
      tenantId = (json ~> "tenantId").toNonEmpty[String],
      request = requestParams,
      completedAt = (json ~> "completion" ~> "date").to[Long],
      document = idDocument,
      faceSimilarityLevel = (json -> "biometrics" -> "face" -> "similarityLevel").to[String],
      faceBase64 = (json -> "biometrics" -> "face" -> "image").to[String]
    )
  }
}

