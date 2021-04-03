package com.ruchij.daos.permission

import com.ruchij.daos.permission.models.{Permission, PermissionType}
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator
import doobie.util.fragments
import com.ruchij.daos.doobie.GetMappings._
import com.ruchij.daos.doobie.PutMappings._

import java.util.UUID

object DoobiePermissionDao extends PermissionDao[ConnectionIO] {
  override def insert(permission: Permission): ConnectionIO[Int] =
    sql"""
      INSERT INTO permission (user_id, created_at, resource_id, permission_type)
        VALUES (${permission.userId}, ${permission.createdAt}, ${permission.resourceId}, ${permission.permissionType})
    """.update.run

  override def findBy(
    requesterId: UUID,
    resourceId: UUID,
    permissionType: PermissionType
  ): ConnectionIO[Option[Permission]] =
    (fr"SELECT user_id, created_at, resource_id, permission_type FROM permission" ++
      fragments.whereAnd(
        fr"user_id = $requesterId",
        fr"resource_id = $resourceId",
        fragments.in(fr"permission_type", permissionType.all)
      ))
      .query[Permission]
      .option

}
