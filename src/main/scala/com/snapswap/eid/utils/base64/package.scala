package com.snapswap.eid.utils


import java.util.Base64

import akka.stream.scaladsl.Source
import akka.util.ByteString

package object base64 {
  def base64StrToBytes(str: String): Source[ByteString, Any] =
    Source.single(
      ByteString(
        Base64.getDecoder.decode(str)
      )
    )
}
