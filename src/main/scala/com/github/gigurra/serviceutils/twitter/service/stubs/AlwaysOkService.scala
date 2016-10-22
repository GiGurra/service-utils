package com.github.gigurra.serviceutils.twitter.service.stubs

import com.twitter.finagle.Service
import com.twitter.finagle.http.Response
import com.twitter.util.Future
import com.github.gigurra.serviceutils.twitter.service.Responses

case class AlwaysOkService[T]() extends Service[T, Response] {
  override def apply(request: T): Future[Response] = {
    Responses.Ok("Dummy response: OK")
  }
}
