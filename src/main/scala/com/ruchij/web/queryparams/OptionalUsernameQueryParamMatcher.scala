package com.ruchij.web.queryparams

import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

object OptionalUsernameQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("username")