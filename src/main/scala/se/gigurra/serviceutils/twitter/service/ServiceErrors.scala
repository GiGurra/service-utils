package se.gigurra.serviceutils.twitter.service

import com.twitter.finagle.http.Response
import com.twitter.util.Future
import se.gigurra.serviceutils.twitter.logging.Logging

trait ServiceErrors extends Logging {

  def serviceName: String = getClass.getSimpleName

  def badRequest(message: String, exc: Throwable = null): Response = {
    val msg = s"Bad Request to service '$serviceName' (exception/cause = $exc): $message"
    logger.error(msg)
    Responses.badRequest(msg)
  }

  def BadRequest(message: String, exc: Throwable = null): Future[Response] = {
    Future.exception(ServiceException(badRequest(message, exc)))
  }

  def unauthorized(message: String, exc: Throwable = null): Response = {
    val msg = s"Unauthorized request to service '$serviceName' (exception/cause = $exc): $message"
    logger.warning(msg)
    Responses.unauthorized(msg)
  }

  def Unauthorized(message: String, exc: Throwable = null): Future[Response] = {
    Future.exception(ServiceException(unauthorized(message, exc)))
  }

  def internalServerError(message: String, exc: Throwable = null): Response = {
    val msg = s"Internal Server Error in service '$serviceName' (exception/cause = $exc): $message"
    logger.error(new RuntimeException(msg, exc), msg)
    Responses.internalServerError(msg)
  }

  def InternalServerError(message: String, exc: Throwable = null): Future[Response] = {
    Future.exception(ServiceException(internalServerError(message, exc)))
  }

  def notFound(message: String, exc: Throwable = null): Response = {
    val msg = s"Not Found in service service '$serviceName' (exception/cause = $exc): $message"
    logger.warning(msg)
    Responses.notFound(msg)
  }

  def NotFound(message: String, exc: Throwable = null): Future[Response] = {
    Future.exception(ServiceException(notFound(message, exc)))
  }

  def conflict(message: String, exc: Throwable = null): Response = {
    val msg = s"Conflict in service '$serviceName' (exception/cause = $exc): $message"
    logger.warning(msg)
    Responses.conflict(msg)
  }

  def Conflict(message: String, exc: Throwable = null): Future[Response] = {
    Future.exception(ServiceException(conflict(message, exc)))
  }

}
