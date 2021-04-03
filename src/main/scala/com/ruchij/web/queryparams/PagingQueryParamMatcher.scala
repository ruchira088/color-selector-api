package com.ruchij.web.queryparams

import cats.data.Validated.Valid
import cats.data.ValidatedNel
import cats.implicits._
import com.ruchij.web.models.Paging
import org.http4s.{ParseFailure, QueryParamDecoder, QueryParameterValue}

object PagingQueryParamMatcher {
  val Default: Paging = Paging(offset = 0, pageSize = 25)

  def unapply(queryParameters: Map[String, collection.Seq[String]]): Some[ValidatedNel[ParseFailure, Paging]] =
    Some {
      (parse(queryParameters, "page-size", Default.pageSize),
        parse(queryParameters, "offset", Default.offset))
        .mapN {
          case (pageSize, offset) => Paging(offset, pageSize)
        }
    }

  private def parse[A: QueryParamDecoder](
    queryParameters: Map[String, collection.Seq[String]],
    name: String,
    default: => A
  ): ValidatedNel[ParseFailure, A] =
    queryParameters
      .get(name)
      .flatMap(_.headOption)
      .fold[ValidatedNel[ParseFailure, A]](Valid(default)) { stringValue =>
        QueryParamDecoder[A].decode(QueryParameterValue(stringValue))
      }

}
