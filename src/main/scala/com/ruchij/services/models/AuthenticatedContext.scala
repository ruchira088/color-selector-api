package com.ruchij.services.models

import com.ruchij.daos.user.models.User
import com.ruchij.web.middleware.CorrelationIdMiddleware.CorrelationID

case class AuthenticatedContext(user: User, correlationId: CorrelationID)
