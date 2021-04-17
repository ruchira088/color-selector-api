package com.ruchij.services.health.models

import shapeless.Generic

case class ServiceStatus(database: HealthStatus) { self =>
  val isHealthy: Boolean =
    Generic[ServiceStatus].to(self).toList.forall(_ == HealthStatus.OK)
}
