package com.ruchij.services.health.models

import enumeratum.{Enum, EnumEntry}

sealed trait HealthStatus extends EnumEntry

object HealthStatus extends Enum[HealthStatus] {
  case object OK extends HealthStatus
  case object Failure extends HealthStatus

  override def values: IndexedSeq[HealthStatus] = findValues
}