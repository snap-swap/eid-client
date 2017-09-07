package com.snapswap.eid.domain

import java.time.{Clock, LocalDateTime}

import com.snapswap.eid.utils.datetime._


object EidProcessEnum extends Enumeration {
  type Process = Value

  val Unattended, Attended = Value
}

object EidSimilarityLevelEnum extends Enumeration {
  type Similarity = Value

  val Low, High, Medium, VeryLow = Value
}

object EidDocTypeEnum extends Enumeration {
  type DocType = Value

  val TD1, TD2, TD3 = Value
}

object EidSexEnum extends Enumeration {
  type Sex = Value

  val M, F = Value
}

case class EidVideoVerificationResult(id: String,
                                      process: EidProcessEnum.Process,
                                      status: String, //TODO: use enum
                                      tenantId: Option[String],
                                      request: EidRequestParams,
                                      completedAt: Option[LocalDateTime],
                                      document: EidDocument,
                                      faceSimilarityLevel: EidSimilarityLevelEnum.Similarity)

object EidVideoVerificationResult {
  def apply(id: String,
            process: String,
            status: String,
            tenantId: Option[String],
            request: EidRequestParams,
            completedAt: Option[Long],
            document: EidDocument,
            faceSimilarityLevel: String): EidVideoVerificationResult =
    new EidVideoVerificationResult(
      id, EidProcessEnum.withName(process), status, tenantId, request,
      completedAt.map(fromMillis),
      document, EidSimilarityLevelEnum.withName(faceSimilarityLevel))
}

case class EidRequestParams(requestedAt: LocalDateTime,
                            minSimilarityLevel: Option[EidSimilarityLevelEnum.Similarity])

object EidRequestParams {
  def apply(requestedAt: Long, minSimilarityLevel: Option[String]): EidRequestParams =
    new EidRequestParams(fromMillis(requestedAt), minSimilarityLevel.map(EidSimilarityLevelEnum.withName))
}

case class EidDocument(docType: EidDocTypeEnum.DocType,
                       issuer: String,
                       expiresAt: Option[LocalDateTime],
                       primaryName: String,
                       secondaryName: String,
                       birthDate: LocalDateTime,
                       sex: Option[EidSexEnum.Sex],
                       nationality: Option[String],
                       personalNumber: Option[String],
                       documentNumber: String) {
  require(issuer == issuer.toUpperCase && issuer.length == 3, "'issuer' must be a 3-letter upper-cased valid country code")
  require(
    nationality.forall(n => n == n.toUpperCase && n.length == 3),
    "if 'nationality' exists, it must be a 3-letter upper-cased valid country code"
  )
  require(expiresAt.forall(exp => exp.compareTo(birthDate) > 0), "'birthDate' should be less than 'expiresAt'")
  require(!primaryName.trim.isEmpty, "'primaryName' can't be empty")
  require(!secondaryName.trim.isEmpty, "'secondaryName' can't be empty")
  require(personalNumber.forall(!_.trim.isEmpty), "if 'personalNumber' exists, it can't be empty")
  require(!documentNumber.trim.isEmpty, "'documentNumber' can't be empty")

}

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

  private def dtResolver(birth: BirthDate, exp: Option[ExpiringDate]): (BirthDate, Option[ExpiringDate]) = exp match {
    case Some(e) =>
      val (resolvedBirth, resolvedExp) = dtResolver(birth, e)
      resolvedBirth -> Some(resolvedExp)
    case None =>
      val birth2k = s"20$birth"
      val currentYear = LocalDateTime.now(Clock.systemUTC()).getYear
      if (birth2k.take(4).toInt < currentYear)
        birth2k -> exp
      else
        s"19$birth" -> exp
  }


  def apply(docType: String,
            issuer: String,
            expiresAt: Option[String],
            primaryName: String,
            secondaryName: String,
            birthDate: String,
            sex: Option[String],
            nationality: Option[String],
            personalNumber: Option[String],
            documentNumber: Option[String],
            passportNumber: Option[String]): EidDocument = {
    val persNum = personalNumber.flatMap{
      case pn if pn.trim.isEmpty =>
        None
      case pn =>
        Some(pn)
    }
    val (birthDateResolved, expiresAtResolved) = dtResolver(birthDate, expiresAt)
    new EidDocument(
      EidDocTypeEnum.withName(docType), issuer,
      expiresAtResolved.map(fromYYYYMMDD),
      primaryName, secondaryName,
      fromYYYYMMDD(birthDateResolved),
      sex.map(EidSexEnum.withName), nationality, persNum,
      documentNumber.orElse(passportNumber).orElse(persNum).getOrElse("")
    )
  }
}

