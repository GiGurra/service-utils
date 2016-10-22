package com.github.gigurra.serviceutils.twitter.store

import com.twitter.util.Future
import com.github.gigurra.franklin.{IndicesWiper, ItemsWiper, YeahReally}
import com.github.gigurra.franklinheisenberg.FHCollection.SelectStatement
import com.github.gigurra.franklinheisenberg.{FHCollection, Versioned}
import com.github.gigurra.heisenberg._
import com.github.gigurra.serviceutils.twitter.future.TwitterFutures._
import com.github.gigurra.serviceutils.twitter.logging.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions
import scala.reflect.runtime.universe._

/**
  * Created by kjolh on 12/26/2015.
  *
  * Forwards everything to FhCollection but returns twitter.util.Future instead of scala.concurrent.Future.
  * Useful in finagle services as they all use twitter futures.
  *
  * This class just forwards all calls to FhCollection and converts returned scalafutures -> twitterfutures
  * + Some minor other stuff
  */
case class Collection[T <: Parsed[S] : WeakTypeTag, S <: Schema[T]](fh: FHCollection[T, S])
  extends Logging {

  def create(entity: T): Future[Unit] = fh.create(entity)

  def where_raw(selector: Map[String, Any]): where_impl = where_impl(fh.where_raw(selector))
  def where_raw(statements: SelectStatement*): where_impl = where_impl(fh.where_raw(statements:_*))
  def where[T2 <: Parsed[_] : WeakTypeTag](query: T2): where_impl = where_impl(fh.where(query))
  def where(selectors: (S => SelectStatement)*): where_impl = where_impl(fh.where(selectors:_*))

  def findAll_raw(selector: Map[String, Any]): Future[Seq[Versioned[T]]] = fh.find_raw(selector)
  def findAll_raw(statements: SelectStatement*): Future[Seq[Versioned[T]]] = fh.find_raw(statements:_*)
  def findAll[T2 <: Parsed[_] : WeakTypeTag](query: T2): Future[Seq[Versioned[T]]] = fh.find(query)
  def findAll(selectors: (S => SelectStatement)*): Future[Seq[Versioned[T]]] = fh.find(selectors:_*)

  def findOne_raw(selector: Map[String, Any]): Future[Option[Versioned[T]]] = fh.find_raw(selector).map(_.headOption)
  def findOne_raw(statements: SelectStatement*): Future[Option[Versioned[T]]] = fh.find_raw(statements:_*).map(_.headOption)
  def findOne[T2 <: Parsed[_] : WeakTypeTag](query: T2): Future[Option[Versioned[T]]] = fh.find(query).map(_.headOption)
  def findOne(selectors: (S => SelectStatement)*): Future[Option[Versioned[T]]] = fh.find(selectors:_*).map(_.headOption)

  def deleteIndex(index: String)(yeahReally: YeahReally): Future[Unit] = fh.deleteIndex(index)(yeahReally)
  def deleteIndex(field: Field[_])(yeahReally: YeahReally): Future[Unit] = fh.deleteIndex(field)(yeahReally)
  def deleteIndex(fGetField: S => Field[_])(yeahReally: YeahReally): Future[Unit] = fh.deleteIndex(fGetField)(yeahReally)

  def size[T2 <: Parsed[_] : WeakTypeTag](query: T2): Future[Int] = where(query).size
  def size(selectors: (S => SelectStatement)*): Future[Int] = where(selectors:_*).size

  def isEmpty[T2 <: Parsed[_] : WeakTypeTag](query: T2): Future[Boolean] = where(query).isEmpty
  def isEmpty(selectors: (S => SelectStatement)*): Future[Boolean] = where(selectors:_*).isEmpty

  def nonEmpty[T2 <: Parsed[_] : WeakTypeTag](query: T2): Future[Boolean] = where(query).nonEmpty
  def nonEmpty(selectors: (S => SelectStatement)*): Future[Boolean] = where(selectors:_*).nonEmpty

  def indices: Future[Seq[String]] = fh.indices

  def createIndex(fGetField: S => Field[_], unique: Boolean): Future[Collection[T, S]] = fh.createIndex(fGetField, unique).map(_ => this)
  def createIndex(field: Field[_], unique: Boolean): Future[Collection[T, S]] = fh.createIndex(field, unique).map(_ => this)

  def wipeItems(): ItemsWiper = fh.wipeItems()
  def wipeIndices(): IndicesWiper = fh.wipeIndices()

  case class where_impl (backing: fh.where_impl) {

    def create(): Future[Unit] = backing.create()
    def size: Future[Int] = backing.size
    def isEmpty: Future[Boolean] = backing.isEmpty
    def nonEmpty: Future[Boolean] = backing.nonEmpty
    def contains: Future[Boolean] = backing.contains

    def findAll: Future[Seq[Versioned[T]]] = backing.find
    def findOne: Future[Option[Versioned[T]]] = findAll.map(_.headOption)

    def update(entity: T, upsert: Boolean = false, expectVersion: Long = -1L): Future[Unit] = {
      if (expectVersion == -1L)
        logger.warning(s"Warning: Updating a ${fh.tag.tpe} without object version - you almost certainly have a data race. This API exists only for legacy reasons! (selector: ${backing.selector})")
      backing.update(entity, upsert, expectVersion)
    }
    def append(fGetFieldData: S => (String, Any), defaultValue: () => T): Future[Unit] = backing.append(fGetFieldData, defaultValue)
    def delete(expectVersion: Long = -1L): Future[Unit] = {
      if (expectVersion == -1L)
        logger.warning(s"Warning: Deleting a ${fh.tag.tpe} without object version - you almost certainly have a data race. This API exists only for legacy reasons! (selector: ${backing.selector})")
      backing.delete(expectVersion)
    }
    def findOrCreate(ctor: () => T): Future[Versioned[T]] = backing.findOrCreate(ctor)
  }

}

object Collection {

  implicit class RichFhCol[T <: Parsed[S] : WeakTypeTag, S <: Schema[T]](fh: FHCollection[T, S]){
    def twittered: Collection[T, S] = fh
  }

  implicit def fh2col[T <: Parsed[S] : WeakTypeTag, S <: Schema[T]](fh: FHCollection[T, S]): Collection[T, S] = Collection(fh)

  implicit class RichReqSeqField[T : WeakTypeTag : MapDataParser : MapDataProducer](field: FieldRequired[Seq[T]]) {
    def -->(item: T): (String, Any) = {
      field.name -> field.-->(Seq(item))._2.asInstanceOf[Seq[Any]].head
    }
  }

  implicit class RichOptSeqField[T : WeakTypeTag : MapDataParser : MapDataProducer](field: FieldOption[Seq[T]]) {
    def -->(item: T): (String, Any) = {
      field.name -> field.-->(Seq(item))._2.asInstanceOf[Seq[Any]].head
    }
  }

  implicit class RichReqSeqFieldSet[T : WeakTypeTag : MapDataParser : MapDataProducer](field: FieldRequired[Set[T]]) {
    def -->(item: T): (String, Any) = {
      field.name -> field.-->(Set(item))._2.asInstanceOf[Set[Any]].head
    }
  }

  implicit class RichOptSeqFieldSet[T : WeakTypeTag : MapDataParser : MapDataProducer](field: FieldOption[Set[T]]) {
    def -->(item: T): (String, Any) = {
      field.name -> field.-->(Set(item))._2.asInstanceOf[Set[Any]].head
    }
  }

}