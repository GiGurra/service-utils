package se.gigurra.serviceutils.twitter.logging

trait Logging {
  import com.twitter.logging.Logger
  protected lazy val logger = Logger.get(getClass)
}
