package se.gigurra.serviceutils.twitter.service

import com.twitter.util.Future
import se.gigurra.serviceutils.twitter.logging.Logging

trait ServiceErrors extends Logging {

  def serviceName: String = getClass.getSimpleName

  def badRequest(message: String, exc: Throwable = null): ServiceException = {
    val msg = s"Bad Request to service '$serviceName' (exception/cause = $exc): $message"
    logger.error(msg)
    ServiceException(Responses.badRequest(msg))
  }

  def BadRequest[T](message: String, exc: Throwable = null): Future[T] = {
    Future.exception(badRequest(message, exc))
  }

  def unauthorized(message: String, exc: Throwable = null): ServiceException = {
    val msg = s"Unauthorized request to service '$serviceName' (exception/cause = $exc): $message"
    logger.warning(msg)
    ServiceException(Responses.unauthorized(msg))
  }

  def Unauthorized[T](message: String, exc: Throwable = null): Future[T] = {
    Future.exception(unauthorized(message, exc))
  }

  def internalServerError(message: String, exc: Throwable = null): ServiceException = {
    val msg = s"Internal Server Error in service '$serviceName' (exception/cause = $exc): $message"
    logger.error(new RuntimeException(msg, exc), msg)
    ServiceException(Responses.internalServerError(msg))
  }

  def InternalServerError[T](message: String, exc: Throwable = null): Future[T] = {
    Future.exception(internalServerError(message, exc))
  }

  def notFound(message: String, exc: Throwable = null): ServiceException = {
    val msg = s"Not Found in service service '$serviceName' (exception/cause = $exc): $message"
    logger.warning(msg)
    ServiceException(Responses.notFound(msg))
  }

  def NotFound[T](message: String, exc: Throwable = null): Future[T] = {
    Future.exception(notFound(message, exc))
  }

  def conflict(message: String, exc: Throwable = null): ServiceException = {
    val msg = s"Conflict in service '$serviceName' (exception/cause = $exc): $message"
    logger.warning(msg)
    ServiceException(Responses.conflict(msg))
  }

  def Conflict[T](message: String, exc: Throwable = null): Future[T] = {
    Future.exception(conflict(message, exc))
  }

}
