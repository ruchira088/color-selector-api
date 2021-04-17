package com.ruchij.daos.health

trait HealthCheckDao[F[_]] {
  val ok: F[Boolean]
}
