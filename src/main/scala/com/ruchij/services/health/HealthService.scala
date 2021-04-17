package com.ruchij.services.health

import com.ruchij.services.health.models.{ServiceInformation, ServiceStatus}

trait HealthService[F[_]] {
  val serviceInformation: F[ServiceInformation]

  val serviceStatus: F[ServiceStatus]
}
