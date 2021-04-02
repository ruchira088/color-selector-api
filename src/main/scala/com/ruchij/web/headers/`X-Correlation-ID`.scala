package com.ruchij.web.headers

import org.http4s.syntax.string._
import org.http4s.util.{CaseInsensitiveString, Writer}
import org.http4s.{Header, HeaderKey, ParseResult}

object `X-Correlation-ID` extends HeaderKey.Singleton {

  override type HeaderT = `X-Correlation-ID`

  override def name: CaseInsensitiveString = "X-Correlation-ID".ci

  override def matchHeader(header: Header): Option[`X-Correlation-ID`] =
    if (header.name == name) Some(`X-Correlation-ID`(header.value)) else None

  override def parse(input: String): ParseResult[`X-Correlation-ID`] =
    Right(`X-Correlation-ID`(input))

}

case class `X-Correlation-ID`(correlationId: String) extends Header.Parsed {
  override def key: HeaderKey = `X-Correlation-ID`

  override def renderValue(writer: Writer): writer.type =
    writer << correlationId
}
