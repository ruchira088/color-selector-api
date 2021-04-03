package com.ruchij.config

import ConfigReaders.dateTimeConfigReader
import cats.ApplicativeError
import com.ruchij.migration.config.DatabaseConfiguration
import com.ruchij.types.FunctionKTypes.eitherToF
import pureconfig.ConfigObjectSource
import pureconfig.error.ConfigReaderException
import pureconfig.generic.auto._

case class ServiceConfiguration(
  databaseConfiguration: DatabaseConfiguration,
  authenticationConfiguration: AuthenticationConfiguration,
  httpConfiguration: HttpConfiguration,
  buildInformation: BuildInformation
)

object ServiceConfiguration {
  def parse[F[_]: ApplicativeError[*[_], Throwable]](configObjectSource: ConfigObjectSource): F[ServiceConfiguration] =
    eitherToF.apply {
      configObjectSource.load[ServiceConfiguration]
        .left.map(error => ConfigReaderException[ServiceConfiguration](error))
    }
}
