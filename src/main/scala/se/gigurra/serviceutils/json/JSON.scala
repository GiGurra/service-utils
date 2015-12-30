package se.gigurra.serviceutils.json

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson._
import se.gigurra.heisenberg.{MapDataParser, MapDataProducer, MapParser, MapProducer}

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._
import scala.util.Try

object JSON {

  implicit val formats = org.json4s.DefaultFormats

  def writeJval[T: MapProducer](t : T): JValue = {
    Extraction.decompose(MapProducer.produce[T](t))
  }

  def write[T: MapProducer](t : T): String = {
    compactJson(writeJval(t))
  }

  def writeMap(data: Map[String, Any]): String = {
    compactJson(writeMapJval(data))
  }

  def writeMapJval(data: Map[String, Any]): JValue = {
    Extraction.decompose(data)
  }

  def writeSingleFieldObjectJval[T: MapDataProducer](value: T, fieldName: String = "data") : JValue = {
    writeMapJval(MapProducer.singleFieldObject(value, fieldName))
  }

  def writeSingleFieldObject[T: MapDataProducer](value: T, fieldName: String = "data") : String = {
    writeMap(MapProducer.singleFieldObject(value, fieldName))
  }

  def read[T : MapParser : ClassTag](json: JValue): T = {
    MapParser.parse[T](readMap(json))
  }

  def read[T : MapParser : ClassTag](json: String): T = {
    read[T](parse(json))
  }

  def tryRead[T : MapParser : ClassTag](json: JValue): Try[T] = {
    Try(read[T](json))
  }

  def tryRead[T : MapParser : ClassTag](json: String): Try[T] = {
    Try(read[T](json))
  }

  def readMap(json: JValue): Map[String, Any] = {
    json.extract[Map[String, Any]]
  }

  def readMap(json: String): Map[String, Any] = {
    readMap(parse(json))
  }

  def readSingleFieldObject[T : MapDataParser : TypeTag : ClassTag](json: JValue, fieldName: String): T = {
    MapParser.parseSingleFieldObject[T](readMap(json), fieldName)
  }

  def readSingleFieldObject[T : MapDataParser : TypeTag : ClassTag](json: JValue): T = {
    readSingleFieldObject[T](json, "data")
  }

  def readSingleFieldObject[T : MapDataParser : TypeTag : ClassTag](json: String, fieldName: String): T = {
    readSingleFieldObject(parse(json), fieldName)
  }

  def readSingleFieldObject[T : MapDataParser : TypeTag : ClassTag](json: String): T = {
    readSingleFieldObject[T](json, "data")
  }

  def tryReadSingleFieldObject[T: MapDataParser : TypeTag : ClassTag](json: String, fieldName: String): Try[T] = {
    Try(readSingleFieldObject[T](json, fieldName))
  }

  def tryReadSingleFieldObject[T: MapDataParser : TypeTag : ClassTag](json: String): Try[T] = {
    tryReadSingleFieldObject[T](json, "data")
  }

  def tryReadSingleFieldObject[T: MapDataParser : TypeTag : ClassTag](json: JValue, fieldName: String ): Try[T] = {
    Try(readSingleFieldObject[T](json, fieldName))
  }

  def tryReadSingleFieldObject[T: MapDataParser : TypeTag : ClassTag](json: JValue): Try[T] = {
    tryReadSingleFieldObject[T](json, "data")
  }

}
