package se.gigurra.serviceutils.twitter.logging

import java.io._

/**
  * Created by kjolh on 3/15/2016.
  */
object Capture {

  case class Forwarded(oldStream: OutputStream, newStream: OutputStream) extends OutputStream {

    private val streams = Seq(oldStream, newStream)

    private def forward(f: OutputStream => Unit): Unit = streams.foreach(f)

    @throws[IOException]
    override def write(b: Int): Unit = forward(_.write(b))

    @throws[IOException]
    override def flush(): Unit = forward(_.flush())

    @throws[IOException]
    override def write(b: Array[Byte]): Unit = forward(_.write(b))

    @throws[IOException]
    override def write(b: Array[Byte], off: Int, len: Int): Unit = forward(_.write(b, off, len))

    @throws[IOException]
    override def close(): Unit = forward(_.close())
  }

  def stdOut(newStream: OutputStream): Unit = {
    System.setOut(new PrintStream(Forwarded(System.out, newStream), true, "UTF-8"))
  }

  def stdErr(newStream: OutputStream): Unit = {
    System.setErr(new PrintStream(Forwarded(System.err, newStream), true, "UTF-8"))
  }

  def stdOutToFile(path: String, append: Boolean = true): Unit = {
    stdOut(new FileOutputStream(prepareFile(path, append), append))
  }

  def stdErrToFile(path: String, append: Boolean = true): Unit = {
    stdErr(new FileOutputStream(prepareFile(path, append), append))
  }

  def prepareFile(path: String, append: Boolean): File = {

    val file = new File(path)

    if (append) {
      if (!file.exists && !file.createNewFile())
        throw new RuntimeException(s"Cannot create file: $path")
    } else {
      if (file.exists && !file.delete)
        throw new RuntimeException(s"Cannot delete old file: $path")
      if (!file.createNewFile)
        throw new RuntimeException(s"Cannot create file: $path")
    }

    if (!file.canWrite)
      throw new RuntimeException(s"Cannot write to file: $path")

    file
  }

}
