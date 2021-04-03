package com.ruchij.web.requests

import com.ruchij.daos.permission.models.PermissionType

case class AuthorizationRequest(userId: String, permissionType: PermissionType)
