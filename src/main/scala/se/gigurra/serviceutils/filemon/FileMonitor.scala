package se.gigurra.serviceutils.filemon

import java.nio.file.{Path, StandardWatchEventKinds, WatchEvent}
import java.util.concurrent.TimeUnit

import se.gigurra.serviceutils.twitter.logging.Logging

import scala.collection.JavaConversions._
import scala.util.Try
import scala.util.control.NonFatal

/**
  * Created by kjolh on 3/20/2016.
  */
case class FileMonitor(path: Path,
                       pollTimeMillis: Long,
                       interestEvents: Seq[WatchEvent.Kind[Path]],
                       eventFilter: WatchEvent[_] => Boolean,
                       handler: (FileMonitor, WatchEvent[_]) => Unit) extends Logging {

  logger.info(s"Starting file monitor on: $path")

  @volatile private var shouldLive = true
  private val watchService = path.getFileSystem.newWatchService()
  path.register(watchService, interestEvents.toArray.asInstanceOf[Array[WatchEvent.Kind[_]]])

  private val thread = new Thread() {

    override def run(): Unit = {

      while (shouldLive) {

        val watchKey = watchService.poll(pollTimeMillis, TimeUnit.MILLISECONDS)

        if (watchKey != null) {

          for (event <- watchKey.pollEvents().filter(eventFilter)) {
            Try(handler(FileMonitor.this, event)).recover {
              case NonFatal(e) => logger.error(e, s"Exception thrown by FileMonitor handler")
            }
          }

          if (!watchKey.reset()) {
            logger.warning(s"Watch service for '$path' no longer valid.. exiting")
            watchKey.cancel()
            kill()
          }
        }
      }

      watchService.close()
    }
  }

  def start(isDaemon: Boolean = true): this.type = {
    thread.setDaemon(true)
    thread.start()
    this
  }

  def kill(): Unit = {
    shouldLive = false
  }
}

object FileMonitor {

  def apply(relPath: Path,
            pollTimeMillis: Long = 100,
            interestEvents: Seq[WatchEvent.Kind[Path]] = Seq(
              StandardWatchEventKinds.ENTRY_CREATE,
              StandardWatchEventKinds.ENTRY_MODIFY,
              StandardWatchEventKinds.ENTRY_DELETE
            ),
            isDaemon: Boolean = true)(handler: (FileMonitor, WatchEvent[_]) => Unit): FileMonitor = {

    val path = relPath.toAbsolutePath

    if (path.toFile.isFile) {
      val file = path.toFile
      val dir = file.getParentFile
      val eventFilter = (e: WatchEvent[_]) => e.context.toString == file.getName
      new FileMonitor(dir.toPath, pollTimeMillis, interestEvents, eventFilter, handler).start(isDaemon)
    } else {
      new FileMonitor(path, pollTimeMillis, interestEvents, _ => true, handler).start(isDaemon)
    }
  }

/*
  def main(args: Array[String]): Unit = {
    FileMonitor(new java.io.File("C:\\gt\\dcs-remote2\\static-data.json").toPath) { (_, event) =>
      event.kind() match {
        case StandardWatchEventKinds.ENTRY_CREATE =>
          println(s"ENTRY_CREATE ${event.context()}")
        case StandardWatchEventKinds.ENTRY_DELETE =>
          println(s"ENTRY_DELETE ${event.context()}")
        case StandardWatchEventKinds.ENTRY_MODIFY =>
          println(s"ENTRY_MODIFY ${event.context()}")
        case StandardWatchEventKinds.OVERFLOW =>
        case _ =>
      }
    }
    Thread.sleep(100000)
  }*/
}