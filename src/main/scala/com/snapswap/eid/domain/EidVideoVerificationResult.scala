package com.snapswap.eid.domain

import java.time.LocalDateTime

import com.snapswap.eid.utils.datetime._

case class EidVideoVerificationResult(id: String,
                                      process: String,
                                      status: String,
                                      tenantId: String,
                                      request: EidRequestParams,
                                      completedAt: LocalDateTime,
                                      document: EidDocument,
                                      faceSimilarityLevel: String)

object EidVideoVerificationResult {
  def apply(id: String,
            process: String,
            status: String,
            tenantId: String,
            request: EidRequestParams,
            completedAt: Long,
            document: EidDocument,
            faceSimilarityLevel: String): EidVideoVerificationResult =
    new EidVideoVerificationResult(
      id, process, status, tenantId, request,
      fromMillis(completedAt),
      document, faceSimilarityLevel)
}

case class EidRequestParams(requestedAt: LocalDateTime, minSimilarityLevel: String)

object EidRequestParams {
  def apply(requestedAt: Long, minSimilarityLevel: String): EidRequestParams =
    new EidRequestParams(fromMillis(requestedAt), minSimilarityLevel)
}

case class EidDocument(docType: String,
                       issuer: String,
                       expiresAt: LocalDateTime,
                       primaryName: String,
                       secondaryName: String,
                       birthDate: LocalDateTime,
                       sex: Option[String],
                       nationality: Option[String],
                       personalNumber: Option[String],
                       documentNumber: String)

object EidDocument {
  private type BirthDate = String
  private type ExpiringDate = String

  private def dtResolver(birth: BirthDate, exp: ExpiringDate, both2k: Boolean = true): (BirthDate, ExpiringDate) = {
    val (birth_, exp_) = if (both2k) {
      s"20$birth" -> s"20$exp"
    } else {
      s"19$birth" -> s"20$exp"
    }

    if (birth_.toInt > exp_.toInt && both2k) {
      dtResolver(birth, exp, both2k = false)
    } else {
      require(birth_.toInt < exp_.toInt, s"birthDate: $birth should be less than expirationDate: $exp")
      birth_ -> exp_
    }
  }


  def apply(docType: String,
            issuer: String,
            expiresAt: String,
            primaryName: String,
            secondaryName: String,
            birthDate: String,
            sex: Option[String],
            nationality: Option[String],
            personalNumber: Option[String],
            documentNumber: String): EidDocument = {
    val (birthDate_, expiresAt_) = dtResolver(birthDate, expiresAt)
    new EidDocument(
      docType, issuer,
      fromYYYYMMDD(expiresAt_),
      primaryName, secondaryName,
      fromYYYYMMDD(birthDate_),
      sex, nationality, personalNumber, documentNumber
    )
  }
}

