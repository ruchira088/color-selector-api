package com.ruchij.web.requests

import com.ruchij.daos.permission.models.PermissionType

import java.util.UUID

case class AuthorizationRequest(userId: UUID, permissionType: PermissionType)
