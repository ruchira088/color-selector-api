package com.ruchij.web.requests

import com.ruchij.daos.user.models.Email

case class CreateUserRequest(username: String, password: String, firstName: String, lastName: String, email: Email)
