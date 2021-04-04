package com.ruchij.exceptions

case class ValidationException(errorMessage: String) extends Exception(errorMessage)
