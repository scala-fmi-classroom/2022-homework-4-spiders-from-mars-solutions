package homework4.processors

import java.net.URI
import java.nio.file.{Files, Path, Paths}
import java.util.UUID
import homework4.Processor
import homework4.http.HttpResponse
import homework4.math.Monoid

import scala.concurrent.{ExecutionContext, Future}

case class SavedFiles(urlToPath: Map[String, Path])

object SavedFiles:
  def empty = SavedFiles(Map.empty)

  given Monoid[SavedFiles] with
    extension (a: SavedFiles) def |+|(b: SavedFiles): SavedFiles = SavedFiles(a.urlToPath ++ b.urlToPath)

    def identity: SavedFiles = empty

class FileOutput(targetDir: String)(blockingEc: ExecutionContext) extends Processor[SavedFiles]:
  private val targetPath = Paths.get(targetDir)

  private def generatePathFor(url: String): Path =
    val urlFileName = Option(Paths.get(new URI(url).getPath).getFileName).map(_.toString).getOrElse("")
    val fileName = s"${UUID.randomUUID().toString}-$urlFileName"

    targetPath.resolve(fileName)

  def apply(url: String, response: HttpResponse): Future[SavedFiles] = Future {
    if response.isSuccess then
      val path = Files.write(generatePathFor(url), response.bodyAsBytes)

      SavedFiles(Map(url -> path))
    else SavedFiles.empty
  }(blockingEc)
