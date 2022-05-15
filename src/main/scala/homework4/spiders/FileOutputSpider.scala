package homework4.spiders

import homework4.math.Monoid
import homework4.processors.{FileOutput, SavedFiles}

import scala.concurrent.ExecutionContext

class FileOutputSpider(targetDir: String)(ec: ExecutionContext) extends ProcessingSpider:
  type Output = SavedFiles

  def processor = new FileOutput(targetDir)(ec)

  def monoid: Monoid[SavedFiles] = summon

  def prettify(output: SavedFiles): String = output.urlToPath
    .map { case (url, path) =>
      s"$url was saved to $path"
    }
    .mkString("\n")
