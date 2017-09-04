package com.snapswap.eid.utils

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}

package object datetime {

  def fromMillis(millis: Long): LocalDateTime =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDateTime

  def fromYYYYMMDD(yyyyMMdd: String): LocalDateTime =
    LocalDate.parse(yyyyMMdd, DateTimeFormatter.ofPattern("yyyyMMdd")).atStartOfDay()

}
