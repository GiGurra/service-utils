package com.github.gigurra.serviceutils.twitter.service

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import com.github.gigurra.serviceutils.twitter.service.ServiceErrors.AutoLoggingOff

import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

case class ExceptionFilter[T <: Throwable: ClassTag]()
  extends SimpleFilter[Request, Response]
  with ServiceErrors with AutoLoggingOff {

  override def apply(request: Request,
                     continue: Service[Request, Response]): Future[Response] = {
    Try {
      continue(request).rescue {
        case e: T => transformToResponse(e, bug = false)
        case e => throw e
      }
    } match {
      case Success(response) => response
      case Failure(e: ServiceException) => Future.value(e.response)
      case Failure(e: T) => transformToResponse(e, bug = true)
      case Failure(e) => throw e
    }

  }

  def transformToResponse(e: T, bug: Boolean): Future[Response] = {
    logger.error(e, s"Caught an ${implicitly[ClassTag[T]].runtimeClass} ${if (bug) "this should not happen!" else ""}")
    Future.value(internalServerError(e.getMessage).response)
  }
}
