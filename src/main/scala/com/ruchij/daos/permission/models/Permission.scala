package com.ruchij.daos.permission.models

import org.joda.time.DateTime

import java.util.UUID

case class Permission(userId: UUID, createdAt: DateTime, resourceId: UUID, permissionType: PermissionType)