package se.gigurra.serviceutils.twitter.service

import java.nio.charset.Charset

import com.twitter.finagle.http
import com.twitter.util.Future
import org.json4s.JsonAST.JValue
import com.github.gigurra.heisenberg.{MapDataProducer, MapProducer}
import se.gigurra.serviceutils.json.JSON

object Responses {

  type Message = String

  object json {

    import org.json4s.JsonDSL._
    import org.json4s.jackson.JsonMethods._

    val charset = Charset.forName("UTF-8")

    def apply(json: JValue,
              status: http.Status = http.Status.Ok,
              httpVersion: http.Version = http.Version.Http11): http.Response = {

      val response = http.Response(httpVersion, status)

      response.setContentTypeJson()
      response.setContentString(compact(json))
      response

    }

    object jval {

      def ok(json: JValue,
             httpVersion: http.Version = http.Version.Http11): http.Response = {
        apply(json, http.Status.Ok, httpVersion)
      }

      def unauthorized(json: JValue,
                       httpVersion: http.Version = http.Version.Http11): http.Response = {
        apply(json, http.Status.Unauthorized, httpVersion)
      }

      def badRequest(json: JValue,
                     httpVersion: http.Version = http.Version.Http11): http.Response = {
        apply(json, http.Status.BadRequest, httpVersion)
      }

      def internalServerError(json: JValue,
                              httpVersion: http.Version = http.Version.Http11): http.Response = {
        apply(json, http.Status.InternalServerError, httpVersion)
      }

      def notFound(json: JValue,
                   httpVersion: http.Version = http.Version.Http11): http.Response = {
        apply(json, http.Status.NotFound, httpVersion)
      }

      def timeout(json: JValue,
                   httpVersion: http.Version = http.Version.Http11): http.Response = {
        apply(json, http.Status.GatewayTimeout, httpVersion)
      }

      def tooManyRequests(json: JValue,
                          httpVersion: http.Version = http.Version.Http11): http.Response = {
        apply(json, http.Status.TooManyRequests, httpVersion)
      }

      def unavailable(json: JValue,
                      httpVersion: http.Version = http.Version.Http11): http.Response = {
        apply(json, http.Status.ServiceUnavailable, httpVersion)
      }

      def conflict(json: JValue,
                   httpVersion: http.Version = http.Version.Http11): http.Response = {
        apply(json, http.Status.Conflict, httpVersion)
      }
    }

    object singleFieldObject {

      def ok[T: MapDataProducer](t: T, fieldName: String = "data", httpVersion: http.Version = http.Version.Http11): http.Response = {
        jval.ok(JSON.writeSingleFieldObjectJval(t, fieldName), httpVersion)
      }

      def unauthorized[T: MapDataProducer](t: T, fieldName: String = "data", httpVersion: http.Version = http.Version.Http11): http.Response = {
        jval.unauthorized(JSON.writeSingleFieldObjectJval(t, fieldName), httpVersion)
      }

      def badRequest[T: MapDataProducer](t: T, fieldName: String = "data", httpVersion: http.Version = http.Version.Http11): http.Response = {
        jval.badRequest(JSON.writeSingleFieldObjectJval(t, fieldName), httpVersion)
      }

      def internalServerError[T: MapDataProducer](t: T, fieldName: String = "data", httpVersion: http.Version = http.Version.Http11): http.Response = {
        jval.internalServerError(JSON.writeSingleFieldObjectJval(t, fieldName), httpVersion)
      }

      def notFound[T: MapDataProducer](t: T, fieldName: String = "data", httpVersion: http.Version = http.Version.Http11): http.Response = {
        jval.notFound(JSON.writeSingleFieldObjectJval(t, fieldName), httpVersion)
      }

      def conflict[T: MapDataProducer](t: T, fieldName: String = "data", httpVersion: http.Version = http.Version.Http11): http.Response = {
        jval.conflict(JSON.writeSingleFieldObjectJval(t, fieldName), httpVersion)
      }

    }

    def ok[T: MapProducer](t: T,
                           httpVersion: http.Version = http.Version.Http11): http.Response = {
      jval.ok(JSON.writeJval(t), httpVersion)
    }

    def unauthorized[T: MapProducer](t: T,
                                     httpVersion: http.Version = http.Version.Http11): http.Response = {
      jval.unauthorized(JSON.writeJval(t), httpVersion)
    }

    def badRequest[T: MapProducer](t: T,
                                   httpVersion: http.Version = http.Version.Http11): http.Response = {
      jval.badRequest(JSON.writeJval(t), httpVersion)
    }

    def internalServerError[T: MapProducer](t: T,
                                            httpVersion: http.Version = http.Version.Http11): http.Response = {
      jval.internalServerError(JSON.writeJval(t), httpVersion)
    }

    def notFound[T: MapProducer](t: T,
                                 httpVersion: http.Version = http.Version.Http11): http.Response = {
      jval.notFound(JSON.writeJval(t), httpVersion)
    }

    def conflict[T: MapProducer](t: T,
                                 httpVersion: http.Version = http.Version.Http11): http.Response = {
      jval.conflict(JSON.writeJval(t), httpVersion)
    }

    def Ok[T: MapProducer](t: T,
                           httpVersion: http.Version = http.Version.Http11): Future[http.Response] = {
      Future.value(jval.ok(JSON.writeJval(t), httpVersion))
    }

    def Unauthorized[T: MapProducer](t: T,
                                     httpVersion: http.Version = http.Version.Http11): Future[http.Response] = {
      Future.value(jval.unauthorized(JSON.writeJval(t), httpVersion))
    }

    def BadRequest[T: MapProducer](t: T,
                                   httpVersion: http.Version = http.Version.Http11): Future[http.Response] = {
      Future.value(jval.badRequest(JSON.writeJval(t), httpVersion))
    }

    def InternalServerError[T: MapProducer](t: T,
                                            httpVersion: http.Version = http.Version.Http11): Future[http.Response] = {
      Future.value(jval.internalServerError(JSON.writeJval(t), httpVersion))
    }

    def NotFound[T: MapProducer](t: T,
                                 httpVersion: http.Version = http.Version.Http11): Future[http.Response] = {
      Future.value(jval.notFound(JSON.writeJval(t), httpVersion))
    }

    def Conflict[T: MapProducer](t: T,
                                 httpVersion: http.Version = http.Version.Http11): Future[http.Response] = {
      Future.value(jval.conflict(JSON.writeJval(t), httpVersion))
    }

    def message(message: String): JValue = {
      render("message" -> message)
    }

  }

  def ok(message: Message = "",
         httpVersion: http.Version = http.Version.Http11): http.Response = {
    json.jval.ok(json.message(message), httpVersion)
  }

  def unauthorized(message: Message = "",
                   httpVersion: http.Version = http.Version.Http11): http.Response = {
    json.jval.unauthorized(json.message(message), httpVersion)
  }

  def badRequest(message: Message = "",
                 httpVersion: http.Version = http.Version.Http11): http.Response = {
    json.jval.badRequest(json.message(message), httpVersion)
  }

  def internalServerError(message: Message = "",
                          httpVersion: http.Version = http.Version.Http11): http.Response = {
    json.jval.internalServerError(json.message(message), httpVersion)
  }

  def notFound(message: Message = "",
               httpVersion: http.Version = http.Version.Http11): http.Response = {
    json.jval.notFound(json.message(message), httpVersion)
  }

  def timeout(message: Message = "",
              httpVersion: http.Version = http.Version.Http11): http.Response = {
    json.jval.timeout(json.message(message), httpVersion)
  }

  def tooManyRequests(message: Message = "",
                      httpVersion: http.Version = http.Version.Http11): http.Response = {
    json.jval.tooManyRequests(json.message(message), httpVersion)
  }

  def unavailable(message: Message = "",
                  httpVersion: http.Version = http.Version.Http11): http.Response = {
    json.jval.unavailable(json.message(message), httpVersion)
  }

  def conflict(message: Message = "",
               httpVersion: http.Version = http.Version.Http11): http.Response = {
    json.jval.conflict(json.message(message), httpVersion)
  }

  def Ok(message: Message = "",
         httpVersion: http.Version = http.Version.Http11): Future[http.Response] = {
    Future.value(ok(message, httpVersion))
  }

  def Unauthorized(message: Message = "",
                   httpVersion: http.Version = http.Version.Http11): Future[http.Response] = {
    Future.value(unauthorized(message, httpVersion))
  }

  def BadRequest(message: Message = "",
                 httpVersion: http.Version = http.Version.Http11): Future[http.Response] = {
    Future.value(badRequest(message, httpVersion))
  }

  def InternalServerError(message: Message = "",
                          httpVersion: http.Version = http.Version.Http11): Future[http.Response] = {
    Future.value(internalServerError(message, httpVersion))
  }

  def NotFound(message: Message = "",
               httpVersion: http.Version = http.Version.Http11): Future[http.Response] = {
    Future.value(notFound(message, httpVersion))
  }

  def TimeOut(message: Message = "",
               httpVersion: http.Version = http.Version.Http11): Future[http.Response] = {
    Future.value(timeout(message, httpVersion))
  }

  def TooManyRequests(message: Message = "",
                      httpVersion: http.Version = http.Version.Http11): Future[http.Response] = {
    Future.value(tooManyRequests(message, httpVersion))
  }

  def Unavailable(message: Message = "",
                  httpVersion: http.Version = http.Version.Http11): Future[http.Response] = {
    Future.value(unavailable(message, httpVersion))
  }

  def Conflict(message: Message = "",
               httpVersion: http.Version = http.Version.Http11): Future[http.Response] = {
    Future.value(conflict(message, httpVersion))
  }

}
