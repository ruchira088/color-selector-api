package com.ruchij.daos.health

import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator

object DoobieHealthCheckDao extends HealthCheckDao[ConnectionIO] {

  override val ok: ConnectionIO[Boolean] = sql"SELECT 1".query[Int].unique.map(_ == 1)

}
