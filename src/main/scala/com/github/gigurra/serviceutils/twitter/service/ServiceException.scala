package com.github.gigurra.serviceutils.twitter.service

import com.twitter.finagle.http.Response
import com.twitter.util.Future

import scala.util.{Failure, Success, Try}

case class ServiceException(response: Response)
  extends RuntimeException(response.contentString)

object ServiceException {
  def filter(expr: => Future[Response]): Future[Response] = {

    val future = Try(expr) match {
      case Success(result) => result
      case Failure(e) => Future.exception(e)
    }

    future.rescue {
      case e: ServiceException => Future.value(e.response)
    }
  }
}

object ServiceExceptionFilter {
  def apply(expr: => Future[Response]): Future[Response] = ServiceException.filter(expr)
}