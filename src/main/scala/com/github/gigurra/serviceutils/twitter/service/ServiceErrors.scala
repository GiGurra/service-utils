package com.github.gigurra.serviceutils.twitter.service

import com.twitter.util.Future
import com.github.gigurra.serviceutils.twitter.logging.Logging
import com.github.gigurra.serviceutils.twitter.service.ServiceErrors.{AutoLoggingOff, AutoLoggingOn}

trait ServiceErrors extends Logging {

  def autoLog: Boolean

  def serviceName: String = getClass.getSimpleName

  def badRequest(message: String, exc: Throwable = null): ServiceException = {
    val msg = s"Bad Request to service '$serviceName' (exception/cause = $exc): $message"
    if (autoLog)
      logger.error(msg)
    ServiceException(Responses.badRequest(msg))
  }

  def BadRequest[T](message: String, exc: Throwable = null): Future[T] = {
    Future.exception(badRequest(message, exc))
  }

  def unauthorized(message: String, exc: Throwable = null): ServiceException = {
    val msg = s"Unauthorized request to service '$serviceName' (exception/cause = $exc): $message"
    if (autoLog)
      logger.warning(msg)
    ServiceException(Responses.unauthorized(msg))
  }

  def Unauthorized[T](message: String, exc: Throwable = null): Future[T] = {
    Future.exception(unauthorized(message, exc))
  }

  def internalServerError(message: String, exc: Throwable = null): ServiceException = {
    val msg = s"Internal Server Error in service '$serviceName' (exception/cause = $exc): $message"
    if (autoLog)
      logger.error(new RuntimeException(msg, exc), msg)
    ServiceException(Responses.internalServerError(msg))
  }

  def InternalServerError[T](message: String, exc: Throwable = null): Future[T] = {
    Future.exception(internalServerError(message, exc))
  }

  def notFound(message: String, exc: Throwable = null): ServiceException = {
    val msg = s"Not Found in service service '$serviceName' (exception/cause = $exc): $message"
    if (autoLog)
      logger.warning(msg)
    ServiceException(Responses.notFound(msg))
  }

  def NotFound[T](message: String, exc: Throwable = null): Future[T] = {
    Future.exception(notFound(message, exc))
  }

  def conflict(message: String, exc: Throwable = null): ServiceException = {
    val msg = s"Conflict in service '$serviceName' (exception/cause = $exc): $message"
    if (autoLog)
      logger.warning(msg)
    ServiceException(Responses.conflict(msg))
  }

  def Conflict[T](message: String, exc: Throwable = null): Future[T] = {
    Future.exception(conflict(message, exc))
  }

  def timeout(message: String, exc: Throwable = null): ServiceException = {
    val msg = s"Timeout in service '$serviceName' (exception/cause = $exc): $message"
    if (autoLog)
      logger.warning(msg)
    ServiceException(Responses.timeout(msg))
  }

  def Timeout[T](message: String, exc: Throwable = null): Future[T] = {
    Future.exception(timeout(message, exc))
  }

  def tooManyRequests(message: String, exc: Throwable = null): ServiceException = {
    val msg = s"Too many requests in service '$serviceName' (exception/cause = $exc): $message"
    if (autoLog)
      logger.warning(msg)
    ServiceException(Responses.tooManyRequests(msg))
  }

  def TooManyRequests[T](message: String, exc: Throwable = null): Future[T] = {
    Future.exception(tooManyRequests(message, exc))
  }

  def unavailable(message: String, exc: Throwable = null): ServiceException = {
    val msg = s"Service unavailable in service '$serviceName' (exception/cause = $exc): $message"
    if (autoLog)
      logger.warning(msg)
    ServiceException(Responses.unavailable(msg))
  }

  def Unavailable[T](message: String, exc: Throwable = null): Future[T] = {
    Future.exception(unavailable(message, exc))
  }

}

trait ServiceErrorsWithoutAutoLogging extends ServiceErrors with AutoLoggingOff
trait ServiceErrorsWithAutoLogging extends ServiceErrors with AutoLoggingOn

object ServiceErrors {

  trait AutoLoggingOff { _ : ServiceErrors =>
    def autoLog = false
  }
  trait AutoLoggingOn { _ : ServiceErrors =>
    def autoLog = true
  }

}
