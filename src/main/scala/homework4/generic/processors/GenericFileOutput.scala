package homework4.generic.processors

import homework4.generic.{Concurrent, GenericProcessor}
import homework4.http.HttpResponse
import homework4.processors.{FileOutput, SavedFiles}

import scala.concurrent.ExecutionContext

class GenericFileOutput(targetDir: String)(blockingEc: ExecutionContext) extends GenericProcessor[SavedFiles]:
  val fileOutput = new FileOutput(targetDir)(blockingEc)

  def apply[F[_]: Concurrent](url: String, response: HttpResponse): F[SavedFiles] = Concurrent[F].fromFuture {
    fileOutput(url, response)
  }
