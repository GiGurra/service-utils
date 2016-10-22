package com.github.gigurra.serviceutils.twitter.future

import com.twitter.finagle.http.Response
import com.twitter.{util => twitter}
import com.github.gigurra.serviceutils.tuple.FlattenTuple
import com.github.gigurra.serviceutils.twitter.service.ServiceException

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.implicitConversions
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

object TwitterFutures {

  implicit def scalaToTwitterTry[T](t: Try[T]): twitter.Try[T] = t match {
    case Success(r) => twitter.Return(r)
    case Failure(ex) => twitter.Throw(ex)
  }

  implicit def twitterToScalaTry[T](t: twitter.Try[T]): Try[T] = t match {
    case twitter.Return(r) => Success(r)
    case twitter.Throw(ex) => Failure(ex)
  }

  implicit def scalaToTwitterFuture[T](f: Future[T])(implicit ec: ExecutionContext): twitter.Future[T] = {
    val promise = twitter.Promise[T]()
    f.onComplete(promise update _)
    promise
  }

  implicit def twitterToScalaFuture[T](f: twitter.Future[T]): Future[T] = {
    val promise = Promise[T]()
    f.respond(promise complete _)
    promise.future
  }

  implicit class RichCollFuture[T](val f: Seq[twitter.Future[T]]) extends AnyVal {
    def gather(): twitter.Future[Seq[T]] = twitter.Future.collect(f)

    def join(): twitter.Future[Unit] = twitter.Future.join(f)

    def await(): Seq[T] = gather().await()
  }

  implicit class RichFuture[T](val f: twitter.Future[T]) extends AnyVal {
    def await(): T = twitter.Await.result(f)
  }

  implicit class RichAnything[T](val t: T) extends AnyVal {
    def toFuture: twitter.Future[T] = twitter.Future.value(t)
  }

  implicit def iterf2seqf[T](items: Iterable[twitter.Future[T]]): RichCollFuture[T] = new RichCollFuture[T](items.toSeq)

  implicit class RichEither[LeftType, RightType](val either: Either[LeftType, RightType]) extends AnyVal {
    def future(errorHandler: LeftType => twitter.Future[RightType]): twitter.Future[RightType] = {
      either match {
        case Left(left) => errorHandler(left)
        case Right(right) => twitter.Future.value(right)
      }
    }
  }

  implicit class RichAny[ResultType](expr: => ResultType) extends RichTry(Try(expr))

  implicit class RichTry[SuccessType](expr: => Try[SuccessType]) {

    def rescuePf(errorHandler: PartialFunction[Throwable, twitter.Future[SuccessType]] = null): twitter.Future[SuccessType] = {
      expr match {
        case Success(result) => twitter.Future.value(result)
        case Failure(e) =>
          if (errorHandler != null && errorHandler.isDefinedAt(e))
            errorHandler(e)
          else
            twitter.Future.exception(e)
      }
    }

    def rescue[T <: Throwable : ClassTag](errorExpr: T => twitter.Future[SuccessType] = null): twitter.Future[SuccessType] = {
      expr match {
        case Success(result) => twitter.Future.value(result)
        case Failure(e: T) if errorExpr != null => errorExpr(e)
        case Failure(e) => twitter.Future.exception(e)
      }
    }

    def rescueAll(errorExpr: Throwable => twitter.Future[SuccessType] = null): twitter.Future[SuccessType] = {
      expr match {
        case Success(result) => twitter.Future.value(result)
        case Failure(e) if errorExpr != null => errorExpr(e)
        case Failure(e) => twitter.Future.exception(e)
      }
    }

    def future: twitter.Future[SuccessType] = rescueAll()

  }

  implicit class RichFutureOption[SuccessType](expr: => twitter.Future[Option[SuccessType]]) {

    def getOrElse(default: => twitter.Future[SuccessType]): twitter.Future[SuccessType] = {
      expr flatMap {
        case Some(result) => twitter.Future.value(result)
        case None => default
      }
    }
  }

  implicit class RichFutureIterable[SuccessType](expr: => twitter.Future[Iterable[SuccessType]]) {

    def getOrElse(default: => twitter.Future[SuccessType]): twitter.Future[SuccessType] = {
      expr.map(_.headOption) flatMap {
        case Some(result) => twitter.Future.value(result)
        case None => default
      }
    }
  }

  implicit def response2ServiceException[T](fr: twitter.Future[Response]): twitter.Future[T] = {
    fr.flatMap(response => twitter.Future.exception[T](ServiceException(response)))
  }

  implicit def response2ServiceException(response: Response): ServiceException = {
    ServiceException(response)
  }

  implicit class RichFuture2[SuccessType](expr: => twitter.Future[SuccessType]) {

    def rescueAll(errorExpr: Throwable => twitter.Future[SuccessType]): twitter.Future[SuccessType] = {
      expr.rescue { case e => errorExpr(e) }
    }

    def verify(condition: SuccessType => Boolean, orElse: => twitter.Future[SuccessType]): twitter.Future[SuccessType] = {
      expr.flatMap { result =>
        if (condition(result))
          twitter.Future.value(result)
        else
          orElse
      }
    }

  }

  implicit class RichFuture2Boolean(expr: => twitter.Future[Boolean]) {

    def rescueAll(errorExpr: Throwable => twitter.Future[Boolean]): twitter.Future[Boolean] = {
      expr.rescue { case e => errorExpr(e) }
    }

    def verify(orElse: => twitter.Future[Unit]): twitter.Future[Unit] = {
      expr.flatMap { result =>
        if (result)
          twitter.Future.Unit
        else
          orElse
      }
    }

  }

  implicit class CombiningFutures1[T1](expr1: => twitter.Future[T1]) {

    def *[T2](expr2: => twitter.Future[T2]): twitter.Future[(T1, T2)] = {

      for {
        a <- expr1
        b <- expr2
      } yield {
        (a, b)
      }

    }

    def |>[T2](expr2: T1 => twitter.Future[T2]): twitter.Future[(T1, T2)] = {
      for {
        a <- expr1
        b <- expr2(a)
      } yield {
        (a, b)
      }
    }

    def |[TX](expr: T1 => twitter.Future[TX]): twitter.Future[TX] = {
      expr1.flatMap(expr)
    }

  }

  implicit class CombiningFutures2[T1, T2](expr12: => twitter.Future[(T1, T2)]) {
    def *[T3](expr3: => twitter.Future[T3]) = CombiningFutures1(expr12).*(expr3).map(FlattenTuple(_))

    def |>[T3](expr3: (T1, T2) => twitter.Future[T3]) = CombiningFutures1(expr12).|>(expr3.tupled).map(FlattenTuple(_))


    def |[TX](expr: (T1, T2) => twitter.Future[TX]): twitter.Future[TX] = {
      expr12.flatMap(expr.tupled)
    }
  }

  implicit class CombiningFutures3[T1, T2, T3](expr123: => twitter.Future[((T1, T2), T3)]) {
    def *[T4](expr4: => twitter.Future[T4]) = CombiningFutures1(expr123).*(expr4).map(FlattenTuple(_))

    def |>[T4](expr4: (T1, T2, T3) => twitter.Future[T4]) = {
      for {
        r123 <- expr123
        r4 <- expr4.tupled(FlattenTuple(r123))
      } yield {
        FlattenTuple(r123, r4)
      }
    }

    def |[TX](expr: (T1, T2, T3) => twitter.Future[TX]): twitter.Future[TX] = {
      expr123.flatMap(x => expr.tupled(FlattenTuple(x)))
    }
  }

  implicit class CombiningFutures4[T1, T2, T3, T4](expr1234: => twitter.Future[(((T1, T2), T3), T4)]) {
    def *[T5](expr5: => twitter.Future[T5]) = CombiningFutures1(expr1234).*(expr5).map(FlattenTuple(_))

    def |>[T5](expr5: (T1, T2, T3, T4) => twitter.Future[T5]) = {
      for {
        r1234 <- expr1234
        r5 <- expr5.tupled(FlattenTuple(r1234))
      } yield {
        FlattenTuple(r1234, r5)
      }
    }

    def |[TX](expr: (T1, T2, T3, T4) => twitter.Future[TX]): twitter.Future[TX] = {
      expr1234.flatMap(x => expr.tupled(FlattenTuple(x)))
    }
  }

}