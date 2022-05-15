package homework4.generic.processors

import homework4.generic.{Concurrent, GenericProcessor}
import homework4.http.HttpResponse
import homework4.processors.BrokenLinkDetector

object GenericBrokenLinkDetector extends GenericProcessor[Set[String]]:
  def apply[F[_]: Concurrent](url: String, response: HttpResponse): F[Set[String]] = Concurrent[F].fromFuture {
    BrokenLinkDetector(url, response)
  }
